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

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import math.RandomSource;
import parameter.DoubleParameter;
import parameter.InvalidParameterValueException;

public class ScaleModifier extends AbstractModifier<DoubleParameter> {
	
	DoubleParameter param = null;
	
	double scaleMin = 0.02;
	double scaleMax = 0.5;
	double scaleSize = 0.1;

	public ScaleModifier() {
		super(new HashMap<String, String>());
	}
	
	public ScaleModifier(Map<String, String> attributes) {
		super(attributes);
		
		modStr="Scale modifier for pop size";
	}
	
	public Double modify() throws InvalidParameterValueException, IllegalModificationException, ModificationImpossibleException {
		if (! param.isProposeable()) {
			throw new IllegalModificationException("Parameter " + param.getName() + " is not in a valid state, cannot modify");	 
		}
		
		
		double r = RandomSource.getNextUniform();
		Double val = param.getValue();
	
		
		Double multiplier = Math.exp( scaleSize * (r-0.5));
				
		Double newVal = val * multiplier; 
		
		//Reflecting boundaries?
		if (newVal < param.getLowerBound()) {
			double dif = param.getLowerBound() - newVal;
			newVal = param.getLowerBound()+dif;
		}
		
		if (newVal > param.getUpperBound()) {
			double dif = newVal - param.getUpperBound();
			newVal = param.getUpperBound()-dif;
		}
		
		if (newVal < param.getLowerBound()) {
			double dif = param.getLowerBound() - newVal;
			newVal = param.getLowerBound()+dif;
		}
		
		if (newVal > param.getUpperBound()) {
			double dif = newVal - param.getUpperBound();
			newVal = param.getUpperBound()-dif;
		}
		
		if (newVal < param.getLowerBound()) {
			double dif = param.getLowerBound() - newVal;
			newVal = param.getLowerBound()+dif;
		}
		
		if (newVal > param.getUpperBound()) {
			double dif = newVal - param.getUpperBound();
			newVal = param.getUpperBound()-dif;
		}
		if (newVal <= param.getLowerBound()) {
			throw new IllegalModificationException("New value is less than lower bound (value: " + newVal + ", lower bound: " + param.getLowerBound() +")" );
		}
		
		if (newVal > param.getUpperBound()) {
			throw new IllegalModificationException("New value is greater than upper bound (value: " + newVal + ", lower bound: " + param.getUpperBound() +")");
		}
		
		
		param.proposeValue(newVal);
		
		tallyCall();
		if (getCallsSinceReset() > 100 & getTotalCalls() % 50 == 0) {
			changeTuning();
		}
		return multiplier; 
	}

	private void changeTuning() {
		if (getRecentAcceptanceRatio() < lowRatio && scaleSize > scaleMin) { //We're not accepting many states, so shrink the window
			scaleSize *= 0.9;
//			if (param.getName().equals("kappa"))
//				System.out.println("Acceptances too big (" + getRecentAcceptanceRatio() + "), shrinking window to : " + scaleSize);
		}
		//Hard upper bound makes it so that we never multiply by more than exp(1.0*0.5)=exp(1)=1.648
		if (getRecentAcceptanceRatio() > highRatio && scaleSize < scaleMax) { //We're accepting too manu, grow the window
			scaleSize *= 1.1;
//			if (param.getName().equals("kappa"))
//				System.out.println("Acceptances too small (" + getRecentAcceptanceRatio() + "), growing window to : " + scaleSize);
		}
	}

	public void setParameter(DoubleParameter param) {
		this.param = param;
	}


}
