/********************************************************************
*
* 	Copyright 2011 Brendan O'Fallon
*
*   Licensed under the Apache License, Version 2.0 (the "License");
*   you may not use this file except in compliance with the License.
*   You may obtain a copy of the License at
*
*       http://www.apache.org/licenses/LICENSE-2.0
*
*   Unless required by applicable law or agreed to in writing, software
*   distributed under the License is distributed on an "AS IS" BASIS,
*   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*   See the License for the specific language governing permissions and
*   limitations under the License.
*
***********************************************************************/


package coalescent;

import logging.StringUtils;

/**
 * A class that represents a piecewise function that describes population size. 
 * @author brendano
 *
 */
public class PopulationSizeFunction {
	
	//Maximum number of change points, this is the length of the arrays
	public final int maxPoints = 32;
	
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
