package edu.umass.nlp.ml.classification;

import edu.umass.nlp.utils.BasicValued;
import edu.umass.nlp.utils.IValued;

import java.util.ArrayList;
import java.util.List;

public class BasicLabeledClassifierDatum<L> implements LabeledClassifierDatum<L> {
  private final List<IValued<String>> preds;
  private final L label;

  public BasicLabeledClassifierDatum(List<IValued<String>> preds, L label) {
    this.preds = preds;
    this.label = label;
  }

  @Override
  public L getTrueLabel() {
    return label;
  }

  @Override
  public List<IValued<String>> getPredicates() {
    return preds;
  }

  /**
   *
   */
  public static <L> LabeledClassifierDatum<L>
    getBinaryDatum(L label,String... preds) {
    List<IValued<String>> predPairs = new ArrayList<IValued<String>>();
    for (String pred : preds) {
      predPairs.add(BasicValued.make(pred,1.0));
    }
    return new BasicLabeledClassifierDatum(predPairs, label);
  }

}
