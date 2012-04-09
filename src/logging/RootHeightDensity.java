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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Map;

import parameter.AbstractParameter;
import sequence.SiteMap;
import xml.XMLLoader;
import xml.XMLUtils;

import math.Histogram;
import math.LazyHistogram;
import mcmc.MCMC;
import mcmc.MCMCListener;
import arg.ARG;
import arg.CoalNode;
import arg.TreeUtils;

/**
 * A listener that builds histograms of the marginal root height across the sites of an arg. 
 * 
 * @author brendano
 *
 */
public class RootHeightDensity extends PropertyLogger {

	public static final String XML_SEQBINS = "sequence.bins";
	
	ARG arg;
	double[] rootHeights; //Tracks mean root height across sites
	LazyHistogram[] heightHistos; //Stores histograms of root heights across sequence length
	
	int bins = 100;
	int binStep;

	SiteMap siteMap = null; //A mapping for sites, used to translate output only
		
	
	public RootHeightDensity(Map<String, String> attrs, ARG arg) {
		super(attrs); //Burnin, collection frequency, and file name are set in base class
		Integer sBins = XMLUtils.getOptionalInteger(XML_SEQBINS, attrs);
		if (sBins != null)
			bins = sBins;
		
		this.arg = arg;
		this.binStep = arg.getSiteCount() / bins;
		rootHeights = new double[bins];
		heightHistos = new LazyHistogram[bins];
	}
	
	public RootHeightDensity(ARG arg) {
		this(arg, 1000, 100, System.out);
	}
	
	public RootHeightDensity(ARG arg, int collectionFrequency, int bins, PrintStream stream) {
		super(1000000, collectionFrequency);
		this.bins = bins;
		this.binStep = arg.getSiteCount() / bins;
		this.arg = arg;
		rootHeights = new double[bins];
		heightHistos = new LazyHistogram[bins];
		if (stream != null) {
			outputStream = stream;
		}
	}
	
	public RootHeightDensity(ARG arg, int collectionFrequency, int bins, File outputFile) {
		super(1000000, collectionFrequency);
		this.bins = bins;
		this.binStep = arg.getSiteCount() / bins;
		this.arg = arg;
		rootHeights = new double[bins];
		heightHistos = new LazyHistogram[bins];
		if (outputFile != null) {
			try {
				setOutputFile(outputFile);
			} catch (FileNotFoundException e) {
				System.err.println("Could not open file " + outputFile + " for summary file writing, reverting System.out");
			} catch (IOException e) {
				System.err.println("Could not open file " + outputFile + " for summary file writing, reverting System.out");
			}
		}
	}
	
	/**
	 * Obtain an array containing the mean TMRCA across sites
	 * @return
	 */
	public synchronized double[] getMeans() {
		if (means == null) {
			means = new double[heightHistos.length];
		}
		if (heightHistos[0] != null) {
			for(int i=0; i<heightHistos.length; i++) {
				means[i] = heightHistos[i].getMean();
			}
		}
		return means;
	}
	
	/**
	 * Returns true if the histograms have dumped their values yet
	 * @return
	 */
	public boolean getHistoTriggerReached() {
		if (heightHistos[0]==null)
			return false;
		else 
			return heightHistos[0].triggerReached();
	}
	
	
	public double[] getLower95s() {
		if (lower95s == null) {
			lower95s = new double[heightHistos.length];
		}
		if (heightHistos[0] != null) {
			for(int i=0; i<heightHistos.length; i++) {
				lower95s[i] = heightHistos[i].lowerHPD(0.05);
			}
		}
		return lower95s;
	}
	
	public double[] getUpper95s() {
		if (upper95s == null) {
			upper95s = new double[heightHistos.length];
		}
		if (heightHistos[0] != null) {
			for(int i=0; i<heightHistos.length; i++) {
				upper95s[i] = heightHistos[i].upperHPD(0.05);
			}
		}
		return upper95s;
	}
	
	public double[] getBinPositions() {
		if (binSites == null) {
			int site = 0;
			binSites = new double[heightHistos.length];
			for(int i=0; i<rootHeights.length; i++) {
				binSites[i] = site;
				site += binStep;
			}
		}
		
		return binSites;
	}
	
	/**
	 * Sets a map to translate the output of this collector, 
	 * @param map
	 */
	public void setSiteMap(SiteMap map) {
		this.siteMap = map;
	}
	
	public void addValue(int stateNumber) {
		if (stateNumber >= burnin/2 && heightHistos[0] == null) {
			for(int i=0; i<bins; i++) {
				heightHistos[i] = new LazyHistogram(1000);
			}
		}
		
		int site = 0;
		for(int i=0; i<rootHeights.length; i++) {
			CoalNode marginalRoot = TreeUtils.createMarginalTree(arg, site);
			rootHeights[i] += marginalRoot.getHeight();
			heightHistos[i].addValue( marginalRoot.getHeight() );
			site += binStep;
		}
		calls++;
		
		
		double heat = chain.getTemperature();
		if (heat != 1.0) {
			throw new IllegalStateException("Chain heat is not 1.0, RootHeightDensity is collecting data from the wrong chain");
		}
	}


	
	@Override
	public void setMCMC(MCMC chain) {
		this.chain = chain;
		ARG newARG = findARG(chain);
		if (newARG == null) {
			throw new IllegalArgumentException("Cannot listen to a chain without an arg  parameter");
		}
		this.arg = newARG;
	}
	
	private ARG findARG(MCMC mc) {
		for(AbstractParameter<?> par : mc.getParameters()) {
			if (par instanceof ARG)
				return (ARG)par;
		}
		return null;
	}

	
	public String getSummaryString() {
		StringBuilder strB = new StringBuilder();
		if (calls == 0) {
			strB.append("Histogram of root heights : No data to display (probably burn-in has not been exceeded) " + lineSep);
			return strB.toString();
		}
		strB.append("# Histogram of root heights across sites : " + lineSep);
		strB.append("# lower95\t lower90\t lower80\t mean\t upper80\t upper90\t upper95: " + lineSep);
		
		int site = 0;
		
		for(int i=0; i<rootHeights.length; i++) {
			int mappedSite = site;
			if (siteMap != null)
				mappedSite = siteMap.getOriginalSite(site);
			if (getHistoTriggerReached())
				strB.append(mappedSite + "\t" + heightHistos[i].lowerHPD(0.025) + "\t" + heightHistos[i].lowerHPD(0.05)+ "\t" + heightHistos[i].lowerHPD(0.1)  + "\t" + heightHistos[i].getMedian() + "\t" + heightHistos[i].upperHPD(0.1) + "\t" + heightHistos[i].upperHPD(0.05) + "\t" + heightHistos[i].upperHPD(0.025) + " " + lineSep);
			else
				strB.append(mappedSite + "\t" +  heightHistos[i].getMean() + " " + lineSep);

			site += binStep;
		}
		
		site = 0;
//		strB.append("\n Mean marginal tree height across sites \n");
//
//		for(int i=0; i<rootHeights.length; i++) {
//			int mappedSite = site;
//			if (siteMap != null)
//				mappedSite = siteMap.getOriginalSite(site);
//			
//			strB.append(mappedSite + "\t" + heightHistos[i].getMean() );
//			site += binStep;
//		}
		
		return strB.toString();
	}

	@Override
	public String getName() {
		return "Root height logger";
	}

	private double[] binSites = null;
	private double[] means = null; //Stores mean values across sites for rapid retrieval
	private double[] lower95s = null;
	private double[] upper95s = null;
}
