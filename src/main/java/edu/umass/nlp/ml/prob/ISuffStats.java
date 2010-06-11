package edu.umass.nlp.ml.prob;

import edu.umass.nlp.utils.IMergable;

import java.io.Serializable;

public interface ISuffStats<T> extends IMergable<ISuffStats<T>>, Serializable {
  public void observe(T elem, double weight);
  public IDistribution<T> toDistribution();
}
