package edu.umass.nlp.ml.feats;


import edu.umass.nlp.functional.Fn;
import edu.umass.nlp.functional.Functional;
import edu.umass.nlp.utils.BasicValued;
import edu.umass.nlp.utils.IValued;
import edu.umass.nlp.utils.Indexer;

import java.io.Serializable;
import java.util.List;

public class PredicateManager<T> implements Serializable {

  private IPredExtractor<T> predFn;
  private Indexer<Predicate> predIndexer = new Indexer<Predicate>();
  private boolean isLocked = false;

  public PredicateManager(IPredExtractor predFn) {
    this.predFn = predFn;
  }

  public void lock() {
    if (!isLocked) {
      predIndexer.lock();
      isLocked = true;
    }
  }

  public boolean isLocked() {
    return isLocked;
  }

  public List<IValued<Predicate>> getPredicates(T elem) {
    return Functional.map(predFn.getPredicates(elem), new Fn<IValued<Predicate>,IValued<Predicate>>() {
      public IValued<Predicate> apply(IValued<Predicate> input) {
        return BasicValued.make(getIndexedPredicate(input.getElem()),input.getValue());
      }});
  }

  private Predicate getIndexedPredicate(Predicate pred) {
    if (pred.isIndexed()) return pred;
    int index = predIndexer.indexOf(pred);
    if (index < 0) {
      if (isLocked) return null;
      pred = pred.withIndex(predIndexer.size());
      predIndexer.add(pred);
      return pred;
    }
    return predIndexer.get(index);
  }

  public Indexer<Predicate> getPredIndexer() {
    return predIndexer;
  }

  public void indexAll(Iterable<T> elems) {
    assert !isLocked();
    for (T elem : elems) {
      for (IValued<Predicate> pv : predFn.getPredicates(elem)) {
        // side-effect: indexs pred
        getIndexedPredicate(pv.getElem());
      }
    }
  }
}