package edu.umass.nlp.utils;


import edu.umass.nlp.functional.DoubleFn;
import edu.umass.nlp.functional.Fn;
import edu.umass.nlp.functional.Functional;
import edu.umass.nlp.ml.prob.DirichletMultinomial;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Counters {

  public static <T> ICounter<T> scale(ICounter<T> counts, final double alpha) {
    return counts.map(new DoubleFn<IValued<T>>() {
      public double valAt(IValued<T> x) {
        return x.getValue() * alpha;
      }
    });
  }

  public static <T> void scaleDestructive(ICounter<T> counts, final double alpha) {
    counts.mapDestructive(new DoubleFn<IValued<T>>() {
      public double valAt(IValued<T> x) {
        return x.getValue() * alpha;
      }
    });
  }

  public static <T> List<IValued<T>> getTopK(ICounter<T> counts, int k) {
    return getTopK(counts, k, null);
  }

  public static <T> List<IValued<T>> getTopK(ICounter<T> counts, int k, final DoubleFn<IValued<T>> f) {
    List<IValued<T>> vals = Collections.toList(counts.iterator());
    if (f != null) {
      vals = Functional.map(vals, new Fn<IValued<T>, IValued<T>>() {
        public IValued<T> apply(IValued<T> input) {
          return BasicValued.make(input.getElem(), f.valAt(input));
        }
      });
    }
    java.util.Collections.sort(vals, new Comparator<IValued<T>>() {
      public int compare(IValued<T> o1, IValued<T> o2) {
        if (o2.getValue() > o1.getValue()) return 1;
        if (o2.getValue() < o1.getValue()) return -1;
        return 0;
      }
    });
    return k < vals.size() ? vals.subList(0, k) : vals;
  }

  public static <T> void incAll(ICounter<T> accum, ICounter<T> counts) {
    for (IValued<T> elem : counts) {
      accum.incCount(elem.getElem(), elem.getValue());
    }
  }

  public static <T> Set<T> getKeySet(ICounter<T> counter) {
    Set<T> res = new HashSet<T>();
    for (IValued<T> valued : counter) {
      res.add(valued.getElem());
    }
    return res;
  }

  public static <T> ICounter<T> from(Iterable<IValued<T>> values) {
    ICounter<T> counts = new MapCounter<T>();
    for (IValued<T> value : values) {
      counts.incCount(value.getElem(), value.getValue());
    }
    return counts;
  }

  public static <T> ICounter<T> from(double[] vals, List<T> list) {
    ICounter<T> counts = new MapCounter<T>();
    for (int i = 0; i < vals.length; i++) {
      counts.incCount(list.get(i),vals[i]);
    }
    return counts;
  }
}