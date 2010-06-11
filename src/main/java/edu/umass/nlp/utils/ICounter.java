package edu.umass.nlp.utils;

import edu.umass.nlp.functional.DoubleFn;

import java.util.Collection;


public interface ICounter<K> extends Collection<IValued<K>> {

  public void incCount(K key, double incAmt);
  public void setCount(K key, double v);

  // Mutable Operations
  public double getCount(K key);
  public double totalCount();

  // Generic
  public String toString(int maxKeys);


  // Abstract operations
  public ICounter<K> map(DoubleFn<IValued<K>> f);
  public void mapDestructive(DoubleFn<IValued<K>> f);
}