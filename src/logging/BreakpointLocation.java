package logging;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import parameter.AbstractParameter;
import sequence.SiteMap;
import xml.XMLUtils;

import math.Histogram;
import mcmc.MCMC;
import mcmc.MCMCListener;

import arg.ARG;
import arg.CoalNode;
import arg.RecombNode;

public class BreakpointLocation extends PropertyLogger {

	public static final String XML_SEQBINS = "sequence.bins";
	public static final String XML_HEIGHTBINS = "height.bins";
	public static final String XML_HEIGHT = "height";
	
	
	ARG arg;
	int seqBins = 200;
	int depthBins = 400;
	int count = 0;
	double maxTreeHeight;
	
	Double userMaxHeight = null;
	
	int[][] hist = new int[seqBins][depthBins];
		
	//Optional mapping for sites, may be used in cases where columns have been removed from original alignment but 
	//we want to map back to orig coords. for output purposes
	SiteMap siteMap = null;
	
	public BreakpointLocation(Map<String, String> attrs, ARG arg) {
		super(attrs); //Burnin, collection frequency, and file name are set in base class
		Integer bins = XMLUtils.getOptionalInteger(XML_SEQBINS, attrs);
		if (bins != null)
			seqBins = bins;
		
		Integer hBins = XMLUtils.getOptionalInteger(XML_HEIGHTBINS, attrs);
		if (hBins != null)
			depthBins = hBins;
		
		userMaxHeight = XMLUtils.getOptionalDouble(XML_HEIGHT, attrs); //This can be null
		
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
		this.userMaxHeight = height;
		maxTreeHeight = userMaxHeight;
	}
	
	/**
	 * Set a mapping to translate output sites by
	 * @param map
	 */
	public void setSiteMap(SiteMap map) {
		this.siteMap = map;
	}
	
	public void addValue(int stateNumber) {
		if (userMaxHeight == null && stateNumber == burnin) {
			List<CoalNode> dlNodes = arg.getDLCoalNodes();
			Collections.sort(dlNodes, arg.getNodeHeightComparator());
			double maxDLHeight = dlNodes.get( dlNodes.size()-1).getHeight();
			maxTreeHeight = 2.0*Math.round(maxDLHeight*10000.0)/10000.0;
		}
	
		List<RecombNode> rNodes = arg.getDLRecombNodes();
		for(RecombNode rNode : rNodes) {
			int loc = rNode.getInteriorBP();
			double height = rNode.getHeight();
			
			int locBin = (int)(seqBins*(double)loc/(double)arg.getSiteCount());
			int depthBin = (int) ((double)depthBins*height/(double)maxTreeHeight);
			if (locBin < seqBins && depthBin < depthBins) {
				hist[locBin][depthBin]++;
				count++;
			}
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
		strB.append("\n Density of breakpoints : \n");
		strB.append("Max depth : " + maxTreeHeight + "\n");
		
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
			strB.append("\n");
			site += siteBinStep;
		}
		return strB.toString();
	}

	
}
