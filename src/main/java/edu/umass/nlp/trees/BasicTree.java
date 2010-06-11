package edu.umass.nlp.trees;

import edu.umass.nlp.utils.Collections;

import java.util.ArrayList;
import java.util.List;

public class BasicTree<L> implements ITree<L> {

  private final L label;
  private final List<ITree<L>> children;

  public BasicTree(L label) {
    this(label, java.util.Collections.<ITree<L>>emptyList());
  }

  public BasicTree(L label, List<ITree<L>> children) {
    this.label = label;
    this.children = java.util.Collections.unmodifiableList(new ArrayList<ITree<L>>(children));
  }

  //
  // ITree
  //

  public List<ITree<L>> getChildren() {
    return children;
  }

  public L getLabel() {
    return label;
  }

  //
  // Object
  //

  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    BasicTree basicTree = (BasicTree) o;

    if (children != null ? !children.equals(basicTree.children) : basicTree.children != null) return false;
    if (label != null ? !label.equals(basicTree.label) : basicTree.label != null) return false;

    return true;
  }

  public int hashCode() {
    int result = label != null ? label.hashCode() : 0;
    result = 31 * result + (children != null ? children.hashCode() : 0);
    return result;
  }

  public String toString() {
    return Trees.toString(this);
  }
}
