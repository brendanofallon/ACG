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
import parameter.DoubleParameter;

/**
 * The simplest form of recombination, constant across all sites and for all time. 
 * @author brendan
 *
 */
public class ConstantRecombination extends DoubleParameter implements RecombinationParameter {

	public ConstantRecombination(double value) {
		super(value, "rec.rate", "Recombination rate", 0, Double.MAX_VALUE);
	}
	
	
	
	public ConstantRecombination(Map<String, String> attrs) {
		super(attrs);
		
	}
	
	public ConstantRecombination(Map<String, String> attrs, Modifier mod) {
		this(attrs);
		addModifier(mod);
	}
	


	
	@Override
	public double getInstantaneousRate(double t) {
		return getValue();
	}

	@Override
	public double getIntegral(double start, double end) {
		return getValue()*(end-start);
	}

	@Override
	public double getSiteProbability(int site) {
		return 1.0;
	}

}
