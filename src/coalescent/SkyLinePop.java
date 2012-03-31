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

import java.util.Map;

import modifier.Modifier;
import modifier.ScaleModifier;
import modifier.SimpleModifier;
import java.lang.Double;

import parameter.DoubleParameter;
import parameter.CompoundParameter;
import parameter.Parameter;
import modifier.ModificationImpossibleException;

/**
 * Class representing the "Skyline" model for popluation dynamics.  This 
 * class is currently simply a place holder, it is not implemented.
 * Stay tuned.
 * @author elliottb
 *
 */
public class SkyLinePop extends CompoundParameter<Void> implements DemographicParameter {
	DoubleParameter crashSize; // population size at t==0 and the size it reaches at a crash
	DoubleParameter growthRate; // the rate that a population grows at (should be positive?)
	DoubleParameter crashTime; // time when population crashes
	
	public SkyLinePop(Map<String, String> attrs, DoubleParameter newCrashSize, 
			          DoubleParameter newGrowthRate, DoubleParameter newCrashPoint) {	
		super(attrs);
		this.crashSize = newCrashSize;
		addParameter(crashSize);
		this.growthRate = newGrowthRate;
		addParameter(growthRate);
		this.crashTime = newCrashPoint;
		addParameter(crashTime);
		
		crashSize.acceptValue();
		growthRate.acceptValue();
		crashTime.acceptValue();
	}
	
	public SkyLinePop(Map<String, String> attrs) {
		super(attrs);
	}
	
	@Override
	protected void proposeNewValue(Parameter<?> source) {
		try {
			fireParameterChange();
		} catch (ModificationImpossibleException e) {
			if (crashSize.isProposed()) {
				crashSize.revertValue();
			}
			if (growthRate.isProposed()) {
				growthRate.revertValue();
			}
			if (crashTime.isProposed()) {
				crashTime.revertValue();
			}
		}
	}
	
	@Override
	public double getIntegral(double t0, double t1) {
		// In this case t0 -> t1 does not cross the crashpoint
		double ct = crashTime.getValue();
 
		double size0 = getPopSize(t0);
		double size1 = getPopSize(t1);		
		if ((t0 < ct && t1 < ct) ||
		    (t0 > ct && t1 > ct)) {
			double integral = (size1 - size0)*(t1-t0)/2;
			return integral;
		}
		double sizeBeforeCrash = getPopSize(ct); 
		double baseSize = crashSize.getValue();		
		double integral = (baseSize - sizeBeforeCrash)*ct/2;
		integral += (getPopSize(t1) - baseSize)*t1/2;
		return integral;
	}
	/**
	 * Calculates the population size at any point t.
	 * TODO: This function assumes that t is always positive.  This should have
	 * some checking on this and probably should throw an exception otherwise
	 */
	public double getPopSize(double t) {
		if (t <= crashTime.getValue()) {
			return crashSize.getValue() + growthRate.getValue()*t;
		}
		return crashSize.getValue() + growthRate.getValue()*(t-crashTime.getValue());
	}
}
