package dlCalculation;

import java.util.Stack;

public class DoubleStore {

	final int MAX_SIZE = 500;

	Stack<double[][]> stack = new Stack<double[][]>();
	final int arraySize;

	public DoubleStore(int arraySize, int initialCapacity) {
		this.arraySize = arraySize;

		for(int i=0; i<initialCapacity; i++) {
			double[][] arr = new double[arraySize][4];
			stack.push(arr);
		}
	}

	/**
	 * Return a new array from the collection. This always returns a new array, even if the collection is empty
	 * @return
	 */
	public double[][] getArray() {
		if (stack.isEmpty()) {
			//System.out.println("Partials stack empty... adding new partials list");
			return new double[arraySize][4];
		}
		else
			return stack.pop();

	}

	/**
	 * Add a new array to the store of arrays
	 * @param array
	 */
	public void add(double[][] array) {
		if (array.length != arraySize) {
			throw new IllegalArgumentException("Array is wrong size for this integer store (was " + array.length + " but should be " + arraySize);
		}

		if (stack.size() < MAX_SIZE)
			stack.push(array);

	}

}
