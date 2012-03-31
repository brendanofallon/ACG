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

import modifier.Modifier;
import modifier.ScaleModifier;
import modifier.SimpleModifier;
import java.lang.Double;
import java.util.Map;

import parameter.CompoundParameter;
import parameter.DoubleParameter;
import parameter.Parameter;
import modifier.ModificationImpossibleException;

/**
 * Class representing a linear model for population growth
 * @author elliottb
 *
 */
public class LinearPopModel extends CompoundParameter<Void> implements DemographicParameter {
	
	DoubleParameter baseSize; // units ?
	DoubleParameter growthRate; // units ?

	/**
	 * TODO: Explicitly define parameters
	 * @param attrs
	 * @param baseSize
	 * @param growthRate
	 */
	public LinearPopModel(Map<String, String> attrs, DoubleParameter newBaseSize, 
			              DoubleParameter newGrowthRate) {	
		super(attrs);
		baseSize = newBaseSize;
		addParameter(newBaseSize);
		growthRate = newGrowthRate;
		addParameter(growthRate);
		baseSize.acceptValue();
		growthRate.acceptValue();
	}

	/**
	 * ELB, 2012/01/18
	 * Analytic calculation of the integral of the population size.
	 * Simply the area of the equivalent rectangle divided by 2.
	 * 
	 * TODO: throw exception for t1 < t0?
	 */
	@Override
	public double getIntegral(double t0, double t1) {
		double size0 = getPopSize(t0);
		double size1 = getPopSize(t1);
//		double integral = (t1-t0)/(size1 - size0)*2;
		double integral = (t1-t0)/size1;
		if (integral < 1.e-10) { 
			return 1.e-10;
		}
		return integral;
	}
	
	/**
	 * ELB, 2012/01/18
	 * Returns the linear function of the population size at a 
	 * time t, past (or before) the initial time.
	 */
	@Override
	public double getPopSize(double t) {
		return baseSize.getValue() + growthRate.getValue()*t;
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
	
	@Override
	public String getName() {
		return "linear.growth";
	}
}
