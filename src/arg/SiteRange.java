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


package arg;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * A class describing a range of sites. Current implementation is very simple, we just describe use a min and max. 
 * Interval is assumed to be half-open, so that max is the next site after the end. If the range is sites 0, 1, and 2,
 * min  0 and max = 3.
 * 
 * This class also contains a handful of methods and functions for testing if two ranges intersect or are adjacent, as
 * well as merging lists of ranges. 
 *  
 * @author brendan
 *
 */
public class SiteRange {
	
	protected int min; //First site in this range
	protected int max; //One beyond last site in this range
	
	//Id of tip or coalescent node to which these sites descend. 
	//Not all SiteRanges use this
	protected int referringNode = -1; 
	
	protected int lChild = -1;
	protected int rChild = -1;
	
	protected static RangeComparator rangeComparator = new RangeComparator();
	
	//Buffer used for some internal calculations
	private static List<SiteRange> tmp0 = new ArrayList<SiteRange>();
	
	public SiteRange(int min, int max) {
		if (max <= min) {
			throw new IllegalArgumentException("Max site range must be strictly greater than min (Got max = " + max + " min = " + min);
		}
		this.min = min;
		this.max = max;
	}

	/**
	 * Construct a new SiteRange with the given min, max and ID of node referring these sites
	 * @param min
	 * @param max
	 * @param refNodeID
	 */
	public SiteRange(int min, int max, int refNodeID) {
		this(min, max);
		this.referringNode = refNodeID;
	}
	
	/**
	 * Identical to this.copy(), returns an complete clone of this object
	 */
	public Object clone() {
		return this.copy();
	}
	
	/**
	 * Identical to clone but doesn't require casting
	 * @return
	 */
	public SiteRange copy() {
		SiteRange r = new SiteRange(min, max, referringNode);
		r.lChild = lChild;
		r.rChild = rChild;
		return r;
	}
	
	public int getRefNodeID() {
		return referringNode;
	}
	
	public void setRefNodeID(int id) {
		referringNode = id;
	}
	
	public void setMin(int newMin) {
		this.min = newMin;
	}
	
	public void setMax(int newMax) {
		this.max = newMax;
	}
	
	public int getMin() {
		return min;
	}
	
	public int getMax() {
		return max;
	}
	
	/**
	 * Returns true if the min and max of this range are equal to the given range.  
	 */
	public boolean equalsRange(SiteRange r) {
		return r.getMin() == min &&
			   r.getMax() == max;
	}
	
	/**
	 * Subtracts from this range whatever is in 'negRange', and adds the remains to the list. The 'negation' is the range or
	 * ranges of sites that DO NOT OVERLAP with the range provided. This may add zero, one, or two 
	 * ranges to the list of ranges. 
	 * This sets the referring node ID of the new ranges to that of this range
	 * @param negRange Range to subtract from this range
	 * @param ranges List to add results to
	 * 
	 */
	public void addNegation(SiteRange negRange, List<SiteRange> ranges) {
		//If negRange totally overlaps there is nothing to add
		if (negRange.getMin()<= min && negRange.getMax()>=max)
			return;
		
		//If they do not intersect at all, then add a single copy of this range and return
		if (! this.intersects(negRange)) {
			ranges.add(this.copy());
			return;
		}
		
		if (negRange.getMin()>min) {
			SiteRange lower = new SiteRange(min, negRange.getMin(), referringNode);
			ranges.add(lower);
		}
		
		if (negRange.getMax()<max) {
			SiteRange upper = new SiteRange(negRange.getMax(), max, referringNode);
			ranges.add(upper);
		}
		
		
	}
	
	/**
	 * Merge sites in this range with those of the given range. Its illegal to do this
	 * with ranges that do not overlap and are not adjacent
	 * @param r
	 */
	public void merge(SiteRange r) {
		if (isMergeable(r)) {
			min = Math.min(r.getMin(), min);
			max = Math.max(r.getMax(), max);
			referringNode = r.getRefNodeID();
		}
		else {
			throw new IllegalArgumentException("Illegal merge: Given range " + r + " cannot be merged with " + this);
		}
	}
	
	
	/**
	 * Returns true if the given range is adjacent to this range and has the same node ref. ID
	 * @param r
	 * @return
	 */
	public boolean isMergeable(SiteRange r) {
		return (isAdjacent(r) 
				&& this.getRefNodeID()==r.getRefNodeID() 
				&& this.lChild == r.lChild 
				&& this.rChild == r.rChild);
	}
	
	
	/**
	 * Returns true if the given range shares a boundary with this range. This is true only if
	 * a.min == b.max or a.max == b.min
	 * @param r
	 * @return
	 */
	public boolean isAdjacent(SiteRange r) {
		return ((r.getMin() == max) || (r.getMax() == min)) ? true : false;
	}
	
	
	/**
	 * Returns true if any part of the given range overlaps this range. Note that if range a = [0..5) and
	 * range b = [5..10), they do NOT overlap
	 * @param r
	 * @return
	 */
	public boolean intersects(SiteRange r) {
		return r.getMax()<= min || r.getMin()>= max ? false : true;
	}

	/**
	 * Returns true if any part of the given range overlaps this range. Note that if range a = [0..5) and
	 * range b = [5..10), they do NOT overlap
	 * @param r
	 * @return
	 */
	public boolean intersects(SiteRangeList l) {
		return l.getMax() <= min || l.getMin()>=max ? false : true;
	}
	
	/**
	 * Returns true if the site is >= min and strictly < max
	 * @param site
	 * @return
	 */
	public boolean contains(int site) {
		if (site>=min && site<max) {
			return true;
		}
		return false;
	}
	
	public boolean equals(Object obj) {
		if (! (obj instanceof SiteRange) ) {
			return false;
		}
		
		SiteRange comp = (SiteRange)obj;
		return (comp.getMin()==getMin() && comp.getMax()==getMax());
	}
	
	public String toString() {
		return "[" + getMin() + " .. " + getMax() + ") id=" + referringNode;
	}
	
	
	
	/**************************** Static Utilities for unions, intersections, etc **********************************/
	
	/**
	 * Create a return a new SiteRange that is the union of both ranges. Its illegal to do this with
	 * ranges that are not adjacent and do not intersect
	 * @param a
	 * @param b
	 * @return
	 */
	public static SiteRange merge(SiteRange a, SiteRange b) {
		if (a.intersects(b) || a.isAdjacent(b)) {
			SiteRange m = new SiteRange(Math.min(a.getMin(), b.getMin()), Math.max(a.getMax(), b.getMax()));
			return m;
		}
		else {
			throw new IllegalArgumentException("Cannot merge range " + a + " with range " + b);
		}
	}
	
	/**
	 * Remove the given range from the list of ranges. 
	 * Ranges is assumed to be a list of non-overlapping, nonincreasing ranges.
	 * @param ranges List of ranges to remove range from
	 * @param removeRange range to subtract from all ranges
	 */
	public static void removeRange(List<SiteRange> ranges, SiteRange removeRange) {
		tmp0.clear();
		tmp0.addAll(ranges);
		ranges.clear();
		for(SiteRange r : tmp0) {
			r.addNegation(removeRange, ranges);
		}
	}

	
	/**
	 * Construct a new list of ranges that is the intersection of the ranges provided. Each list
	 * is assumed to contain non-overlapping ranges in strictly increasing order
	 * @param a Nonoverlapping, nonincreasing list of ranges
	 * @param b
	 * @return List of SiteRanges representing intersection of aRange and bRange
	 */
	public static List<SiteRange> intersect(List<SiteRange> a, List<SiteRange> b, int refNode) {
		List<SiteRange> intersection = new ArrayList<SiteRange>();
		if (a.size()==0 || b.size()==0)
			return intersection;
		
		//We use a O(N^2) algorithm for this, but I don't think we're ever likely to see more than a handful
		//of ranges in either list, so it's probably not too big of a hit. Some shortcutting is also 
		//possible here which should help keep things reasonably fast
		int minB = b.get(0).getMin();
		int maxB = b.get( b.size()-1).getMax();
		SiteRange totalBRange = new SiteRange(minB, maxB);
		
		for(int i=0; i<a.size(); i++) {
			SiteRange aRange = a.get(i);
			if (aRange.intersects(totalBRange)) {
				for(int j=0; j<b.size(); j++) {
					SiteRange bRange = b.get(j);
					if (bRange.getMin() > aRange.getMax())
						break;
					if (aRange.intersects(bRange)) {
						SiteRange inter = SiteRange.intersection(aRange, bRange, refNode);
						intersection.add( inter );
					}
				}
			}
		}
		
		return intersection;
	}
	
	/**
	 * Returns a new List of SiteRanges that is the union of everything in a and everything in b.
	 * Note that the range lists are assumed to contain ONLY NON-INTERSECTING, STRICTLY INCREASING ranges
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public static List<SiteRange> mergeRanges(List<SiteRange> a, List<SiteRange> b) {
		List<SiteRange> merger = new ArrayList<SiteRange>();
		
		int start = nextStart(a, b, -1); //Must start at -1 so we find site 0
		while(start < Integer.MAX_VALUE) {
			int end = findEndpoint(a, b, start);
			merger.add(new SiteRange(start, end));
			start = nextStart(a, b, end);
		}
		return merger;
	}

	/**
	 * Obtain the index of the next site at which a range starts which is greater than the given
	 * start site. As usual, each range list is assumed to contain no intersections and to be in
	 * increasing range order
	 * @param a
	 * @param b
	 * @param start
	 * @return
	 */
	private static int nextStart(List<SiteRange> a, List<SiteRange> b, int start) {
		int minA = Integer.MAX_VALUE;
		for(int i=0; i<a.size(); i++) {
			SiteRange r = a.get(i);
			if (r.getMin()>start) {
				minA = r.getMin();
				break;
			}
		}
		
		int minB = Integer.MAX_VALUE;
		for(int i=0; i<b.size(); i++) {
			SiteRange r = b.get(i);
			if (r.getMin()>start) {
				minB = r.getMin();
				break;
			}
		}
		
		return Math.min(minA, minB);
	}
	
	
	/**
	 * Scan the given list to ensure that its ranges are non-overlapping and strictly increasing
	 * @param a List of ranges to test. Throws IllegalStateException if list is not valid
	 */
	public static void listIsValid(List<SiteRange> a) {
		
		for(int i=0; i<a.size()-1; i++) {
			if (a.get(i).intersects( a.get(i+1)) ) {
				throw new IllegalStateException("Range list is not valid");
			}
			if (a.get(i).getMin() >= a.get(i+1).getMin()) {
				throw new IllegalStateException("Range list is not valid");
			}
		}

	}
	
	/**
	 * Helper for mergeRanges, scan along sites beginning at 'start', returns the smallest index
	 * such that index > start and no SiteRange in either list overlaps index. Thus, everything 
	 * between start and index can be merged into a single SiteRange
	 * @param a
	 * @param b
	 * @param start
	 * @return
	 */
	private static int findEndpoint(List<SiteRange> a, List<SiteRange> b, int start) {
		SiteRange aRange = rangeForSite(a, start);
		if (aRange == null) 
			aRange = rangeForSite(b, start);
		if (aRange == null)
			throw new IllegalArgumentException("No ranges in either list contain the start site");
		
		int end = aRange.getMax();
		while(aRange != null) {
			end = aRange.getMax();
			aRange = rangeForSite(a, end);
			if (aRange == null)
				aRange = rangeForSite(b, end);
		}
		
		
		return end;
	}
	
	/**
	 * Return the first SiteRange encountered in the list that contains the given site,
	 * or null if no such range exists
	 * @param a List of ranges to scan in INCREASING ORDER
	 * @param site 
	 * @return A SiteRange containing the given site
	 */
	private static SiteRange rangeForSite(List<SiteRange> a, int site) {
		for(SiteRange r : a) {
			if (r.contains(site))
				return r;
			
			//Since ranges in the list are assumed to be increasing, if we scan past the site
			//we know the site is not contained by anything
			if (r.getMin() > site)
				return null;
		}
		return null;
	}
	

	
	/**
	 * Return a new SiteRange that is the intersection of a and b. Its illegal to pass in ranges that
	 * do not intersect
	 * @param a
	 * @param b
	 * @return
	 */
	public static SiteRange intersection(SiteRange a, SiteRange b) {
//		if (! a.intersects(b)) {
//			throw new IllegalArgumentException("Cannot intersect range " + a + " with range " + b);
//		}
		SiteRange newRange = new SiteRange(Math.max(a.getMin(), b.getMin()), Math.min(a.getMax(), b.getMax()));
		newRange.lChild = a.getRefNodeID();
		newRange.rChild = b.getRefNodeID();
		return newRange;
	}

	/**
	 * Returns a new SiteRange that is the intersection of a and b, and set the referringNodeID to the
	 * given value. This also sets the lChild and rChild indices to the the a and b refNodeIDs
	 * @param a
	 * @param b
	 * @param refNodeID
	 * @return New SiteRange representing intersection of the given ranges
	 */
	public static SiteRange intersection(SiteRange a, SiteRange b, int refNodeID) {
		SiteRange newRange = SiteRange.intersection(a, b);
		newRange.setRefNodeID(refNodeID);
		return newRange;
	}

	/**
	 * Return an instance of the site range comparator, which compares ranges based on getMin
	 * @return
	 */
	public static RangeComparator getRangeComparator() {
		return rangeComparator;
	}
	/**
	 * Class to compare ranges based on minimum value
	 * @author brendano
	 *
	 */
	static class RangeComparator implements Comparator<SiteRange> {

		@Override
		public int compare(SiteRange nodeA, SiteRange nodeB) {
			if (nodeA.getMin()>nodeB.getMin())
				return 1;
			else
				return -1;
		}
	}
	
	public static void main(String[] args) {
		List<SiteRange> a = new ArrayList<SiteRange>();
		List<SiteRange> b = new ArrayList<SiteRange>();
		
//		a.add(new SiteRange(0, 15, 0));
//		a.add(new SiteRange(16, 25, 0));
//		
//		b.add(new SiteRange(3, 25, 1));
//		b.add(new SiteRange(28, 50, 1));

//		List<SiteRange> intersection = SiteRange.intersect(a, b);
		
		SiteRange r0 = new SiteRange(0, 10, 1);
		SiteRange neg = new SiteRange(3, 8);
		
		r0.addNegation(neg, a);
		
		System.out.println("Negation ranges:");
		for(SiteRange r: a) {
			System.out.println(r);
		}
	}


}
