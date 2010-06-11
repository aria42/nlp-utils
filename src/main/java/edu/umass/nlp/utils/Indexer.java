package edu.umass.nlp.utils;

import java.io.Serializable;
import java.util.*;

public class Indexer<L> extends AbstractList<L> implements Serializable, ILockable {

  private final List<L> elems = new ArrayList<L>();
  private final Map<L, Integer> indexMap = new HashMap<L, Integer>();
  private boolean locked = false;

  public class IndexedWrapper<L> implements IIndexed<L> {
    private final L elem ;

    public L getElem() {
      return elem;
    }

    public IndexedWrapper(L elem) {
      this.elem = elem;
    }

    public int getIndex() {
      return indexMap.get(elem);
    }
  }

  public IIndexed<L> getIndexed(L elem) {
    return new IndexedWrapper<L>(elem);
  }

  public boolean isLocked() {
    return locked;
  }

  public void lock() {
    if (locked) {
      throw new RuntimeException("Tryed to lock() a locked Indexer");
    }
    locked = true;
  }

  public int indexOf(Object elem) {    
    Integer i = indexMap.get(elem);
    return i == null ? -1 : i;
  }

  @Override
  public boolean add(L elem) {
    if (isLocked()) {
      throw new RuntimeException("Tryed to sum() to a locked Indexer");
    }
    Integer index = indexMap.get(elem);
    if (index != null) {
      return false;
    }
    elems.add(elem);
    indexMap.put(elem, elems.size() - 1);
    return true;
  }

  public int getIndex(L elem) {
    Integer index = indexMap.get(elem);
    return index != null ? index.intValue() : -1;
  }

  public L get(int index) {
    return elems.get(index);
  }

  public int size() {
    return elems.size();
  }
}
