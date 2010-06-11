package edu.umass.nlp.ml.sequence;


import edu.umass.nlp.utils.IPair;

import java.util.List;

public interface SequenceModel<T> {

  public void train(Iterable<List<IPair<T,String>>> labeledInstances);

  public List<String> tag(List<T> input);

}