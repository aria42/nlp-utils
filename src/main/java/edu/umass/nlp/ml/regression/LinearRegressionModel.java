package edu.umass.nlp.ml.regression;

import edu.umass.nlp.exec.Execution;
import edu.umass.nlp.functional.Fn;
import edu.umass.nlp.functional.Functional;
import edu.umass.nlp.ml.feats.IPredExtractor;
import edu.umass.nlp.ml.feats.Predicate;
import edu.umass.nlp.ml.feats.PredicateManager;
import edu.umass.nlp.optimize.GradientDescent;
import edu.umass.nlp.optimize.IDifferentiableFn;
import edu.umass.nlp.optimize.IOptimizer;
import edu.umass.nlp.optimize.LBFGSMinimizer;
import edu.umass.nlp.utils.*;

import java.util.List;

public class LinearRegressionModel<T> {

  private final PredicateManager<T> predManager;
  private double[] weights;
  private double sigmaSquared = 1.0;
  private int maxIters = 100;

  public LinearRegressionModel(IPredExtractor<T> predFn) {
    this.predManager = new PredicateManager<T>(predFn);
  }

  private double getPredictionInternal(List<IValued<Predicate>> pvs) {
    double sum = 0.0;
    for (IValued<Predicate> pv : pvs) {
      if (pv.getElem() == null) continue;
      sum += weights[pv.getElem().getIndex()] * pv.getValue();
    }
    return sum;
  }

  public void setSigmaSquared(double sigmaSquared) {
    this.sigmaSquared = sigmaSquared;
  }


  public void setMaxIterations(int maxIters) {
    this.maxIters = maxIters;
  }

  public void train(final Iterable<IPair<T,Double>> data) {
    predManager.indexAll(Functional.map(data, new Fn<IPair<T, Double>, T>() {
      public T apply(IPair<T, Double> input) {
        return input.getFirst();
      }}));
    predManager.lock();
    IDifferentiableFn objFn = new IDifferentiableFn() {
      public IPair<Double, double[]> computeAt(double[] x) {
        weights = DoubleArrays.clone(x);
        double obj = 0.0;
        double[] grad = new double[getDimension()];

        for (IPair<T, Double> pair : data) {
          T elem = pair.getFirst();
          List<IValued<Predicate>> pvs = predManager.getPredicates(elem);
          double trueY = pair.getSecond();
          double predictY = getPredictionInternal(pvs);
          double diffY = (trueY - predictY);
          obj += 0.5 * diffY * diffY;
          for (IValued<Predicate> pv : pvs) {
            grad[pv.getElem().getIndex()] += diffY * pv.getValue();
          }
        }
        DoubleArrays.scaleInPlace(grad, -1);
        //obj += (new L2Regularizer(sigmaSquared)).update(weights, grad, 1.0);
        return BasicPair.make(obj, grad);
      }

      public int getDimension() {
        return predManager.getPredIndexer().size();
      }
    };
    IOptimizer.Result res =
      (new LBFGSMinimizer()).minimize(objFn, new double[objFn.getDimension()],new LBFGSMinimizer.Opts());
    weights = res.minArg;
    System.out.println("weights: " + DoubleArrays.toString(weights));
//    ICounter<Predicate> preds = Counters.toCounter(weights, predManager.getPredIndexer());
//    preds.keepTopNKeysByAbsValue(100);
//    System.out.println("final weights: " + preds);
  }

  public double getPrediction(T elem) {
    return getPredictionInternal(predManager.getPredicates(elem));
  }

  public static void main(String[] args) {
    Execution.init();
    List<String> doc = Collections.makeList("fuzzy", "wuzzy");
    IPredExtractor<List<String>> predFn = new IPredExtractor<List<String>>() {
      public List<IValued<Predicate>> getPredicates(List<String> elem) {
        return Functional.map(elem, new Fn<String, IValued<Predicate>>() {
          public IValued<Predicate> apply(String input) {
            return BasicValued.make(new Predicate(input),1.0);
          }
        });
      }};
    (new LinearRegressionModel<List<String>>(predFn)).train(Collections.makeList(BasicPair.make(doc,1.0)));

  }

}