package edu.umass.nlp.examples;

import edu.umass.nlp.functional.Fn;
import edu.umass.nlp.functional.Functional;
import edu.umass.nlp.io.IOUtils;
import edu.umass.nlp.ml.prob.BasicConditionalDistribution;
import edu.umass.nlp.ml.prob.DirichletMultinomial;
import edu.umass.nlp.ml.prob.IConditionalDistribution;
import edu.umass.nlp.ml.prob.IDistribution;
import edu.umass.nlp.ml.sequence.*;
import edu.umass.nlp.trees.ITree;
import edu.umass.nlp.trees.Trees;
import edu.umass.nlp.utils.DoubleArrays;
import edu.umass.nlp.utils.IPair;

import java.io.File;
import java.util.*;

public class HMMTest<O> {
  
  O startWord, stopWord;
  double obsLambda = 1.0;
  double transLambda = 1.0e-4;
  StateSpace stateSpace;
  int numIters = 100;
  Random rand = new Random(0);

  HMMTest(List<String> states,O startWord, O stopWord) {
    this.stateSpace = StateSpaces.makeFullStateSpace(states);
    this.startWord = startWord;
    this.stopWord = stopWord;
  }

  class HMM {

    IConditionalDistribution<State, O> obsDistr;
    IConditionalDistribution<State, State> transDistr;

    public HMM() {
      obsDistr = new BasicConditionalDistribution<State, O>(
        new Fn<State, IDistribution<O>>() {
          public IDistribution<O> apply(State input) {
            return DirichletMultinomial.make(obsLambda);
          }
        });
      transDistr = new BasicConditionalDistribution<State,State>(
        new Fn<State, IDistribution<State>>() {
          public IDistribution<State> apply(State input) {
            return DirichletMultinomial.make(transLambda);
          }
        });
    }

    public ForwardBackwards.Result random(List<O> seq) {
      ForwardBackwards fb = new ForwardBackwards(stateSpace);
      // uniform potentials will give uniform marginals, we add some noise
      double[][] pots = new double[seq.size()-1][stateSpace.getTransitions().size()];
      ForwardBackwards.Result fbRes = fb.compute(pots);
      for (double[] row : fbRes.stateMarginals) {
        DoubleArrays.addNoiseInPlace(row, rand, 1.0);
      }
      for (double[] row : fbRes.transMarginals) {
        DoubleArrays.addNoiseInPlace(row, rand, 1.0);
      }

      return fbRes;
    }

    public ForwardBackwards.Result doInference(List<O> seq) {
      double[][] pots = new double[seq.size()-1][stateSpace.getTransitions().size()];
      // fill potentials
      for (int i=0; i+1 < seq.size(); ++i) {
        for (Transition trans : stateSpace.getTransitions()) {
          // only first transition can be from start state
          if (i > 0 && trans.from.equals(stateSpace.startState)) continue;
          // only last transitiion can go to stop state
          if (i+2 < seq.size() && trans.to.equals(stateSpace.stopState)) continue;
          double logTransProb = transDistr.getDistribution(trans.from).getLogProb(trans.to);
          double logObsProb = i > 0 ?
            obsDistr.getDistribution(trans.from).getLogProb(seq.get(i)) :
            0.0;
          pots[i][trans.index] = logObsProb + logTransProb;
        }
      }
      return (new ForwardBackwards(stateSpace)).compute(pots);  
    }

    public void observe(ForwardBackwards.Result fbRes, List<O> seq) {
      for (int i=1; i+1 < seq.size(); ++i) {
        O obs = seq.get(i);
        for (int j = 0; j < fbRes.stateMarginals[i].length; j++) {
          State state = stateSpace.getStates().get(j);
          double post = fbRes.stateMarginals[i][j];
          obsDistr.observe(state,obs,post);
        }
      }
      for (int i=0; i+1 < seq.size(); ++i) {
        for (int t=0; t < fbRes.transMarginals[i].length; ++t) {
          Transition trans = stateSpace.getTransitions().get(t);
          double post  = fbRes.transMarginals[i][t];
          transDistr.observe(trans.from,trans.to, post);
        }
      }
    }

    public void mStep() {
      // should be a no-op, but the distributions
      // may want to do something (i.e LogLinear distributions)
      for (IPair<State, IDistribution<O>> pair : obsDistr) {
        pair.getSecond().lock();
      }
      for (IPair<State, IDistribution<State>> pair : transDistr) {
        pair.getSecond().lock();
      }
    }
  }

  public void learn(Iterable<List<O>> data) {
    HMM hmm = new HMM();
    for (int iter=0; iter < numIters; ++iter) {
      HMM newHMM = new HMM();
      double logLike = 0.0;
      for (List<O> datum  : data) {
        // on first iteration, we randomize
        // E-Step since params are not initialized
        ForwardBackwards.Result res = iter == 0 ?
          hmm.random(datum) :
          hmm.doInference(datum);
        newHMM.observe(res, datum);
        logLike += res.logZ;
      }
      newHMM.mStep();
      if (iter > 0) {
        System.out.println("negLogLike: " + (-logLike));
        for (IPair<State, IDistribution<O>> pair : newHMM.obsDistr) {
          System.out.println("for state: " + pair.getFirst() + " probs: " + pair.getSecond());
        }
      }
      hmm = newHMM;
    }
  }

  public static void main(String[] args) {
    List<String> states = new ArrayList<String>();
    for (int i=0; i < 5; ++i) {
      states.add(String.format("H%d",i));
    }
    HMMTest<String> test = new HMMTest<String>(states,"<s>","</s>");
    List<List<String>> data = Functional.map(
      Trees.readTrees(IOUtils.text(IOUtils.readerFromResource("samples/trees.mrg"))),
      new Fn<ITree<String>, List<String>>() {
        public List<String> apply(ITree<String> input) {
          List<String> tags = Trees.getLeafYield(input);
          tags.add(0, "<s>");
          tags.add("</s>");
          return tags;
        }});
    test.learn(data);
  }

}