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

import modifier.ModificationImpossibleException;

import parameter.CompoundParameter;
import parameter.DoubleParameter;
import parameter.Parameter;

/**
 * A demographic model of exponential growth over time. The population is assumed to be
 * at baseSize at time t=0, then grows (or shrinks) exponentially as one looks back
 * into the past, with growth rate given by the growthRate param. 
 * @author brendan
 *
 */
public class ExponentialGrowth extends CompoundParameter<Void> implements DemographicParameter {

	DoubleParameter baseSize;
	DoubleParameter growthRate;
	
	public ExponentialGrowth(Map<String, String> attrs, DoubleParameter baseSize, DoubleParameter growthRate) {	
		super(attrs);
		this.baseSize = baseSize;
		addParameter(baseSize);
		this.growthRate = growthRate;
		addParameter(growthRate);
		baseSize.acceptValue();
		growthRate.acceptValue();
	}
	
	@Override
	public double getIntegral(double t0, double t1) {
		double r = growthRate.getValue();
		if (Math.abs(r) < 1e-10) {
			return (t1-t0)/baseSize.getValue();
		}
		else 
			return (Math.exp(r*t1)-Math.exp(r*t0))/(baseSize.getValue()*r);
	}

	@Override
	public double getPopSize(double t) {
		return baseSize.getValue()*Math.exp(-growthRate.getValue()*t);
	}
	
	@Override
	public String getName() {
		return "exp.growth";
	}

	@Override
	protected void proposeNewValue(Parameter<?> source) {
		try {
			fireParameterChange();
		} catch (ModificationImpossibleException e) {
			if (growthRate.isProposed())
				growthRate.revertValue();
			if (baseSize.isProposed())
				baseSize.revertValue();
		}
	}
	/**
	 * ELB, 2012/01/27
	 * Attempting to understand why these parameters are not included in the StateLogger
	 * Perhpas because this function is not overloaded?
 	 */
	@Override
	public String getLogHeader() {
		return String.format("%9s %9s", "growthRate", "baseSize");
	}

	/**
	 * ELB, 2012/01/27
	 * Attempting to understand why these parameters are not included in the StateLogger
	 * Perhpas because this function is not overloaded?
	 */
	@Override
	public String getLogString() {
		return String.format("%.9e %.9e", growthRate.getValue(), baseSize.getValue());
	}	
}
	