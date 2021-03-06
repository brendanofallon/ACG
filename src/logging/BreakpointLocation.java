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

import java.io.PrintStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import parameter.AbstractParameter;
import sequence.SiteMap;
import xml.XMLUtils;

import mcmc.MCMC;
import arg.ARG;
import arg.CoalNode;
import arg.RecombNode;

/**
 * Keeps track of the locations of recombination breakpoints along the sequence AND over time as
 * a 2D histogram. We always track all sites, but only go back in time as far as maxTreeHeight,
 * which can optionally be specified as an argument to the xml node. If not specified, we
 * use twice the "dl height" (deepest 'visible' node) in the ARG as the max height
 * @author brendan
 *
 */
public class BreakpointLocation extends PropertyLogger {

	public static final String XML_SEQBINS = "sequence.bins";
	public static final String XML_HEIGHTBINS = "height.bins";
	public static final String XML_HEIGHT = "height";
	
	
	ARG arg;
	int seqBins = 200;
	int depthBins = 200;
	int count = 0;
	double approxMaxDensity = 0; // For UI purposes, the most recent maximum density of bin calculated in getDensities()
	Double maxTreeHeight = null;
	
	int[][] hist = new int[seqBins][depthBins];
		
	//Optional mapping for sites, may be used in cases where columns have been removed from original alignment but 
	//we want to map back to orig coords. for output purposes. Now defunct since we can handle gapped sequences
	SiteMap siteMap = null;
	
	public BreakpointLocation(Map<String, String> attrs, ARG arg) {
		super(attrs); //Burnin, collection frequency, and file name are set in base class
		Integer bins = XMLUtils.getOptionalInteger(XML_SEQBINS, attrs);
		if (bins != null)
			seqBins = bins;
		
		Integer hBins = XMLUtils.getOptionalInteger(XML_HEIGHTBINS, attrs);
		if (hBins != null)
			depthBins = hBins;
		
		maxTreeHeight = XMLUtils.getOptionalDouble(XML_HEIGHT, attrs); //This can be null
		
		this.arg = arg;
		hist = new int[seqBins][depthBins];
	}
	
	public BreakpointLocation(ARG arg) {
		this(arg, 1000,  System.out);
	}
	
	public BreakpointLocation(ARG arg, int collectionFrequency, PrintStream stream)  {
		super(5000000, collectionFrequency);
		
		this.arg = arg;
		if (stream != null) {
			setPrintStream(stream);
		}
	}
	
	/**
	 * Set the maximum height of the histogram, recombinations with heights greater than the given
	 * height will not be tracked. If this is not set directly, a hopefully logical height
	 * will be guessed from the data
	 * @param height
	 */
	public void setMaxHeight(double height) {
		maxTreeHeight = height;
	}
	
	/**
	 * Set a mapping to translate output sites by
	 * @param map
	 */
	public void setSiteMap(SiteMap map) {
		this.siteMap = map;
	}
	
	public void addValue(int stateNumber) {
		if (maxTreeHeight == null && stateNumber >= burnin) {
			List<CoalNode> dlNodes = arg.getDLCoalNodes();
			List<RecombNode> rNodes = arg.getRecombNodes();
			Collections.sort(rNodes, arg.getNodeHeightComparator());
			Collections.sort(dlNodes, arg.getNodeHeightComparator());
			double maxRecombHeight = 0;
			if (rNodes.size()>0)
				maxRecombHeight = rNodes.get(rNodes.size()-1).getHeight();
			double maxDLHeight = dlNodes.get( dlNodes.size()-1).getHeight();
			double height = maxDLHeight;
			if (rNodes.size()>6 && maxRecombHeight > 0)
				height = Math.min(maxRecombHeight, maxDLHeight);
			maxTreeHeight = Math.round(height*10000.0)/15000.0;
		}
	
		List<RecombNode> rNodes = arg.getDLRecombNodes();
		for(RecombNode rNode : rNodes) {
			int loc = rNode.getInteriorBP();
			double height = rNode.getHeight();
			
			int locBin = (int)(seqBins*(double)loc/(double)arg.getSiteCount());
			int depthBin = (int) ((double)depthBins*height/(double)maxTreeHeight);
			if (locBin < seqBins && depthBin < depthBins) {
				hist[locBin][depthBin]++;
			}
		}
		count++;
	}
	
	/**
	 * Get the height (depth) of the deepest bin we're tracking 
	 * @return
	 */
	public double getTreeHeight() {
		return maxTreeHeight; 
	}
	
	/**
	 * The number of bins going back in time
	 * @return
	 */
	public int getDepthBins() {
		return depthBins;
	}
	
	/**
	 * The number of bins spanning sequence space
	 * @return
	 */
	public int getSeqBins() {
		return seqBins;
	}
	
	public int[] getSiteColumn(int seqBin) {
		return hist[seqBin];
	}
	
	/**
	 * Return the number of sites in the ARG
	 * @return
	 */
	public int getARGSites() {
		return arg.getSiteCount();
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
	
	/**
	 * Compute and return the densities of all bins in the given array. If the array
	 * is null, a new one with the correct lengths is created. If it is not null, it must
	 * must have dimensions (seqBins, depthBins)
	 * The maximum densities encountered is returned. s
	 * @param densities
	 * @return
	 */
	public double[][] getDensities(double[][] densities) {
		if (densities == null)
			densities = new double[seqBins][depthBins];
		else {
			if (densities.length != seqBins)
				throw new IllegalArgumentException("Incorrect number of sequence bins");
			if (densities[0].length != depthBins) 
				throw new IllegalArgumentException("Incorrect number of depth bins");
		}
		
		
		if (count == 0)
			return densities;
		
		double max = 0;
		for(int i=0; i<seqBins; i++) {
			for(int j=0; j<depthBins; j++) {
				densities[i][j] = (double)hist[i][j] / (double)count;
				if (densities[i][j] > max)
					max = densities[i][j];
			}
		}
		
		//System.out.println("Max is : " + max);
		approxMaxDensity = max;
		return densities;
	}

	/**
	 * The most recent maximum bin density computed. Will b zero until getDensities() is called
	 * @return
	 */
	public double getApproxMaxDensity() {
		return approxMaxDensity;
	}
	
	@Override
	public String getSummaryString() {
		StringBuilder strB = new StringBuilder();
		strB.append("# Fraction of states with a recombination breakpoint at the given position." + lineSep);
		strB.append("# Rows of following matrix describe sites along sequence, columns are depths in ARG with rightmost column being the greatest depth" + lineSep);
		strB.append("# Max depth : " + maxTreeHeight + " " + lineSep);
		strB.append("# States sampled : " + count + " " + lineSep);
		
		double site = 0;
		double siteBinStep =  (double)arg.getSiteCount() / (double)seqBins;
		
		for(int i=0; i<seqBins; i++) {
			int mappedSite = (int)Math.round(site);
			if (siteMap != null)
				mappedSite = siteMap.getOriginalSite(mappedSite);	
			
			strB.append(mappedSite + "\t");
			for(int j=0; j<depthBins; j++) {
				strB.append(StringUtils.format(hist[i][j]/(double)count, 4) + "\t");
			}
			strB.append(lineSep);
			site += siteBinStep;
		}
		return strB.toString();
	}

	

	
}
