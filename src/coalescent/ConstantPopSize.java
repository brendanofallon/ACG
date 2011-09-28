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

public class ConstantPopSize extends DoubleParameter implements DemographicParameter  {

	public ConstantPopSize(Double size) {
		super(size, "pop.size", "Population size", 0, Double.MAX_VALUE);
		proposedValue = size;
		lowerBound = 0.0;
	}
	
	public ConstantPopSize(Map<String, String> attrs) {
		super(attrs);
	}
	
	public ConstantPopSize(Map<String, String> attrs, Modifier<?> mod) {
		this(attrs);
		addModifier(mod);
	}

	
	public String getLogHeader() {
		return "popSize";
	}


	/**
	 * Returns the integral of the COALESCENT RATE, not the population size, over time. 
	 */
	public double getIntegral(double t0, double t1) {
		double size = getValue();
		return (t1-t0)/size;
	}


	public double getPopSize(double t) {
		return getValue();
	}

	

}
