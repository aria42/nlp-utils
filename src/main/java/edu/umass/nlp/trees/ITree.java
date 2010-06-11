package edu.umass.nlp.trees;

import edu.umass.nlp.utils.Span;
import java.util.List;

/**
 * Abstraction of a Tree. See class Tree for
 * methods on a tree (getNodes,  
 * @param <L>
 */
public interface ITree<L> {

  public L getLabel();

  public List<ITree<L>> getChildren();
  
}
