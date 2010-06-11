package edu.umass.nlp.utils;

public class BasicPair<S,T> implements IPair<S,T> {

  private final S s;
  private final T t;

  public BasicPair(S s, T t) {
    this.s = s;
    this.t = t;
  }

  public S getFirst() {
    return s;
  }

  public T getSecond() {
    return t;
  }

  public static <S,T> IPair<S,T> make(S s, T t) {
    return new BasicPair<S,T>(s,t);
  }
}