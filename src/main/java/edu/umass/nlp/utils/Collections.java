package edu.umass.nlp.utils;


import edu.umass.nlp.functional.FactoryFn;

import java.util.*;

/**
 * Methods on Collections which are generally
 * non-destructive and functional in nature.
 *
 * @author aria42 (Aria Haghighi)
 */
public class Collections {

  public static <T> List<T> makeList(T... arr) {
    List<T> res = new ArrayList<T>();
    for (T t : arr) {
      res.add(t);
    }
    return res;
  }

  public static <T> List<T> toList(T[] arr) {
    List<T> res = new ArrayList<T>();
    for (T t : arr) {
      res.add(t);
    }
    return res;
  }

  public static <T> List<T> toList(Iterable<T> arr) {
    List<T> res = new ArrayList<T>();
    for (T t : arr) {
      res.add(t);
    }
    return res;
  }

  public static <T> List<T> toList(Iterator<T> it) {
    List<T> res = new ArrayList<T>();
    while (it.hasNext()) {
      T t = it.next();
      res.add(t);
    }
    return res;
  }

  public static <T> T randChoice(List<T> elems, Random rand) {
    int index = rand.nextInt(elems.size());
    return elems.get(index);
  }

  public static <K,V> V getMut(Map<K,V> map, K key, V notFound) {
    V val = map.get(key);
    if (val == null) {
      val = notFound;
      map.put(key,val);
    }
    return val;
  }

  public static <K,V> V getMut(Map<K,V> map, K key, FactoryFn<V> fn) {
    V val = map.get(key);
    if (val == null) {
      val = fn.make();
      map.put(key,val);
    }
    return val;
  }

  public static <K,V> V get(Map<K,V> map, K key, V notFound) {
    V val = map.get(key);
    if (val == null) {
      return notFound;
    }
    return val;
  }

  public static <T> IPair<List<T>,List<T>> splitAt(Iterable<T> elems, int index) {

    List<T> before = new ArrayList<T>();
    List<T> after = new ArrayList<T>();
    Iterator<T> it = elems.iterator();
    for (int i=0; it.hasNext(); ++i) {
      T elem = it.next();
      (i < index ? before : after).add(elem);
    }
    return BasicPair.make(before, after);
  }

  public static <T> T randChoice(List<T> elems) {
    return randChoice(elems, new Random());
  }

  public static <T> List<T> subList(List<T> elems, List<Integer> indices) {
    List<T> res = new ArrayList<T>();
    for (Integer index: indices) {
      res.add(elems.get(index));
    }
    return res;
  }

  public static <T> List<T> subList(List<T> elems, Span span) {
    return subList(elems, span.getStart(), span.getStop());
  }

  public static <T> List<T> subList(List<T> elems, int start) {
    return subList(elems, start, elems.size());
  }

  public static <T> Set<T> intersect(Iterable<T> s1, Set<T> s2) {
    Set<T> res = new HashSet<T>();
    for (T elem : s1) {
      if (s2.contains(elem)) res.add(elem);
    }
    return res;
  }

  public static <T> Set<T> intersect(Iterable<T> s1, Collection<T> s2) {
    return intersect(s1, new HashSet<T>(s2));
  }

  /**
   * Partitions <code>elems</code> into <code>numParts</code>
   * each of which are the same size (except possibly the last)
   *
   * Shouldn't copy list just have views
   */
  public static <T> List<List<T>> partition(List<T> elems, int numParts) {
    List<List<T>> res = new ArrayList<List<T>>();
    int sizeOfPart = (int) Math.ceil(((double) elems.size()) / numParts);
    for (int i=0; i < numParts; ++i) {
      int start = i*sizeOfPart;
      int stop = Math.min((i + 1) * sizeOfPart, elems.size());
      res.add(Collections.subList(elems,start,stop));
    }
    return res;
  }

  /**
   * Make a set from  varargs
   */
  public static <T> Set<T> set(T...elems) {
    Set<T> res = new HashSet<T>();
    for (T elem : elems) {
      res.add(elem);
    }
    return res;
  }

  /**
   * Much faster than java.util.List.subList, might be a little less safe.  
   *
   * @author aria42 
   */
  public static <T> List<T> subList(List<T> elems, int start, int stop) {
    class SubList<T> extends AbstractList<T> {
      List<T> elems;
      int start;
      int stop;

      SubList(final List<T> elems, final int start, final int stop) {        
        if (elems instanceof SubList) {
          this.elems = ((SubList)elems).elems;
          this.start = ((SubList)elems).start + start;
          this.stop = ((SubList)elems).start + stop;
        } else {
          this.elems = elems;
          this.start = start;
          this.stop = stop;
        }
      }

      public T get(int index) {
        return elems.get(index+start);
      }

      public int size() {
        return (stop-start);
      }
    }
    return new SubList(elems,start,stop);    
  }

  public static <K> List<K> concat(List<K>...items) {
    List<K> res = new ArrayList<K>();
    for (List<K> item : items) {
      res.addAll(item);
    }
    return res;
  }

  public static <T> List<T> take(Iterable<T> elems, int n) {
	  List<T> res = new ArrayList<T>();
	  for (T elem: elems) {
		res.add(elem);
		if (res.size() == n) break;
	  }
	  return res;
  }
}