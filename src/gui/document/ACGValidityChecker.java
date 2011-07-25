package gui.document;


import java.util.List;

import org.w3c.dom.Element;

import parameter.CompoundParameter;

public class ACGValidityChecker implements ValidityChecker {

	@Override
	public boolean checkValidity(ACGDocument doc) throws Exception {
		List<String> params = doc.getParameterLabels();
		List<String> likelihoods = doc.getLikelihoodLabels();
		List<String> mcmcs = doc.getMCMCLabels();
		
		//Make sure all (non-compound) pararameters are referred to by an MCMC, 
		for(String param : params) {
			Class cls = doc.loader.getClassForLabel(param);
			//If parameter is not a compound parameter, make sure an MCMC refers to it...
			if (! CompoundParameter.class.isAssignableFrom(cls)) {
				boolean getsReferredTo = false;
				for(String mcmc : mcmcs) {
					Element mcmcElement = doc.getFirstElement(mcmc);
					if ( doc.getElementRefersToLabel(mcmcElement, param)) {
						getsReferredTo = true;
						break;
					}
				}
				
				if (!getsReferredTo) {
					throw new StructureWarningException("Parameter with label " + param + " is not used in any MCMC object!");
				}
			}
		}
		
		
		for(String likeLabel : likelihoods) {
			Class cls = doc.loader.getClassForLabel(likeLabel);
			//If parameter is not a compound parameter, make sure an MCMC refers to it...
			if (! CompoundParameter.class.isAssignableFrom(cls)) {
				boolean getsReferredTo = false;
				for(String mcmc : mcmcs) {
					Element mcmcElement = doc.getFirstElement(mcmc);
					if ( doc.getElementRefersToLabel(mcmcElement, likeLabel)) {
						getsReferredTo = true;
						break;
					}
				}
				
				if (!getsReferredTo) {
					throw new StructureWarningException("Likelihood with label " + likeLabel + " is not used in any MCMC object!");
				}
			}
		}
		
		return true; //As in, we're valid
	}

}
