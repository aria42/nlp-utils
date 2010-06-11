package edu.umass.nlp.utils;

import org.apache.commons.collections.primitives.ArrayDoubleList;
import org.apache.commons.collections.primitives.DoubleList;

public class LogAdder {

  DoubleList xs = new ArrayDoubleList();

  public void add(double x) {
    if (x > Double.NEGATIVE_INFINITY) xs.add(x);
  }

  public double logSum() {
    return SloppyMath.logAdd(xs.toArray());
  }

}