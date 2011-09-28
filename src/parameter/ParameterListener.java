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


package parameter;

import modifier.ModificationImpossibleException;

/**
 * Classes implementing this interface may listen for parameter change events. The parameterChanged(...) method is called
 * whenever some aspect of the parameter has been altered
 * @author brendan
 *
 */
public interface ParameterListener {

	public void parameterChanged(Parameter<?> source) throws ModificationImpossibleException;
	
}
