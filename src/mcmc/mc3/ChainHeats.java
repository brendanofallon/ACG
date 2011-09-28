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


package mcmc.mc3;

import java.util.List;

import parameter.AbstractParameter;

/**
 * Model specifying how the chains in an MC3 run are to be 'heated'. Right now the only implementation of this is 
 * the 'ExpChainHeats' model, in which chain i has temperature exp( -l *i ), where i is a parameter
 * @author brendano
 *
 */
public interface ChainHeats {

	public static final String XML_CHAINNUMBER = "numberOfChains";

	/**
	 * Get the heat for chain 'chainNumber'
	 * @param chainNumber
	 * @return
	 */
	public double getHeat(int chainNumber);
	
	/**
	 * Return the number of heats stored in this ChainHeats object
	 * @return
	 */
	public int getHeatCount();
	
	/**
	 * Tell the heating model that a proposed swap was rejected
	 */
	public void tallyRejection();
	
	/**
	 * Tell the heating model that a proposed swap was accepted
	 */
	public void tallyAcceptance();
	
	/**
	 * In a typical MC3 run we call clearObjectMap() on the XML loader so we can create now instances of all the objects. 
	 * However, this can potentially interfere with ChainHeating stuff, since we DONT want to create multiple versions of
	 * these things. They're 'global' with respect to MC objects. So here we can return a list of all parameters we depend
	 * on (for instance, the lambda chain heating parameter) to prevent these from being cleared. 
	 * @return
	 */
	List<AbstractParameter<?>> getGlobalParameters();  
	
}
