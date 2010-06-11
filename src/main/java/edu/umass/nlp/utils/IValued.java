package edu.umass.nlp.utils;

/**
 * Abstraction for an element and a value associated
 * with that element. A feature value pair is an IValued
 * over the feature where the value is the feature-value.
 *
 * We don't just use IPair to avoid double auto-boxing
 * @param <K>
 */
public interface IValued<K> extends IPair<K,Double>, Comparable<IValued<K>>, IWrapper<K> {

  public double getValue();
  public IValued<K> withValue(double x);

}