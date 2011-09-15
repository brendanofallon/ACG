package logging;

import java.util.Map;

import math.Histogram;
import math.LazyHistogram;
import arg.ARG;
import arg.TreeUtils;
import parameter.AbstractParameter;
import xml.XMLUtils;

/**
 * Simple collector that just collects the distribution of root heights at a single marginal site 
 * @author brendano
 *
 */
public class MarginalRootHeightLogger extends PropertyLogger {

	Histogram histo = new Histogram(0, 10.0, 100);
	ARG arg;
	int site;
	
	public MarginalRootHeightLogger(ARG arg, int burnin, int collectionFrequency, int site) {
		super(burnin, collectionFrequency);
		this.arg = arg;
		this.site = site;
	}

	public MarginalRootHeightLogger(Map<String, String> attrs, ARG arg) {
		super(attrs); //Burnin, collection frequency, and file name are set in base class
		this.arg = arg;
	}

	@Override
	public String getSummaryString() {
		String str = "Distribution of marginal root heights at site " + site + "\n";
		str += histo.toString();
		return str;
	}

	@Override
	public void addValue(int stateNumber) {
		histo.addValue( TreeUtils.createMarginalTree(arg, site).getHeight());
	}

}
