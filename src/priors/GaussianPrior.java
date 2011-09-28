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

import math.RandomSource;
import parameter.DoubleParameter;
import xml.XMLUtils;
import cern.jet.random.Gamma;
import cern.jet.random.Normal;

/**
 * A Gaussian (Normal) prior on a DoubleParameter, specified by mean and standard deviation
 * @author brendano
 *
 */
public class GaussianPrior extends AbstractPrior {

	public static final String XML_MEAN = "mean";
	public static final String XML_STDEV = "stdev";
	
	Normal gaussian;
	DoubleParameter param;
	
	public GaussianPrior(Map<String, String> attrs, DoubleParameter param) {
		super(attrs, param);
		this.param = param;
		double mean = XMLUtils.getDoubleOrFail( XML_MEAN, attrs);
		double stdev = XMLUtils.getDoubleOrFail(XML_STDEV, attrs);
			
		gaussian = new Normal(mean , stdev, RandomSource.getEngine());
		
		proposedLogLikelihood = computeProposedLikelihood();
		this.stateAccepted();
	}
	

	
	@Override
	public Double computeProposedLikelihood() {
		return Math.log( gaussian.pdf(param.getValue()));
	}

	@Override
	public String getLogHeader() {
		return "GaussianPrior[" + param.getName() + "]";
	}

}
