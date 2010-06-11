package edu.umass.nlp.utils;


public class Span {

  private int start, stop;

  public Span(int start, int stop) {
    setAndEnsure(start,stop);
  }

  public Span shift(int offset) {
    return new Span(start+offset,stop+offset);
  }

  public boolean contains(Span other) {
    return this.start <= other.start && this.stop >= other.stop;
  }

  public boolean contains(int i) {
    return i >= start && i < stop;
  }

  private void setAndEnsure(int start, int stop) {
    assert stop >= start;
    this.start = start;
    this.stop = stop;
  }

  public int getLength() {
    return stop-start;
  }

  public int getStart() {
    return start;
  }

  public void setStart(int start) {
    setAndEnsure(start,stop);
  }

  public int getStop() {
    return stop;
  }

  public void setStop(int stop) {
    setAndEnsure(start,stop);
  }

  public String toString() {
    return String.format("(%d,%d)", start, stop);
  }

  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Span span = (Span) o;

    if (start != span.start) return false;
    if (stop != span.stop) return false;

    return true;
  }

  public int hashCode() {
    int result = start;
    result = 31 * result + stop;
    return result;
  }

  
}