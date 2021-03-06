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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import logging.LogItemProvider;
import logging.Named;
import logging.StringUtils;
import mcmc.AcceptRejectListener;
import mcmc.MCMCListener;

import parameter.AbstractParameter;
import parameter.Parameter;
import parameter.ParameterListener;
import testing.Timer;


/**
 * The basic component of the likelihood model, this thing . Components depend on one or more Parameters and compute some likelihood 
 * based on the parameter states. 
 * 
 * All Components should observe the current vs. proposed distinction. As the MCMC progresses, new accepted states become the
 * 'current state'. A new proposal occurs when a parameter value is changed. This notifies the associated listeners that they
 * should recompute their likelihood and store it as the 'proposedLikelihood'. If the new state is accepted,  
 *  
 * @author brendan
 *
 */
public abstract class LikelihoodComponent implements ParameterListener, AcceptRejectListener, LogItemProvider, Named {

	//Whether to use Timers to do some simple profiling. Nice for debugging, but should be turned off for releases. 
	private final boolean useTimers = false;
	
	private Timer timer = new Timer();
	
	protected List<Parameter<?>> parameters = new ArrayList<Parameter<?>>();
	
	protected boolean recalculateLikelihood = true;
	
	protected double currentLogLikelihood = Double.NaN;
	protected double proposedLogLikelihood = Double.NaN;
	
	protected int proposalCount = 0;
	protected int acceptanceCount = 0;
	
	//Attributes provided from xml elements, default is empty map
	protected Map<String, String> attrs = new HashMap<String, String>();
	
	public LikelihoodComponent(Map<String, String> attrs) {
		this.attrs = attrs;
	}
	
	/**
	 * Compute and return the log likelihood based on proposed parameter values. 
	 */
	public abstract Double computeProposedLikelihood();
		
	public String getAttribute(String key) {
		return attrs.get(key);
	}
	
	/**
	 * Called to notify this component that it should accept the proposed state. This sets the current likelihood
	 * equal to the proposed likelihood. 
	 */
	public void stateAccepted() {
		currentLogLikelihood = proposedLogLikelihood;
		proposalCount++;
		acceptanceCount++;
	}
	
	/**
	 * Reject the proposed state, and set the proposed likelihood equal to the current likelihood (does this matter?) 
	 */
	public void stateRejected() {
		proposedLogLikelihood = currentLogLikelihood;
		proposalCount++;
	}
	
	public void parameterChanged(Parameter<?> source) {
		recalculateLikelihood = true;
	}
	
	public double getAcceptanceRate() {
		return (double)acceptanceCount / (double)proposalCount;
	}
	
	/**
	 * The number of times stateAccepted and stateRejected have been called
	 * @return
	 */
	public int getProposalCount() {
		return proposalCount;
	}
	
	/**
	 * Add a parameter to this component and set this component to listen for parameter change events.
	 * @param p
	 */
	public void addParameter(Parameter<?> p) {
		if (!parameters.contains(p)) {
			parameters.add(p);
			p.addListener(this);
		}
	}
	
	/**
	 * Remove the parameter from this component and remove the component as a parameter listener
	 * @param p
	 */
	public void removeParameter(Parameter<?> p) {
		parameters.remove(p);
		p.removeListener(this);
	}
	
	
	/**
	 * Obtain the current likelihood value of this component. 
	 * @return
	 */
	public Double getCurrentLogLikelihood() {
		return currentLogLikelihood;
	}
	
	/**
	 * Obtain the current likelihood value of this component. 
	 * @return
	 */
	public Double getProposedLogLikelihood() {
		
		if (recalculateLikelihood) {
			if (useTimers)
				timer.start();
			proposedLogLikelihood = computeProposedLikelihood();
			recalculateLikelihood = false;
			if (useTimers)
				timer.stop();
		}
		return proposedLogLikelihood;
	}
	
	/**
	 * Force the next computation of the likelihood to be recomputed 'from scratch'.
	 * 
	 * @return A parameter that was altered by the computation, 
	 */
	public void forceRecomputeLikelihood() {
		recalculateLikelihood = true;
	}
	
	public String toString() {
		StringBuilder str = new StringBuilder();
		str.append("Likelihood component with " + parameters.size() + " parameters \n");
		int count = 0;
		for(Parameter<?> p : parameters) {
			str.append("\t" + count + "\t" + p.getName() + "\n");
			count++;
		}
		return str.toString();
	}
	
	/**
	 * Obtain the string used as the column header when logging
	 * @return
	 */
	public abstract String getLogHeader();

	/**
	 * Return log string for this likelihood component. The default is to return the current log likelihood
	 * @return
	 */
	public String getLogString() {
		return StringUtils.format(getCurrentLogLikelihood(), 4);
	}
	
	/**
	 * Get the total amount of time this likelihood component has spent in 
	 * proposed likelihood calculation
	 */
	public long getTotalTimeMS() {
		return timer.getTotalTimeMS();
	}
	
	
	/**
	 * Called when the chain is finished executing. In general, we don't care about this
	 */
	public void chainIsFinished() {
		      //Most components don't care about this
	}
	
	
	//////////////////// LogItemProvider implementation ///////////////////////
	/** Default behavior is just to return a single log item, the proposed likelihood **/
	
	public int getKeyCount() {
		return 1;
	}
	
	public String[] getLogKeys() {
		return logKeys;
	}

	public Object getLogItem(String key) {
		if (key.equals(logKeys[0])) {
			return getProposedLogLikelihood();
		}
		else {
			return null;
		}
	}
	
	
	private String[] logKeys = new String[]{"likelihood"};
}
