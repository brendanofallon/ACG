package dlCalculation;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import arg.SiteRange;

/**
 * Maintains a sorted list of SiteRanges which represents the ranges of sites where 'final' (rootmost) coalescence is reached.  
 * @author brendano
 *
 */
public class SortedSiteRangeList {

	private LinkedList<SiteRange> ranges = new LinkedList<SiteRange>();
		
	private int maxSite;
	
	private final boolean verbose = false;
	
	public SortedSiteRangeList(int max) {
		maxSite = max;
	}
	
	/**
	 * Clear all elements from the list of ranges
	 */
	public void clear() {
		ranges.clear();
	}
	
	
	/**
	 * Add a new range to the list of root ranges tracked by this object
	 * @param begin First site of range to attemp to add
	 * @param end Last site (exclusive) of range to attempt to add
	 * @param id ComputeNode id of range to add
	 * @return True if this range is 'complete', if all sites are associated with a range
	 */
	public boolean add(int begin, int end, int id) {
		if (verbose)
			System.out.println("Adding range " + new SiteRange(begin, end, id) + " to root range list");
		
		if (ranges.size()==0) {
			ranges.add(new SiteRange(begin, end, id));
			if (verbose)
				System.out.println("Root range list is now \n " + this);
			return (begin==0 && end==maxSite);
		}
		
		
		SiteRange el = null;
		boolean inserted = false;
		int prevMax = 0;
		int nextMin;
		
		boolean complete = true; 
		
		ListIterator<SiteRange> it = null;
		
		
		for(it = ranges.listIterator(); it.hasNext(); ) {
			el = it.next();
			nextMin = el.getMin();
			
			//Insert the new range at the spot preceding the iterator
			if (begin >= prevMax && end <= nextMin) {
				el = it.previous();
				complete = complete ? begin == prevMax : false;				
				prevMax = end;
				it.add(new SiteRange(begin, end, id));
				el = it.next();
				inserted = true;
			}
			
			

			complete = complete ? nextMin == prevMax : false;
			
			if (inserted && !complete) //If we already inserted the range and we're not complete, don't bother looking any further
				return false;
			
			prevMax = el.getMax();
		}
		
		if (!inserted && begin >= prevMax) {
			complete = complete ? begin == prevMax : false;		
			
			ranges.add(new SiteRange(begin, end, id));
			prevMax = end;
		} 
				
		complete = complete ? maxSite == prevMax : false;	

		
		if (verbose)
			System.out.println("Root range list is now \n" + this);
		
		return complete;
	}
	
	
	/**
	 * Returns the current list of site ranges
	 * @return
	 */
	public List<SiteRange> getRanges() {
		return ranges;
	}
	
	/**
	 * Returns true if every site in 0..maxSite is associated with a site range
	 * @return
	 */
	public boolean isComplete() {
		if (ranges.size()==0)
			return false;
		
		ListIterator<SiteRange> it = ranges.listIterator();
		SiteRange first = it.next();
		SiteRange next = null;
		
		if (first.getMin() != 0) {
			return false;
		}
		else {
			//first.getMin is 0
			if (first.getMax()==maxSite) {
				return true;
			}
		}
		
		int max = first.getMax();
		while(it.hasNext()) {
			next = it.next();
			
			//Seems like a good place to do a bit of error checking...
			if (next.getMin() < first.getMax() || next.intersects(first)) {
				throw new IllegalStateException("Root ranges are not coherent");
			}
			
			if (next.getMin() != first.getMax()) {
				return false;
			}
			first = next;
			max = next.getMax();
		}
		
		if (max != maxSite) {
			return false;
		}
		
		return true;
	}
	
	public String toString() {
		StringBuilder b = new StringBuilder();
		for(SiteRange r : ranges) {
			b.append(r.getRefNodeID() + " : " + r.getMin() + " .. " + r.getMax() + "\n");
		}
		return b.toString();
	}
	
//	public static void main(String[] args) {
//		SiteRange a = new SiteRange(10, 250, 0);
//		SiteRange b = new SiteRange(256, 600, 1);
//		SiteRange c = new SiteRange(0, 700, 2);
//		SiteRange d = new SiteRange(300, 1000, 3);
//		SortedSiteRangeList list = new SortedSiteRangeList(1000);
//		
//		list.add(a);
//		list.add(b);
//		list.add(c);
//		list.add(d);
//		
//		System.out.println("The list : \n" + list);
//		
//		if (list.isComplete()) {
//			System.out.println("List is 'complete'");
//		}
//		else {
//			System.out.println("List is not complete.");
//		}
//	}
}
