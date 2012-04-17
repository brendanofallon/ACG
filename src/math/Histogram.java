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

import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * A list of bins of values. The number of bins, bin spacing, histogram minimum and maximum are all set once
 * when the histogram is created. addValue(...) increases the count of the appropriate bin, and toString() emits
 * a decently formatted summary. This also tracks the number of values added, their sum (so that the true mean can
 * be computed), and the current running standard deviation of the values
 * 
 * @author brendan
 *
 */
public class Histogram {

	//Used for platform-specific writing of histograms - this is user facing since it's used 
	//to create the log files for some property loggers
	public static final String lineSep = System.getProperty("line.separator");
	double[] hist;
	double minValue;
	double maxValue;
	double binSpacing;
	
	//The minimum and maximum of all values that have been added to this histogram
	double minValueAdded = Double.NaN;
	double maxValueAdded = Double.NaN;
	
	//Index of the bin with the maximum count
	int maxCountBin = -1;
	
	double lessThanMin = 0;
	double moreThanMax = 0;
	
	double currentSum = 0;
	
	//We also maintain a running tally of the standard deviation, computing using an algorithm
	//from the wikipedia page on standard deviation
	double currentStdev = 0;
	
	int count = 0;
	
	NumberFormat formatter = new DecimalFormat("0.0###");

	public Histogram(double minValue, double maxValue, int bins) {
		this.binSpacing = (maxValue - minValue)/(double)bins;
		this.minValue = minValue;
		this.maxValue = maxValue;
		hist = new double[bins];
	}
	
	/**
	 * Create a new histogram with the given data provided
	 * @param hist 
	 * @param binWidth
	 * @param histoMin
	 * @param sum
	 * @param count
	 * @param moreThanMax
	 * @param lessThanMin
	 */
	public Histogram(double[] hist, double binWidth, double histoMin, double sum, double count, double moreThanMax, double lessThanMin) {
		this.hist = hist;
		this.binSpacing = binWidth;
		this.minValue = histoMin;
		this.currentSum = sum;
		this.count = (int)count;
		this.moreThanMax = moreThanMax;
		this.lessThanMin = lessThanMin;
		this.maxValue = hist.length * binWidth;
		this.minValueAdded = minValue;
		this.maxValueAdded = maxValue;
		findMaxCountBin();
	}
	
	/**
	 * Set the value of maxCountBin to the correct bin by searching all bins for the
	 * one with the highest count
	 */
	private void findMaxCountBin() {
		double max = 0;
		for(int i=0; i<hist.length; i++) {
			if (hist[i] > max) {
				maxCountBin = i;
				max = hist[i];
			}
		}
	}
	
	/**
	 * Return the number of bins in this histogram
	 * @return
	 */
	public int getBinCount() {
		return hist.length;
	}
	
	/**
	 * Return the total number of times addValue has been called
	 * @return
	 */
	public int getCount() {
		return count;
	}
	
	public double getMin() {
		return minValue;
	}
	
	public double getMax() {
		return maxValue;
	}
	
	/**
	 * Return the number of values added that were greater than the maximum value
	 * @return
	 */
	public double getMoreThanMax() {
		return moreThanMax;
	}
	
	/**
	 * Return the number of values added that were less than the minimum value
	 * @return
	 */
	public double getLessThanMin() {
		return lessThanMin;
	}
	
	/**
	 * Returns a reference to the raw count data of this histogram. Altering the contents
	 * will invalidate this histogram
	 * @return
	 */
	public double[] getData() {
		return hist;
	}
	
	/**
	 * Get the sum of all of the values that have been added
	 * @return
	 */
	public double getSum() {
		return currentSum;
	}
	
	/**
	 * Set all counts in the histogram to zero
	 */
	public void clear() {
		for(int i=0; i<hist.length; i++) {
			hist[i] = 0;
		}
		count = 0;
		moreThanMax = 0;
		lessThanMin = 0;
		currentSum = 0;
		currentStdev = 0;
	}
	
	
//	public void removeValue(double val) {
//		double prevMean = getMean(); //Needed to compute running stdev
//		count--;		
//		currentSum -= val;
//		
//		if (count>1)
//			currentStdev += (val-prevMean)*(val-getMean());
//		if (val<minValue) {
//			lessThanMin--;
//			return;
//		}
//		if (val>=maxValue) {
//			moreThanMax--;
//			return;
//		}
//		
//		int bin = getBin(val);
//		hist[ bin ]--;
//	}
	
	/**
	 * The minimum of all values added via addValue(x)
	 * @return
	 */
	public double getMinValueAdded() {
		return minValueAdded;
	}
	
	/**
	 * The maximum of all values added via addValue(x)
	 * @return
	 */
	public double getMaxValueAdded() {
		return maxValueAdded;
	}
	
	public void addValue(double val) {
		double prevMean = getMean(); //Needed to compute running stdev
		if (Double.isNaN(minValueAdded) || val < minValueAdded)
			minValueAdded = val;
		
		if (Double.isNaN(maxValueAdded) || val > maxValueAdded)
			maxValueAdded = val;
		
		count++;		
		currentSum += val;
		
		if (count>1)
			currentStdev += (val-prevMean)*(val-getMean());
		if (val<minValue) {
			lessThanMin++;
			return;
		}
		if (val>=maxValue) {
			moreThanMax++;
			return;
		}
		
		int bin = getBin(val);
		hist[ bin ]++;
		
		if (maxCountBin < 0 || hist[bin] > hist[maxCountBin]) {
			maxCountBin = bin;
		}
	}
	
	/**
	 * Returns the bin that the given value would fall into. Doesn't do error checking, so this value
	 * may be negative or >= getBinCount()
	 * @param val
	 * @return
	 */
	public int getBin(double val) {
		return (int)Math.floor( (val-minValue)/(maxValue-minValue)*(double)hist.length );
	}
	
	public double getMean() {
		return currentSum / (double)count;
	}
	
	/**
	 * Returns the frequency of the bin with the greatest frequency
	 * @return
	 */
	public double getMaxCount() {
		if (maxCountBin < 0)
			return 0;
		else
			return hist[maxCountBin];
//		double max = 0;
//		for(int i=0; i<hist.length; i++) {
//			if (hist[i] > max) {
//				max = hist[i];
//			}
//		}
//		return max;
	}
	
	/**
	 * Returns the x-value of the first bin for which the sum of the frequencies in all bins with 
	 * lower indices is greater than the given argument. If the hpd is 0.05, for instance, this
	 * returns the lower 95% confidence boundary
	 * @param hpd
	 * @return
	 */
	public double lowerHPD(double hpd) {
		double lessThanMinDensity =  (double)lessThanMin / (double)count;
		double density = lessThanMinDensity;
		if (density > hpd)
			return minValue;
		int bin = 0;
		while(density < hpd) {
			double binDensity = getFreq(bin);
			density += binDensity;
			bin++;
		}
		return minValue + bin*getBinWidth();
	}
	
	public double upperHPD(double hpd) {
		return lowerHPD( 1.0 - hpd ); // We start counting from bottom
	}
	
	/**
	 * This actually returns an exact standard deviation - it's not computed from the bins frequencies; it's kept as a running
	 * value from all of the values added. 
	 * @return
	 */
	public double getStdev() {
		return Math.sqrt( currentStdev / (double)count );
	}
	
	public double getBinWidth() {
		return binSpacing;
	}
	
	public double getFreq(int whichBin) {
		if (whichBin>=0 && whichBin<hist.length) {
			return hist[whichBin]/(double)count; 
		}
		
		return Double.NaN;
	}

	
	public double getCount(int whichBin) {
		if (whichBin>=0 && whichBin<hist.length) {
			return hist[whichBin]; 
		}
		
		return Double.NaN;
	}
	
	/**
	 * Returns the approximate x-value which divides the mass in half
	 * @return
	 */
	public double getMedian() {
		return lowerHPD(0.49999);
	}
	
	public String toString() {
		StringBuilder str = new StringBuilder();
		if (count == 0) {
			str.append("(no data collected)");
		}
		else {
			str.append(" < " + minValue + " : " + formatter.format((double)lessThanMin/(double)count) + " " + lineSep);
			for(int i=0; i<hist.length; i++) {
				str.append(formatter.format(i*binSpacing+minValue) + "\t" + formatter.format(hist[i]/(double)count) + " " + lineSep);
			}
			str.append(" > " + maxValue + " : " + formatter.format((double)moreThanMax/(double)count) + " " + lineSep);
			
		}
		return str.toString();
	}
}
