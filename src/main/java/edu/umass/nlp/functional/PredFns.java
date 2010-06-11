package edu.umass.nlp.functional;

public class PredFns {

  public static <T> PredFn<T> getTruePredicate() {
    return new PredFn<T>() {
      public boolean holdsAt(T elem) {
        return true;
      }};
  }
}