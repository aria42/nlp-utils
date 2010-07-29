package edu.umass.nlp.optimize;

import org.apache.log4j.Level;


public interface ILineMinimizer {

    public static final double STEP_SIZE_TOLERANCE = 1.0e-10;

    public static class Opts {
      public double stepSizeMultiplier = 0.5;//was 0.9;
      public double sufficientDecreaseConstant = 1e-4;
      public double initialStepSize = 1.0;
      public double tol = 1.e0-6;
      public int maxIterations = Integer.MAX_VALUE;
      public Level logLevel = Level.OFF;
    }

    public static class Result {
      double stepSize;
      double[] minimized = null;
      public boolean didStepSizeUnderflow() {
        return stepSize < STEP_SIZE_TOLERANCE;
      }
    }

    public Result minimizeAlongDirection(IDifferentiableFn fn, double[] initial, double[] direction, Opts opts);
    
}