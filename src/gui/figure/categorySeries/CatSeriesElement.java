package figure.categorySeries;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import element.Point;
import figure.Figure;
import figure.FigureElement;
import figure.series.AxesElement;
import figure.series.CategorySeries;
import figure.series.SeriesElement;
import figure.series.SeriesFigure;
import figure.series.XYSeries;
import figure.series.XYSeriesElement;

public class CatSeriesElement extends XYSeriesElement {

	figure.series.CategorySeries data;
	
	public CatSeriesElement(SeriesFigure parent, AxesElement axes, CategorySeries data) {
		super(null, axes, parent);
		this.data = data;
		series = data;
	
		//Iterator<String> kit = data.getIterator();
		ArrayList<Point> pointList = new ArrayList<Point>();
		int count = 0;
		for(int i=0; i<data.size(); i++) {
			pointList.add(new Point(i, data.getDataPoint(i)));		
		}
		
		xySeries = new XYSeries(pointList);
		
		currentMode = BOXES;
	}

	/**
	 * Return the list of x labels that was supplied with this series data
	 * @return
	 */
	public List<String> getXLabels() {
		return data.getLabels();
	}
	
	/**
	 * Re-constructs the list of points to match the order given by the reference list of labels. 
	 * Labels which aren't in the ref list appear in no particular order at the end of the
	 * list. New points are created with a value of zero if there are items in ref that
	 * are not in the current list. 
	 * @param ref The list of reference labels that we attempt to match the current list to. 
	 */
//	public void reorder(List<String> ref) {
//		ArrayList<Point> pointList = new ArrayList<Point>();
//		int count = 0;
//		
//		//keep track of the labels that we rearrange so we know which ones are
//		//left over after we re arrange (in case ref doesn't include every label here)
//		List<String> currentLabels = new ArrayList<String>();
//		currentLabels.addAll(xLabels);
//		
//		for(String refLabel : ref) {
//			Double val = data.get(refLabel);
//			if (val!=null) {
//				pointList.add(new Point(count, val));
//				currentLabels.remove(refLabel);
//			}
//			else {
//				pointList.add(new Point(count, 0));
//			}
//			
//			xLabels.add(refLabel);
//			count++;
//		}
//		
//		
//		//There may be some points in the original list that didn't
//		//have labels in the ref list and hence weren't reordered, these
//		//have labels left over in current labels
//		for(String leftOverLabel : currentLabels) {
//			Double val = data.get(leftOverLabel);
//			if (val!=null) { //It should never be null, but you never know...
//				pointList.add(new Point(count, val));
//				xLabels.add(leftOverLabel);
//				count++;
//			}
//		}
//		
//		xySeries = new XYSeries(pointList);
//	}
	
	@Override
	/**
	 * Returns the largest Y value among all members of this series. 
	 */
	public double getMaxY() {
		List<String> labels = data.getLabels();
		Double max = Double.MIN_VALUE;
		for(String key : labels) {
			double val = data.get(key); 
			if ( val > max ) {
				max = val;
			}
		}
		return max;
	}

	@Override
	/**
	 * Returns the smallest y value among all members of this series
	 */
	public double getMinY() {
		List<String> labels = data.getLabels();
		Double min = Double.MAX_VALUE;
		for(String key : labels) {
			double val = data.get(key); 
			if ( val < min ) {
				min = val;
			}
		}
		return min;
	}

	@Override
	/**
	 * Returns the number of elements in the data series.
	 */
	public double getMaxX() {
		return data.size()+0.5;
	}

	@Override
	public double getMinX() {
		return 0;
	}

	

}
