package edu.umass.nlp.ml.sequence;


import edu.umass.nlp.utils.IValued;

import java.util.List;


public interface ISeqDatum {
  public List<List<String>> getNodePredicates();
  public double getWeight();
}
