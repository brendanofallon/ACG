package dlCalculation;

import java.util.Stack;

public class PartialsStore {

	   final int MAX_SIZE = 500;

	   Stack<double[][][]> stack = new Stack<double[][][]>();
	   final int arraySize;
	   final int rateCount;

	    public PartialsStore(int arraySize, int rateCount, int initialCapacity) {
	        this.arraySize = arraySize;
	        this.rateCount = rateCount;
	        for(int i=0; i<initialCapacity; i++) {
	            double[][][] arr = new double[rateCount][arraySize][4];
	            stack.push(arr);
	        }
	    }

	    /**
	     * Return a new array from the collection. This always returns a new array, even if the collection is empty
	     * @return
	     */
	    public double[][][] getArray() {
	        if (stack.isEmpty()) {
	        	//System.out.println("Partials stack empty... adding new partials list");
	            return new double[rateCount][arraySize][4];
	        }
	        else
	           return stack.pop();

	    }

	    /**
	     * Add a new array to the store of arrays
	     * @param array
	     */
	    public void add(double[][][] array) {
	        if (array[0].length != arraySize) {
	            throw new IllegalArgumentException("Array is wrong size for this partials store (was " + array.length + " but should be " + arraySize);
	        }
	        
	        if (stack.size() < MAX_SIZE) //Only push onto stack if stack size is not too huge
	        	stack.push(array);
	    }
	    
}
