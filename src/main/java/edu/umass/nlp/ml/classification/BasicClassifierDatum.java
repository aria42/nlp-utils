package edu.umass.nlp.ml.classification;

import edu.umass.nlp.utils.IValued;

import java.util.List;

public class BasicClassifierDatum implements ClassifierDatum {
  private final List<IValued<String>> preds;

  public BasicClassifierDatum(List<IValued<String>> preds) {
    this.preds = preds;
  }

  @Override
  public List<IValued<String>> getPredicates() {
    return preds;
  }
}
