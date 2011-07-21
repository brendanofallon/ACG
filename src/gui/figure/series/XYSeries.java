package figure.series;


import element.Point;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;


/**
 * A list of x,y values with a few drawing options that are relicts of a more... civilized era
 * This should be rehashed at some point so it doesn't rely in element.Point and can be constructed in a more
 * general manner. 
 * 
 *  These are immutable, which allows for some additional optimizations. 
 * 
 * @author brendan
 *
 */
public class XYSeries extends AbstractSeries {

	//TODO : Use of List here is pointless since these are immutable, array of Point would be much faster
	//and have less overhead, but would require somewhat significant refactoring, and the current
	//implementation appears to be speedy enough despite this shortcoming 
	protected Point[] pointList;

	public XYSeries(List<Point> points, String name) {
		this.name = name;
		constructPointsFromList(points);
	}
	
	public XYSeries(List<Point> points) {
		this(points, "Untitled series");
	}
	
	public XYSeries(Point[] points, String name) {
		this.pointList = points;
		sortByX();
		this.name = name;
	}
	
	/**
	 * Constructs a new array of points from the given list of points. 
	 * @param points
	 */
	protected void constructPointsFromList(List<Point> points) {
		pointList = new Point[points.size()];
		int index = 0;
		for(Point p : points) {
			pointList[index] = p;
			index++;
		}
		sortByX();
	}
	
	/**
	 * Sorts the values in X order
	 */
	private void sortByX() {
		Arrays.sort(pointList, getXComparator());
	}

	/**
	 * Return the x-value of the point with the given index
	 * @param index
	 * @return
	 */
	public double getX(int index) {
		return pointList[index].x;
	}
	
	/**
	 * return y-yalue of point at given index
	 * @param index
	 * @return
	 */
	public double getY(int index) {
		return pointList[index].y;
	}
	
	
	public Point[] getLineForXVal(double xVal) {
		int lower = getIndexForXVal(xVal);
		
		if (lower<0 || lower>=(pointList.length-1))
			return null;
		
		int upper = lower+1;
		Point[] line = new Point[2];
		line[0] = pointList[lower];
		line[1] = pointList[upper];
		return line;
	}

	/**
	 * Returns the index with the highest x found such that points.get(index).x < xVal. 
	 * Since the x values are sorted, we can use a bisection search. Additionally, since we
	 * usually expect x-values to be linearly increasing, we can make an educated guess about
	 * what the right index is at the start
	 */
	public int getIndexForXVal(double xVal) {
		int upper = pointList.length-1;
		int lower = 0;
		
		//TODO Are we sure an arrays.binarySearch(pointList, xVal) wouldn't be a better choice here?
		//it can gracefully handle cases where the key isn't in the list of values...
		double stepWidth = (pointList[pointList.length-1].x-pointList[0].x)/(double)pointList.length;
		
		int index = (int)Math.floor( (xVal-pointList[0].x)/stepWidth );
		//System.out.println("Start index: " + index);
		
		//Check to see if we got it right
		if (index>=0 && (index<pointList.length-1) && pointList[index].x < xVal && pointList[index+1].x>xVal) {
			//System.out.println("Got it right on the first check, returning index : " + index);
			return index;
		}
		
		//Make sure the starting index is sane (between upper and lower)
		if (index<0 || index>=pointList.length )
			index = (upper+lower)/2; 
		
		if (xVal < pointList[0].x) {
			return 0;
		}
			
		if(xVal > pointList[pointList.length-1].x) {
			return pointList.length-1;
		}
		
		int count = 0;
		while( upper-lower > 1) {
			//System.out.println("Step : " + count + " upper: " + upper + " lower: " + lower + " index: " + index);
			if (xVal < pointList[index].x) {
				upper = index;
			}
			else
				lower = index;
			index = (upper+lower)/2;
			count++;
		}
		
		return index;
	}
	
	/**
	 * Returns the Point with the highest index found such that points.get(index).x < xVal. 
	 * Since the x values are sorted, we can use a bisection search. Additionally, since we
	 * usually expect x-values to be linearly increasing, we can make an educated guess about
	 * what the right index is at the start
	 */
	public Point getClosePointForXVal(double xVal) {
		int index = getIndexForXVal(xVal);
		
		if (index<0 || index>=pointList.length)
			return null;
		else
			return pointList[index];
	}
	
	
	/**
	 * Return the minimum x value in the list. Since the list is sorted, this is always the x-val of the first 
	 * point in the list. 
	 * @return
	 */
	public double getMinX() {
		if (pointList.length==0) {
			return 0;
		}
		return pointList[0].x;
	}
	
	public double getMinY() {
		if (pointList.length==0) {
			return 0;
		}
		double min = pointList[0].y;
		for(int i=0; i<pointList.length; i++)
			if (min>pointList[i].y)
				min = pointList[i].y;
		return min;
	}
	
	/**
	 * Return the maximum x-value in the list. Since the list is sorted by x-value, this is always the x-val
	 * of the last point in the list. 
	 * 
	 * @return
	 */
	public double getMaxX() {
		if (pointList.length==0) {
			return 0;
		}
		return pointList[pointList.length-1 ].x;
	}
	
	public double getMaxY() {
		if (pointList.length==0) {
			return 0;
		}
		double max = pointList[0].y;
		for(int i=0; i<pointList.length; i++)
			if (max<pointList[i].y)
				max = pointList[i].y;
		return max;
	}
	
	/**
	 * Return the greatest x-val such that the y-val of all subsequent elements is zero. Useful for
	 * some data sets which tend to generate long lists of 0-valued points (such as allele frequency spectra)
	 * @return
	 */
	public int lastNonZero() {
		int i;
		for(i=pointList.length-1; i>=0; i--) {
			if (pointList[i].y > 0)
				return i;
		}
		return 0;
	}
	
	/**
	 * The number of points in the list
	 */
	public int size() {
		return pointList.length;
	}

	
	/**
	 * Returns the Point at the given index in this list of points, or null if  i> this.size()
	 * @param i
	 * @return
	 */
	public Point get(int i) {
		if (i>=pointList.length)
			return null;
		else {
			return pointList[i];
		}
	}
	
	private Comparator<Point> getXComparator() {
		return new XComparator();
	}

	class XComparator implements Comparator<Point> {

		public int compare(Point a, Point b) {
			return a.x > b.x ? 1 : -1;
		}
		
	}
	
}
