package edu.umass.nlp.utils;

import java.util.HashMap;
import java.util.Map;

public class MergableUtils {
  public static <K,M extends IMergable<M>>
    void mergeInto(Map<K, M> map,
                   Map<K, M> omap)
  {
    Map<K, M> res = new HashMap<K,M>();
    for (Map.Entry<K, M> entry : map.entrySet()) {
      K k = entry.getKey();
      M v = entry.getValue();
      M ov = omap.get(k);
      if (ov != null) v.merge(ov);
    }
    for (Map.Entry<K, M> entry : omap.entrySet()) {
      K k = entry.getKey();
      M v = map.get(k);
      if (v == null) {
        map.put(k, v);
      }
    }
  }
}