package edu.umass.nlp.functional;

public interface DoubleFn<T> {
  public double valAt(T x);
}