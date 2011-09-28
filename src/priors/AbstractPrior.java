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


package priors;

import java.util.Map;

import component.LikelihoodComponent;

import parameter.Parameter;

/**
 * Conceptually, priors are not really distinct from any other likelihood component.
 * This class is mostly just a marker for things that we intend to treat as 'priors'
 * @author brendan
 *
 */
public abstract class AbstractPrior extends LikelihoodComponent implements Prior {

	public AbstractPrior(Map<String, String> attrs, Parameter param) {
		super(attrs);
		addParameter(param);
	}
	
	
	
	
}
