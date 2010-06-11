package edu.umass.nlp.ml.prob;

import edu.umass.nlp.functional.DoubleFn;
import edu.umass.nlp.functional.Fn;
import edu.umass.nlp.functional.Functional;
import edu.umass.nlp.utils.BasicValued;
import edu.umass.nlp.utils.Collections;
import edu.umass.nlp.utils.IMergable;
import edu.umass.nlp.utils.IValued;
import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;


public abstract class AbstractDistribution<T>  implements IDistribution<T>, Serializable {

  public static final long serialVersionUID = 42L;
  
  protected boolean locked = false;
  protected Logger logger = Logger.getLogger("AbstractDistribution");

  //
  // IDistribution
  //

  public abstract double getProb(T elem);

  public double getLogProb(T elem) {
    return Math.log(getProb(elem));
  }

  public T getMode() {
    assert !getSupport().isEmpty();
    return Functional.findMax(this, new DoubleFn<IValued<T>>() {
      public double valAt(IValued<T> input) {
        return input.getValue();
      }}).getElem().getElem();
  }
  
  public T getSample(Random r) {
    double target = r.nextDouble();
    double sofar = 0.0;
    for (IValued<T> valued : this) {
      double p = valued.getValue();
      if (target > sofar && target < (sofar+p)) {
        return valued.getElem();
      }
      sofar += p;
    }
    throw new RuntimeException("error: Couldn't get sample. Only saw mass " + sofar);
  }

  public Set<T> getSupport() {
    Set<T> supp = new HashSet<T>();
    for (IValued<T> valued : this) {
      supp.add(valued.getElem());
    }
    return supp;
  }


  //
  // ILockable
  //


  public boolean isLocked() {
    return locked;
  }

  public void lock() {
    if (locked) {
      logger.warn("Trying to lock an already locked distribution");
    }
    else locked = true;
  }

  public String toString() {
    return toString(20);
  }

  public String toString(int numEntries) {
    List<IValued<T>> entries = Collections.toList(this);
    return Functional.mkString(entries.subList(0, Math.min(entries.size(), numEntries)),
      "[", ",", "]",
      new Fn<IValued<T>, String>() {
        public String apply(IValued<T> input) {
          return String.format("%s : %.4f", input.getFirst(), input.getSecond());
        }
      }).toString();
  }

}