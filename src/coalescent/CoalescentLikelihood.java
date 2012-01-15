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


package coalescent;

import java.util.HashMap;
import java.util.Map;

import arg.ARG;
import arg.ARGIntervals;
import arg.ARGIntervals.IntervalType;

import parameter.AbstractParameter;


import component.LikelihoodComponent;

/**
 * A Likelihood component that computes the likelihood of a DemographicParameter and
 * RecombinationParameter conditional on an ARG. This is flexible enough to handle
 * essentially any demographic and recombination model, assuming that they
 * return the correct values for getIntegral(..), etc. 
 *  
 * @author brendan
 *
 */
public class CoalescentLikelihood extends LikelihoodComponent {
	
	DemographicParameter demoParam = null;
	RecombinationParameter recParam = null;
	ARG tree;
	
	//Hard upper limit on maximum number of recombination nodes
	int MAX_RECOMBS = 200;
	
	public CoalescentLikelihood(DemographicParameter demoParam,  ARG tree) {
		this(new HashMap<String, String>(), demoParam, null, tree);
	}
	
	public CoalescentLikelihood(DemographicParameter demoParam, RecombinationParameter recParam, ARG tree) {
		this(new HashMap<String, String>(), demoParam, recParam, tree);
	}
	
	public CoalescentLikelihood(Map<String, String> attrs, DemographicParameter demoParam, ARG tree) {
		this(new HashMap<String, String>(), demoParam, null, tree);
	}
	
	public CoalescentLikelihood(Map<String, String> attrs, DemographicParameter demoParam, RecombinationParameter recParam, ARG tree) {
		super(attrs);
		
		if (recParam == null)
			recParam = new ConstantRecombination(0.0);
			
		addParameter( (AbstractParameter<?>)recParam);
		addParameter( (AbstractParameter<?>)demoParam);
		addParameter(tree);
		
		this.demoParam = demoParam;
		this.recParam = recParam;
		this.tree = tree;

		//Make sure to initialize value so we don't end up with bogus likelihoods for the initial steps..
		proposedLogLikelihood = computeProposedLikelihood();
		this.stateAccepted();
	}
			
	@Override 
	public Double computeProposedLikelihood() {
		Double logProb = 0.0;
		
		int recombNodeCount = tree.getRecombNodes().size();
				 
		//Force a rejection of this state if there are too many recombination nodes
		if (recombNodeCount > MAX_RECOMBS) {
			return Double.NEGATIVE_INFINITY;
		}
		
		
		final boolean emitStuff = false;
		
		
		ARGIntervals intervals = tree.getIntervals();
		
		
		double start = 0;
		double end;
		
		//Proceed through arg from tips to root, assessing each interval independently and
		//adding the log probabilities of each interval to a running sum
		for(int i=0; i<intervals.getIntervalCount(); i++) {
			end = intervals.getIntervalEndTime(i);
			int lineages = intervals.getLineageCount(i);
			
			//Probability that there was not a coalescence event between start and end of interval
			double noCoalProb = demoParam.getIntegral(start, end)*lineages*(lineages-1.0) * 0.5; //This 0.5 means that we get answers in terms of pop. size (=N*mu), not theta (=2*N*mu)
			
			//Probability of no recombinations in interval in question
			double noRecProb = lineages*recParam.getIntegral(start, end);

			double endProb;
			if (intervals.getIntervalType(i)==IntervalType.COALESCENT) {
				endProb = Math.log( demoParam.getPopSize(end) );
			}
			else {
				double recRate = recParam.getInstantaneousRate(end);
				endProb = -Math.log( recRate );
			}
			
			double intervalProb = noCoalProb + noRecProb + endProb;
			logProb -= intervalProb;
			if (emitStuff)
				System.out.println(end + " lines: " + lineages + " no coal: " + noCoalProb + " no recomb: " + noRecProb + " end: " + endProb + " interval: " + intervalProb );
			start = end;
		}
		
		if (emitStuff)
			System.out.println("Final prob: " + logProb);
		
		return logProb;
	}

	
	@Override
	public String getLogHeader() {
		return "Coalescent";
	}
	
	
}
