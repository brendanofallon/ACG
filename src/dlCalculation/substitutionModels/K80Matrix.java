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


package dlCalculation.substitutionModels;

import java.util.HashMap;
import java.util.Map;

import parameter.DoubleParameter;
import parameter.Parameter;



public class K80Matrix extends F84Matrix {

	public K80Matrix(BaseFrequencies stationaries, DoubleParameter kappa) {
		super(stationaries, kappa);
		throw new IllegalArgumentException("Cannot explicitly set stationaries for K80 mutation model");
	}
	
	public K80Matrix(DoubleParameter kappa) {
		super(new BaseFrequencies(new double[]{0.25, 0.25, 0.25, 0.25}), kappa);
	}
	
	public K80Matrix(Map<String, String> attrs, DoubleParameter kappa) {
		super(attrs, kappa);
	}

	@Override
	public String getName() {
		return "K80 mutation model";
	}
	

	@Override
	protected void proposeNewValue(Parameter<?> source) {
		recalcIntermediates = true;
	}

}
