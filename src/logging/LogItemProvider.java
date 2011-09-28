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


package logging;

/**
 * An interface for objects that can be queried for items to log. The idea is that various things, notably
 * parameters, may provide zero or more items to be written to log files. These items have names, such as
 * 'population size', 'recombination rate', etc, and it is helpful to be able to query the current value based
 * on the name, so we could, for instance, ask a parameter to get the value of the item whose name is 'popSize', or
 * whatever. 
 * 
 * @author brendan
 *
 */
public interface LogItemProvider {
	
	/**
	 * Obtain the number of loggable items given by this provider
	 * @return
	 */
	public int getKeyCount();
	
	/**
	 * Get the names (keys) of all loggable items provided by this provider
	 * @return
	 */
	public String[] getLogKeys();
	
	
	/**
	 * Get the current value of the loggable item associated with the given key
	 * @param key
	 * @return
	 */
	public Object getLogItem(String key);

}
