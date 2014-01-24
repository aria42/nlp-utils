package edu.umass.nlp.optimize;

import edu.umass.nlp.utils.IPair;

/**
 * A differentiable function is one that can take a vector of
 * numbers and return the pair f(x) and gradient of f at x
 */
public interface IDifferentiableFn {
  public IPair<Double,double[]> computeAt(double[] x);
  public int getDimension();
}
