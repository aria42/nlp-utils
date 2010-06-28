package edu.umass.nlp.ml.classification;


import edu.umass.nlp.functional.DoubleFn;
import edu.umass.nlp.ml.Regularizers;
import edu.umass.nlp.optimize.GradientDescent;
import edu.umass.nlp.optimize.IDifferentiableFn;
import edu.umass.nlp.optimize.IOptimizer;
import edu.umass.nlp.optimize.LBFGSMinimizer;
import edu.umass.nlp.utils.*;

import java.util.List;
import java.util.Set;

public class Reranker<L>  {

  public static class Opts {
    public double sigmaSq = 1.0;
  }

  private Indexer<String> featIndexer;
  private double[] weights;

  public static interface Datum<L> {
    public List<IValued<String>> getFeatures(L label);
    public Set<L> getAllowedLabels();
  }

  public static interface LabeledDatum<L> extends Datum<L> {
    public L getTrueLabel();
  }

  private ICounter<L> getLabelProbs(Datum<L> datum) {
    ICounter<L> logProbs = new MapCounter<L>();
    for (L label : datum.getAllowedLabels()) {
      double logProb = 0.0;
      for (IValued<String> valued : datum.getFeatures(label)) {
        logProb += valued.getValue() * weights[featIndexer.getIndex(valued.getElem())];
      }
      logProbs.setCount(label, logProb);
    }
    final double logSum = SloppyMath.logAdd(logProbs);
    logProbs.mapDestructive(new DoubleFn<IValued<L>>() {
      public double valAt(IValued<L> x) {
        return Math.exp(x.getValue()-logSum);
      }
    });
    return logProbs;
  }

  public void train(final Iterable<LabeledDatum> data) {
    featIndexer = new Indexer<String>();
    for (LabeledDatum<L> datum : data) {
      for (L label : datum.getAllowedLabels()) {
        for (IValued<String> valued : datum.getFeatures(label)) {
          featIndexer.add(valued.getElem());
        }
      }
    }
    featIndexer.lock();
    IDifferentiableFn objFn = new IDifferentiableFn() {
      public IPair<Double, double[]> computeAt(double[] x) {
        double logObj = 0.0;
        double[] grad = new double[getDimension()];
        weights = DoubleArrays.clone(x);

        for (LabeledDatum<L> datum: data) {
          ICounter<L> labelProbs = getLabelProbs(datum);
          logObj += Math.log(labelProbs.getCount(datum.getTrueLabel()));
          for (L label : datum.getAllowedLabels()) {
            for (IValued<String> valued : datum.getFeatures(label)) {
              int featIndex = featIndexer.getIndex(valued.getElem());
              double val = valued.getSecond();
              grad[featIndex] -= val * labelProbs.getCount(label);
              if (label.equals(datum.getTrueLabel()))  {
                grad[featIndex] += 1.0 * labelProbs.getCount(label);
              }
            }
          }
        }

        logObj *= -1;
        DoubleArrays.scaleInPlace(grad, -1);

        //side-effect: grad updated
        IPair<Double,double[]> regRes = (Regularizers.getL2Regularizer(1.0)).apply(x);
        logObj += regRes.getFirst();
        DoubleArrays.addInPlace(grad, regRes.getSecond());
        return BasicPair.make(logObj, grad);
      }

      public int getDimension() {
        return featIndexer.size();
      }
    };
    IOptimizer.Result res = (new LBFGSMinimizer()).minimize(objFn, new double[objFn.getDimension()], new IOptimizer.Opts());
    this.weights = DoubleArrays.clone(res.minArg);
  }



}