package edu.umass.nlp.ml.sequence;

import java.util.List;

public class BasicLabelSeqDatum implements ILabeledSeqDatum {

  private final List<List<String>> nodePreds;
  private final List<String> labels;
  private final double weight;

  public BasicLabelSeqDatum(List<List<String>> nodePreds, List<String> labels, double weight) {
    this.nodePreds = nodePreds;
    this.labels = labels;
    this.weight = weight;
  }

  public List<String> getLabels() {
    return labels;
  }

  public List<List<String>> getNodePredicates() {
    return nodePreds;
  }

  public boolean isLabeled() {
    return labels != null;
  }

  public double getWeight() {
    return weight;
  }

  


}
