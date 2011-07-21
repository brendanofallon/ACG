package logging;

import java.util.Map;

import parameter.AbstractParameter;
import xml.XMLUtils;
import math.Histogram;

/**
 * A class that listens to an mcmc and logs a particular value 
 * obtained from a parameter and stores the results in a histogram. 
 * 
 * @author brendan
 *
 */
public class HistogramCollector extends PropertyLogger {

	public static final String HIST_MIN = "min";
	public static final String HIST_MAX = "max";
	public static final String BINS = "bins";

	
	Histogram histo;
	AbstractParameter<?> param;
	String logKey;

	public HistogramCollector( AbstractParameter<?> param, String itemName, int collectionFrequency, double histMin, double histMax, int bins) {
		super(5000000, collectionFrequency);
		histo = new Histogram(histMin, histMax, bins);
		this.param = param;
		this.logKey = itemName;
		this.collectionFrequency = collectionFrequency;
	}
	
	public HistogramCollector(Map<String, String> attrs, AbstractParameter<?> param, String itemName, double histMin, double histMax, int bins) {
		super(attrs);
		
		histo = new Histogram(histMin, histMax, bins);
		this.param = param;
		this.logKey = itemName;
	}
	
	public HistogramCollector(Map<String, String> attrs, AbstractParameter<?> param, String itemName) {
		super(attrs);

		Double histMin = XMLUtils.getDoubleOrFail(HIST_MIN, attrs);
		Double histMax = XMLUtils.getDoubleOrFail(HIST_MAX, attrs);
		Integer bins = XMLUtils.getOptionalInteger(BINS, attrs);
		if (bins == null)
			bins = 100;
		
		histo = new Histogram(histMin, histMax, bins);
		this.param = param;
		this.logKey = itemName;
	}
		
	/**
	 * Add current value of parameter to the histogram
	 */
	public void addValue(int stateNumber) {
		Object val = param.getLogItem(logKey);
		if (val instanceof Integer)
			histo.addValue( (Integer)param.getLogItem(logKey));
		if (val instanceof Double) {
			Double dVal = (Double)param.getLogItem(logKey);
			//System.out.println("Histo is adding value of : " + dVal);
			histo.addValue( dVal);
		}
	}

	@Override
	public String getSummaryString() {
		StringBuilder strB = new StringBuilder();
		
		strB.append("\n Histogram of values for parameter " + param.getName() + " item: " + logKey + "\n");
		if (histo.getCount()==0) {
			strB.append("Histogram is empty, burnin (" + burnin + " states) has probably not been exceeded.");
		}
		else {
			strB.append( histo.toString() );
			strB.append("Mean value: "+  histo.getMean() + "\n");
			strB.append("Count : " + histo.getCount() + "\n");
		}
		
		return strB.toString();
	}

	

}
