package edu.umass.nlp.ml.feats;



import edu.umass.nlp.utils.IIndexed;

import java.io.Serializable;

public class Predicate implements IIndexed<Predicate>, Serializable {

  private final String pred;
  private final int index;

  public Predicate(String pred, int index) {
    this.pred = pred;
    this.index = index;
  }

  public Predicate(String pred) {
    this(pred,-1);
  }

  public boolean isIndexed() {
    return index >= 0;
  }

  public int getIndex() {
    return index;
  }

  public Predicate getElem() {
    return this;
  }

  public Predicate withIndex(int index) {
    return new Predicate(pred, index);
  }

  @Override
  public String toString() {
    return "Pred(" + pred + ')';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Predicate predicate = (Predicate) o;

    if (pred != null ? !pred.equals(predicate.pred) : predicate.pred != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    return pred != null ? pred.hashCode() : 0;
  }
}