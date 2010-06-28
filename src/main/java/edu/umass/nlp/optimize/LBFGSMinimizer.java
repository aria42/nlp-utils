package edu.umass.nlp.optimize;

import edu.umass.nlp.utils.DoubleArrays;
import edu.umass.nlp.utils.IPair;
import edu.umass.nlp.utils.SloppyMath;
import org.apache.log4j.Logger;

import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Dan Klein
 * @author aria42   (significant mods)
 */
public class LBFGSMinimizer implements IOptimizer {

  /**
   * LBFGS specific options, when you call minimize()
   * you should pass in an instance of this class
   * (or you get the defaults)
   */
  public static class Opts extends IOptimizer.Opts {
    public int maxHistorySize = 6;
    public int maxHistoryResets = 0;
    public String checkpointPath = null;
  }

  private List<double[]> inputDiffs = new LinkedList<double[]>();
  private List<double[]> derivDiffs = new LinkedList<double[]>();
  private Logger logger = Logger.getLogger("LBFGSMinimizer");
  private Opts opts;

  private double[] getInitialInverseHessianDiagonal(IDifferentiableFn fn) {
    double scale = 1.0;
    if (!derivDiffs.isEmpty()) {
      double[] lastDerivativeDifference = derivDiffs.get(0);
      double[] lastInputDifference = inputDiffs.get(0);
      double num = DoubleArrays.innerProduct(lastDerivativeDifference,
        lastInputDifference);
      double den = DoubleArrays.innerProduct(lastDerivativeDifference,
        lastDerivativeDifference);
      scale = num / den;
    }
    return DoubleArrays.constantArray(scale, fn.getDimension());
  }

  private int getHistorySize() {
    assert inputDiffs.size() == derivDiffs.size();
    return inputDiffs.size();
  }

  public double[] getSearchDirection(IDifferentiableFn fn, double[] derivative) {
    double[] initialInverseHessianDiagonal = getInitialInverseHessianDiagonal(fn);
    double[] direction = implicitMultiply(initialInverseHessianDiagonal, derivative);
    return direction;
  }


  private double[] implicitMultiply(double[] initialInverseHessianDiagonal,
                                    double[] derivative) {
    double[] rho = new double[getHistorySize()];
    double[] alpha = new double[getHistorySize()];
    double[] right = DoubleArrays.clone(derivative);
    // loop last backward
    for (int i = getHistorySize() - 1; i >= 0; i--) {
      double[] inputDifference = inputDiffs.get(i);
      double[] derivativeDifference = derivDiffs.get(i);
      rho[i] = DoubleArrays.innerProduct(inputDifference, derivativeDifference);
      if (rho[i] == 0.0) {
        logger.fatal("LBFGSMinimizer.implicitMultiply: Curvature problem.");
      }
      alpha[i] = DoubleArrays.innerProduct(inputDifference, right) / rho[i];
      right = DoubleArrays.addMultiples(right, 1.0, derivativeDifference, -1.0 * alpha[i]);
    }
    double[] left = DoubleArrays.pointwiseMultiply(initialInverseHessianDiagonal, right);
    for (int i = 0; i < getHistorySize(); i++) {
      double[] inputDifference = inputDiffs.get(i);
      double[] derivativeDifference = derivDiffs.get(i);
      double beta = DoubleArrays.innerProduct(derivativeDifference, left) / rho[i];
      left = DoubleArrays.addMultiples(left, 1.0, inputDifference, alpha[i] - beta);
    }
    return left;
  }

  private void clearHistories() {
    inputDiffs.clear();
    derivDiffs.clear();
  }

  protected void updateHistories(double[] cur, double[] next, List<double[]> diffs) {
    double[] diff = DoubleArrays.addMultiples(next, 1.0, cur, -1.0);
    diffs.add(0, diff);
    if (diffs.size() > opts.maxHistorySize) {
      diffs.remove(diffs.size() - 1);
    }
  }

  private Result getResult(IDifferentiableFn fn, double[] x) {
    Result res = new Result();
    res.minArg = DoubleArrays.clone(x);
    IPair<Double, double[]> fnEval = fn.computeAt(res.minArg);
    res.minObjVal = fnEval.getFirst();
    res.minGrad = fnEval.getSecond();
    return res;
  }

  public Result minimize(IDifferentiableFn fn, double[] initial, IOptimizer.Opts opts) {
    int numResets = 0;
    if (opts == null) opts = new Opts();
    clearHistories();
    logger.setLevel(opts.logLevel);
    if (!(opts instanceof Opts)) {
      logger.warn("opts are not LBFGS specific, reverting to all-default opts");
      opts = new Opts();
    }
    this.opts = (Opts) opts;

    Result cur = getResult(fn, initial);
    for (int iter = 0; iter < opts.maxIters; ++iter) {
      double[] dir = getSearchDirection(fn, cur.minGrad);
      DoubleArrays.scaleInPlace(dir, -1);
      ILineMinimizer.Result lineMinRes =
        OptimizeUtils.doLineMinimization(fn, cur.minArg, dir, opts, iter);
      Result next = getResult(fn,lineMinRes.minimized);
      final double relDiff = SloppyMath.relativeDifference(cur.minObjVal, next.minObjVal);
      //logger.info("relDiff: " + relDiff);
      if (iter > opts.minIters && relDiff < opts.tol) {
        if (numResets < ((Opts) opts).maxHistoryResets) {
          logger.info("Dumping Cache");
          iter--;
          numResets++;
          clearHistories();
          continue;
        }
        logger.info(String.format("Finished: value: %.5f",cur.minObjVal));
        return cur;
      }
      // Updates
      updateHistories(cur.minArg,next.minArg, inputDiffs);
      updateHistories(cur.minGrad,next.minGrad, derivDiffs);
      logger.info(String.format("End of iter %d: value: %.5f relDiff: %.3f",
        iter+1,
        cur.minObjVal,
        SloppyMath.relativeDifference(cur.minObjVal, next.minObjVal)));
      cur = next;

    }
    return cur;
  }
}
