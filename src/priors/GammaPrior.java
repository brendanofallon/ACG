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

import math.RandomSource;
import cern.jet.random.Gamma;

import component.LikelihoodComponent;

import parameter.DoubleParameter;
import xml.XMLUtils;

/**
 * A gamma-distributed prior with user-defined mean and stdev
 * @author brendano
 *
 */
public class GammaPrior extends AbstractPrior {

	public static final String XML_MEAN = "mean";
	public static final String XML_STDEV = "stdev";
	
	Gamma gamma;
	DoubleParameter param;
	
	public GammaPrior(Map<String, String> attrs, DoubleParameter param) {
		super(attrs, param);
		this.param = param;
		double mean = XMLUtils.getDoubleOrFail(XML_MEAN, attrs);
		double stdev = XMLUtils.getDoubleOrFail(XML_STDEV, attrs);
		double var = stdev*stdev;
		
		gamma = new Gamma(mean*mean/var , var/mean, RandomSource.getEngine());
		
		proposedLogLikelihood = computeProposedLikelihood();
		this.stateAccepted();
	}
	

	
	@Override
	public Double computeProposedLikelihood() {
		return Math.log( gamma.pdf(param.getValue()));
	}

	@Override
	public String getLogHeader() {
		return "GammaPrior[" + param.getName() + "]";
	}

}
