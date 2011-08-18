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

	double[] hist;
	double minValue;
	double maxValue;
	double binSpacing;
	
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
	
	public void addValue(double val) {
		double prevMean = getMean(); //Needed to compute running stdev
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
		double max = 0;
		for(int i=0; i<hist.length; i++) {
			if (hist[i] > max) {
				max = hist[i];
			}
		}
		return max;
	}
	
	/**
	 * Returns the x-value of the first bin for which the sum of the frequencies in all bins with 
	 * lower indices is greater than the given argument. If the hpd is 0.05, for instance, this
	 * returns the lower 95% confidence boundary
	 * @param hpd
	 * @return
	 */
	public double lowerHPD(double hpd) {
		if (hpd < 0 || hpd > 0.5) {
			throw new IllegalArgumentException("Invalid density supplied to lowerHPD, value must be between 0 and 0.5, but got " + hpd);
		}
		double lessThanMinDensity =  (double)lessThanMin / (double)count;
		double density = lessThanMinDensity;
		int bin = 0;
		while(density < hpd) {
			double binDensity = getFreq(bin);
			density += binDensity;
			bin++;
		}
		return minValue + bin*getBinWidth();
	}
	
	public double upperHPD(double hpd) {
		if (hpd < 0 || hpd > 0.5) {
			throw new IllegalArgumentException("Invalid density supplied to lowerHPD, value must be between 0 and 0.5, but got " + hpd);
		}
		double moreThanMaxDensity = (double)moreThanMax / (double)count;
		double density = moreThanMaxDensity;
		int bin = hist.length-1;
		while(density < hpd) {
			double binDensity = getFreq(bin);
			density += binDensity;
			bin--;
		}
		return minValue + bin*getBinWidth();
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
			str.append(" < " + minValue + " : " + formatter.format((double)lessThanMin/(double)count) + "\n");
			for(int i=0; i<hist.length; i++) {
				str.append(formatter.format(i*binSpacing+minValue) + "\t" + formatter.format(hist[i]/(double)count) + "\n");
			}
			str.append(" > " + maxValue + " : " + formatter.format((double)moreThanMax/(double)count) + "\n");
			
		}
		return str.toString();
	}
}
