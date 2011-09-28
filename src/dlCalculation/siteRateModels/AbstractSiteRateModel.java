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

import java.util.Map;

import parameter.CompoundParameter;

/**
 * Base class for objects that implement a discrete series of evolutionary rates over sites, such as the discretized-gamma-distributed
 * rate often employed. This is a CompoundParameter since we have a particular 'value' (a SiteRates object), but we (potentially) also 
 * depend on the state of other parameters - for instance the 'Alpha' parameter in the Gamma rates model
 * @author brendano
 *
 */
public abstract class AbstractSiteRateModel extends CompoundParameter<SiteRates> implements SiteRateModel {

	public AbstractSiteRateModel(Map<String, String> attrs, int categories) {
		super(attrs);
		currentValue = new SiteRates();
		currentValue.probabilities = new double[categories];
		currentValue.rates = new double[categories];
		
		proposedValue = new SiteRates();
		proposedValue.probabilities = new double[categories];
		proposedValue.rates = new double[categories];
		
		activeValue = proposedValue;
	}

	

	public int getCategoryCount() {
		return activeValue.probabilities.length;
	}

	public double getRateForCategory(int category) {
		return activeValue.rates[category];
	}

	public double getProbForCategory(int category) {
		return activeValue.probabilities[category];
	}

}
