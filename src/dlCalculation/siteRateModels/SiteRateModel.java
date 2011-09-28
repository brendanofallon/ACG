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

import parameter.Parameter;

/**
 * A description of how evolutionary rate 
 * @author brendan
 *
 */
public interface SiteRateModel extends Parameter<SiteRates>{
	
	/**
	 * The number of rate categories
	 * @return
	 */
	public int getCategoryCount();
	
	/**
	 * The rate associated with the given category 
	 * @param category
	 * @return
	 */
	public double getRateForCategory(int category);
	
	/**
	 * Get the probability that a site in the given rate category
	 * @param category
	 * @return
	 */
	public double getProbForCategory(int category);
	
	


}
