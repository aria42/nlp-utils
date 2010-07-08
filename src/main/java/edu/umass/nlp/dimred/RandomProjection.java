package edu.umass.nlp.dimred;

import edu.umass.nlp.utils.DoubleArrays;

import java.util.Random;


/**
 * To do dimension reduction put your sparse matrix
 * in a SmallSparseVector[], call setInput and then
 * getProjectedMatrix where each row corresponds
 * to your reduced vectors.  
 */
public class RandomProjection {

	private double[][] projected ;

	public void setInput(SmallSparseVector[] sparseX, int kPrincipalComponents) {
		int maxDimension = 0;
		for (SmallSparseVector vec: sparseX) {
			for (int i=0; i < vec.size(); ++i) {
				int dim = vec.getActiveDimension(i);
				maxDimension = Math.max(maxDimension,dim);
			}
		}
		double[][] randomMatrix = getRandomMatrix(maxDimension+1, kPrincipalComponents);
		projected = new double[sparseX.length][kPrincipalComponents];
		for (int i=0; i < sparseX.length; ++i) {
			SmallSparseVector vec = sparseX[i];
			for (int j=0; j < vec.size(); ++j) {
				int dim = vec.getActiveDimension(j);
				double count = vec.getCount(dim);
				if (count == 0.0) continue;
				for (int k=0; k < kPrincipalComponents; ++k) {
					double v = count * randomMatrix[dim][k];
					if (v != 0.0) projected[i][k] += v;
				}
			}
			double vecLen = DoubleArrays.vectorLength(projected[i]);
			if (vecLen > 0.0) DoubleArrays.scaleInPlace(projected[i], 1.0/vecLen);
		}
	}

	private double[][] getRandomMatrix(int biggerDim, int smallerDim) {
		double[][] randomMatrix = new double[biggerDim][smallerDim];
		Random rand = new Random(0);
		double[] probs = {1.0/6.0,2.0/3.0,1.0/6.0};
		double[] vals = {Math.sqrt(3) * 1, 0, Math.sqrt(3) * -1};
		for (int i=0; i < biggerDim; ++i) {
			for (int j=0; j < smallerDim; ++j) {
				int rIndex = DoubleArrays.sample(probs,rand);
				randomMatrix[i][j] = vals[rIndex];
			}
		}
		return randomMatrix;
	}

	public double[][] getProjectedMatrix() {
		return projected;
	}

}
