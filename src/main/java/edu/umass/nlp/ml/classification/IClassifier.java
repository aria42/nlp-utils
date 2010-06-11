package edu.umass.nlp.ml.classification;

import edu.umass.nlp.utils.IPair;

import java.io.Serializable;


public interface IClassifier<T,L> extends Serializable {

  public void train(Iterable<IPair<T,L>> data) ;

  public L classify(T datum);

}