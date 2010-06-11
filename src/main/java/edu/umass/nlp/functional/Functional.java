package edu.umass.nlp.functional;

import edu.umass.nlp.ml.sequence.CRF;
import edu.umass.nlp.ml.sequence.ILabeledSeqDatum;
import edu.umass.nlp.utils.*;

import java.util.*;


/**
 * Collection of Functional Utilities you'd
 * find in any functional programming language.
 * Things like map, filter, reduce, etc..
 *
 */
public class Functional {


  public static <T> List<T> take(Iterator<T> it, int n) {
    List<T> result = new ArrayList<T>();
    for (int i=0; i < n && it.hasNext(); ++i) {
      result.add(it.next());
    }
    return result;
  }


  public static <T> IValued<T> findMax(Iterable<T> xs, DoubleFn<T> fn) {
    double max = Double.NEGATIVE_INFINITY;
    T argMax = null;
    for (T x : xs) {
      double val = fn.valAt(x);
      if (val > max)  { max = val ; argMax = x; }
    }
    return BasicValued.make(argMax,max);
  }

  public static <T> IValued<T> findMin(Iterable<T> xs, Fn<T,Double> fn) {
    double min= Double.POSITIVE_INFINITY;
    T argMin = null;
    for (T x : xs) {
      double val = fn.apply(x);
      if (val < min)  { min= val ; argMin = x; }
    }
    return BasicValued.make(argMin,min);
  }

	public static<K,I,V> Map<K,V> map(Map<K,I> map, Fn<I,V> fn, PredFn<K> pred, Map<K,V> resultMap) {
		for (Map.Entry<K,I> entry: map.entrySet()) {
		  K key = entry.getKey();
		  I inter = entry.getValue();
		  if (pred.holdsAt(key)) resultMap.put(key, fn.apply(inter));
		}
		return resultMap;
	}

  public static<I,O> Map<I,O> mapPairs(Iterable<I> lst, Fn<I,O> fn)
  {
    return mapPairs(lst,fn,new HashMap<I,O>());
  }

  public static<I,O> Map<I,O> mapPairs(Iterable<I> lst, Fn<I,O> fn, Map<I,O> resultMap)
  {
    for (I input: lst) {
		  O output = fn.apply(input);
		  resultMap.put(input,output);
		}
		return resultMap;
  }

	public static<I,O> List<O> map(Iterable<I> lst, Fn<I,O> fn) {
		return map(lst,fn,(PredFn<O>) PredFns.getTruePredicate());
	}

  public static<I,O> Iterator<O> map(final Iterator<I> it, final Fn<I,O> fn) {
		return new Iterator<O>() {
      public boolean hasNext() {
        return it.hasNext();
      }

      public O next() {
        return fn.apply(it.next());
      }

      public void remove() {
        throw new RuntimeException("remove() not supported");
      }
    };
	}

  public static <I,O> Map<I,O> makeMap(Iterable<I> elems, Fn<I,O> fn, Map<I,O> map) {
    for (I elem : elems) {
      map.put(elem, fn.apply(elem));
    }
    return map;
  }

  public static <I,O> Map<I,O> makeMap(Iterable<I> elems, Fn<I,O> fn) {
    return makeMap(elems, fn, new HashMap<I,O>()) ;
  }

	public static<I,O> List<O> flatMap(Iterable<I> lst,
	                                   Fn<I,List<O>> fn) {
		PredFn<List<O>> p = PredFns.getTruePredicate();
		return flatMap(lst,fn,p);
	}


	public static<I,O> List<O> flatMap(Iterable<I> lst,
	                                   Fn<I,List<O>> fn,
	                                   PredFn<List<O>> pred) {
		List<List<O>> lstOfLsts = map(lst,fn,pred);
		List<O> init = new ArrayList<O>();
		return reduce(lstOfLsts, init,
				new Fn<IPair<List<O>, List<O>>, List<O>>() {
					public List<O> apply(IPair<List<O>, List<O>> input) {
						List<O> result = input.getFirst();
						result.addAll(input.getSecond());
						return result;
					}
				});
	}

	public static<I,O> O reduce(Iterable<I> inputs,
	                            O initial,
	                            Fn<IPair<O,I>,O> fn) {
			O output = initial;
			for (I input: inputs) {
				output = fn.apply(BasicPair.make(output,input));
			}
			return output;
	}

	public static<I,O> List<O> map(Iterable<I> lst, Fn<I,O> fn, PredFn<O> pred) {
			List<O> outputs = new ArrayList();
			for (I input: lst) {
        O output = fn.apply(input);
				if (pred.holdsAt(output)) {
          outputs.add(output);
				}
			}
			return outputs;
	}

  public static<I> List<I> filter(final Iterable<I> lst, final PredFn<I> pred) {
    List<I> ret = new ArrayList<I>();
    for (I input : lst) {
      if (pred.holdsAt(input)) ret.add(input);
    }
    return ret;
  }


  public static <T> T first(Iterable<T> objs, PredFn<T> pred) {
    for (T obj : objs) {
      if (pred.holdsAt(obj)) return obj;
    }
    return null;
  }



  public static List<Integer> range(int n) {
    List<Integer> result = new ArrayList<Integer>();
    for (int i = 0; i < n; i++) {
      result.add(i);
    }
    return result;
  }

  /**
   *
   * @return
   */
  public static <T> boolean any(Iterable<T> elems, PredFn<T> p) {
    for (T elem : elems) {
      if (p.holdsAt(elem)) return true;
    }
    return false;
  }

  public static <T> boolean all(Iterable<T> elems, PredFn<T> p) {
    for (T elem : elems) {
      if (!p.holdsAt(elem)) return false;
    }
    return true;
  }


  public static <T> T find(Iterable<T> elems, PredFn<T> pred) {
    return first(elems, pred);
  }

  public static <T> int findIndex(Iterable<T> elems, PredFn<T> pred) {
    int index = 0;
    for (T elem : elems) {
      if (pred.holdsAt(elem)) return index;
      index += 1;
    }
    return -1;
  }

  public static <T> List<Integer> indicesWhere(Iterable<T> elems, PredFn<T> pred) {
    List<Integer> res = new ArrayList<Integer>();
    int index = 0;
    for (T elem : elems) {
      if (pred.holdsAt(elem)) {
        res.add(index);
      }
      index ++;
    }
    return res;
  }

  public static <T> String mkString(Iterable<T> elems, String start, String middle, String stop) {
    return mkString(elems, start, middle, stop, null);
  }

	public static <T> String mkString(Iterable<T> elems, String start, String middle, String stop,Fn<T,String> strFn) {
    StringBuilder sb = new StringBuilder();
    sb.append(start);
    Iterator<T> it = elems.iterator();
    while (it.hasNext()) {
      T t = it.next();
      sb.append((strFn != null ? strFn.apply(t) : t.toString()));
      if (it.hasNext()) {
        sb.append(middle);
      }
    }
    sb.append(stop);
    return sb.toString();
  }

  public static <T> String mkString(Iterable<T> elems) {
    return mkString(elems,"(",",",")",null);
  }

  public static <T> List<T> takeWhile(Iterable<T> elems, PredFn<T> pred) {
    Iterator<T> it = elems.iterator();
    return takeWhile(it,pred);
  }

  public static <T> List<T> takeWhile(Iterator<T> it, PredFn<T> pred) {
    List<T> res = new ArrayList<T>();
    while (it.hasNext()) {
      T elem = it.next();
      if (pred.holdsAt(elem)) res.add(elem);
      else break;
    }
    return res;
  }

  public static <T> List<Span>
  rangesWhere(Iterable<T> elems, PredFn<T> pred) {
    int index = 0;
    int lastStart = -1;
    List<Span> res = new ArrayList<Span>();
    for (T elem: elems) {
      boolean matches = pred.holdsAt(elem);
      if (matches && lastStart < 0) {
        lastStart = index;
      }
      if (!matches && lastStart >= 0) {
        res.add(new Span(lastStart,index));
        lastStart = -1;
      }
      index += 1;
    }
    if (lastStart >= 0) {
      res.add(new Span(lastStart, index));
    }
    return res;
  }

  public static <T> List<List<T>> subseqsWhere(List<T> elems, PredFn<T> pred) {
    List<Span> ranges = rangesWhere(elems, pred);
    List<List<T>> res = new ArrayList<List<T>>();
    for (Span span: ranges) {
      res.add(new ArrayList<T>(elems.subList(span.getStart(), span.getStop())));
    }
    return res;
  }

  public static <A,R> FactoryFn<R> curry(final Fn<A,R> fn, final A fixed) {
    return new FactoryFn<R>() {
      public R make() {
        return fn.apply(fixed);
      }
    };
  }

  public static <I,O> Iterable<O> lazyMap(final Iterable<I> xs, final Fn<I,O> fn) {
    return new Iterable<O>() {
      public Iterator<O> iterator() {
        return new Iterator<O>() {

          private Iterator<I> it = xs.iterator();

          public boolean hasNext() {
            return it.hasNext();
          }

          public O next() {
            return fn.apply(it.next());
          }

          public void remove() {
            throw new RuntimeException("Not Implemented");
          }
        };
      }
    };
  }
}