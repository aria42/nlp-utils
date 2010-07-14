package edu.umass.nlp.ml.classification;


import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import edu.umass.nlp.functional.DoubleFn;
import edu.umass.nlp.functional.Fn;
import edu.umass.nlp.functional.Functional;
import edu.umass.nlp.ml.Regularizers;
import edu.umass.nlp.optimize.CachingDifferentiableFn;
import edu.umass.nlp.optimize.IDifferentiableFn;
import edu.umass.nlp.optimize.IOptimizer;
import edu.umass.nlp.optimize.LBFGSMinimizer;
import edu.umass.nlp.parallel.ParallelUtils;
import edu.umass.nlp.utils.*;
import org.apache.commons.collections.primitives.ArrayDoubleList;
import org.apache.commons.collections.primitives.ArrayIntList;
import org.apache.commons.collections.primitives.DoubleList;
import org.apache.commons.collections.primitives.IntList;
import org.apache.log4j.Logger;

public class Reranker<L>  {

	public static class Opts {		
		public LBFGSMinimizer.Opts optimizerOpts = new LBFGSMinimizer.Opts();
		public double sigmaSq = 1.0; 
	}

	private Indexer<String> featIndexer;
	private double[] weights;
  private transient Logger logger = Logger.getLogger("Reranker");

	public double[] getWeights() {

		return weights;
	}

	public static interface Datum<L> {
		public List<IValued<String>> getFeatures(L label);
		public Set<L> getAllowedLabels();
	}

  private static class InternalDatum {
    IntList[] featIndices;
    DoubleList[] featValues;
    int trueLabel;

    private InternalDatum(IntList[] featIndices, DoubleList[] featValues, int trueLabel) {
      this.featIndices = featIndices;
      this.featValues = featValues;
      this.trueLabel = trueLabel;
    }
  }

  private InternalDatum toInternalDatum(LabeledDatum<L> datum) {
    List<L> labels = new ArrayList<L>(datum.getAllowedLabels());
    IntList[] featIndices = new IntList[labels.size()];
    DoubleList[] featVals = new DoubleList[labels.size()];
    int trueLabelIndex = labels.indexOf(datum.getTrueLabel());
    for (int i = 0; i < labels.size(); i++) {
      L label = labels.get(i);
      List<IValued<String>> feats = datum.getFeatures(label);
      featIndices[i] = new ArrayIntList();
      featVals[i] = new ArrayDoubleList();
      for (int f=0; f < feats.size(); ++f) {
        int featIndex = featIndexer.indexOf(feats.get(f).getFirst());
        if (featIndex >= 0) {
          featIndices[i].add(featIndex);
          featVals[i].add(feats.get(f).getSecond());
        }
      }
    }
    return new InternalDatum(featIndices, featVals, trueLabelIndex);
  }

	public static interface LabeledDatum<L> extends Datum<L> {
		public L getTrueLabel();
	}

	public ICounter<L> getLabelProbs(Datum<L> datum) {
		List<L> labels = new ArrayList<L>(datum.getAllowedLabels());
		double[] logProbs = new double[labels.size()];		
		for (int i=0; i < logProbs.length; ++i) {		
			L label = labels.get(i);
			double logProb = 0.0;
			for (IValued<String> valued : datum.getFeatures(label)) {
				logProb += valued.getValue() * weights[featIndexer.getIndex(valued.getElem())];
			}
			logProbs[i] = logProb;
		}
		final double logSum = SloppyMath.logAdd(logProbs);
		ICounter<L> res = new MapCounter<L>();
		for (int i=0; i < labels.size(); ++i) {
			L label = labels.get(i);
			res.setCount(label, Math.exp(logProbs[i]-logSum));
		}
		return res;
	}

  private double[] getLabelProbs(InternalDatum datum) {
		double[] logProbs = new double[datum.featIndices.length];
		for (int i=0; i < logProbs.length; ++i) {
			double logProb = 0.0;
			for (int f=0; f < datum.featIndices[i].size(); ++f) {
				logProb += datum.featValues[i].get(f) * weights[datum.featIndices[i].get(f)];
			}
			logProbs[i] = logProb;
		}
		final double logSum = SloppyMath.logAdd(logProbs);
		double[] res = new double[logProbs.length];
		for (int i=0; i < logProbs.length; ++i) {
      res[i] = Math.exp(logProbs[i]-logSum);
		}
		return res;
	}

  private class ObjFn implements IDifferentiableFn {
    Iterable<InternalDatum> data ;
    Opts opts;
    ObjFn(Iterable<InternalDatum> data,Opts opts) {
      this.data = data;
      this.opts = opts;
    }

    private IPair<Double,double[]> computeInternal(Iterable<InternalDatum> datums) {
      double obj = 0.0;
      double[] grad = new double[getDimension()];
      for (InternalDatum datum: data) {
        double[] labelProbs = getLabelProbs(datum);
        //System.out.println(labelProbs);
        obj += Math.log(labelProbs[datum.trueLabel]);
        for (int l=0; l < datum.featIndices.length ;++l) {
          for (int f=0; f < datum.featIndices[l].size(); ++f) {
            int featIndex = datum.featIndices[l].get(f);
            double val = datum.featValues[l].get(f);
            grad[featIndex] -= val * labelProbs[l];
            if (l == datum.trueLabel) {
              grad[featIndex] += val * 1.0;
            }
          }
        }
      }
      return BasicPair.make(obj, grad);
    }

    class Worker implements Runnable {
      Iterable<InternalDatum> data;
      IPair<Double,double[]> res ;

      Worker(Iterable<InternalDatum> data) {
        this.data = data;
      }

      public void run() {
        res = computeInternal(data);
      }
    }

    public IPair<Double, double[]> computeAt(double[] x) {
      double logObj = 0.0;
      double[] grad = new double[getDimension()];
      weights = DoubleArrays.clone(x);

      if (data instanceof List) {
        List<Iterable<InternalDatum>> parts =
          Collections.partition((List) data, Runtime.getRuntime().availableProcessors());
        List<Worker> workers = Functional.map(parts, new Fn<Iterable<InternalDatum>, Worker>() {
          public Worker apply(Iterable<InternalDatum> input) {
            return new Worker(input);
          }});
        ParallelUtils.doParallelWork(workers, workers.size());
        for (Worker worker : workers) {
          logObj += worker.res.getFirst();
          DoubleArrays.addInPlace(grad, worker.res.getSecond());
        }
      } else {
        Worker singleton = new Worker(data);
        singleton.run();
        logObj += singleton.res.getFirst();
        grad = singleton.res.getSecond();
      }

      logObj *= -1;
      DoubleArrays.scaleInPlace(grad, -1);

      // Regularizer
      IPair<Double,double[]> regRes = (Regularizers.getL2Regularizer(opts.sigmaSq)).apply(x);
      logObj += regRes.getFirst();

      DoubleArrays.addInPlace(grad, regRes.getSecond());
      return BasicPair.make(logObj, grad);
    }

    public int getDimension() {
      return featIndexer.size();
    }
  }
  
	public void train(final Iterable<LabeledDatum<L>> data, final Opts opts) {
		featIndexer = new Indexer<String>();
    logger.trace("Start Indexing Features");
		for (LabeledDatum<L> datum : data) {
			for (L label : datum.getAllowedLabels()) {
				for (IValued<String> valued : datum.getFeatures(label)) {					
					featIndexer.add(valued.getElem());
				}
			}
		}
		featIndexer.lock();
    logger.trace("Done Indexing Features");
    logger.info("Number of Features: " + featIndexer.size());
    logger.trace("Start Caching Data to Internal Representation");
    final Iterable<InternalDatum> internalData = Functional.map(data, new Fn<LabeledDatum<L>, InternalDatum>() {
      public InternalDatum apply(LabeledDatum<L> input) {
        return toInternalDatum(input);
      }});        
		logger.trace("Done Caching Data");
    IDifferentiableFn objFn = new CachingDifferentiableFn(new ObjFn(internalData,opts));
    logger.trace("Starting Optimization");
		IOptimizer.Result res = (new LBFGSMinimizer()).minimize(objFn, new double[objFn.getDimension()],opts.optimizerOpts); 		    
		this.weights = DoubleArrays.clone(res.minArg);
    logger.trace("Done with optimization");
	}

}