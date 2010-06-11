package edu.umass.nlp.ml.prob;

import edu.umass.nlp.functional.Fn;
import edu.umass.nlp.utils.ILockable;
import edu.umass.nlp.utils.IPair;


public interface IConditionalDistribution<C,O> extends Iterable<IPair<C,IDistribution<O>>>,
                                                       Fn<C, IDistribution<O>>,
                                                       ILockable {
  public IDistribution<O> getDistribution(C cond);
  public void observe(C cond, O obs, double weight);
}