package edu.umass.nlp.utils;


import edu.umass.nlp.functional.DoubleFn;
import edu.umass.nlp.functional.Fn;
import edu.umass.nlp.functional.Functional;

import java.util.*;

public class MapCounter<K> extends AbstractCollection<IValued<K>> implements ICounter<K>  {

  private final static MutableDouble zero_ = new MutableDouble(0.0);
 
  private final Map<K, MutableDouble> counts_;
  private final MutableDouble totalCount_;

  private MapCounter(Map<K, MutableDouble> counts_, MutableDouble totalCount_) {
    this.counts_ = counts_;
    this.totalCount_ = totalCount_;
  }

  public MapCounter(Map<K, MutableDouble> counts_) {
    this.counts_ = counts_;
    this.totalCount_ = new MutableDouble(computeTotalCount());
  }

  public MapCounter() {
    this(new HashMap<K, MutableDouble>());
  }

  private double computeTotalCount() {
    double sum = 0.0;
    for (Map.Entry<K, MutableDouble> entry : counts_.entrySet()) {
      sum += entry.getValue().doubleValue();
    }
    return sum;
  }

  public double getCount(K key) {
    return Collections.get(counts_, key, zero_).doubleValue();
  }

  public void incCount(K key, double incAmt) {
    MutableDouble oldVal = Collections.getMut(counts_, key, new MutableDouble(0.0));
    double newVal = oldVal.doubleValue() + incAmt;
    if (newVal == 0.0) {
      counts_.remove(key);
    } else {
      oldVal.inc(incAmt);
    }
    totalCount_.inc(incAmt);
  }

  public void setCount(K key, double v) {
    if (v == 0.0) {
      MutableDouble oldVal = Collections.get(counts_, key, zero_);
      totalCount_.inc(v - oldVal.doubleValue());
      counts_.remove(key);
    } else {
      MutableDouble oldVal = Collections.getMut(counts_, key, new MutableDouble(0.0));
      totalCount_.inc(v - oldVal.doubleValue());
      oldVal.set(v);
    }
  }

  public double totalCount() {
    return totalCount_.doubleValue();
  }

  public ICounter<K> map(DoubleFn<IValued<K>> f) {
    Map<K, MutableDouble> newCounts = new HashMap<K, MutableDouble>();
    double newTotalCount = 0.0;
    for (IValued<K> valued : this) {
      double newVal = f.valAt(valued);
      newCounts.put(valued.getElem(), new MutableDouble(newVal));
      newTotalCount += newVal;
    }
    return new MapCounter(newCounts, new MutableDouble(newTotalCount));
  }

  public void mapDestructive(DoubleFn<IValued<K>> f) {
    double newTotalCount = 0.0;
    for (Map.Entry<K, MutableDouble> entry : counts_.entrySet()) {
      double oldVal = entry.getValue().doubleValue();
      double newVal = f.valAt(BasicValued.make(entry.getKey(), oldVal));
      entry.getValue().set(newVal);
      newTotalCount += newVal ;
    }
    totalCount_.set(newTotalCount);
  }

  public Iterator<IValued<K>> iterator() {
    return Functional.map(counts_.entrySet().iterator(), new Fn<Map.Entry<K, MutableDouble>, IValued<K>>() {
      public IValued<K> apply(Map.Entry<K, MutableDouble> input) {
        return BasicValued.make(input.getKey(), input.getValue().doubleValue());
      }
    });
  }

  public int size() {
    return counts_.size();
  }

  public boolean add(IValued<K> valued) {
    incCount(valued.getElem(), valued.getValue());
    return true;
  }


  @Override
  public String toString() {
    return toString(25);
  }

  public String toString(int maxKeys) {
    List<IValued<K>> vals = Counters.getTopK(this, maxKeys, null);
    return Functional.mkString(
      vals,  // elems
      "[",   // start
      ",",   // middle
      "]",   // stop
      new Fn<IValued<K>, String>() {
        public String apply(IValued<K> input) {
          return String.format("%s: %.4f\n", input.getElem(), input.getValue());
        }
      });
  }




  public static void main(String[] args) {
    ICounter<String> counts = new MapCounter<String>();
    System.out.println(counts);
    counts.incCount("planets", 7);
    System.out.println(counts);
    counts.incCount("planets", 1);
    System.out.println(counts);
    counts.setCount("suns", 1);
    System.out.println(counts);
    counts.incCount("aliens", 0);
    counts.add(BasicValued.make("aria",42.0));
    System.out.println(counts.toString(1));
    System.out.println(Counters.getTopK(counts,1,null));
    Counters.scaleDestructive(counts,2.0);
    System.out.println(counts.toString());
    System.out.println("Total: " + counts.totalCount());

  }
}