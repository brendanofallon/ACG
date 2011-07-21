package math;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A histogram for which you don't need to specify the min and max initially. This collects the
 * first 1000 values in a list, then computes the min and max based on the list, makes a real 
 * histogram, and dumps the list and all subsequent values into the histogram
 * @author brendan
 *
 */
public class LazyHistogram {

	Histogram histo;
	int triggerSize = 500;
	List<Double> vals = new ArrayList<Double>(500);
	final int bins;
	int count = 0;
	
	//Gets set to true when the trigger size has been reached
	boolean triggerReached = false;
	
	
	public LazyHistogram(int bins) {
		this.bins = bins;
	}
	
	public void addValue(double val) {
		count++;
		
		if (! triggerReached) {
			if (vals.size() < triggerSize) {
				vals.add(val);
				return;
			}
			if (vals.size() == triggerSize) {
				makeHistoFromList();
				vals.clear();
				histo.addValue(val);
				triggerReached = true;
				return;
			}
		}
		else {
			histo.addValue(val);
		}
	}

	
	public double getMean() {
		if (vals.size()>0) {
			double sum = 0;
			for(Double val : vals)
				sum += val;
			return sum / (double)vals.size();
		}
		else
			return histo.getMean();
	}
	
	public String toString() {
		if (histo == null)
			return "Lazy histogram has not been reached the trigger value yet (size is " + vals.size() + ")";
		else
			return histo.toString();
	}
	
	/**
	 * Infer min and max histogram values, then dump all values into the histogram
	 */
	private void makeHistoFromList() {
		Collections.sort(vals);
		double min = vals.get(0);
		double max = vals.get(vals.size()-1);
		
		if (min > 0 && min < (max-min)/2.0) 
			min = 0;
		else {
			min = Math.floor(min*0.5 * 10000) / 10000.0;
		}
		
		max = Math.floor(max*1.5 * 10000) / 10000.0;
		
		histo = new Histogram(min, max, bins);
		
		for(Double val : vals)
			histo.addValue(val);
		
	}
	
	
	/**
	 * Returns the x-value of the first bin for which the sum of the frequencies in all bins with 
	 * lower indices is greater than the given argument. If the hpd is 0.05, for instance, this
	 * returns the lower 95% confidence boundary
	 * @param hpd
	 * @return
	 */
	public double lowerHPD(double hpd) {
		if (histo == null)
			makeHistoFromList();
		
		return histo.lowerHPD(hpd);
	}
	
	public double upperHPD(double hpd) {
		if (histo == null)
			makeHistoFromList();
		
		return histo.upperHPD(hpd);
	}
}
