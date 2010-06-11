package edu.umass.nlp.ml.sequence;

import edu.umass.nlp.utils.LogAdder;
import edu.umass.nlp.utils.Maxer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ForwardBackwards {

  // Result Class
  public static class Result {
    // Log sum over all paths
    public double logZ;
    // For each seq pos, marginals
    // over states
    public double[][] stateMarginals;
    // For each transition (seq-len - 1)
    // marginal over transitions
    public double[][] transMarginals;
  }

  
  // Final
  private final StateSpace stateSpace;
  private final int numStates, numTrans;

  // Mutable
  private double[][] potentials;
  private int seqLen;
  private boolean doneAlphas, doneBetas, doneNodeMarginals, doneEdgeMarginals;
  private double[][] alphas, betas, nodeMarginals, edgeMarginals;

  public ForwardBackwards(StateSpace stateSpace) {
    this.stateSpace = stateSpace;
    this.numStates = stateSpace.getStates().size();
    this.numTrans = stateSpace.getTransitions().size();
  }

  public Result compute(double[][] potentials) {
    this.potentials = potentials;
    this.seqLen = potentials.length+1;
    doneAlphas = false;
    doneBetas = false;
    doneNodeMarginals = false;
    doneEdgeMarginals = false;
    Result res = new Result();
    res.logZ = getLogZ();
    res.stateMarginals = getNodeMarginals();
    res.transMarginals = getEdgeMarginals();
    return res;
  }

  private void computeAlphas() {
    alphas = new double[seqLen][numStates];
    for (double[] row: alphas) Arrays.fill(row, Double.NEGATIVE_INFINITY);
    // Start
    alphas[0][stateSpace.startState.index] = 0.0;
    // Subseqent
    for (int i=1; i < seqLen; ++i) {
      for (int s=0; s < numStates; ++s) {
        LogAdder logAdder = new LogAdder();
        List<Transition> inTrans = stateSpace.getTransitionsTo(s);
        for (Transition trans: inTrans) {
          logAdder.add(
            alphas[i-1][trans.from.index] +
            potentials[i-1][trans.index]
          );
        }
        alphas[i][s] = logAdder.logSum();
      }
    }
  }

  private void ensureAlphas() {
    if (!doneAlphas) {
      computeAlphas();
      doneAlphas = true;
    }
  }

  public double getLogZ() {
    ensureAlphas();
    return alphas[seqLen-1][stateSpace.stopState.index];
  }

  public double[][] getAlphas() {
    ensureAlphas();
    return alphas;
  }

  private void ensureBetas() {
    ensureAlphas();
    if (!doneBetas) {
      computeBetas();
      doneBetas = true;
    }
  }

  private void computeBetas() {
    betas = new double[seqLen][numStates];
    for (double[] row: betas) Arrays.fill(row, Double.NEGATIVE_INFINITY);
    // Start
    betas[seqLen-1][stateSpace.stopState.index] = 0.0;
    // Subseqent
    for (int i=seqLen-2; i >= 0; --i) {
      for (int s=0; s < numStates; ++s) {
        LogAdder logAdder = new LogAdder();
        List<Transition> outTrans = stateSpace.getTransitionsFrom(s);
        for (Transition trans: outTrans) {
          logAdder.add(
            betas[i+1][trans.to.index] +
            potentials[i][trans.index]
          );
        }
        betas[i][s] = logAdder.logSum();
      }
    }
  }

  public double[][] getBetas() {
    ensureBetas();
    return betas;
  }

  public void setInput(double[][] potentials) {
    this.potentials = potentials;
    this.seqLen = potentials.length+1;
    doneAlphas = false;
    doneBetas = false;
    doneNodeMarginals = false;
    doneEdgeMarginals = false;
  }

  public double[][] getEdgeMarginals() {
    ensureEdgeMarginals();
    return edgeMarginals;
  }

  private void ensureEdgeMarginals() {
    ensureAlphas();
    ensureBetas();
    if (!doneEdgeMarginals) {
      computeEdgeMarginals();
      doneEdgeMarginals = true;
    }
  }

  private void computeEdgeMarginals() {
    edgeMarginals = new double[seqLen-1][numTrans];
    double logZ = getLogZ();
    for (int i=0; i < seqLen-1; ++i) {
      for (int s=0; s < numStates; ++s) {
        if (alphas[i][s] == Double.NEGATIVE_INFINITY) continue;
        for (Transition trans: stateSpace.getTransitionsFrom(s)) {
          double numer = alphas[i][s] +
            potentials[i][trans.index] + betas[i+1][trans.to.index];
          edgeMarginals[i][trans.index] = Math.exp(numer-logZ);
        }
      }
    }
  }

  public double[][] getNodeMarginals() {
    ensureNodeMarginals();
    return nodeMarginals;
  }

  private void ensureNodeMarginals() {
    ensureEdgeMarginals();
    if (!doneNodeMarginals) {
      computeNodeMarginals();
      doneNodeMarginals = true;
    }
  }

  private void computeNodeMarginals() {
    ensureEdgeMarginals();
    nodeMarginals = new double[seqLen][numStates];
    // Fist: Must Have All Mass on Start State
    nodeMarginals[0][stateSpace.startState.index] = 1.0;
    // Middle States
    for (int i=1; i < seqLen-1; ++i) {
      for (int s=0; s < numStates; ++s) {
        double nodeSum = 0.0;
        for (Transition trans : stateSpace.getTransitionsFrom(s)) {
          nodeSum += edgeMarginals[i][trans.index];
        }
        nodeMarginals[i][s] = nodeSum;
      }
    }
    // Last: Must Have All Mass on Stop State
    nodeMarginals[seqLen-1][stateSpace.stopState.index] = 1.0;
  }

  public List<String> viterbiDecode() {
    double[][] viterbiAlphas = new double[seqLen][numStates];
    for (double[] row: viterbiAlphas) Arrays.fill(row, Double.NEGATIVE_INFINITY);
    // Start
    viterbiAlphas[0][stateSpace.startState.index] = 0.0;
    // Subseqent
    for (int i=1; i < seqLen; ++i) {
      for (int s=0; s < numStates; ++s) {
        List<Transition> inTrans = stateSpace.getTransitionsTo(s);
        double max = Double.NEGATIVE_INFINITY;
        for (Transition trans: inTrans) {
          double val =
            viterbiAlphas[i-1][trans.from.index] +
            potentials[i-1][trans.index];
          if (val > max) {
            max = val;
          }
        }
        viterbiAlphas[i][s] = max;
      }
    }
    List<String> res = new ArrayList<String>();
    res.add(stateSpace.stopState.label);
    double trgVal =  viterbiAlphas[seqLen-1][stateSpace.stopState.index];
    int trgState = stateSpace.stopState.index;
    for (int pos=seqLen-2; pos >= 0; --pos) {
      boolean found = false;
      for (Transition trans : stateSpace.getTransitionsTo(trgState)) {
        double guess = potentials[pos][trans.index] + viterbiAlphas[pos][trans.from.index];
        if (Math.abs(guess-trgVal) < 1.0e-8) {
          trgVal = viterbiAlphas[pos][trans.from.index];
          trgState = trans.from.index;
          res.add(stateSpace.getStates().get(trgState).label);
          found = true;
          break;
        }
      }
      if (!found) throw new RuntimeException("Bad");
    }
    java.util.Collections.reverse(res);
    return res;
  }

}