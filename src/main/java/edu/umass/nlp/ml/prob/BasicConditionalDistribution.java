package edu.umass.nlp.ml.prob;

import edu.umass.nlp.functional.Fn;
import edu.umass.nlp.functional.Functional;
import edu.umass.nlp.utils.BasicPair;
import edu.umass.nlp.utils.Collections;
import edu.umass.nlp.utils.IPair;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class BasicConditionalDistribution<C,O> implements IConditionalDistribution<C,O> {

  private final Logger logger = Logger.getLogger("BasicCondDistr");
  private final Map<C,IDistribution<O>> distrs ;
  private final Fn<C, IDistribution<O>> distrFact ;
  private boolean locked = false;

  public BasicConditionalDistribution(Fn<C, IDistribution<O>> distrFact) {
    this.distrs = new HashMap<C, IDistribution<O>>();
    this.distrFact = distrFact;
  }

  //
  // IConditional Distribution
  //

  public IDistribution<O> getDistribution(C cond) {
    assert isLocked();
    return Collections.getMut(distrs, cond, Functional.curry(distrFact, cond));
  }

  public IDistribution<O> apply(C input) {
    return getDistribution(input);
  }

  public void observe(C cond, O obs, double weight) {
    assert !isLocked();
    Collections.getMut(distrs, cond, Functional.curry(distrFact, cond)).observe(obs, weight);
  }

  public Iterator<IPair<C, IDistribution<O>>> iterator() {
    return Functional.map(distrs.entrySet().iterator(),new Fn<Map.Entry<C, IDistribution<O>>, IPair<C, IDistribution<O>>>() {
      public IPair<C, IDistribution<O>> apply(Map.Entry<C, IDistribution<O>> input) {
        return BasicPair.make(input.getKey(), input.getValue());
      }});
  }

  //
  // ILockable
  //

  public boolean isLocked() {
    return locked;
  }

  public void lock() {
    if (locked) {
      logger.warn("lock() called on locked object");
    }
    locked = true;
  }
}