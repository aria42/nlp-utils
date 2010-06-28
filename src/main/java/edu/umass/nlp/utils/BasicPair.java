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

  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    BasicPair basicPair = (BasicPair) o;

    if (s != null ? !s.equals(basicPair.s) : basicPair.s != null) return false;
    if (t != null ? !t.equals(basicPair.t) : basicPair.t != null) return false;

    return true;
  }

  public int hashCode() {
    int result = s != null ? s.hashCode() : 0;
    result = 31 * result + (t != null ? t.hashCode() : 0);
    return result;
  }

  public static <S,T> IPair<S,T> make(S s, T t) {
    return new BasicPair<S,T>(s,t);
  }
}