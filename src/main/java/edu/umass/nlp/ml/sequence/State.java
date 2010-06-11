package edu.umass.nlp.ml.sequence;

import java.io.Serializable;

public class State implements Serializable {
  public final String label;
  public final int index;
  public State(String label, int index) {
    this.label = label;
    this.index = index;
  }

  @Override
  public String toString() {
    return String.format("State(%s)",label);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    State state = (State) o;

    if (index != state.index) return false;
    if (label != null ? !label.equals(state.label) : state.label != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = label != null ? label.hashCode() : 0;
    result = 31 * result + index;
    return result;
  }
}