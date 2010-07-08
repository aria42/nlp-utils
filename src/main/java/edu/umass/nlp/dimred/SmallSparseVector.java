package edu.umass.nlp.dimred;

import java.io.Serializable;
import java.util.Arrays;


public class SmallSparseVector implements Serializable {

	private static final long serialVersionUID = 42L;

	float[] data = new float[0];
	int[] indices = new int[0];
	int length = 0;

	private void grow() {
		int curSize = data.length;
		int newSize = curSize + 10;

		float[] newData = new float[newSize];
		System.arraycopy(data, 0, newData, 0, curSize);
		data = newData;
		int[] newIndices = new int[newSize];
		System.arraycopy(indices, 0, newIndices, 0, curSize);
		for (int i=curSize; i < newIndices.length; ++i) {
			newIndices[i] = Integer.MAX_VALUE;
			newData[i] = Float.POSITIVE_INFINITY;
		}
		indices = newIndices;
	}

	public double getCount(int index) {
		int res = Arrays.binarySearch(indices, index);
		if (res >= 0 && res < length) {
			return data[res];
		}
		return 0.0;
	}

	public void incrementCount(int index0, double x0) {
		double curCount = getCount(index0);
		setCount(index0, curCount + x0);
	}

	public int size() {
		return length;
	}

	public void setCount(int index0, double x0) {
		float x = (float) x0;
//		short index = (short) index0;
		int res = Arrays.binarySearch(indices, index0);
		// Greater than everything
		if (res >= 0 && res < length) {
			data[res] = x;
			return;
		}
		if (length+1 >= data.length) {
			grow();
		}
		// In the middle
		int insertionPoint = -(res+1);
		assert insertionPoint >= 0 && insertionPoint <= length : String.format("length: %d insertion: %d",length,insertionPoint);
		// Shift The Stuff After
		System.arraycopy(data, insertionPoint, data, insertionPoint+1, length-insertionPoint);
		System.arraycopy(indices, insertionPoint, indices, insertionPoint+1, length-insertionPoint);
		indices[insertionPoint] = index0;
		data[insertionPoint] = x;
		length++;
	}

	public int getActiveDimension(int i) {
		assert i < indices.length;
		return indices[i];
	}

  public double getActiveDimensionCount(int i) {
		assert i < data.length;
		return data[i];
	}

	public double l2Norm() {
		double sum = 0.0;
		for (int i=0; i < length; ++i) {
			sum += data[i] * data[i];
		}
		return Math.sqrt(sum);
	}

	public void scale(double c) {
		for (int i=0; i < length; ++i) {
			data[i] *= c;
		}
	}

	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("{ ");
		for (int i=0; i < length; ++i) {
			builder.append(String.format("%d : %.5f",indices[i],data[i]));
			builder.append(" ");
		}
		builder.append(" }");
		return builder.toString();
	}

	public double dotProduct(SmallSparseVector other) {
		double sum = 0.0;
		for (int i=0; i < this.size(); ++i) {
			int dim = getActiveDimension(i);
			sum += this.getCount(dim) * other.getCount(dim);
		}
		return sum;
	}

	public static void main(String[] args) {
		SmallSparseVector sv = new SmallSparseVector();
		sv.setCount(0, 1.0);
		sv.setCount(1, 2.0);
		sv.incrementCount(1, 1.0);
		sv.incrementCount(-1, 10.0);
		System.out.println(sv);
	}


}
