package gui.figure.series;


import java.awt.geom.Point2D;
import java.util.ArrayList;
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

	protected List<Point2D> pointList;

	public XYSeries(List<Point2D> points, String name) {
		this.name = name;
		this.pointList = points;
		sortByX();
	}

	/**
	 * Create a new XY series with the given name and no data
	 * @param name
	 */
	public XYSeries(String name) {
		this.name = name;
		pointList = new ArrayList<Point2D>();
	}
	
	public XYSeries(List<Point2D> points) {
		this(points, "Untitled series");
	}

	
	public void addPointInOrder(Point2D newPoint) {
		if (pointList.size()>0 && newPoint.getX() < getMaxX())
			throw new IllegalArgumentException("Non-increasing x value");
		
		pointList.add(newPoint);
	}
	
	/**
	 * Constructs a new array of points from the given list of points. 
	 * @param points
	 */
//	protected void constructPointsFromList(List<Point2D> points) {
//		pointList = new Point2D[points.size()];
//		int index = 0;
//		for(Point2D p : points) {
//			pointList[index] = p;
//			index++;
//		}
//		sortByX();
//	}
	
	/**
	 * Sorts the values in X order
	 */
	private void sortByX() {
		Collections.sort(pointList, getXComparator());
	}

	/**
	 * Return the x-value of the point with the given index
	 * @param index
	 * @return
	 */
	public double getX(int index) {
		return pointList.get(index).getX();
	}
	
	/**
	 * return y-yalue of point at given index
	 * @param index
	 * @return
	 */
	public double getY(int index) {
		return pointList.get(index).getY();
	}
	
	
	public Point2D[] getLineForXVal(double xVal) {
		int lower = getIndexForXVal(xVal);
		
		if (lower<0 || lower>=(pointList.size()-1))
			return null;
		
		int upper = lower+1;
		Point2D[] line = new Point2D[2];
		line[0] = pointList.get(lower);
		line[1] = pointList.get(upper);
		return line;
	}

	/**
	 * Returns the index with the highest x found such that points.get(index).x < xVal. 
	 * Since the x values are sorted, we can use a bisection search. Additionally, since we
	 * usually expect x-values to be linearly increasing, we can make an educated guess about
	 * what the right index is at the start
	 */
	public int getIndexForXVal(double xVal) {
		int upper = pointList.size()-1;
		int lower = 0;
		
		if (pointList.size()==0)
			return 0;
		
		//TODO Are we sure an arrays.binarySearch(pointList, xVal) wouldn't be a better choice here?
		//it can gracefully handle cases where the key isn't in the list of values...
		double stepWidth = (pointList.get(pointList.size()-1).getX()-pointList.get(0).getX())/(double)pointList.size();
		
		int index = (int)Math.floor( (xVal-pointList.get(0).getX())/stepWidth );
		//System.out.println("Start index: " + index);
		
		//Check to see if we got it right
		if (index>=0 && (index<pointList.size()-1) && pointList.get(index).getX() < xVal && pointList.get(index+1).getX()>xVal) {
			//System.out.println("Got it right on the first check, returning index : " + index);
			return index;
		}
		
		//Make sure the starting index is sane (between upper and lower)
		if (index<0 || index>=pointList.size() )
			index = (upper+lower)/2; 
		
		if (xVal < pointList.get(0).getX()) {
			return 0;
		}
			
		if(xVal > pointList.get(pointList.size()-1).getX()) {
			return pointList.size()-1;
		}
		
		int count = 0;
		while( upper-lower > 1) {
			if (xVal < pointList.get(index).getX()) {
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
	public Point2D getClosePointForXVal(double xVal) {
		int index = getIndexForXVal(xVal);
		
		if (index<0 || index>=pointList.size())
			return null;
		else
			return pointList.get(index);
	}
	
	
	/**
	 * Return the minimum x value in the list. Since the list is sorted, this is always the x-val of the first 
	 * point in the list. 
	 * @return
	 */
	public double getMinX() {
		if (pointList.size()==0) {
			return 0;
		}
		return pointList.get(0).getX();
	}
	
	public double getMinY() {
		if (pointList.size()==0) {
			return 0;
		}
		double min = pointList.get(0).getY();
		for(int i=0; i<pointList.size(); i++)
			if (min>pointList.get(i).getY())
				min = pointList.get(i).getY();
		return min;
	}
	
	/**
	 * Return the maximum x-value in the list. Since the list is sorted by x-value, this is always the x-val
	 * of the last point in the list. 
	 * 
	 * @return
	 */
	public double getMaxX() {
		if (pointList.size()==0) {
			return 0;
		}
		return pointList.get(pointList.size()-1 ).getX();
	}
	
	public double getMaxY() {
		if (pointList.size()==0) {
			return 0;
		}
		double max = pointList.get(0).getY();
		for(int i=0; i<pointList.size(); i++)
			if (max<pointList.get(i).getY())
				max = pointList.get(i).getY();
		return max;
	}
	
	/**
	 * Return the greatest x-val such that the y-val of all subsequent elements is zero. Useful for
	 * some data sets which tend to generate long lists of 0-valued points (such as allele frequency spectra)
	 * @return
	 */
	public int lastNonZero() {
		int i;
		for(i=pointList.size()-1; i>=0; i--) {
			if (pointList.get(i).getY() > 0)
				return i;
		}
		return 0;
	}
	
	/**
	 * The number of points in the list
	 */
	public int size() {
		return pointList.size();
	}

	
	/**
	 * Returns the Point at the given index in this list of points, or null if  i> this.size()
	 * @param i
	 * @return
	 */
	public Point2D get(int i) {
		if (i>=pointList.size())
			return null;
		else {
			return pointList.get(i);
		}
	}
	
	private Comparator<Point2D> getXComparator() {
		return new XComparator();
	}

	class XComparator implements Comparator<Point2D> {

		public int compare(Point2D a, Point2D b) {
			return a.getX() > b.getX() ? 1 : -1;
		}
		
	}
	
}