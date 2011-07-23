package gui.figure.series;

import math.Histogram;

/**
 * A figure that displays a collection of values as a histogram. Right now we just support a single
 * series. 
 * 
 * @author brendan
 *
 */
public class HistogramFigure extends XYSeriesFigure {

	protected int bins = 100;
	protected double histoMin = 0;
	protected double histoMax = 1;
	
	//Minimum and maximum of values that have been added 
	protected double valueMin = Double.POSITIVE_INFINITY;
	protected double valueMax = Double.NEGATIVE_INFINITY;
	
	protected boolean hasUserMin = false;
	protected boolean hasUserMax = false;
	
	
	//Contains list of raw values, not a histogram
	XYSeries values = null;
	
	//The actual histogram, does not store raw values
	HistogramSeries histoSeries = null;
	
	public HistogramFigure() {
		
	}
	
	public void addValue(double val) {
		if (val < valueMin)
			valueMin = val;
		if (val > valueMax)
			valueMax = val;
		
		if (values == null)
			values = new XYSeries("Values");
		if (histoSeries == null) {
			histoSeries = new HistogramSeries("Frequency", bins, valueMin, valueMax);
			recalculateBins();
		}
		
	}
	
	public void setBinNumber(int newBins) {
		this.bins = newBins;
		recalculateBins();
	}
	
	/**
	 * Reconvert the values in the values list into the histogram 
	 */
	protected void recalculateBins() {
		histoSeries.replace(values.pointList, bins, valueMin, valueMax);
		this.inferBoundsPolitely();
		repaint();
	}
}
