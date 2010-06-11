package edu.umass.nlp.ml.classification;

import edu.umass.nlp.ml.prob.IDistribution;

public interface IProbabilisticClassifier<T,L> extends IClassifier<T,L> {

  public IDistribution<L> getLabelDistribution(T elem);

}