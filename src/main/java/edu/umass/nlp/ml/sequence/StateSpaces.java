package edu.umass.nlp.ml.sequence;


import java.util.ArrayList;
import java.util.List;

public class StateSpaces {

  public static StateSpace makeFullStateSpace(List<String> labels) {
    assert !labels.contains(StateSpace.startLabel);
    assert !labels.contains(StateSpace.stopLabel);
    StateSpace res = new StateSpace();
    for (String label : labels) {
      res.addState(label);
    }
    for (String label : labels) {
      res.addTransition(StateSpace.startLabel,label);
      res.addTransition(label,StateSpace.stopLabel);
      for (String nextLabel : labels) {
        res.addTransition(label, nextLabel);
      }
    }
    return res;
  }

//  private static <L> Tree<List<L>> buildTransitionTree(Tree<List<L>> root, StateSpace<L> stateSpace, int depth) {
//    if (depth == 0) {
//      return root;
//    }
//    List<L> curNGram = root.getLabel();
//    List<Tree<List<L>>> children = new ArrayList<Tree<List<L>>>();
//    if (curNGram.isEmpty()) {
//      for (State<L> state : stateSpace.getStates()) {
//        Tree<List<L>> child =
//          buildTransitionTree(new Tree(Collections.singletonList(state.label)),stateSpace,depth-1);
//        children.sum(child);
//      }
//    } else {
//      L last = curNGram.get(curNGram.size()-1);
//      State<L> lastState = stateSpace.getState(last);
//      for (Transition<L> trans : stateSpace.getTransitionsFrom(lastState.index)) {
//        List<L> newNGram = new ArrayList<L>(curNGram);
//        newNGram.sum(trans.to.label);
//        Tree<List<L>> child =
//          buildTransitionTree(new Tree(newNGram),stateSpace,depth-1);
//        children.sum(child);
//      }
//    }
//    return new Tree<List<L>>(curNGram, children);
//  }
//
//  public static <L> StateSpace<List<L>> makeNGramStateSpace(StateSpace<L> stateSpace, int nGram) {
//    Tree<List<L>> transTree = buildTransitionTree(new Tree<List<L>>(new ArrayList<L>()),stateSpace, nGram);
//    System.out.println(transTree.getTerminalYield());
//    return null;
//  }

}