package edu.umass.nlp.optimize;

import edu.umass.nlp.utils.BasicPair;
import edu.umass.nlp.utils.DoubleArrays;
import edu.umass.nlp.utils.IPair;


public class CachingDifferentiableFn implements IDifferentiableFn {
  private final IDifferentiableFn fn ;
  private double[] lastX;
  private IPair<Double,double[]> lastVal;

  public CachingDifferentiableFn(IDifferentiableFn fn) {
    this.fn = fn;
  }

  public IPair<Double, double[]> computeAt(double[] x) {
    if (lastX != null && java.util.Arrays.equals(x,lastX)) {
      return BasicPair.make(lastVal.getFirst(),DoubleArrays.clone(lastVal.getSecond()));          
    }
    this.lastX = DoubleArrays.clone(x);
    this.lastVal = fn.computeAt(x);
    return BasicPair.make(lastVal.getFirst(),DoubleArrays.clone(lastVal.getSecond()));
  }

  public int getDimension() {
    return fn.getDimension();
  }
}
