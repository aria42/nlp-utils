package edu.umass.nlp.ml.sequence;

import edu.umass.nlp.ml.prob.IDistribution;

import java.util.List;

public interface ProbabilisticSequenceModel<T> extends SequenceModel<T> {

  public List<IDistribution<String>> getTagMarginals(List<T> input);

}