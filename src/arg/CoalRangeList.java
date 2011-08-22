package arg;

import dlCalculation.computeCore.ComputeNode;

/**
 * A list of ranges of coalescing sites, stored in a manner very similar to SiteRangeList, where
 * we track the beginning and end of ranges as entries in an array of ints. We also track the
 * indices of the left and right nodes here
 * @author brendano
 *
 */
public class CoalRangeList extends SiteRangeList {
	
	private int[] rChilds = null;
	private int[] lChilds = null;
	
	//References to the actual compute nodes used
	private ComputeNode[] nodes = null;
	
	boolean used = false;

	//When true, when we attempt to append a range that can be merged with the previous range
	//we do so. Ideally, this reduces the number of compute nodes that are created, and it may
	//help to speed up dl comp since it will reduce the number of patterns computed
	private final boolean mergeIfPossible = true;

	
	public boolean appendRange(int begin, int end, int id, int lChild, int rChild) {		
		if (sites == null) {
			allocateArrays(initialRangeSize);
		}
		
		if (rangeMax == refIDs.length) {
			expandArrays(2*rangeMax);
		}
		
		int startIndex = rangeMax*2;
		
		if (mergeIfPossible
				&& startIndex>0
				&& sites[startIndex-1] == begin
				&& lChilds[rangeMax-1] == lChild
				&& rChilds[rangeMax-1] == rChild) {
			sites[startIndex-1] = end;
			return true;
		}
		else {
			sites[startIndex] = begin;
			sites[startIndex+1] = end;
			refIDs[rangeMax] = id;
			lChilds[rangeMax] = lChild;
			rChilds[rangeMax] = rChild;
			rangeMax++;
			return false;
		}
	}
	

	
	

	public boolean hasMergeableRanges() {
		for(int i=0; i<rangeCount()-1; i++) {
			if (lChilds[i] == lChilds[i+1]
					&& rChilds[i] == rChilds[i+1]
					&& sites[2*i] == sites[2*i+1]) {
				return true;
			}
		}
		return false;
	}
	

	
	public Object clone() {
		return copy();
	}

	
	/**
	 * Obtain a complete clone of this object
	 * @return
	 */
	public SiteRangeList copy() {
		SiteRangeList newList = new SiteRangeList();
		return copy(newList);
	}
	
//	public SiteRangeList copy(SiteRangeList dest) {
//
//		dest.clear();
//		
//		if (sites != null) {
//			dest.allocateArrays(refIDs.length);
//			System.arraycopy(sites, 0, dest.sites, 0, 2*rangeMax);
//			System.arraycopy(refIDs, 0, dest.refIDs, 0, rangeMax);
//			System.arraycopy(lChilds, 0, dest.lChilds, 0, rangeMax);
//			System.arraycopy(rChilds, 0, dest.rChilds, 0, rangeMax);
//			dest.rangeMin = 0;
//			dest.rangeMax = rangeMax;
//		}
//		return dest;		
//	}
	

	
	/**
	 * Allocate integer arrays used to store data. We do this only on the first call
	 * to appendRange so that empty range lists dont maintain lots of arrays
	 * @param maxRangeCount
	 */
	private void allocateArrays(int maxRangeCount) {
		sites = new int[2*maxRangeCount];
		refIDs = new int[maxRangeCount];
		lChilds = new int[maxRangeCount];
		rChilds = new int[maxRangeCount];
       // System.out.println("Allocating arrays for site range list");
	}
	
	/**
	 * Replace all arrays with bigger ones and copy all info from old to new arrays
	 * @param newRangeCount
	 */
	private void expandArrays(int newRangeCount) {
		int[] newSites = new int[2*newRangeCount];
		int[] newIDs = new int[newRangeCount];
		int[] newLChilds = new int[newRangeCount];
		int[] newRChilds = new int[newRangeCount];
		
		System.arraycopy(sites, 0, newSites, 0, 2*rangeMax);
		System.arraycopy(refIDs, 0, newIDs, 0, rangeMax);
		System.arraycopy(lChilds, 0, newLChilds, 0, rangeMax);
		System.arraycopy(rChilds, 0, newRChilds, 0, rangeMax);
		
		sites = newSites;
		refIDs = newIDs;
		lChilds = newLChilds;
		rChilds = newRChilds;
	}

	

	
	public int countMergableRanges() {
		int sum = 0;
		if (isEmpty())
			return 0;
		for(int r=rangeMin; r<rangeMax-1; r++) {
			if ( getRangeEnd(r) == getRangeBegin(r+1) 
				&& getRefID(r) == getRefID(r+1)
				&& getLChild(r) == getLChild(r+1)
				&& getRChild(r) == getRChild(r+1) )
			sum++;
		}
		
		return sum;
	}

	
	/**
	 * Return the compute node id of the right child of this range. This is only defined if
	 * this range is a coalescent range
	 * @param range
	 * @return
	 */	
	public int getLChild(int range) {
		if (range < rangeMin || range >= rangeMax)
			throw new IllegalArgumentException("Cannot access ID for range " + range + ", there are only " + rangeMax);
		return lChilds[range];
	}
	
	/**
	 * Return the compute node id of the left child of this range. This is only defined if
	 * this range is a coalescent range
	 * @param range
	 * @return
	 */
	public int getRChild(int range) {
		if (range < rangeMin || range >= rangeMax)
			throw new IllegalArgumentException("Cannot access ID for range " + range + ", there are only " + rangeMax);
		return rChilds[range];
	}
	
	
	/**
	 * Returns site boundary at the given index in the sites[] array. Index is NOT a range number, 
	 * if you want the beginning of range x, use at(2*x)
	 * @param index
	 * @return
	 */
	public int at(int index) {
//		if (index < 2*rangeMin || index >= (rangeMax*2)) {
//			throw new IllegalArgumentException("Cannot access index " + index + ", there are only " + rangeMax + " total ranges");
//		}
		return sites[index];
	}
	
	public int rangeIndexForSite(int site) {
		int siteIndex = siteIndexForSite(site);
		if (siteIndex%2==1)
			throw new IllegalArgumentException("Site " + site + " is not in a range, are things broken?");
		return siteIndex/2;
	}
	
	
	/**
	 * Returns the index of the sites[] array containing the given site, such that sites[index] <= site < sites[index+1]
	 * This returns -1 if site < sites[0] and indexCount() (=2*numRanges) if site > getMax()
	 * @param site
	 * @return Index of site in sites[] array
	 */
	private int siteIndexForSite(int site) {
		return siteIndexForSite(site, rangeMin*2, rangeMax*2);
	}
	
	/**
	 * Returns the index of the sites[] array containing the given site, where the search is constrained to
	 * the range of sites defined by minIndex and maxIndex 
	 * @return Index of site in sites[] array
	 */
	private int siteIndexForSite(int site, int minIndex, int maxIndex) {
		int first = minIndex; //rangeMin*2;
		int upto = maxIndex;
		int mid = (first + upto) / 2;  
		while (first < upto) {
			mid = (first + upto) / 2;  // Compute mid point.
			if (site < sites[mid]) {
				upto = mid;     // repeat search in bottom half.
			} else if (site > sites[mid]) {
				first = mid + 1;  // Repeat search in top half.
			} else {
				while(mid < maxIndex && sites[mid]==site) //Found it, advance mid to last index with that site, but not beyond end of sites we track
					mid++;
				mid--;
				return mid;     //return position
			}
		}
		return first-1; //Site not found, but we return index of range containing it  
	}
	

	
	
	/**
	 * Obtain a site range object corresponding to the given range 
	 * @param range
	 * @return
	 */
	public SiteRange toSiteRange(int range) {
		if (range < rangeMin || range>=rangeMax) 
			throw new IllegalArgumentException("Cannot access range #" + range);
		SiteRange r = new SiteRange(getRangeBegin(range), getRangeEnd(range), getRefID(range));
		r.lChild = getLChild(range);
		r.rChild = getRChild(range);
		return r;
	}
	

	
	public String toString() {
		StringBuilder strB = new StringBuilder();
		strB.append("Coal range list with " + rangeMax + " ranges rangeMin: " + rangeMin + " rangeMax: " + rangeMax + "\n");
		for(int i=rangeMin; i<rangeMax; i++) {
			int index = i*2;
			strB.append(sites[index] + " .. " + sites[index+1] + "\t id: " + refIDs[i] + "\t lChild: " + lChilds[i] + "\t rChild: " + rChilds[i] + "\n");
		}
		return strB.toString();
	}

	
}
