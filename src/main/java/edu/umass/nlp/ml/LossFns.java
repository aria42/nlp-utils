package edu.umass.nlp.ml;

import edu.umass.nlp.functional.Fn;
import edu.umass.nlp.utils.Collections;
import edu.umass.nlp.utils.ICounter;
import edu.umass.nlp.utils.IPair;
import edu.umass.nlp.utils.MapCounter;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


public class LossFns {

  public static <L> Map<L, ICounter<L>> compileLossFn(LossFn<L> lossFn, Collection<L> labels) {
    Map<L, ICounter<L>> res = new HashMap<L, ICounter<L>>();
    for (L label : labels) {
      for (L otherLabel : labels) {
        Collections.getMut(res, label, new MapCounter<L>())
          .incCount(otherLabel, lossFn.getLoss(label, otherLabel));
      }
    }
    return res;
  }

}
