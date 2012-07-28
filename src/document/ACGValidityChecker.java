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


package document;



import java.util.List;

import mcmc.mc3.MC3;

import org.w3c.dom.Element;


import parameter.CompoundParameter;

public class ACGValidityChecker implements ValidityChecker {

	@Override
	public boolean checkValidity(ACGDocument doc) throws Exception {
		List<String> params = doc.getParameterLabels();
		List<String> likelihoods = doc.getLikelihoodLabels();
		List<String> mcmcs = doc.getMCMCLabels();
		
		//Make sure all (non-compound) parameters are referred to by an MCMC or MC3 object, 
		for(String param : params) {
			Class cls = doc.loader.getClassForLabel(param);
			//If parameter is not a compound parameter, make sure an MCMC refers to it...
			if (! CompoundParameter.class.isAssignableFrom(cls)) {
				boolean getsReferredTo = false;
				for(String mcmc : mcmcs) {
					Element mcmcElement = doc.getElementForLabel(mcmc);
					if ( doc.getElementRefersToLabel(mcmcElement, param)) {
						getsReferredTo = true;
						break;
					}
				}
				
				if (! getsReferredTo) {
					List<String> mc3s = doc.getLabelForClass(MC3.class);
					for(String mc3 : mc3s) {
						Element mcmcElement = doc.getElementForLabel(mc3);
						if ( doc.getElementRefersToLabel(mcmcElement, param)) {
							getsReferredTo = true;
							break;
						}
					}
				}
				
				
				if (!getsReferredTo) {
					throw new StructureWarningException("Parameter with label " + param + " was declared, but not referred by to an MCMC object");
				}
			}
		}
		
		//Make sure all likelihoods are referenced as well
		for(String likeLabel : likelihoods) {
			Class cls = doc.loader.getClassForLabel(likeLabel);
			//If parameter is not a compound parameter, make sure an MCMC refers to it...
			if (! CompoundParameter.class.isAssignableFrom(cls)) {
				boolean getsReferredTo = false;
				for(String mcmc : mcmcs) {
					Element mcmcElement = doc.getElementForLabel(mcmc);
					if ( doc.getElementRefersToLabel(mcmcElement, likeLabel)) {
						getsReferredTo = true;
						break;
					}
				}
				
				if (!getsReferredTo) {
					throw new StructureWarningException("Likelihood with label " + likeLabel + " was declared but not referred to by an MCMC object.");
				}
			}
		}
		
		return true; //As in, we're valid
	}

}
