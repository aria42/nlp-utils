package edu.umass.nlp.ml.sequence;

import edu.umass.nlp.functional.Fn;
import edu.umass.nlp.functional.Functional;
import edu.umass.nlp.optimize.CachingDifferentiableFn;
import edu.umass.nlp.optimize.IDifferentiableFn;
import edu.umass.nlp.optimize.IOptimizer;
import edu.umass.nlp.optimize.LBFGSMinimizer;
import edu.umass.nlp.parallel.ParallelUtils;
import edu.umass.nlp.utils.*;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Conditional Random Field.
 */
public class CRF implements Serializable {

  private static final long serialVersionUID = 42L;

  // States are just CRF labels (assuming Start/Stop)
  private StateSpace stateSpace;
  private Indexer<String> labels;

  // A predicate is a feautre except
  // it hasn't been conjoined with a label
  // So "word=man" is a predicate and
  // we generate a feautre "word=man & tag=NN"
  // for each possible CRF label
  private Indexer<String> predIndexer;


  // Weights for node features
  // It is layed out predicate-major as follows
  // (pred_1,label_1) ... (pred_1,label_k)
  // (pred_2,label_1) ... (pred_2,label_k)
  //              ..........
  // (pred_m,label_1) ... (pred_m,label_k)
  private double[] nodeWeights;

  // Weights for each trans.
  // Has size stateSpace.getTransitions.size
  private double[] transWeights;
  private double[] weights;
  //private double sigmaSquared = 1.0;

  public Indexer<String> getPredIndexer() {
    return predIndexer;
  }

  public StateSpace getStateSpace() {
    return stateSpace;
  }

  public double[] getTransWeights() {
    return DoubleArrays.clone(transWeights);
  }

//  public int getWeightIndex(int coarsePredIndex, int label) {
//
//  }


  public static class Opts {
    public LBFGSMinimizer.Opts optimizerOpts = new LBFGSMinimizer.Opts();
    public int minPredCuttoffCount = 1;
    //public double sigmaSquared = 1.0;
    public boolean reuseWeights = false;
    public boolean cachePredTrainData = true;
    public int numThreads = Runtime.getRuntime().availableProcessors();
    public Fn<double[], IPair<Double, double[]>> regularizer;
  }

  /**
   *
   */
  public static enum InfMode {
    VITERBI, MINRISK
  }


  private static class InternalDatum {
    final List<int[]> predIndices;
    final List<String> labels;
    final double weight;

    private InternalDatum(List<int[]> predIndices, List<String> labels, double weight) {
      this.labels = labels;
      this.predIndices = predIndices;
      this.weight = weight;
    }

    public int size() {
      return labels.size();
    }
  }

  /**
   * Labeled Data Looks like This
   * [ [ < [pred1, pred2, pred3, ..], label >, < [pred1, pred2, pred3, ..], label > ]
   * [ < [pred1, pred2, pred3, ..], label >, < [pred1, pred2, pred3, ..], label > ]]
   * <p/>
   * Basically each labeled input consists of a seq of pairs
   * the first element of each pair are predicates active for
   * the given node position. The second element is the label
   * for that position.
   */

  //private transient Iterable<ILabeledSeqDatum> trainData;
  private transient Logger logger;
  //private transient Opts opts;


  {
    logger = Logger.getLogger("CRF");
    logger.setLevel(Level.INFO);
  }

  /**
   * Predicated data replaces the list of pred strings
   * with predicate indices. This just makes inference faster. So
   * we don't hash strings repeatedly.
   */
  //private transient Iterable<InternalDatum> cachedPredTrainData;
  //private transient final boolean cacheTrainData = true;
  private StateSpace getStateSpace(Iterable<ILabeledSeqDatum> labeledInstances) {
    StateSpace ss = new StateSpace();
    for (ILabeledSeqDatum inst : labeledInstances) {
      for (String label : inst.getLabels()) {
        ss.addState(label);
      }
    }
    ss.lockStates();
    for (ILabeledSeqDatum inst : labeledInstances) {
      for (int i = 0; i + 1 < inst.getLabels().size(); i++) {
        String s = inst.getLabels().get(i);
        String t = inst.getLabels().get(i + 1);
        ss.addTransition(s, t);
      }
    }
    return ss;
  }

  private Indexer<String> getPredIndexer(Iterable<ILabeledSeqDatum> labeledInstances, Opts opts) {
    ICounter<String> predCounts = new MapCounter<String>();
    for (ILabeledSeqDatum inst : labeledInstances) {
      for (List<String> preds : inst.getNodePredicates()) {
        for (String pred : preds) {
          predCounts.incCount(pred, 1.0);
        }
      }
    }
    Indexer<String> predIndexer = new Indexer<String>();
    for (IValued<String> valued : predCounts) {
      if (valued.getValue() > opts.minPredCuttoffCount) {
        predIndexer.add(valued.getElem());
      }
    }
    return predIndexer;
  }

  private Indexer<String> getLabelIndexer(Iterable<ILabeledSeqDatum> labeledInstances) {
    Indexer<String> labelIndexer = new Indexer<String>();
    for (ILabeledSeqDatum inst : labeledInstances) {
      labelIndexer.addAll(inst.getLabels());
    }
    return labelIndexer;
  }

//  public void train(Iterable<ILabeledSeqDatum> labeledInstances) {
//    train(labeledInstances, null);
//  }

//  public double[] getWeights() {
//    return null;
//  }

  private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
    ois.defaultReadObject();
    logger = Logger.getLogger("CRF");
    logger.setLevel(Level.INFO);
  }


  public void train(Iterable<ILabeledSeqDatum> labeledInstances, Opts opts) {
    if (weights == null || !opts.reuseWeights) init(labeledInstances, opts);
    //this.trainData = labeledInstances;
    //if (opts.cachePredTrainData) cachePredTrainData();
    logger.info(String.format("Training with state-size:%d", stateSpace.getStates().size()));
    IDifferentiableFn fn = new CachingDifferentiableFn(new ObjFn(getPredicatedData(labeledInstances, opts), opts));
    final IOptimizer optimizer = new LBFGSMinimizer();
    double[] initWeights = opts.reuseWeights && weights != null ?
      weights :
      new double[fn.getDimension()];
    IOptimizer.Result optRes = optimizer.minimize(fn, initWeights, opts.optimizerOpts);
    setWeights(optRes.minArg);
  }


  private void init(Iterable<ILabeledSeqDatum> labeledInstances, Opts opts) {
    logger.trace("starting init");
    //logger.trace("used memory: " + Runtime.getRuntime().totalMemory() / 1.0e06);
    logger.info(Runtime.getRuntime().freeMemory() / 1.e06 + "M");
    this.stateSpace = getStateSpace(labeledInstances);
    logger.trace("constructed state space");
    //logger.trace("used memory: " + Runtime.getRuntime().totalMemory() / 1.0e06);
    logger.info(Runtime.getRuntime().freeMemory() / 1.e06 + "M");
    this.predIndexer = getPredIndexer(labeledInstances, opts);
    logger.trace("constructed pred indexed");
    //logger.trace("used memory: " + Runtime.getRuntime().totalMemory() / 1.0e06);
    logger.info(Runtime.getRuntime().freeMemory() / 1.e06 + "M");
    this.labels = getLabelIndexer(labeledInstances);
    logger.trace("constructed labels");
    logger.trace("used memory: " + Runtime.getRuntime().totalMemory() / 1.0e06);
  }

  private void setWeights(double[] x) {
    // We organize the weights as follows
    // the first getNumNodeWeights() elements
    // are node feat-weights and the last
    // stateSpace.getTransitions.size()
    // are for each stateSpace transition
    nodeWeights = DoubleArrays.clone(x, 0, getNumNodeWeights());
    transWeights = DoubleArrays.clone(x, getNumNodeWeights(), x.length);
    weights = DoubleArrays.clone(x);
  }

  private int getNumNodeWeights() {
    return predIndexer.size() * stateSpace.getStates().size();
  }

  public int getNodeWeightIndex(int predIndex, int stateIndex) {
    return predIndex * stateSpace.getStates().size() + stateIndex;
  }

  private double getWordPotential(List<Integer> preds, int stateIndex) {
    double sum = 0.0;
    for (Integer predIndex : preds) {
//      int predIndex = predIndexer.indexOf(pred);
//      if (predIndex < 0) continue;
      sum += nodeWeights[getNodeWeightIndex(predIndex, stateIndex)];
    }
    return sum;
  }

  private double[][] getPotentialsAtTestTime(List<List<String>> inputSeq) {
    int numTrans = inputSeq.size() - 1;
    double[][] logPots = new double[numTrans][stateSpace.getTransitions().size()];
    for (int i = 0; i < numTrans; i++) {
      java.util.Arrays.fill(logPots[i], Double.NEGATIVE_INFINITY);
      List<State> states;
      if (i == 0) states = Collections.makeList(stateSpace.startState);
      else states = stateSpace.getStates();
      List<Integer> predIndices = new ArrayList<Integer>();
      for (String p : inputSeq.get(i)) {
        int predIndex = predIndexer.indexOf(p);
        if (predIndex >= 0) predIndices.add(predIndex);
      }
      for (State state : states) {
        double nodePotential = getWordPotential(predIndices, state.index);
        for (Transition trans : stateSpace.getTransitionsFrom(state.index)) {
          logPots[i][trans.index] = nodePotential + transWeights[trans.index];
        }
      }
    }
    return logPots;
  }

  public class ObjFn implements IDifferentiableFn {

    private final Opts opts;
    private final Iterable<InternalDatum> data;

    ObjFn(Iterable<InternalDatum> data, Opts opts) {
      this.opts = opts;
      this.data = data;
    }

    private double[][] getPotentials(List<int[]> inputSeq) {
      int numTrans = inputSeq.size() - 1;
      double[][] logPots = new double[numTrans][stateSpace.getTransitions().size()];
      for (int i = 0; i < numTrans; i++) {
        java.util.Arrays.fill(logPots[i], Double.NEGATIVE_INFINITY);
//        List<State> states ;
//        if (i == 0) states = Collections.makeList(stateSpace.startState);
//        else if (i == numTrans-1) states = Collections.makeList(stateSpace.startState);
//        else states = stateSpace.getStates();
        for (State state : stateSpace.getStates()) {
          double nodePotential = getWordPotential(inputSeq.get(i), state.index);
          for (Transition trans : stateSpace.getTransitionsFrom(state.index)) {
            logPots[i][trans.index] = nodePotential + transWeights[trans.index];
          }
        }
      }
      return logPots;
    }

    private IPair<Double, double[]> compute(Iterable<InternalDatum> datums) {
      double logLike = 0.0;
      double[] grad = new double[getDimension()];
      for (InternalDatum labeledInst : datums) {
        double[][] logPots = getPotentials(labeledInst.predIndices);
        ForwardBackwards.Result res = (new ForwardBackwards(stateSpace)).compute(logPots);
        //  Obj Fn
        //  logLike += log P( correct-sequence | input)  =  sum( true-log-pots ) - logZ
        //  grad +=  (Empirical-Feat-Counts - Expected-Feat-Counts)

        // Objective Component
        int numTrans = labeledInst.size() - 1;
        for (int i = 0; i < numTrans; ++i) {
          String trueState = labeledInst.labels.get(i);
          String nextTrueState = labeledInst.labels.get(i + 1);
          Transition trueTrans = stateSpace.findTransition(trueState, nextTrueState);
          logLike += labeledInst.weight * logPots[i][trueTrans.index];
        }
        logLike -= labeledInst.weight * res.logZ;

        // Graident Component
        // Empirical
        for (int i = 0; i < numTrans; ++i) {
          String trueStateLabel = labeledInst.labels.get(i);
          State trueState = stateSpace.getState(trueStateLabel);
          // Empirical State Feats
          int[] preds = labeledInst.predIndices.get(i);
          for (int pred : preds) {
            grad[getNodeWeightIndex(pred, trueState.index)] += labeledInst.weight * 1.0;
          }
          // Empirical Trans Feat
          String nextTrueStateLabel = labeledInst.labels.get(i + 1);
          Transition trueTrans = stateSpace.findTransition(trueStateLabel, nextTrueStateLabel);
          grad[getNumNodeWeights() + trueTrans.index] += labeledInst.weight * 1.0;
        }
        // Expected
        for (int i = 0; i < numTrans; ++i) {
          int[] preds = labeledInst.predIndices.get(i);
          for (State state : stateSpace.getStates()) {
            double statePost = res.stateMarginals[i][state.index];
            if (statePost == 0.0) continue;
            // Node
            for (int pred : preds) {
              grad[getNodeWeightIndex(pred, state.index)] -= labeledInst.weight * statePost;
            }
            // Transition
            for (Transition trans : stateSpace.getTransitionsFrom(state.index)) {
              grad[getNumNodeWeights() + trans.index] -= labeledInst.weight * res.transMarginals[i][trans.index];
            }
          }
        }
      }
      return BasicPair.make(logLike, grad);
    }

    private double getWordPotential(int[] preds, int stateIndex) {
      double sum = 0.0;
      for (int pred : preds) {
        sum += nodeWeights[getNodeWeightIndex(pred, stateIndex)];
      }
      return sum;
    }

    class Worker implements Runnable {
      double logLike = 0.0;
      double[] grad = new double[getDimension()];
      List<InternalDatum> datums;
      boolean done;

      Worker(List<InternalDatum> datums) {
        this.datums = datums;
        done = false;
      }

      public void run() {
        IPair<Double, double[]> res = compute(datums);
        logLike = res.getFirst();
        grad = res.getSecond();
        datums = null;
        done = true;
      }
    }

    public IPair<Double, double[]> computeAt(double[] x) {
      setWeights(x);
      double logLike = 0.0;
      double[] grad = new double[getDimension()];

      // Cached
      if (data instanceof List) {
        // Shuffle data for parallelism
        //List<InternalDatum> predicatedData = new ArrayList<InternalDatum>((List) data);
        //java.util.Collections.shuffle(predicatedData);
        List<List<InternalDatum>> parts =
          Collections.partition((List)data, opts.numThreads);
//        int sum = 0;
//        for (List<InternalDatum> part : parts) {
//          sum += part.size();
//        }
//        assert sum == ((List) data).size();

        List<Worker> workers = Functional.map(parts, new Fn<List<InternalDatum>, Worker>() {
          public Worker apply(List<InternalDatum> input) {
            return new Worker(input);
          }
        });
        logger.trace("Computing Objective with " + workers.size() + " threads");
        ParallelUtils.doParallelWork(workers, workers.size());
        for (Worker worker : workers) {
          assert worker.done;
        }
        //logger.info("Memory: " + Runtime.getRuntime().freeMemory() / 1.e06 + "M");
        for (Worker worker : workers) {
          logLike += worker.logLike;
          DoubleArrays.addInPlace(grad, worker.grad);
        }
        logger.trace("Done with Computing Objective with " + workers.size() + " threads");
      } else {
        IPair<Double, double[]> res = compute(data);
        logLike = res.getFirst();
        grad = res.getSecond();
      }


      // Since the optimization framework is minimizing,
      // we minimize negLogLikelihood
      // so must scale gradient by -1 as well
      logLike *= -1.0;
      DoubleArrays.scaleInPlace(grad, -1.0);

      if (opts.regularizer != null) {
        IPair<Double, double[]> res = opts.regularizer.apply(x);
        logLike += res.getFirst();
        DoubleArrays.addInPlace(grad, res.getSecond());
      }
//      for (int i = 0; i < x.length; i++) {
//        double w = x[i];
//        logLike += (0.5 * w * w) / sigmaSquared;
//        grad[i] += w / sigmaSquared;
//      }
//      logger.info("Num Feats: " + x.length);
//      logger.info("Grad: " + DoubleArrays.toString(grad,10));
//      logger.info("Weights: " + DoubleArrays.toString(x,10));
//      logger.info("Value: " + logLike);
      //if (true) System.exit(0);
      logger.trace("Done with Computing Objective");
      return BasicPair.make(logLike, grad);
    }


    public int getDimension() {
      return getNumNodeWeights() + stateSpace.getTransitions().size();
    }
  }

  public InternalDatum toInternalDatum(ILabeledSeqDatum input) {
    List<int[]> predIndices = new ArrayList<int[]>();
    List<String> labels = new ArrayList<String>();
    final List<List<String>> nodePreds = input.getNodePredicates();
    for (int ii = 0; ii < nodePreds.size(); ++ii) {
      List<String> predList = nodePreds.get(ii);
      List<Integer> tempIndices = new ArrayList<Integer>();
      for (String pred : predList) {
        int predIndex = predIndexer.indexOf(pred);
        if (predIndex >= 0) {
          tempIndices.add(predIndex);
        }
      }
      int[] arr = new int[tempIndices.size()];
      for (int i = 0; i < arr.length; i++) {
        arr[i] = tempIndices.get(i);
        assert arr[i] >= 0;
      }
      String label = input.getLabels().get(ii);
      predIndices.add(arr);
      labels.add(label);
    }
    return new InternalDatum(predIndices, labels, input.getWeight());
  }

  final Fn<ILabeledSeqDatum, InternalDatum> internalDatumFn = new Fn<ILabeledSeqDatum, InternalDatum>() {
    public InternalDatum apply(ILabeledSeqDatum input) {
      return toInternalDatum(input);
    }
  };

  private Iterable<InternalDatum> getPredicatedData(Iterable<ILabeledSeqDatum> data, Opts opts) {
    if (opts.cachePredTrainData) {
      //if (cachedPredTrainData == null) {
      logger.info("Caching Predicated Data");
      logger.info("Free Memory: " + (Runtime.getRuntime().freeMemory() / 1e6) + "M");
      List<InternalDatum> cachedPredTrainData = Functional.map(data, internalDatumFn);
      logger.info("Done.");
      logger.info("Free Memory: " + Runtime.getRuntime().freeMemory() / 1.e06 + "M");
      return cachedPredTrainData;
      //trainData = null;
    } else {
      return Functional.lazyMap(data, internalDatumFn);
    }
  }

  public class Result {
    public List<List<String>> input;
    public ForwardBackwards.Result fbRes;

    public List<String> getViterbiGuess() {
      return null;
    }

    public double getLogProb(List<String> guessLabels) {
      return 0.0;
    }
  }


  public ForwardBackwards.Result getResult(List<List<String>> input) {
    double[][] pots = getPotentialsAtTestTime(input);
    return new ForwardBackwards(stateSpace).compute(pots);
  }

  public double getTagLogProb(ILabeledSeqDatum datum) {
    final List<List<String>> nodePreds = datum.getNodePredicates();
    double[][] logPots = getPotentialsAtTestTime(nodePreds);
    ForwardBackwards.Result res = getResult(nodePreds);
    int numTrans = nodePreds.size() - 1;
    double logProb = 0.0;
    for (int i = 0; i < numTrans; ++i) {
      String trueState = datum.getLabels().get(i);
      String nextTrueState = datum.getLabels().get(i + 1);
      Transition trueTrans = stateSpace.findTransition(trueState, nextTrueState);
      logProb += logPots[i][trueTrans.index];
    }
    // Fixed - 06/17
    assert logProb < res.logZ;
    logProb -= res.logZ;
    return logProb;
  }

  public List<String> getViterbiTagging(List<List<String>> input) {
    double[][] logPots = getPotentialsAtTestTime(input);
    ForwardBackwards fb = new ForwardBackwards(stateSpace);
    fb.setInput(logPots);
    return fb.viterbiDecode();
  }

  /**
   * Gets MinBayesRisk sequence which minimizes expected loss
   * where expectation is over state posteriors. Assume loss decomposes
   * over elements.
   *
   * @param input
   * @param lossFn Representation of loss where the first element
   *               is the true label and the counter is over potentials. If this
   *               is <code>null</code> assumes hamming loss.
   * @return
   */
  public List<String> getMinBayesRiskTagging(List<List<String>> input,
                                             Map<String, ICounter<String>> lossFn) {
    ForwardBackwards.Result fbRes = getResult(input);
    return getMinBayesRiskTagging(fbRes.stateMarginals, lossFn);
  }

  public List<String> getMinBayesRiskTagging(
    double[][] statePosts,
    Map<String, ICounter<String>> lossFn) {
    double[][] lossPots = new double[statePosts.length - 1][stateSpace.getTransitions().size()];
    for (int i = 0; i < lossPots.length; i++) {
      java.util.Arrays.fill(lossPots[i], Double.NEGATIVE_INFINITY);
      for (int s = 0; s < stateSpace.getStates().size(); ++s) {
        //lossPots[i][s] = statePosts[i][s];
        String label = stateSpace.getStates().get(s).label;
        double expLoss = 0.0;
        for (int sp = 0; sp < stateSpace.getStates().size(); ++sp) {
          String otherLabel = stateSpace.getStates().get(sp).label;
          double post = statePosts[i][sp];
          double loss = lossFn.get(otherLabel).getCount(label);
          expLoss += post * loss;
        }
        for (Transition trans : stateSpace.getTransitionsFrom(s)) {
          lossPots[i][trans.index] = -expLoss;
        }
      }
    }
    ForwardBackwards fb = new ForwardBackwards(stateSpace);
    fb.setInput(lossPots);
    return fb.viterbiDecode();
  }

  public List<String> getLabels() {
    return labels;
  }

  /**
   * Perform sequence inference.
   *
   * @param infMode
   * @param input
   * @param auxData If VITERBI, ignored. If MINRISK, this should be lossFn
   * @return
   */
  public List<String> getTagging(InfMode infMode,
                                 List<List<String>> input,
                                 Object auxData) {
    switch (infMode) {
      case VITERBI:
        return getViterbiTagging(input);
      case MINRISK:
        return getMinBayesRiskTagging(input, (Map) auxData);
      default:
        throw new RuntimeException("No valid Inference Mode: " + infMode);
    }
  }
}
