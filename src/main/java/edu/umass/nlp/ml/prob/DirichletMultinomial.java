package edu.umass.nlp.ml.prob;

import edu.umass.nlp.functional.Fn;
import edu.umass.nlp.functional.Functional;
import edu.umass.nlp.utils.*;

import java.util.Iterator;

public class DirichletMultinomial<T> extends AbstractDistribution<T>
                                     implements IMergable<DirichletMultinomial<T>> {

  ICounter<T> counts_ = new MapCounter<T>();
  double lambda_ = 0.0;
//  int numKeys = -1;

  //
  // IDistribution
  //

  public double getProb(T elem) {
    double numer = counts_.getCount(elem) + lambda_;
    double denom = counts_.totalCount() + counts_.size() * lambda_;
    assert denom > 0.0 :
      String.format("Bad Denom: %.3f %s %d", denom, counts_, counts_.size());
    double prob = numer / denom;
    assert prob > 0.0 && prob <= 1.0 : String.format("Bad prob: %.5f  for key: %s", prob, elem);
    return prob;
  }

  public Iterator<IValued<T>> iterator() {
    return Functional.map(counts_, new Fn<IValued<T>, IValued<T>>() {
      public IValued<T> apply(IValued<T> input) {
        return input.withValue(getProb(input.getElem()));
      }}).iterator();
  }

  public void observe(T elem, double count) {
    if (count != 0.0) counts_.incCount(elem, count);
  }

  //
  // IMergeable
  //

  public void merge(DirichletMultinomial<T> other) {
    Counters.incAll(this.counts_, other.counts_);
  }

  //
  // DirichletMultinomial
  //

  public double getLambda() {
    return lambda_;
  }

  public void setLambda(double lambda) {
    assert lambda > 0.0 : "Bad Lambda: " + lambda;
    this.lambda_ = lambda;
  }


  //
  // Object
  //

  public String toString() {
    return Counters.from(this).toString();
  }


  //
  // Factory Methods
  //

  public static <T> DirichletMultinomial<T> make(double lambda) {
    DirichletMultinomial<T> d = new DirichletMultinomial<T>();
    d.setLambda(lambda);
    return d;
  }

  public static <T> DirichletMultinomial<T> make(ICounter<T> elems) {
    DirichletMultinomial<T> d = new DirichletMultinomial<T>();
    for (IValued<T> entry : elems) {
      d.observe(entry.getElem(), entry.getValue());
    }
    return d;
  }


  //
  // Main
  //

  public static void main(String[] args) {
    DirichletMultinomial<String> d = DirichletMultinomial.make(1.0);
    d.observe("a",2.0);
    d.observe("c",1.0);
    System.out.println(d);
  }
}