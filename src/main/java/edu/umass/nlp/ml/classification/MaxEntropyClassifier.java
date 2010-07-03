package edu.umass.nlp.ml.classification;

import edu.umass.nlp.exec.Execution;
import edu.umass.nlp.functional.Fn;
import edu.umass.nlp.functional.Functional;
import edu.umass.nlp.ml.prob.DirichletMultinomial;
import edu.umass.nlp.ml.prob.IDistribution;
import edu.umass.nlp.optimize.CachingDifferentiableFn;
import edu.umass.nlp.optimize.IDifferentiableFn;
import edu.umass.nlp.optimize.IOptimizer;
import edu.umass.nlp.optimize.LBFGSMinimizer;
import edu.umass.nlp.utils.*;

import java.util.ArrayList;
import java.util.List;

public class MaxEntropyClassifier<L> implements IProbabilisticClassifier<L> {

  private double[] weights;
  private Indexer<String> predIndexer;
  private Indexer<L> labelIndexer;
    
  @Override
  public IDistribution<L> getLabelDistribution(ClassifierDatum datum) {
    InnerDatum innerDatum = toInnerDatum(datum);
    double[] probs = getLabelProbs(innerDatum);
    ICounter<L> probCounts = new MapCounter<L>();
    for (int labelIndex = 0; labelIndex < probs.length; labelIndex++) {
      double prob = probs[labelIndex];
      probCounts.setCount(labelIndexer.get(labelIndex),prob);
    }
    return DirichletMultinomial.make(probCounts);
  }

  private static class PredValPair {
    public final int pred;
    public final double val;

    private PredValPair(int pred, double val) {
      assert pred >= 0;
      this.pred = pred;
      this.val = val;
    }
  }

  private static class InnerDatum {
    List<PredValPair> pvs = new ArrayList<PredValPair>();
    int trueLabelIndex;
  }

  private InnerDatum toInnerDatum(ClassifierDatum datum) {
    InnerDatum res = new InnerDatum();
    for (IValued<String> valued : datum.getPredicates()) {
      String pred = valued.getElem();
      int predIndex = predIndexer.indexOf(pred);
      if (predIndex >= 0) {
        res.pvs.add(new PredValPair(predIndex, valued.getValue()));
      }
    }
    return res;
  }

  private InnerDatum toInnerLabeledDatum(LabeledClassifierDatum<L> datum) {
    InnerDatum res = toInnerDatum(datum);
    res.trueLabelIndex = labelIndexer.indexOf(datum.getTrueLabel());
    return res;
  }

  private void indexPredicatesAndLabels(Iterable<LabeledClassifierDatum<L>> data) {
    predIndexer = new Indexer<String>();
    labelIndexer = new Indexer<L>();
    for (LabeledClassifierDatum<L> datum : data) {
      for (IValued<String> valued : datum.getPredicates()) {
        predIndexer.add(valued.getElem());
      }
      labelIndexer.add(datum.getTrueLabel());
    }
    predIndexer.lock();
    labelIndexer.lock();
  }

  @Override
  public void train(Iterable<LabeledClassifierDatum<L>> data)
  {
    indexPredicatesAndLabels(data);
    Iterable<InnerDatum> innerData =
        Functional.map(data, new Fn<LabeledClassifierDatum<L>, InnerDatum>() {
          @Override
          public InnerDatum apply(LabeledClassifierDatum<L> input) {
            return toInnerLabeledDatum(input);
          }});
    IDifferentiableFn objFn = new CachingDifferentiableFn(new ObjFn(innerData));
    IOptimizer.Result optRes = (new LBFGSMinimizer()).minimize(
        objFn,
        new double[objFn.getDimension()],
        new LBFGSMinimizer.Opts());
    weights = DoubleArrays.clone(optRes.minArg);
  }

  public double[] getLabelProbs(InnerDatum datum) {
    double[] logProbs = new double[labelIndexer.size()];
    for (int l = 0; l < logProbs.length; l++) {
      double sum = 0.0;
      for (PredValPair pair : datum.pvs) {
        int f = getWeightIndex(pair.pred, l);
        sum += pair.val * weights[f];
      }
      logProbs[l] = sum;
    }
    return SloppyMath.logScoresToProbs(logProbs);
  }

  private int getWeightIndex(int predIndex, int labelIndex) {
    return predIndex * labelIndexer.size() + labelIndex;
  }

  class ObjFn implements IDifferentiableFn {

    private Iterable<InnerDatum> data;

    ObjFn(Iterable<InnerDatum> data) {
      this.data = data;
    }

    @Override
    public IPair<Double, double[]> computeAt(double[] x) {
      weights = DoubleArrays.clone(x);
      double logObj = 0.0;
      double[] grad = new double[getDimension()];

      for (InnerDatum datum : data) {
        double[] probs = getLabelProbs(datum);
        logObj +=  Math.log(probs[datum.trueLabelIndex]);
        for (int l = 0; l < probs.length; l++) {
          for (PredValPair pair : datum.pvs) {
            int f = getWeightIndex(pair.pred, l);
            grad[f] += pair.val * probs[l];
            if (l == datum.trueLabelIndex) {
              grad[f] -= pair.val * 1.0;
            }
          }
        }
      }

      // Negate
      logObj *= -1;
      DoubleArrays.scale(grad, -1);

//      IPair<Double,double[]> regRes = Regularizers.getL2Regularizer(1.0).apply(x);
//      logObj += regRes.getFirst();
//      DoubleArrays.addInPlace(grad,regRes.getSecond());

      return BasicPair.make(logObj, grad);
    }

    @Override
    public int getDimension() {
      return predIndexer.size() * labelIndexer.size();
    }
  }

  @Override
  public L classify(ClassifierDatum datum) {
    return getLabelDistribution(datum).getMode();
  }

}
