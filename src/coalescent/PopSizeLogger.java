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

import java.io.PrintStream;
import java.util.Map;

import arg.ARG;
import arg.CoalNode;
import arg.TreeUtils;
import logging.StringUtils;
import math.LazyHistogram;
import mcmc.MCMC;
import mcmc.MCMCListener;

/**
 * Creates histograms of population sizes over time. At some point this should be
 * converted to extend PropertyLogger so it can inherit all that functionality, but
 * for now this mostly just exists for testing/debugging purposes. 
 * 
 * ACTUALLY ACG DOCUMENT READING DEPENDS ON THIS BEING A PROPERTYLOGGER! YOU WONT
 * BE ABLE TO READ THESE SETTINGS FROM AN ACGDOCUMENT UNTIL THIS IS FIXED 
 * 
 * @author brendano
 *
 */
public class PopSizeLogger implements MCMCListener {

	ARG arg;
	DemographicParameter popSize;
	
	int bins = 100;
	int burnin = 1000000;
	
	LazyHistogram[] sizeHistos;
	
	double maxHeight = Double.NEGATIVE_INFINITY; //Tracks max height of trees during 
	double binStep;
	int collectionFrequency;
	
	PrintStream outStream = System.out;
	
	public PopSizeLogger(Map<String, String> attrs, ARG arg, DemographicParameter demoParam) {
		this.arg = arg;
		this.popSize = demoParam;
	}
	
	
	public PopSizeLogger(int burnin, int collectionFrequency, ARG arg, DemographicParameter demoParam, PrintStream outputStream) {
		this(burnin, collectionFrequency, arg, demoParam);
		this.outStream = outputStream;
	}
	
	
	public PopSizeLogger(int burnin, int collectionFrequency, ARG arg, DemographicParameter demoParam) {
		this.burnin = burnin;
		this.collectionFrequency = collectionFrequency;
		this.arg = arg;
		this.popSize = demoParam;
		sizeHistos = new LazyHistogram[100];
	}
	

	protected void addValue() {
		double time = 0;
		for(int i=0; i<sizeHistos.length; i++) {
			sizeHistos[i].addValue( popSize.getPopSize(time));
			time += binStep;
		}
	}
	
	
	@Override
	public void newState(int stateNumber) {
		if ( stateNumber < burnin && stateNumber > burnin/2) {
			double height = arg.getMaxHeight();
			if (height > maxHeight)
				maxHeight = height;
		}
		
		if (stateNumber == burnin) {
			
			//If burnin is very low maxHeight may not have been set at all
			if (Double.isInfinite(maxHeight))
				maxHeight = arg.getMaxHeight();
			
			//Convert max height to reasonable value
			maxHeight = Math.round( 2.0*maxHeight * 1000.0) / 1000.0;
			binStep = maxHeight / (double)bins;
			System.out.println("PopSizeLogger is setting max root height to : " + maxHeight);
			
			for(int i=0; i<bins; i++) {
				sizeHistos[i] = new LazyHistogram(1000);
			}
			
			
		}
		
		if (stateNumber > 0 && stateNumber % collectionFrequency == 0 && stateNumber>burnin) {
			addValue();
		}		
	}

	@Override
	public void chainIsFinished() {
		outStream.println("\n Population sizes over time : \n");
		if (sizeHistos[0] == null) {
			outStream.println("Histograms have not been initialized, probably burnin ( " + burnin +" states ) has not been reached yet");
			return;
		}
		double time = 0;
		for(int i=0; i<bins; i++) {
			outStream.println(StringUtils.format(time, 4) + "\t" + sizeHistos[i].lowerHPD(0.05) + "\t" + sizeHistos[i].lowerHPD(0.1) + "\t" + sizeHistos[i].lowerHPD(0.20) + "\t" + sizeHistos[i].getMean() + "\t" + sizeHistos[i].upperHPD(0.20) + "\t" + sizeHistos[i].upperHPD(0.10) + "\t" + sizeHistos[i].upperHPD(0.05));
			time += binStep;
		}
	}

	
	@Override
	public void setMCMC(MCMC chain) {		
	}
}
