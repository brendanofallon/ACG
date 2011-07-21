package logging;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import arg.ARG;
import parameter.AbstractParameter;

/**
 * Collects root height only when arg has a given number of breakpoints. 
 * @author brendano
 *
 */
public class RootHeightCollector extends HistogramCollector {

	int bps; //Number of breakpoints 
	ARG arg; //Arg to collect root height of 

	NumberFormat formatter = new DecimalFormat("0.0####");
	
	public RootHeightCollector(ARG arg, int breakpoints,
			int collectionFrequency, double histMin, double histMax, int bins) {
		super(arg, "root.height", collectionFrequency, histMin, histMax, bins);
		
		this.arg = arg;
		this.bps = breakpoints;
	}

	
	/**
	 * Add current value of parameter to the histogram
	 */
	protected void addValue() {
		if (arg.getRecombNodes().size()==bps) {
			Double rootHeight = arg.getMaxHeight();
			histo.addValue(rootHeight);
		}
	}
	
	public void chainIsFinished() {
		outputStream.append("\n Histogram of root height conditional on breakpoints =  " + bps + "\n");
		for(int i=0; i<histo.getBinCount(); i++) {
			int site = (int)Math.round(i*histo.getBinWidth());
			if (siteMap != null)
				site = siteMap.getOriginalSite(site);
			outputStream.append(site + "\t" + formatter.format( histo.getFreq(i)) + "\n");
		}
		outputStream.append("Count : " + histo.getCount() + "\n");
		outputStream.append("Mean value: "+  histo.getMean() + "\n");
		outputStream.flush();
	}
}
