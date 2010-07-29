package edu.umass.nlp.optimize;

import edu.umass.nlp.exec.Execution;
import edu.umass.nlp.utils.BasicPair;
import edu.umass.nlp.utils.DoubleArrays;
import edu.umass.nlp.utils.IPair;
import org.apache.log4j.Logger;

public class BacktrackingLineMinimizer implements ILineMinimizer {

  public Result minimizeAlongDirection(IDifferentiableFn fn, double[] initial, double[] direction, Opts opts) {
    final Logger logger = Logger.getLogger(BacktrackingLineMinimizer.class.getSimpleName());
    logger.setLevel(opts.logLevel);
    double stepSize = opts.initialStepSize;
    final IPair<Double,double[]> initPair = fn.computeAt(initial);
    final double initVal = initPair.getFirst();
    final double[] initDeriv = initPair.getSecond();
    final double directDeriv = DoubleArrays.innerProduct(initDeriv, direction);
    logger.trace("DirectionalDeriv: " + directDeriv);
    for (int iter=0; iter < opts.maxIterations; ++iter) {
      final double[] guess = DoubleArrays.addMultiples(initial, 1.0, direction, stepSize);
      final double curVal = fn.computeAt(guess).getFirst();
      final double targetVal = initVal + opts.sufficientDecreaseConstant * directDeriv * stepSize;
      final double diff = curVal - targetVal;
      logger.trace(String.format("iter=%d stepSize=%.6f curVal=%.4f targetVal=%.4f diff=%.5f",iter,stepSize, curVal,targetVal,diff));
      if (curVal <= targetVal) {
        Result res = new Result();
        res.minimized = guess;
        res.stepSize = stepSize;
        return res;        
      }
      stepSize *=  opts.stepSizeMultiplier ;
      if (stepSize < ILineMinimizer.STEP_SIZE_TOLERANCE) {
        logger.warn("step size underflow");
        break;
      }
    }
    Result deflt = new Result();
    deflt.minimized = initial;
    deflt.stepSize = stepSize;
    return deflt;
  }

  public static void main(String[] args) {
    Execution.init(null);
    IDifferentiableFn function = new IDifferentiableFn() {
      public int getDimension() {
        return 1;
      }
            
      public IPair<Double,double[]> computeAt(double[] x) {

        double val = x[0] * (x[0] - 0.01);
        double[] grad = new double[] { 2*x[0] - 0.01 };
        return BasicPair.make(val, grad);
      }
    };

    ILineMinimizer.Opts opts = new ILineMinimizer.Opts();
    Result res = (new BacktrackingLineMinimizer()).minimizeAlongDirection(function,
      new double[] { 0 },
      new double[] { 1 },
      opts);
    System.out.println(res.stepSize);
  }
}
