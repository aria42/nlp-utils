package edu.umass.nlp.ml.sequence;

import java.util.List;


public interface ILabeledSeqDatum extends ISeqDatum {
  public List<String> getLabels();
}
