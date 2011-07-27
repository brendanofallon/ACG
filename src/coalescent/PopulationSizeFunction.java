package coalescent;

import logging.StringUtils;

/**
 * A class that represents a piecewise function that describes population size. 
 * @author brendano
 *
 */
public class PopulationSizeFunction {
	
	//Maximum number of change points, this is the length of the arrays
	public final int maxPoints = 64;
	
	//List of sizes beginning at first change point, whose time is > 0  
	double[] sizes;
	
	//List of times at which change points occur
	double[] times;
	
	//The number of change points. Can be 0. 
	int changePoints;
	
	public PopulationSizeFunction() {
		changePoints = 0;
		sizes = new double[maxPoints];
		times = new double[maxPoints];
		sizes[0] = 1.0;
		times[0] = 0;
	}
	
	public String toString() {
		StringBuilder strB = new StringBuilder();
		strB.append("Population size function, change points: " + changePoints +"\n");
		for(int i=0; i<changePoints+1; i++) 
			strB.append( StringUtils.format(times[i], 4) + "\t");
		strB.append("\n");
		for(int i=0; i<changePoints+1; i++) 
			strB.append( StringUtils.format(sizes[i], 4) + "\t");		
		return strB.toString();
	}
}
