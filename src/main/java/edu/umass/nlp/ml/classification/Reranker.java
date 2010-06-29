package edu.umass.nlp.ml.classification;


import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import edu.umass.nlp.functional.DoubleFn;
import edu.umass.nlp.ml.Regularizers;
import edu.umass.nlp.optimize.IDifferentiableFn;
import edu.umass.nlp.optimize.IOptimizer;
import edu.umass.nlp.optimize.LBFGSMinimizer;
import edu.umass.nlp.utils.BasicPair;
import edu.umass.nlp.utils.DoubleArrays;
import edu.umass.nlp.utils.ICounter;
import edu.umass.nlp.utils.IPair;
import edu.umass.nlp.utils.IValued;
import edu.umass.nlp.utils.Indexer;
import edu.umass.nlp.utils.MapCounter;
import edu.umass.nlp.utils.SloppyMath;

public class Reranker<L>  {

	public static class Opts {		
		public LBFGSMinimizer.Opts optimizerOpts = new LBFGSMinimizer.Opts();
		public double sigmaSq = 1.0; 
	}

	private Indexer<String> featIndexer;
	private double[] weights;


	public double[] getWeights() {

		return weights;
	}


	public static interface Datum<L> {
		public List<IValued<String>> getFeatures(L label);
		public Set<L> getAllowedLabels();
	}

	public static interface LabeledDatum<L> extends Datum<L> {
		public L getTrueLabel();
	}

	private ICounter<L> getLabelProbs(Datum<L> datum) {
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

	public void train(final Iterable<LabeledDatum<L>> data, final Opts opts) {
		featIndexer = new Indexer<String>();
		for (LabeledDatum<L> datum : data) {
			for (L label : datum.getAllowedLabels()) {
				for (IValued<String> valued : datum.getFeatures(label)) {
					
					featIndexer.add(valued.getElem());
				}
			}
		}
		featIndexer.lock();
		IDifferentiableFn objFn = new IDifferentiableFn() {
			public IPair<Double, double[]> computeAt(double[] x) {
				double logObj = 0.0;
				double[] grad = new double[getDimension()];
				weights = DoubleArrays.clone(x);

				for (LabeledDatum<L> datum: data) {
					ICounter<L> labelProbs = getLabelProbs(datum);
					//System.out.println(labelProbs);
					logObj += Math.log(labelProbs.getCount(datum.getTrueLabel()));
					for (L label : datum.getAllowedLabels()) {
						for (IValued<String> valued : datum.getFeatures(label)) {
							int featIndex = featIndexer.getIndex(valued.getElem());
							double val = valued.getSecond();
							grad[featIndex] -= val * labelProbs.getCount(label);
							if (label.equals(datum.getTrueLabel()))  {
								grad[featIndex] += val * 1.0 ;
							}
						}
					}
				}

				logObj *= -1;
				DoubleArrays.scaleInPlace(grad, -1);

				//side-effect: grad updated
				IPair<Double,double[]> regRes = (Regularizers.getL2Regularizer(opts.sigmaSq)).apply(x);
				logObj += regRes.getFirst();
				DoubleArrays.addInPlace(grad, regRes.getSecond());
				return BasicPair.make(logObj, grad);
			}

			public int getDimension() {
				return featIndexer.size();
			}
		};
		IOptimizer.Result res = (new LBFGSMinimizer()).minimize(objFn, new double[objFn.getDimension()],opts.optimizerOpts); 		    
		this.weights = DoubleArrays.clone(res.minArg);
	}



}