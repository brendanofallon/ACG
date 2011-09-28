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


package dlCalculation.siteRateModels;

import java.util.HashMap;
import java.util.Map;

import modifier.Modifier;
import parameter.InvalidParameterValueException;
import parameter.Parameter;
import parameter.ParameterListener;

/**
 * Only one rate category in this simplest of site rate models
 * @author brendan
 *
 */
public class SingleRateModel extends AbstractSiteRateModel {
	
	public SingleRateModel(Map<String, String> attrs, double rate) {
		super(attrs, 1);
		proposedValue.rates[0] = rate;
		proposedValue.probabilities[0] = 1.0;
		activeValue = proposedValue;
	}

	public String getName() {
		return "Single rate model";
	}

	public String getLogHeader() {
		return "site.rate";
	}

	public String getLogString() {
		return "" + activeValue.rates[0];
	}

	@Override
	protected void proposeNewValue(Parameter<?> source) {
		throw new IllegalArgumentException("Nothing can be proposed to SingleRate model");
	}

}
