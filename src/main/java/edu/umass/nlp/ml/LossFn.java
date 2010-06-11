package edu.umass.nlp.ml;

public interface LossFn<L> {
  public double getLoss(L trueLabel, L guessLabel);
}
