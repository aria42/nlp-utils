//package edu.umass.nlp.ml.classification;
//
//
//import edu.umass.nlp.functional.Fn;
//import edu.umass.nlp.functional.Functional;
//import edu.umass.nlp.ml.feats.IPredExtractor;
//import edu.umass.nlp.ml.feats.Predicate;
//import edu.umass.nlp.ml.feats.PredicateManager;
//import edu.umass.nlp.ml.feats.WeightsManager;
//import edu.umass.nlp.ml.prob.DirichletMultinomial;
//import edu.umass.nlp.ml.prob.IDistribution;
//import edu.umass.nlp.optimize.GradientDescent;
//import edu.umass.nlp.optimize.IDifferentiableFn;
//import edu.umass.nlp.optimize.IOptimizer;
//import edu.umass.nlp.utils.*;
//
//import java.util.List;
//
//public class MaxEntClassifier<T,L> implements IProbabilisticClassifier<T,L> {
//
//  private final PredicateManager<T> predManager;
//  private final WeightsManager<L> weightsManager;
//  private final double sigmaSq ;
//
//  public MaxEntClassifier(IPredExtractor<T> predFn, Indexer<L> labels, double sigmaSq) {
//    this.predManager = new PredicateManager<T>(predFn);
//    this.weightsManager = new WeightsManager<L>(predManager.getPredIndexer(),labels);
//    this.sigmaSq = sigmaSq;
//  }
//
//  private double[] getProbs(List<IValued<Predicate>> pvs) {
//    double[] logPots = new double[weightsManager.getNumLabels()];
//    for (IValued<Predicate> pv : pvs) {
//      weightsManager.addScores(pv.getElem(), pv.getValue(), logPots);
//    }
//    return SloppyMath.logScoresToProbs(logPots);
//  }
//
//  public IDistribution<L> getLabelDistribution(T elem) {
//    double[] probs = getProbs(predManager.getPredicates(elem));
//    return DirichletMultinomial.make(Counters.from(probs, weightsManager.getLabelIndexer()));
//  }
//
//  public void train(final Iterable<IPair<T, L>> data) {
//    predManager.indexAll(Functional.map(data, new Fn<IPair<T, L>, T>() {
//      public T apply(IPair<T, L> input) {
//        return input.getFirst();
//      }}));
//    IDifferentiableFn objFn = new IDifferentiableFn() {
//      public IPair<Double, double[]> computeAt(double[] x) {
//        double logProb = 0.0;
//        double[] grad = new double[getDimension()];
//        weightsManager.setWeights(x);
//
//        for (IPair<T, L> datum : data) {
//          List<IValued<Predicate>> pvs = predManager.getPredicates(datum.getFirst());
//          double[] probs = getProbs(pvs);
//          int trueLabelIndex = weightsManager.getLabelIndexer().indexOf(datum.getSecond());
//          logProb += probs[trueLabelIndex];
//          for (IValued<Predicate> pv : pvs) {
//            weightsManager.addFeatExpecations(pv.getElem(), probs, grad);
//          }
//        }
//
//        logProb *= -1;
//        DoubleArrays.scaleInPlace(grad,-1);
//
//        //side-effect: grad updated
//        //logProb += (new L2Regularizer()).update(x, grad, 1.0);
//
//        return BasicPair.make(logProb, grad);
//      }
//
//      public int getDimension() {
//        return weightsManager.getNumFeats();
//      }
//    };
//    IOptimizer.Result res = (new GradientDescent()).minimize(objFn, new double[objFn.getDimension()],new IOptimizer.Opts());
//    weightsManager.setWeights(res.minArg);
//  }
//
//  public L classify(T datum) {
//    return getLabelDistribution(datum).getMode();
//  }
//
//  public static void main(String[] args) {
//
//  }
//}