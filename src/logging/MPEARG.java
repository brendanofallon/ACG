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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

import component.LikelihoodComponent;

import dlCalculation.DataLikelihood;
import parameter.AbstractParameter;
import arg.ARG;
import arg.argIO.ARGParser;
import mcmc.MCMC;
import mcmc.MCMCListener;

/**
 * A logger that writes the arg with the maximum data likelihood to a file. 
 * @author brendano
 *
 */
public class MPEARG extends PropertyLogger {
	
	ARGParser parser = new ARGParser();
	DataLikelihood dataLikelihood = null;
	MCMC currentChain = null;
	double maxLnL = Double.NEGATIVE_INFINITY; //Tracks current max likelihood so we know if we reach a new max
	int burnin = 10000;
	int frequency = 10000;
	String argStr = null; //Stores string representation of max-lnl ARG


	
	public MPEARG(Map<String, String> attrs) {
		super(attrs);
		this.dataLikelihood = null;
	}
	
	public MPEARG(Map<String, String> attrs, ARG arg) {
		super(attrs);
		this.dataLikelihood = null;
	}


	public void addValue(int stateNumber) {
		if (stateNumber > burnin && stateNumber % frequency == 0) {
			if (dataLikelihood == null)
				dataLikelihood = findDL(currentChain);
			
			Double currentDL = dataLikelihood.getCurrentLogLikelihood();
			double heat = currentChain.getTemperature();
			if (heat != 1.0) {
				throw new IllegalArgumentException("Hmm chain heat is not 1.0, MPE arg may not be listening to the cold chain");
			}
			if (currentDL > maxLnL) {
				maxLnL = currentDL;
				argStr = parser.argToXml( dataLikelihood.getTree() );					
			}

		}
	
	}
	
	@Override
	public void setMCMC(MCMC chain) {
		currentChain = chain;
		dataLikelihood = findDL(chain);
		if (dataLikelihood == null) {
			throw new IllegalArgumentException("Cannot listen to a chain without a data likelihood component");
		}
	}
	
	private DataLikelihood findDL(MCMC mc) {
		for(LikelihoodComponent comp : mc.getComponents()) {
			if (comp instanceof DataLikelihood) {
				return (DataLikelihood)comp;
			}
		}
		return null;
	}

	@Override
	public String getSummaryString() {
		if (argStr == null) 
			return "?";
		return argStr;
	}

	public String getName() {
		return "Most likely ARG";
	}

}
