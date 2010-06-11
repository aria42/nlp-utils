package edu.umass.nlp.ml.prob;

import edu.umass.nlp.utils.ILockable;
import edu.umass.nlp.utils.IValued;

import java.io.Serializable;
import java.util.Collection;
import java.util.Random;
import java.util.Set;

/**
 * Distribution abstraction. Most will be fine just sub-classing
 * AbstractDistribution, which has default implementations
 * of many of these functions. 
 */
public interface IDistribution<T> extends Iterable<IValued<T>>,                                          
                                          ILockable,
                                          Serializable {
  public Set<T> getSupport();
  public double getProb(T elem);
  public double getLogProb(T elem);
  public T getMode();
  public T getSample(Random r);  
  public void observe(T elem, double weight);
}