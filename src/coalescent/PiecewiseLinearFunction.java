package coalescent;

import logging.StringUtils;

/**
 * A list of x-values and y-values that describe some piecewise linear function. At any given time we assume only the
 * first changePoints of them are in use. x-values are assumed to be increasing, but yVals can be anything. 
 * @author brendano
 *
 */
public class PiecewiseLinearFunction {

	//Maximum number of change points, this is the length of the arrays
	public final int maxPoints = 50;
	
	//List of y-values beginning   
	double[] yVals;
	
	//List of x-values of function. Only first changePoints of them are valid. 
	double[] xVals;
	
	//The number of change points. Can be 0. 
	public int changePoints;
	
	public PiecewiseLinearFunction() {
		changePoints = 0;
		yVals = new double[maxPoints];
		xVals = new double[maxPoints];
		xVals[0] = 0;
	}
	
	public double[] getXVals() {
		return xVals;
	}
	
	public double[] getYVals() {
		return yVals;
	}
	
	public int getChangePointCount() {
		return changePoints;
	}
	
	/**
	 * Add a new point to this function. The point is inserted so that all x-values remain sorted. Points
	 * with x-values greater than x are shifted 'to the right', such they their index increased by one
	 * @param x
	 * @param y
	 */
	public void addPoint(double x, double y) {
		if (x<=0)
			throw new IllegalArgumentException("Cannot insert point with x <= 0");
		if (changePoints == maxPoints) {
			throw new IllegalArgumentException("Maximum number of change points reached");
		}
		
		//Find the point to insert
		int i=0;
		while(xVals[i] < x && i <= changePoints) {
			i++;
		}
		
		//Shift all points with indices greater than i one to the right
		for(int j=changePoints; j>=i; j--) {
			xVals[j+1] = xVals[j];
			yVals[j+1] = yVals[j];
		}
		
		xVals[i] = x;
		yVals[i] = y;
		
		changePoints++;
	}

	/**
	 * Adds a new point at position x, and takes y to be on the line that connects
	 * the two points on either side of the inserted point, such that this move
	 * does not actually change the shape of the function
	 * @param x
	 */
	public void addMidPoint(double x) {
		if (x<=0)
			throw new IllegalArgumentException("Cannot insert point with x <= 0");
		if (changePoints == maxPoints) {
			throw new IllegalArgumentException("Maximum number of change points reached");
		}
		
		//Find the point to insert
		int i=0;
		while(xVals[i] < x && i <= changePoints) {
			i++;
		}
		

		double newY;
		if (i==changePoints+1)
			newY = yVals[changePoints];
		else {
			double weight = (x-xVals[i-1])/(xVals[i]-xVals[i-1]);
			newY = yVals[i-1]*(1-weight) + weight*yVals[i];
		}
		
		//Shift all points with indices greater than i one to the right
		for(int j=changePoints; j>=i; j--) {
			xVals[j+1] = xVals[j];
			yVals[j+1] = yVals[j];
		}
		
		
		xVals[i] = x;
		yVals[i] = newY;
		
		changePoints++;
	}
	
	
	public void removePoint(int which) {
		if (which==0)
			throw new IllegalArgumentException("Cannot remove change point #0");
		
		if (which > changePoints)
			throw new IllegalArgumentException("Cannot remove point " + which + ", because there are only " + changePoints + " total points");
		
		for(int i=which; i<=changePoints; i++) {
			xVals[i]  = xVals[i+1];
			yVals[i] = yVals[i+1];
		}
		
		changePoints--;
	}
	
	/**
	 * Obtain a clone this object, no casting needed
	 * @return
	 */
	public PiecewiseLinearFunction getCopy() {
		PiecewiseLinearFunction copy = new PiecewiseLinearFunction();
		copy.changePoints = changePoints;
		copy.xVals = new double[maxPoints];
		copy.yVals = new double[maxPoints];
		System.arraycopy(xVals, 0, copy.xVals, 0, changePoints+1);
		System.arraycopy(yVals, 0, copy.yVals, 0, changePoints+1);
		return copy;
	}
	
	public String toString() {
		StringBuilder strB = new StringBuilder();
		strB.append("Population size function, change points: " + changePoints +"\n");
		for(int i=0; i<changePoints+1; i++) 
			strB.append( StringUtils.format(xVals[i], 4) + "\t");
		strB.append("\n");
		for(int i=0; i<changePoints+1; i++) 
			strB.append( StringUtils.format(yVals[i], 4) + "\t");		
		return strB.toString();
	}
	

}
