package edu.umass.nlp.optimize;

import edu.umass.nlp.exec.Execution;
import edu.umass.nlp.utils.BasicPair;
import edu.umass.nlp.utils.DoubleArrays;
import edu.umass.nlp.utils.IPair;
import edu.umass.nlp.utils.SloppyMath;
import org.apache.log4j.Logger;

public class GradientDescent implements IOptimizer {
  

  public Result minimize(IDifferentiableFn fn, double[] initial, Opts opts) {
    Logger logger = Logger.getLogger(GradientDescent.class.getSimpleName());
    logger.setLevel(opts.logLevel);
    double lastVal = Double.POSITIVE_INFINITY;
    double[] curGuess = initial;
    for (int iter=0; iter < opts.maxIters; ++iter) {
      final IPair<Double, double[]> fnPair = fn.computeAt(curGuess);
      final double curVal = fnPair.getFirst();
      final double[] curGrad = fnPair.getSecond();
      final double relDiff = SloppyMath.relativeDifference(curVal, lastVal);
      logger.info("curGrad: " + DoubleArrays.toString(curGrad));
      logger.info(String.format("iter: %d curVal: %.3f lastVal: %.3f relDiff: %.3f",iter,curVal,lastVal,relDiff));
      if (relDiff <= opts.tol) {
        Result res = new Result();
        res.minArg = curGuess;
        res.minObjVal = curVal;
        res.minGrad = curGrad;
        res.didConverge = true;
        return res;
      }            
      ILineMinimizer.Result lineMinResult = OptimizeUtils.doLineMinimization(fn,curGuess,DoubleArrays.scale(curGrad,-1),opts,iter);
      lastVal = curVal;
      curGuess = lineMinResult.minimized;
      logger.info("curGuess: " + DoubleArrays.toString(curGuess));
    }
    Result deflt = new Result();
    deflt.minArg = curGuess;
    return deflt;
  }

  public static void main(String[] args) {
    Execution.init(null);
    IDifferentiableFn fn = new IDifferentiableFn() {
      public IPair<Double, double[]> computeAt(double[] x) {
        return BasicPair.make( (x[0] -1.0)* (x[0]-1.0), new double[] { 2 * x[0] - 2 } );
      }

      public int getDimension() {
        return 1;
      }
    };
    IOptimizer.Result res = (new LBFGSMinimizer()).minimize(fn, new double[] { 1.0 }, new LBFGSMinimizer.Opts());
    System.out.println("res: " + DoubleArrays.toString(res.minArg));
  }
}
