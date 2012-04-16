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


package logging;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import coalescent.DemographicParameter;

import parameter.AbstractParameter;

import arg.ARG;
import arg.CoalNode;
import math.LazyHistogram;
import mcmc.MCMC;

/**
 * Creates histograms of population sizes over time. At some point this should be
 * converted to extend PropertyLogger so it can inherit all that functionality, but
 * for now this mostly just exists for testing/debugging purposes. 
 * 
 * @author brendano
 *
 */
public class PopSizeLogger extends PropertyLogger {

	protected ARG arg;
	protected DemographicParameter popSize;
	
	protected final int bins = 100; //Number of bins over time 
	protected LazyHistogram[] sizeHistos;
	//Quick storage  for means and confidence boundaries
	private double[] binSites = null;
	protected double[] means = new double[bins];
	protected double[] upper95s = new double[bins];
	protected double[] lower95s = new double[bins];
	double binStep;
	
	Double maxTreeHeight = null; 

	public PopSizeLogger(Map<String, String> attrs, ARG arg, DemographicParameter demoParam) {
		super(attrs);
		this.arg = arg;
		this.popSize = demoParam;
		
		sizeHistos = new LazyHistogram[bins];
		for(int i=0; i<sizeHistos.length; i++) {
			sizeHistos[i] = new LazyHistogram(500);
		}

	}

	public void addValue(int stateNumber) {
		//If it's the first call, figure out how tall the tree is and use that for the 
		//deepest time bin
		if (maxTreeHeight == null) {
			List<CoalNode> dlNodes = arg.getDLCoalNodes();
			Collections.sort(dlNodes, arg.getNodeHeightComparator());
			double maxDLHeight = dlNodes.get( dlNodes.size()-1).getHeight();
			maxTreeHeight = 2.0*Math.round(maxDLHeight*10000.0)/10000.0;

			binStep = maxTreeHeight / (sizeHistos.length-1);
		}
		
		double time = 0;
		for(int i=0; i<sizeHistos.length; i++) {
			sizeHistos[i].addValue( popSize.getPopSize(time));
			time += binStep;
		}
	}
	

	/**
	 * Returns true if the histograms have dumped their values yet
	 * @return
	 */
	public boolean getHistoTriggerReached() {
		if (sizeHistos[0]==null)
			return false;
		else 
			return sizeHistos[0].triggerReached();
	}
	
	public double[] getMeans() {
		if (means == null) {
			means = new double[sizeHistos.length];
		}
		if (sizeHistos[0] != null) {
			for(int i=0; i<sizeHistos.length; i++) {
				means[i] = sizeHistos[i].getMean();
			}
		}
		return means;
	}
	
	public double[] getBinPositions() {
		if (binSites == null) {
			double time = 0;
			binSites = new double[sizeHistos.length];
			for(int i=0; i<sizeHistos.length; i++) {
				binSites[i] = time;
				time += binStep;
			}
		}
		
		return binSites;
	}
	
	public double[] getLower95s() {
		if (lower95s == null) {
			lower95s = new double[sizeHistos.length];
		}
		if (sizeHistos[0] != null) {
			for(int i=0; i<sizeHistos.length; i++) {
				lower95s[i] = sizeHistos[i].lowerHPD(0.05);
			}
		}
		return lower95s;
	}
	
	public double[] getUpper95s() {
		if (upper95s == null) {
			upper95s = new double[sizeHistos.length];
		}
		if (sizeHistos[0] != null) {
			for(int i=0; i<sizeHistos.length; i++) {
				upper95s[i] = sizeHistos[i].upperHPD(0.05);
			}
		}
		return upper95s;
	}
	
	
	@Override
	public void setMCMC(MCMC chain) {
		this.chain = chain;
		this.arg = findARG(chain);
		if (arg == null) {
			throw new IllegalArgumentException("No ARG found in chain");
		}
		
		this.popSize = findDemoParam(chain);
		if (popSize == null) {
			throw new IllegalArgumentException("No demographic parameter found in chain");
		}
	}

	@Override
	public String getSummaryString() {
		StringBuilder str = new StringBuilder();
		if (sizeHistos[0] == null) {
			str.append("Histograms have not been initialized, probably burnin has not been exceeded\n");
		}
		
		double binStep = maxTreeHeight / (sizeHistos.length-1);
		double time = 0;
		for(int i=0; i<bins; i++) {
			str.append(StringUtils.format(time, 5) + "\t" + sizeHistos[i].lowerHPD(0.025) + "\t" + sizeHistos[i].lowerHPD(0.05) + "\t" + sizeHistos[i].lowerHPD(0.1) + "\t" + sizeHistos[i].getMean() + "\t" + sizeHistos[i].upperHPD(0.10) + "\t" + sizeHistos[i].upperHPD(0.05) + "\t" + sizeHistos[i].upperHPD(0.025) + "\n");
			//str.append(StringUtils.format(time, 5) + "\t" + sizeHistos[i].getMean() + "\n");
			time += binStep;
		}
		
		return str.toString();
	}

	
	private ARG findARG(MCMC mc) {
		for(AbstractParameter<?> par : mc.getParameters()) {
			if (par instanceof ARG)
				return (ARG)par;
		}
		return null;
	}
	
	private DemographicParameter findDemoParam(MCMC mc) {
		for(AbstractParameter<?> par : mc.getParameters()) {
			if (par instanceof DemographicParameter)
				return (DemographicParameter)par;
		}
		return null;
	}

	
}
