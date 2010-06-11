package edu.umass.nlp.optimize;

import edu.umass.nlp.functional.CallbackFn;
import org.apache.log4j.Level;

/**
 * 
 *
 *
 * @author aria42
 */
public interface IOptimizer {


  public static class Opts {
    public int minIters = 10;
    public int maxIters = 25;
    public double tol = 1.0e-4;
    public Level logLevel = Level.INFO;
    public double initialStepSizeMultiplier = 0.01;
    public double stepSizeMultiplier = 0.5;
    // iterCallback at the end of each
    // iteration with a single Result argument
    public CallbackFn iterCallback = null;
  }

  public static class Result {
    public double  minObjVal;
    public double[] minGrad;
    public double[] minArg;
    public boolean didConverge = false;
  }

  Result minimize(IDifferentiableFn fn, double[] initial, Opts opts);
}