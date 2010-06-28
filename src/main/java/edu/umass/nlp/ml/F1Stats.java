package edu.umass.nlp.ml;

public class F1Stats {

  public int tp = 0, fp = 0, fn = 0;
  public final String label;
  
  public F1Stats(String label) {
    this.label = label;
  }

  public void merge(F1Stats other) {
    tp += other.tp;
    fp += other.fp;
    fn += other.fn;
  }
    

  public double getPrecision() {
    if (tp + fp > 0.0) {
      return (tp / (tp + fp + 0.0));
    } else {
      return 0.0;
    }
  }

  public double getRecall() {
    if (tp + fn > 0.0) {
      return (tp / (tp + fn + 0.0));
    } else {
      return 0.0;
    }
  }

  public double getFMeasure(double beta) {
    double p = getPrecision();
    double r = getRecall();
    if (p + r > 0.0) {
      return ((1+beta*beta)* p * r) / ((beta*beta)*p + r);
    } else {
      return 0.0;
    }
  }

  public void observe(String trueLabel, String guessLabel) {
    assert (label.equals(trueLabel) || label.equals(guessLabel));
    if (label.equals(trueLabel)) {
        if (trueLabel.equals(guessLabel)) {
            tp++;
        } else {
            fn++;
        }
    } else {
        fp++;
    }
    if (trueLabel.equals(label)) {
      tp++;
    } else if (label.equals(trueLabel)) {
      fn++;
    } else if (label.equals(guessLabel)) {
      fp++;
    }
  }

  public String toString() {
    return String.format("f1: %.3f f2: %.3f prec: %.3f recall: %.3f (tp: %d, fp: %d, fn: %d)",
      getFMeasure(1.0), getFMeasure(2.0), getPrecision(), getRecall(), tp, fp, fn);
  }
}
