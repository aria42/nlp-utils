package edu.umass.nlp.ml.classification;

import java.io.Serializable;


public interface IClassifier<L> extends Serializable {

  public void train(Iterable<LabeledClassifierDatum<L>> data, Object opts);

  public L classify(ClassifierDatum datum);

}