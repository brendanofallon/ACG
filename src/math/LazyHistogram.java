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


package math;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A histogram for which you don't need to specify the min and max initially. This collects the
 * first 'triggerSize' values in a list, then computes the min and max based on the list, makes a real 
 * histogram, and dumps the list and all subsequent values into the histogram
 * @author brendan
 *
 */
public class LazyHistogram {

	Histogram histo;
	int triggerSize = 500;
	List<Double> vals = Collections.synchronizedList(new ArrayList<Double>(500));
	final int bins;
	int count = 0;
	
	//Gets set to true when the trigger size has been reached
	boolean triggerReached = false;
	
	
	public LazyHistogram(int bins) {
		this.bins = bins;
	}
	
	public synchronized void addValue(double val) {
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

	
	/**
	 * Return the point at the (approximate) middle of the probability mass
	 * @return
	 */
	public double getMedian() {
		return lowerHPD(0.4999);
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
	
	public double getMax() {
		if (histo == null)
			return Double.NaN; //Trigger not reached
		else 
			return histo.getMax();
	}
	
	public double getMoreThanMax() {
		if (histo == null)
			return Double.NaN; //Trigger not reached
		else 
			return histo.moreThanMax;
	}
	
	/**
	 * Infer min and max histogram values, then dump all values into the histogram
	 */
	private synchronized void makeHistoFromList() {
		Collections.sort(vals);
		double min = 0;
		double max = 1;
		if (vals.size()>0) {
			min = vals.get(0);
			max = vals.get(vals.size()-1);

			if (min > 0 && min < (max-min)/2.0) 
				min = 0;
			else {
				min = Math.floor(min*0.5 * 10000) / 10000.0;
			}

			max = Math.floor(max*1.5 * 10000) / 10000.0;
		}
		histo = new Histogram(min, max, bins);
		
		for(Double val : vals)
			histo.addValue(val);
		System.out.println("Dumping values to histo, values size is: " + vals.size() + " min: " + min + " max: " + max);
	}
	
	/**
	 * Returns true if the trigger size has been reached and the boundaries have been 
	 * inferred for this histogram
	 * @return
	 */
	public boolean triggerReached() {
		return triggerReached;
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
