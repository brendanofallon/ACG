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


package modifier;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import arg.ARG;

import math.RandomSource;

import parameter.InvalidParameterValueException;

import coalescent.PiecewiseLinearFunction;
import coalescent.PiecewiseLinearPopSize;

public class LinearFunctionMover extends AbstractModifier<PiecewiseLinearPopSize> {

	double sizeWindow = 50;
	ARG arg;
	
	final boolean verbose = false;

	public LinearFunctionMover() {
		this(new HashMap<String, String>(), null);
	}
	
	public LinearFunctionMover(ARG arg) {
		this(new HashMap<String, String>(), arg);
	}
	
	public LinearFunctionMover(Map<String, String> attributes, ARG arg) {
		super(attributes);
		this.arg = arg;
		// TODO Auto-generated constructor stub
	}

	@Override
	public Double modify() throws InvalidParameterValueException,
			IllegalModificationException, ModificationImpossibleException {
		
		

		PiecewiseLinearFunction newValue = param.getValue().getCopy();
		
		//pick a random point to move
		int whichPoint = RandomSource.getNextIntFromTo(0, param.getChangePointCount());
				
	//	double hr = modifySize(newValue, whichPoint);
		
		
		//We can't modify the timing of the first point (it's always zero), so here just
		//modify the size
		double hr;
		if (whichPoint == 0) {
			hr = modifySize(newValue, whichPoint);
		}
		else {
			
			if (RandomSource.getNextUniform() < 0.1) {
				hr = modifyTime(newValue, whichPoint);
			}
			else {
				hr = modifySize(newValue, whichPoint);
			}
		}
		

//		if (newValue.getYVals()[whichPoint] < 1.0) {
//			System.out.println("Hmm, proposing very small value for size");
//			try {
//				System.in.read();
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
		param.proposeValue(newValue);
		return hr / (double)newValue.getChangePointCount();
	}

	/**
	 * Change the time associated with one of the change points
	 * @param func
	 * @param whichPoint
	 * @return
	 */
	private double modifyTime(PiecewiseLinearFunction func, int whichPoint) {
		double hr;
		double min = func.getXVals()[whichPoint-1];
		double[] xVals = func.getXVals();
		

		if (verbose)
			System.out.print("Modifying time of point " + whichPoint + " from " + xVals[whichPoint] );
		
		//System.out.print("Modifying time from " + xVals[whichPoint]);
		
//		if (whichPoint == func.getChangePointCount()) {
//			Double multiplier = Math.exp( sizeWindow * (RandomSource.getNextUniform()-0.5));
//			xVals[whichPoint] = min + (xVals[whichPoint]-min)*multiplier;
//			hr = multiplier;
//		}
//		else {
			double max;
			if (whichPoint == func.getChangePointCount())
				max = arg.getMaxHeight();
			else
				max = func.getXVals()[whichPoint+1];
			double width = max-min;
			double newTime = min + RandomSource.getNextUniform()*width;
			xVals[whichPoint] = newTime;
			hr = 1.0;
		//}
		
		if (verbose)
			System.out.println(" to : " + xVals[whichPoint]);
		return hr;
	}

	/**
	 * Change the size associated with one of the change points
	 * @param func
	 * @param whichPoint
	 * @return
	 * @throws ModificationImpossibleException
	 */
	private double modifySize(PiecewiseLinearFunction func, int whichPoint) throws ModificationImpossibleException {
		
		double dif = sizeWindow * (RandomSource.getNextUniform()-0.5);
		double[] yVals = func.getYVals();

		if (verbose)
			System.out.print("Modifying size of point " + whichPoint + " from " + yVals[whichPoint] );
		
		yVals[whichPoint] += dif;
		if (yVals[whichPoint] < 0) 
			yVals[whichPoint] *= -1;
		
		if (verbose)
			System.out.println(" to " + yVals[whichPoint]);
		
		return 1.0;
	}




}
