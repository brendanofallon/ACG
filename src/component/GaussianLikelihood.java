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


package component;

import java.util.HashMap;

import parameter.DoubleParameter;

/**
 * A simple LikelihoodComponent that depends on a single DoubleParameter and returns 
 * a likelihood from a Gaussian distribution with mean and stdev. given by the mean 
 * and std fields.  
 *  
 * @author brendan
 *
 */
public class GaussianLikelihood extends LikelihoodComponent {

	//Parameter on which this likelihood depends
	DoubleParameter par = null;
	
	double mean1 = 5;
	double std1 = 0.2;
	
	
	public GaussianLikelihood(DoubleParameter par) {
		super(new HashMap<String, String>());
		this.par = par;
		addParameter(par); //Must add parameter to list of params in this likelihood, otherwise we won't be notified when it changes
		
		//Right now, likelihoods should be initialized with the following steps:
		proposedLogLikelihood = computeProposedLikelihood();
		stateAccepted();
		par.acceptValue();
	}
	
	@Override
	public Double computeProposedLikelihood() {
		return Math.log( Math.exp( -(mean1-par.getValue())*(mean1-par.getValue())/(2.0*std1)));
	}

	@Override
	public String getLogHeader() {
		return "GaussianLnL";
	}

}
