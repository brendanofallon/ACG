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

import java.util.HashMap;
import java.util.Map;

import math.RandomSource;
import parameter.InvalidParameterValueException;
import arg.ARG;
import coalescent.PiecewiseLinearFunction;
import coalescent.PiecewiseLinearPopSize;

public class LinearFunctionAddRemove extends AbstractModifier<PiecewiseLinearPopSize> {

	ARG arg;
	
	boolean verbose = false;
	
	double jacobian = 5.5;
	
	public LinearFunctionAddRemove() {
		this(new HashMap<String, String>(), null);
	}
	
	public LinearFunctionAddRemove(ARG arg) {
		this(new HashMap<String, String>(), arg);
	}
	
	public LinearFunctionAddRemove(Map<String, String> attributes, ARG arg) {
		super(attributes);
		this.arg = arg;
	}

	@Override
	public Double modify() throws InvalidParameterValueException,
			IllegalModificationException, ModificationImpossibleException {
		
		PiecewiseLinearFunction newValue = param.getValue().getCopy();
		double hr = 1.0;
		
		if (RandomSource.getNextUniform() < 0.5) {
			hr = addPoint(newValue);
		}
		else {
			if (newValue.changePoints == 0)
				return 1.0;
			
			hr = removePoint(newValue);
		}
		

		param.proposeValue(newValue);
		return hr;
	}

	private double removePoint(PiecewiseLinearFunction func) throws ModificationImpossibleException {
		int which = RandomSource.getNextIntFromTo(1, func.getChangePointCount());
		func.removePoint(which);
		
		double probThisMove = 1.0/(func.getChangePointCount()+1);
		double probReverseMove = 1.0/arg.getMaxHeight();
		return jacobian*probReverseMove / probThisMove;
	}

	private double addPoint(PiecewiseLinearFunction func) {
		double newX = RandomSource.getNextUniform() * arg.getMaxHeight();
		func.addMidPoint(newX);
		
		double probThisMove = 1.0/arg.getMaxHeight();
		double probReverseMove = 1.0/func.getChangePointCount(); 
		return jacobian*probReverseMove / probThisMove;
	}


	
}
