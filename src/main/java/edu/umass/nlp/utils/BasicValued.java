package edu.umass.nlp.utils;

/**
 * Immutable IValued
 * @param <K>
 */
public class BasicValued<K> implements IValued<K> {

  private final K elem;
  private final double val;

  public BasicValued(K elem, double val) {
    this.elem = elem;
    this.val = val;
  }

  public K getElem() {
    return elem;
  }

  public double getValue() {
    return val;
  }

  public IValued<K> withValue(double x) {
    return new BasicValued(elem, x);
  }

  public K getFirst() {
    return getElem();
  }

  public Double getSecond() {
    return val;
  }


  public int compareTo(IValued<K> o) {
    double v1 = this.getValue();
    double v2 = o.getValue();
    if (v1 < v2) return -1;
    if (v2 > v1) return 1;
    return 0;
  }

  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    BasicValued that = (BasicValued) o;

    if (Double.compare(that.val, val) != 0) return false;
    if (elem != null ? !elem.equals(that.elem) : that.elem != null) return false;

    return true;
  }

  public int hashCode() {
    int result;
    long temp;
    result = elem != null ? elem.hashCode() : 0;
    temp = val != +0.0d ? Double.doubleToLongBits(val) : 0L;
    result = 31 * result + (int) (temp ^ (temp >>> 32));
    return result;
  }

  @Override
  public String toString() {
    return String.format("(%s,%.4f)", elem, val);
  }

  public static <K> BasicValued<K> make(K elem, double val) {
    return new BasicValued(elem, val);
  }
}