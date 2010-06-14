package edu.umass.nlp.ml;

public class F1Stats {

  public int tp = 0, fp = 0, fn = 0;
  public final String label;

  public F1Stats(String label) {
    this.label = label;
  }

  public void merge(F1Stats other) {
    tp = other.tp;
    fp = other.fp;
    fn = other.fn;
  }
    

  public double getPrecision() {
    if (tp + fp > 0.0) {
      return (tp / (tp + fp));
    } else {
      return 0.0;
    }
  }

  public double getRecall() {
    if (tp + fn > 0.0) {
      return (tp / (tp + fn));
    } else {
      return 0.0;
    }
  }

  public double getFMeasure() {
    double p = getPrecision();
    double r = getRecall();
    if (p + r > 0.0) {
      return (2 * p * r) / (p + r);
    } else {
      return 0.0;
    }
  }

  public void observe(String trueLabel, String guessLabel) {
    if (trueLabel.equals(guessLabel)) {
      tp++;
    } else if (label.equals(trueLabel)) {
      fn++;
    } else if (label.equals(guessLabel)) {
      fp++;
    }
  }

  public String toString() {
    return String.format("prec: %.3f recall: %.3f f1: %.3f", getPrecision(), getRecall(), getFMeasure());
  }
}
