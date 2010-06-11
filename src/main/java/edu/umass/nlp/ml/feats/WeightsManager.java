package edu.umass.nlp.ml.feats;

import edu.umass.nlp.utils.CounterMap;
import edu.umass.nlp.utils.ICounter;
import edu.umass.nlp.utils.Indexer;
import edu.umass.nlp.utils.Span;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Strange abstraction, but basically, this
 * combines the fact that for each predicate their
 * is a different weight for each possible label
 * (a predicate plus a label yield a feature).
 *
 * @param <L>
 */
public class WeightsManager<P,L> implements Serializable {

  public final Indexer<P> predIndexer;
  private final int numPreds;
  private final Indexer<L> labelIndexer;
  private final int numFeats;
  private double[] weights = null;

  public WeightsManager(Indexer<P> predIndexer, Indexer<L> labelIndexer) {
    predIndexer.lock();
    labelIndexer.lock();
    this.predIndexer = predIndexer;
    this.labelIndexer = labelIndexer;
    this.numPreds = predIndexer.size();
    this.numFeats = numPreds * labelIndexer.size();
  }

  public void setWeights(double[] weights) {
    this.weights = weights;
  }

  public void addScores(P pred, double val, double[] scores) {
    //assert pred.isIndexed();
    int predIndex = predIndexer.indexOf(pred);
    assert predIndex >= 0;
    for (int l=0; l < getNumLabels(); ++l) {
      int weightIndex = getWeightIndex(predIndex, l);
      scores[l] += val * weights[weightIndex];
    }
  }

  public void addFeatExpecations(P pred, double[] labelWeights, double[] accumExpectations) {
    //assert pred.isIndexed();
    int predIndex = predIndexer.indexOf(pred);
    assert predIndex >= 0;
    assert labelWeights.length == getNumLabels();
    assert accumExpectations.length == getNumFeats();
    for (int l=0; l < getNumLabels(); ++l) {
      int weightIndex = getWeightIndex(predIndex, l);
      accumExpectations[weightIndex] += labelWeights[l];
    }
  }

  public Span getIndexSpan(Predicate p) {
    int start = p.getIndex() * getNumLabels();
    return new Span(start, start + getNumLabels());
  }

  public int getNumFeats() {
    return numFeats;
  }

  public int getWeightIndex(int predIndex, int labelIndex) {
    return predIndex * getNumLabels() + labelIndex;
  }

//  public Map<L, ICounter<Predicate>> getWeightsByLabel() {
//    Map<L, ICounter<Predicate>> res = CounterMap.make();
//    for (int l=0; l < getNumLabels(); ++l) {
//      L label = labelIndexer.get(l);
//      for (P pred : predIndexer) {
//        int w = getWeightIndex(pred, l);
//        CounterMap.setCount(res,label,pred,weights[w]);
//      }
//    }
//    return res;
//  }

  public int getNumLabels() {
    return getNumLabels();
  }

//  public void inspect() {
//    Logger.startTrack("Weight Inspect");
//    CounterMap<L,Predicate> cm = getWeightsByLabel();
//    for (L l : labelIndexer) {
//      Logger.startTrack("Label: " + l);
//      Counter<Predicate> labelWeights = cm.getCounter(l);
//      List<Predicate> sortedKeys = Counters.absCounts(labelWeights).getSortedKeys().subList(0,20);
//      labelWeights.pruneExcept(new HashSet<Predicate>(sortedKeys));
//      Logger.logs(labelWeights.toString());
//      Logger.endTrack();
//    }
//    Logger.endTrack();
//  }

  public int getLabelIndex(L l) {
    return labelIndexer.indexOf(l);
  }

  public List<L> getLabelIndexer() {
    return labelIndexer;
  }
}