package edu.umass.nlp.ml.classification;

import edu.umass.nlp.utils.IPair;

import java.io.Serializable;


public interface IClassifier<L> extends Serializable {

  public void train(Iterable<LabeledClassifierDatum<L>> data) ;

  public L classify(ClassifierDatum datum);

}