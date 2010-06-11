package edu.umass.nlp.functional;

public interface PredFn<T> {

  public boolean holdsAt(T elem);

}