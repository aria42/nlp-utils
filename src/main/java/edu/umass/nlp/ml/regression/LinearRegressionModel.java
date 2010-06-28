package edu.umass.nlp.ml.regression;

import edu.umass.nlp.exec.Execution;
import edu.umass.nlp.functional.Fn;
import edu.umass.nlp.functional.Functional;
import edu.umass.nlp.ml.Regularizers;
import edu.umass.nlp.ml.feats.IPredExtractor;
import edu.umass.nlp.ml.feats.Predicate;
import edu.umass.nlp.ml.feats.PredicateManager;
import edu.umass.nlp.optimize.GradientDescent;
import edu.umass.nlp.optimize.IDifferentiableFn;
import edu.umass.nlp.optimize.IOptimizer;
import edu.umass.nlp.optimize.LBFGSMinimizer;
import edu.umass.nlp.utils.*;
import org.apache.commons.collections.primitives.ArrayDoubleList;
import org.apache.commons.collections.primitives.ArrayIntList;
import org.apache.commons.collections.primitives.DoubleList;
import org.apache.commons.collections.primitives.IntList;

import java.util.List;

public class LinearRegressionModel {

  private Indexer<String> featIndexer;
  private double[] weights;

  public static interface Datum {
    public List<IValued<String>> getFeatures();
    public double getWeight();
  }

  public static class BasicLabeledDatum implements LabeledDatum {
    private final List<IValued<String>> feats;
    private final double weight;
    private final double target;

    public BasicLabeledDatum(List<IValued<String>> feats, double target, double weight) {
      this.feats = feats;
      this.target = target;
      this.weight = weight;
    }

    public double getTarget() {
      return target;
    }

    public List<IValued<String>> getFeatures() {
      return feats;
    }

    public double getWeight() {
      return weight;
    }
  }

  public static interface LabeledDatum extends Datum {
    public double getTarget();
  }

  private class InternalDatum {
    public IntList featIndices;
    public DoubleList featVals;
    public double target;
    public double weight;

    private InternalDatum(LabeledDatum datum) {
      featIndices = new ArrayIntList();
      featVals = new ArrayDoubleList();
      target = datum.getTarget();
      weight = datum.getWeight();
      for (IValued<String> valued : datum.getFeatures()) {
        int featIndex = featIndexer.getIndex(valued.getElem());
        if (featIndex < 0) continue;
        featIndices.add(featIndex);
        featVals.add(valued.getSecond());
      }
    }
  }

  public static class Opts {
    public IOptimizer.Opts optimizerOpts = new LBFGSMinimizer.Opts();
    public Fn<double[], IPair<Double, double[]>> regularizer;
  }

  private double getPredictionInternal(List<IValued<String>> pvs) {
    double sum = 0.0;
    for (IValued<String> pv : pvs) {
      int featIndex = featIndexer.getIndex(pv.getElem());
      if (featIndex < 0) continue;
      sum += weights[featIndex] * pv.getValue();
    }
    return sum;
  }

  private double getPredictionInternal(InternalDatum datum) {
    double sum = 0.0;
    for (int i=0; i < datum.featIndices.size(); ++i) {
      int featIndex = datum.featIndices.get(i);
      double val = datum.featVals.get(i);
      if (featIndex < 0) continue;
      sum += weights[featIndex] * val;
    }
    return sum;
  }

  public void train(final Iterable<LabeledDatum> data, final Opts opts) {
    featIndexer = new Indexer<String>();
    for (LabeledDatum datum : data) {
      for (IValued<String> valued : datum.getFeatures()) {
        featIndexer.add(valued.getElem());
      }
    }
    featIndexer.lock();

    final List<InternalDatum> internalData = Functional.map(data, new Fn<LabeledDatum, InternalDatum>() {
      public InternalDatum apply(LabeledDatum input) {
        return new InternalDatum(input);
      }
    });

    IDifferentiableFn objFn = new IDifferentiableFn() {
      public IPair<Double, double[]> computeAt(double[] x) {
        weights = DoubleArrays.clone(x);
        double obj = 0.0;
        double[] grad = new double[getDimension()];

        for (InternalDatum datum: internalData) {
          double trueY = datum.target;
          double predictY = getPredictionInternal(datum);
          double diffY = (trueY - predictY);
          assert !Double.isNaN(diffY);
          obj += datum.weight * 0.5 * diffY * diffY;
          for (int i=0; i < datum.featIndices.size(); ++i) {
            int featIndex = datum.featIndices.get(i);
            if (featIndex < 0) continue;
            double featVal = datum.featVals.get(i);
            grad[featIndex] += datum.weight * diffY * featVal;
          }
        }
        DoubleArrays.scaleInPlace(grad, -1);

        if (opts.regularizer != null) {
          IPair<Double,double[]> res = opts.regularizer.apply(x);
          obj += res.getFirst();
          DoubleArrays.addInPlace(grad, res.getSecond());
        }


        return BasicPair.make(obj, grad);
      }

      public int getDimension() {
        return featIndexer.size();
      }
    };
    IOptimizer.Result res =
      (new LBFGSMinimizer()).minimize(objFn, new double[objFn.getDimension()], opts.optimizerOpts);
    weights = res.minArg;    
  }

  public double getPrediction(Datum datum) {
    return getPredictionInternal(datum.getFeatures());
  }

  public static void main(String[] args) {
    Execution.init();
    List<String> doc = Collections.makeList("fuzzy", "wuzzy");
    class MyDatum implements LabeledDatum {
      private Iterable<String> elems;
      MyDatum(String...elems) {
        this.elems = Collections.toList(elems);
      }
      public double getTarget() {
        return 1.0;
      }
      public double getWeight() {
        return 1.0;
      }

      public List<IValued<String>> getFeatures() {
        return Functional.map(elems, new Fn<String, IValued<String>>() {
          public IValued<String> apply(String input) {
            return new BasicValued<String>(input, 1.0);
          }});
      }
    }

//    IPredExtractor<List<String>> predFn = new IPredExtractor<List<String>>() {
//      public List<IValued<Predicate>> getPredicates(List<String> elem) {
//        return Functional.map(elem, new Fn<String, IValued<Predicate>>() {
//          public IValued<Predicate> apply(String input) {
//            return BasicValued.make(new Predicate(input), 1.0);
//          }
//        });
//      }
//    };
   (new LinearRegressionModel()).train(Collections.<LabeledDatum>makeList(new MyDatum("fuzzy","wuzzy")), new Opts());
  }

}