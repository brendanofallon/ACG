package logging;

import java.io.File;
import java.io.FileNotFoundException;
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
		super(arg, "Breakpoint density", collectionFrequency, 0, arg.getSiteCount(), bins);
		this.arg = arg;
		if (stream != null) {
			setPrintStream(stream);
		}
	}
	
	
	public BreakpointDensity(ARG arg, int collectionFrequency, int bins, File outputFile)  {
		super(arg, "Breakpoint density", collectionFrequency, 0, arg.getSiteCount(), bins);
		this.arg = arg;
		if (outputFile != null) {
			try {
				setOutputFile(outputFile);
			} catch (FileNotFoundException e) {
				System.err.println("Could not open file " + outputFile + " for summary file writing, reverting System.out");
			}
		}
	}
	
	public void addValue(int stateNumber) {
		List<RecombNode> rNodes = arg.getDLRecombNodes();
		for(int i=0; i<rNodes.size(); i++) {
			histo.addValue(rNodes.get(i).getInteriorBP());
		}
	}

	@Override
	public void setMCMC(MCMC chain) {
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
		
		strB.append("\n Histogram of values for parameter " + param.getName() + " item: " + logKey + "\n");
		if (histo.getCount()==0) {
			strB.append("Histogram is empty, burnin (" + burnin + " states) has probably not been exceeded.");
		}
		else {
			for(int i=0; i<histo.getBinCount(); i++) {
				int site = (int)Math.round(i*histo.getBinWidth());
				if (siteMap != null)
					site = siteMap.getOriginalSite(site);
				strB.append(site + "\t" + formatter.format( histo.getFreq(i)) + "\n");
			}
			strB.append("Count : " + histo.getCount() + "\n");
		}
		
		return strB.toString();
	}
}
