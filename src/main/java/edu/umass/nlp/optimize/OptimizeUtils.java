package edu.umass.nlp.optimize;


public class OptimizeUtils {
  public static ILineMinimizer.Opts getLineMinimizerOpts(IOptimizer.Opts opts, int iter) {
    ILineMinimizer.Opts lineMineOpts = new ILineMinimizer.Opts();
    lineMineOpts.logLevel = opts.logLevel;
    lineMineOpts.stepSizeMultiplier = iter > 0 ?
      opts.stepSizeMultiplier :
      opts.initialStepSizeMultiplier;
    return lineMineOpts;
  }

  public static  ILineMinimizer.Result doLineMinimization(IDifferentiableFn fn,
                                                   double[] initial,
                                                   double[] direction,
                                                   IOptimizer.Opts opts,
                                                   int iter) {
    return (new BacktrackingLineMinimizer())
      .minimizeAlongDirection(
        fn,
        initial,
        direction,
        getLineMinimizerOpts(opts, iter));
  }

}
