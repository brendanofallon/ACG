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

import java.util.HashMap;
import java.util.Map;

import math.RandomSource;
import cern.jet.random.Exponential;
import parameter.DoubleParameter;
import xml.XMLUtils;
import component.LikelihoodComponent;

/**
 * A likelihood that takes a single DoubleParameter and returns 1/mean * Exp( val / mean ) - the exponential likelihood
 * @author brendano
 *
 */
public class ExponentialPrior extends AbstractPrior {

	public static final String XML_MEAN = "mean";
	
	double mean;
	Exponential exp; 
	DoubleParameter param;
	
	public ExponentialPrior(Map<String, String> attrs, DoubleParameter param) {
		super(attrs, param);
		addParameter(param);
		this.param = param;
		this.mean = XMLUtils.getDoubleOrFail(XML_MEAN, attrs);
		exp = new Exponential(1.0/mean, RandomSource.getEngine());	
		proposedLogLikelihood = computeProposedLikelihood();
		this.stateAccepted();
	}
	
	public ExponentialPrior(DoubleParameter param, double mean) {
		super(new HashMap<String, String>(), param);
		addParameter(param);
		this.param = param;
		this.mean = mean;
		exp = new Exponential(1.0/mean, RandomSource.getEngine());
		proposedLogLikelihood = computeProposedLikelihood();
		this.stateAccepted();
	}
	
	@Override
	public Double computeProposedLikelihood() {
		if (param.getValue() < 0)
			return Double.NEGATIVE_INFINITY;
		
		return Math.log( exp.pdf(param.getValue()) );
	}

	@Override
	public String getLogHeader() {
		return "ExpPrior[ " + param.getName() + " ]";
	}

}
