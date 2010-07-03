package edu.umass.nlp.ml.classification;

import edu.umass.nlp.ml.prob.IDistribution;

public interface IProbabilisticClassifier<L> extends IClassifier<L> {

  public IDistribution<L> getLabelDistribution(ClassifierDatum datum);

}