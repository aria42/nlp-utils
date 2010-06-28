package edu.umass.nlp.ml.sequence;

import edu.umass.nlp.ml.F1Stats;
import edu.umass.nlp.utils.Collections;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TokenF1Eval {

  public static void updateEval(Map<String, F1Stats> stats,
                                List<String> trueLabels,
                                List<String> guessLabels)                                
  {

    for (int i = 0; i < trueLabels.size(); i++) {
      String trueLabel = trueLabels.get(i);
      String guessLabel = guessLabels.get(i);
      
      if (trueLabel.equals(guessLabel)) {
        Collections.getMut(stats,trueLabel, new F1Stats(trueLabel)).tp++;
      } else {
        Collections.getMut(stats,trueLabel, new F1Stats(trueLabel)).fn++;
        Collections.getMut(stats,guessLabel, new F1Stats(guessLabel)).fp++;
      }
    }
  }

  public static F1Stats getAvgStats(Map<String, F1Stats> stats) {
    return getAvgStats(stats,Collections.set(StateSpace.startLabel, StateSpace.stopLabel, "NONE")) ;
  }

  public static F1Stats getAvgStats(Map<String, F1Stats> stats,Set<String> toIgnore) {
    F1Stats avgStats = new F1Stats("AVG");
    for (Map.Entry<String, F1Stats> entry : stats.entrySet()) {
      String label = entry.getKey();
      if (!toIgnore.contains(label)) {
        avgStats.merge(entry.getValue());
      }
    }
    return avgStats;
  }

}
