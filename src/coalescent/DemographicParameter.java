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

import parameter.AbstractParameter;

/**
 * Base class of things that specify a population size, potentially variable through time
 * @author brendan
 *
 */
public interface DemographicParameter {

	/**
	 * The integral of population size from time t0 to time t1 (t1 > t0)
	 * @param t0
	 * @param t1
	 * @return 
	 */
	public abstract double getIntegral(double t0, double t1);
	
	/**
	 * The population size at time t
	 * @param t
	 * @return
	 */
	public abstract double getPopSize(double t);
	
	
}
