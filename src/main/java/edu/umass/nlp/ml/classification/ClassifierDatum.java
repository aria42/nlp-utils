package edu.umass.nlp.ml.classification;

import edu.umass.nlp.utils.IValued;

import java.util.List;

public interface ClassifierDatum {
  public List<IValued<String>> getPredicates();
}
