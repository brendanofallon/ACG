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

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import parameter.AbstractParameter;

import xml.XMLUtils;

import arg.ARG;
import arg.CoalNode;
import arg.TreeUtils;
import logging.PropertyLogger;
import logging.StringUtils;
import math.LazyHistogram;
import mcmc.MCMC;
import mcmc.MCMCListener;

/**
 * Creates histograms of population sizes over time. At some point this should be
 * converted to extend PropertyLogger so it can inherit all that functionality, but
 * for now this mostly just exists for testing/debugging purposes. 
 * 
 * @author brendano
 *
 */
public class PopSizeLogger extends PropertyLogger {

	private ARG arg;
	private DemographicParameter popSize;
	
	private int bins = 200;
	
	private LazyHistogram[] sizeHistos;
	
	private Double maxTreeHeight = null; //Tracks max height of trees during 


	/**
	 * Example xml-friendly constructor. We need a reference to both the ARG (so we can find it's
	 * height, so we now how deep to make the historgram) and the DemographicParameter, which
	 * stores what the population size function actually looks like.
	 * 
	 * @param attrs
	 * @param arg
	 * @param demoParam
	 */
	public PopSizeLogger(Map<String, String> attrs, ARG arg, DemographicParameter demoParam) {
		super(attrs);
		this.arg = arg;
		this.popSize = demoParam;
	}
	
//	public PopSizeLogger(int burnin, int collectionFrequency, ARG arg, DemographicParameter demoParam, PrintStream outputStream) {
//		this(burnin, collectionFrequency, arg, demoParam);
//		this.outStream = outputStream;
//	}
	
	
//	public PopSizeLogger(int burnin, int collectionFrequency, ARG arg, DemographicParameter demoParam) {
//		this.burnin = burnin;
//		this.collectionFrequency = collectionFrequency;
//		this.arg = arg;
//		this.popSize = demoParam;
//		sizeHistos = new LazyHistogram[100];
//	}
	

	public void addValue(int stateNumber) {
		//If it's the first call, figure out how tall the tree is and use that for the 
		//deepest time bin
		if (maxTreeHeight == null) {
			List<CoalNode> dlNodes = arg.getDLCoalNodes();
			Collections.sort(dlNodes, arg.getNodeHeightComparator());
			double maxDLHeight = dlNodes.get( dlNodes.size()-1).getHeight();
			maxTreeHeight = 2.0*Math.round(maxDLHeight*10000.0)/10000.0;
			
			sizeHistos = new LazyHistogram[bins];
			for(int i=0; i<sizeHistos.length; i++) {
				sizeHistos[i] = new LazyHistogram(500);
			}
		}
		
		double binStep = maxTreeHeight / (sizeHistos.length-1);
		double time = 0;
		for(int i=0; i<sizeHistos.length; i++) {
			sizeHistos[i].addValue( popSize.getPopSize(time));
			time += binStep;
		}
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
