package edu.umass.nlp.utils;

public class Maxer<L> {
  public double max = Double.NEGATIVE_INFINITY;
  public L argMax = null;
  public void observe(L elem, double val) {
    if (val > max) {
      argMax = elem;
      max = val;
    }
  }
}
