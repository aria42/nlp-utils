package edu.umass.nlp.ml.classification;

public interface LabeledClassifierDatum<L> extends ClassifierDatum {
  public L getTrueLabel();
}
