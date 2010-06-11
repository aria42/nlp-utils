package edu.umass.nlp.ml.feats;


import edu.umass.nlp.utils.IValued;

import java.util.List;

public interface IPredExtractor<T> {

  public List<IValued<Predicate>> getPredicates(T elem);

}