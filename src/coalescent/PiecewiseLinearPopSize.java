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

import java.util.HashMap;
import java.util.Map;

import arg.ARG;

import math.RandomSource;
import modifier.IllegalModificationException;
import modifier.LinearFunctionMover;
import modifier.ModificationImpossibleException;
import modifier.Modifier;
import parameter.AbstractParameter;
import parameter.InvalidParameterValueException;

/**
 * A Parameter that uses a piecewise linear function to model population size
 * @author brendano
 *
 */
public class PiecewiseLinearPopSize extends AbstractParameter<PiecewiseLinearFunction> implements DemographicParameter {

	
	public PiecewiseLinearPopSize(Map<String, String> attrs) {
		this(attrs, null);
	}
	
	public PiecewiseLinearPopSize(ARG arg) {
		this(new HashMap<String, String>(), arg);
	}
	
	public PiecewiseLinearPopSize(Map<String, String> attrs, ARG arg) {
		super(attrs);
		currentValue = new PiecewiseLinearFunction();
		proposedValue = new PiecewiseLinearFunction();
		
		currentValue.changePoints = 5;
		double maxHeight = 1.0;
		
		if (arg != null) {
			maxHeight = arg.getMaxHeight();
			System.out.println("Setting max height to : " + maxHeight);
		}
		
		double size = maxHeight/2.0;
		
		for(int i=0; i<=currentValue.changePoints; i++) {
			currentValue.yVals[i] = size;
			currentValue.xVals[i] = maxHeight*(double)i/(double)(currentValue.changePoints);
			System.out.println(i + " : " + currentValue.xVals[i] + ", " + currentValue.yVals[i]);
		}
		
		activeValue = currentValue;
	}
	
	/**
	 * Return a String representation of this parameter suitable for logging
	 * @return
	 */
	public String getLogString() {
		System.out.println(getValue());
		return "" + activeValue.changePoints;
	}

	/**
	 * This string that is displayed on at the top of the log for this parameter
	 * @return
	 */
	public String getLogHeader() {
		return "pop.size.changepoints";
	}
	
	public PiecewiseLinearFunction getFunction() {
		return activeValue;
	}
	
	@Override
	public double getIntegral(double t0, double t1) {
		PiecewiseLinearFunction func = activeValue;
		double lastTime = func.xVals[func.changePoints];
		
		if (t0 > lastTime) {
			double lastSize = func.yVals[func.changePoints];
			return (t1-t0)/lastSize;
		}
		
		
		return integral0ToX(t1) - integral0ToX(t0);
	}
	
	/**
	 * Return the area under 1/N(t) from time 0 to time x
	 * @param x
	 * @return
	 */
	private double integral0ToX(double x) {
		PiecewiseLinearFunction func = activeValue;
		int interval = findInterval(func, x);
		
		double intervalStart = func.xVals[interval];
		
		double sum = 0;
		int i = 0;
		while(i < interval) {
			sum += intervalArea(i);
			i++;
		}
		
		
		if (interval == func.changePoints) {
			sum += (x-intervalStart)/func.yVals[func.changePoints];
		}
		else {
			double x0 = func.xVals[interval];
			double y0 = func.yVals[interval];
			double x1 = x;
			double y1 = getPopSize(x); 
			
			sum += inverseIntegral(x0, y0, x1, y1);
		}
		 
		return sum;
	}
	
	/**
	 * Returns the area (integral) of 1/N(t) within the given interval, where 0 is the first interval
	 * @param interval
	 * @return
	 */
	private double intervalArea(int interval) {
		PiecewiseLinearFunction func = activeValue;
		double x0 = func.xVals[interval];
		double y0 = func.yVals[interval];
		
		double x1 = func.xVals[interval+1];
		double y1 = func.yVals[interval+1];
		
		return inverseIntegral(x0, y0, x1, y1);
	}
	
	
	private static double inverseIntegral(double x0, double y0, double x1, double y1) {
		if (y1==y0) {
			return (x1-x0)/y1;
		} else {
			double a = (y1-y0)/(x1-x0);
			double b = y1-a*x1; 

			return (Math.log(a*x1+b) - Math.log(a*x0+b))/a;
		}
	}
	
	@Override
	public double getPopSize(double t) {
		PiecewiseLinearFunction popSize = activeValue;
		double lastTime = popSize.xVals[popSize.changePoints];
		if (t >= lastTime)
			return popSize.yVals[popSize.changePoints];
		
		int interval = findInterval(popSize, t);
		
		
		return interp(popSize.xVals[interval], popSize.yVals[interval], popSize.xVals[interval+1], popSize.yVals[interval+1], t);
	}
	
	/**
	 * The number of times the pop size function changes in value
	 * @return
	 */
	public int getChangePointCount() {
		return activeValue.changePoints;
	}



	@Override
	public String getName() {
		return "PiecewiseLinearPopSize";
	}
	
	/**
	 * Returns the index of the first interval such that 
	 * @param func
	 * @param t
	 * @return
	 */
	private int findInterval(PiecewiseLinearFunction func, double t) {
		if (func.changePoints == 0 || t==0)
			return 0;
		
		int interval = 0;
		while(interval < func.changePoints && func.xVals[interval] < t)
			interval++;
		interval--; //Above procedure advances one past where we want to be...
		return interval;
	}
	
	/**
	 * Given points (x1, y1) and (x2, y2), returns the result of a linear interpolation at point xp
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @param xp
	 * @return
	 */
	private static double interp(double x0, double y0, double x1, double y1, double xp) {
		return y0 + ((xp-x0)*y1 - (xp-x0)*y0)/(x1-x0);
	}
	
	public static void main(String[] args) {
		RandomSource.initialize(-1303301468);
		
		PiecewiseLinearPopSize size = new PiecewiseLinearPopSize();
		
		Modifier<PiecewiseLinearPopSize> mod = new LinearFunctionMover();
		size.addModifier(mod);
		
		for(int j=0; j<100; j++) {
			
			try {
				System.out.println("\n\nStep #" + j);
				for(int i=0; i<30; i++) {
					double t = (double)i / 5.0;
					System.out.println(t + "\t" + size.getPopSize(t));
				}
				
				mod.modify();
				size.acceptValue();
				
			} catch (InvalidParameterValueException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalModificationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ModificationImpossibleException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		System.out.println("Integrals...");
		
		for(int i=0; i<30; i++) {
			double t = (double)i / 5.0;
			System.out.println(t + "\t" + size.getIntegral(0, t));
		}
		
	}

}
