package edu.umass.nlp.ml.sequence;

import java.io.Serializable;

public class Transition implements Serializable {

  public final State from;
  public final State to;
  public final int index;

  public Transition(State from, State to, int index) {
    this.from = from;
    this.to = to;
    this.index = index;
  }

  @Override
  public String toString() {
    return String.format("Trans(%s,%s)",from.label,to.label);
  }
}