package edu.umass.nlp.ml.sequence;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StateSpace implements Serializable {

  private final List<State> states = new ArrayList<State>();
  private final Map<String, State> stateIndexer = new HashMap<String,State>();
  private boolean lockedStates = false;

  public final State startState ;
  public final State stopState ;

  private final List<Transition> allTrans = new ArrayList<Transition>();
  private List<Transition>[] transFrom;
  private List<Transition>[] transTo;

  public final static String startLabel = "<S>";
  public final static String stopLabel = "</S>";
  
  public StateSpace() {
    startState = addState(startLabel);
    stopState = addState(stopLabel);
  }

  public synchronized State addState(String label) {
    assert (!lockedStates);
    State existing = stateIndexer.get(label) ;
    if (existing != null) return existing;
    State state = new State(label, states.size());
    states.add(state);
    stateIndexer.put(label, state);
    return state;
  }

  public synchronized void lockStates() {
    this.lockedStates = true;
    final int numStates = getStates().size();
    transFrom = new List[numStates];
    transTo = new List[numStates];
    for (int s = 0; s < getStates().size(); ++s) {
      transFrom[s] = new ArrayList<Transition>();
      transTo[s] = new ArrayList<Transition>();
    }
  }

 

  public Transition findTransition(String start, String stop) {
    List<Transition> transs =  getTransitionsFrom(getState(start).index);
    for (Transition trans : transs) {
      if (trans.to.label.equals(stop)) {
        return trans;
      }
    }
    return null;
  }

  public synchronized boolean isStateLocked() {
    return lockedStates;
  }

  public List<State> getStates() {
    return states;
  }

  public State getState(String label) {
    return stateIndexer.get(label);
  }

  public synchronized Transition addTransition(String fromLabel, String toLabel) {
    if (!lockedStates) {
      lockStates();
    }
    State fromState = stateIndexer.get(fromLabel);
    assert (fromState != null);
    State toState = stateIndexer.get(toLabel);
    if (toState == startState) {
      throw new RuntimeException("Added transition to start-state: " + startState);
    }
    assert (toState != null);
    Transition found = findTransition(fromLabel, toLabel);
    if (found != null) return found;
    Transition trans = new Transition(fromState,toState,allTrans.size());
    allTrans.add(trans);
    transFrom[trans.from.index].add(trans);
    transTo[trans.to.index].add(trans);
    return trans;
  }

  public synchronized List<Transition> getTransitions() {
    return allTrans;
  }

  public List<Transition> getTransitionsFrom(int s) {
    return transFrom[s];
  }

  public List<Transition> getTransitionsTo(int s) {
    return transTo[s];
  }
}