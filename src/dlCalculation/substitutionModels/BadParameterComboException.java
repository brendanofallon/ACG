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

import modifier.ModificationImpossibleException;

/**
 * This gets thrown when the stationaries or other parameters in a mutation model are inconsistent.
 * For instance, in TN93 mutation kappa must be greater than (piA*piG + piC*piT)/(piA+piG)/(piC+piT). 
 * Similarly (equivalently, even), in K2P mutation kappa must be greater than 0.5 (right?) 
 * @author brendano
 *
 */
public class BadParameterComboException extends ModificationImpossibleException {

	public BadParameterComboException(String message) {
		super(message);
	}
	
}
