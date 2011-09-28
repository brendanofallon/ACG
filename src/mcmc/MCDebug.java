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


package mcmc;

/**
 * Small debugging class to help with debugging. 
 * @author brendan
 *
 */
public class MCDebug implements MCMCListener {

	MCMC chain = null;
	
	@Override
	public void setMCMC(MCMC chain) {
		this.chain = chain;
	}

	@Override
	public void newState(int stateNumber) {
//		if (stateNumber == 8110) {
//			chain.verbose = true;
//		}
	}

	@Override
	public void chainIsFinished() {	}

	
}
