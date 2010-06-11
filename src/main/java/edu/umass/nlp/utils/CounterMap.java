package edu.umass.nlp.utils;

import java.util.HashMap;
import java.util.Map;

public class CounterMap {

  private static final ICounter zero = new MapCounter();

  public static <K, V> void incCount(Map<K, ICounter<V>> counters, K key, V innerKey, double count) {
    Collections.getMut(counters, key, new MapCounter<V>()).incCount(innerKey, count);
  }

  public static <K, V> void setCount(Map<K, ICounter<V>> counters, K key, V innerKey, double count) {
    Collections.getMut(counters, key, new MapCounter<V>()).setCount(innerKey, count);
  }

  public static <K, V> void getCount(Map<K, ICounter<V>> counters, K key, V innerKey) {
    Collections.get(counters, key, (ICounter<V>)zero).getCount(innerKey);
  }

  public static <K, V> Map<K, ICounter<V>> make() {
    return new HashMap<K, ICounter<V>>();
  }

  public static void main(String[] args) {
    Map<String,ICounter<String>> counts = CounterMap.make();
    CounterMap.incCount(counts, "a","b",1.0);
    System.out.println(counts);
  }
}
