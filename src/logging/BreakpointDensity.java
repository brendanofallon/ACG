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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.Map;

import math.Histogram;
import mcmc.MCMC;
import parameter.AbstractParameter;
import sequence.SiteMap;
import xml.XMLUtils;

import arg.ARG;
import arg.RecombNode;

/**
 * A collector that charts the density of breakpoint location along the sequence
 * @author brendano
 *
 */
public class BreakpointDensity extends HistogramCollector  {

	ARG arg;
	
	NumberFormat formatter = new DecimalFormat("0.0####");
	
	//We keep track of the number of states sampled because we want to normalize by
	//the number of states, not over sites. This way we can compare peak heights 
	//between different runs - otherwise if one run had two bps both peaks would only
	//be half as high as a run with one bp, which seems like it doesn't make sense. 
	private int statesSampled = 0;

	public BreakpointDensity(Map<String, String> attrs, ARG arg) {
		super(attrs, arg, "Breakpoint density", 0, arg.getSiteCount(), 100);
		Integer bins = XMLUtils.getOptionalInteger("bins", attrs);
		if (bins != null)
			histo = new Histogram(0, arg.getSiteCount(), bins);
		this.arg = arg;
			
	}
	
	public BreakpointDensity(ARG arg) {
		this(arg, 1000, 100, System.out);
	}
	
	public BreakpointDensity(ARG arg, int collectionFrequency, int bins, PrintStream stream)  {
		super(arg, "Breakpoint density", 1000000, collectionFrequency, 0, arg.getSiteCount(), bins);
		this.arg = arg;
		if (stream != null) {
			setPrintStream(stream);
		}
	}
	
	
	public BreakpointDensity(ARG arg, int collectionFrequency, int bins, File outputFile)  {
		super(arg, "Breakpoint density", 1000000, collectionFrequency, 0, arg.getSiteCount(), bins);
		this.arg = arg;
		if (outputFile != null) {
			try {
				setOutputFile(outputFile);
			} catch (FileNotFoundException e) {
				System.err.println("Could not open file " + outputFile + " for summary file writing, reverting System.out");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void addValue(int stateNumber) {
		List<RecombNode> rNodes = arg.getDLRecombNodes();
		for(int i=0; i<rNodes.size(); i++) {
			histo.addValue(rNodes.get(i).getInteriorBP());
		}
		
		statesSampled++;
		
		//Debugging stuff, make sure we're collecting only from the cold chain
		double heat = chain.getTemperature();
		if (heat != 1.0) {
			throw new IllegalStateException("Chain heat is not 1.0, BreakpointDensity is collecting data from the wrong chain");
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
	
	@Override
	public String getSummaryString() {
		StringBuilder strB = new StringBuilder();
		
		strB.append("\n#Fraction of sampled states with recombination breakpoints at given position for ARG \'" + param.getName() + "\' \n");
		strB.append("\n#Site \t Density \n");

		if (histo.getCount()==0) {
			strB.append("Histogram is empty, burnin (" + burnin + " states) has probably not been exceeded.");
		}
		else {
			for(int i=0; i<histo.getBinCount(); i++) {
				int site = (int)Math.round(i*histo.getBinWidth());
				if (siteMap != null)
					site = siteMap.getOriginalSite(site);
				strB.append(site + "\t" + formatter.format( histo.getCount(i) / (double)statesSampled ) + "\n");
			}
			strB.append("States sampled : " + statesSampled + "\n");
		}
		
		return strB.toString();
	}
	
	public String getName() {
		return "Recombination along sequence";
	}
}
