package edu.umass.nlp.ml;

import edu.umass.nlp.functional.Fn;
import edu.umass.nlp.utils.BasicPair;
import edu.umass.nlp.utils.IPair;

public class Regularizers {

  public static Fn<double[], IPair<Double, double[]>> getL2Regularizer(final double sigmaSq) {
    return new Fn<double[], IPair<Double, double[]>>() {
      public IPair<Double, double[]> apply(double[] input) {
        double obj = 0.0;
        double[] grad = new double[input.length];
        for (int i = 0; i < input.length; ++i) {
          double w = input[i];
          obj += w * w / sigmaSq;
          grad[i] += 2 * w / sigmaSq;
        }
        return new BasicPair(obj, grad);
      }
    };
  }

}
