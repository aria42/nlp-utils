package edu.umass.nlp.optimize;

import edu.umass.nlp.utils.IPair;

/**
 *
 */
public interface IDifferentiableFn {
  public IPair<Double,double[]> computeAt(double[] x);
  public int getDimension();
}