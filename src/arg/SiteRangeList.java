package arg;

/**
 * A list of discrete, contiguous ranges over sites, stored in a fashion that makes merging such lists relatively
 * easy. The beginning and end of range i is stored at sites[2*i] and sites[2*i+1], respectively, such that
 * all site range boundaries are always stored in ascending order. 
 * 
 * We also maintain some lists of additional info, such as refIDs (the ComputeNode id to which a range refers), and
 * the indices of the rChild and lChild of a range (which ComputeCores need to compute partials)
 * @author brendano
 *
 */
public class SiteRangeList {

	protected int initialRangeSize = 8;
	
	protected int[] refIDs = null;
	protected int[] sites = null;
	
	boolean used = false;
	
	int rangeMax = 0; //Points to next spot where range info will be inserted in refIDs, sites, etc. (Site info is added at rangeMax*2)
	int rangeMin = 0; //Lower bound of ranges to consider, in use only when ranges have been truncated
	
	private boolean isFiltered = false;
	
	//These are used to remember what particular values were when we apply a filter
	int prevRangeMax;
	int prevMaxSite;
	int prevMinSite;
	int prevMaxSiteIndex;
	int prevMinSiteIndex;

	//When true, when we attempt to append a range that can be merged with the previous range
	//we do so. Ideally, this reduces the number of compute nodes that are created, and it may
	//help to speed up dl comp since it will reduce the number of patterns computed
	private final boolean mergeIfPossible = true;
	
	/**
	 * Returns true if all range boundaries are equal between this range and the given range
	 * @param obj
	 * @return
	 */
	public boolean sameRanges(SiteRangeList r) {
			if (rangeMax != r.size())
				return false;
			for(int i=0; i<rangeMax; i++) 
				if (getRangeBegin(i) != r.getRangeBegin(i)
						|| getRangeEnd(i) != r.getRangeEnd(i)
						|| getRefID(i) != r.getRefID(i))
					return false;
 
			return true;
	}
	
	public void appendRange(int begin, int end) {
		appendRange(begin, end, -1);
	}
	

	public boolean appendRange(int begin, int end, int id) {
		
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
				&& refIDs[rangeMax-1] == id) {
			sites[startIndex-1] = end;
			return true;
		}
		else {
			sites[startIndex] = begin;
			sites[startIndex+1] = end;
			refIDs[rangeMax] = id;
			rangeMax++;
			return false;
		}
	}
	
	/**
	 * Append this range to the list of ranges without merging (even if mergeRanges is set to true)
	 * @param begin
	 * @param end
	 * @param id
	 * @param lChild
	 * @param rChild
	 */
	public void forceAppendRange(int begin, int end, int id) {
		if (sites == null) {
			allocateArrays(initialRangeSize);
		}

		if (rangeMax == refIDs.length) {
			expandArrays(2*rangeMax);
		}

		int startIndex = rangeMax*2;

		sites[startIndex] = begin;
		sites[startIndex+1] = end;
		refIDs[rangeMax] = id;
		rangeMax++;
	}
	
	/**
	 * Attempt to merge the given range with the previous range in the list, throwing an error if the ranges are not mergeable
	 * @param begin
	 * @param end
	 * @param id
	 * @param lChild
	 * @param rChild
	 */
	public void mergeRange(int begin, int end, int id) {
		
		int startIndex = rangeMax*2;
		
		if (mergeIfPossible
				&& startIndex>0
				&& sites[startIndex-1] == begin
				&& refIDs[rangeMax-1] == id) {
			sites[startIndex-1] = end;
			System.out.println("Merging ranges!");
		}
		else {
			throw new IllegalStateException("Range is not mergeable!");
		}
	}
	
	
	public void appendRangeWithFilter(int begin, int end, int id, SiteRange filter) {
		if (filter == null) {
			appendRange(begin, end, id);
			return;
		}
		
		int startSite = Math.max(filter.getMin(), begin);
		int endSite = Math.min(filter.getMax(), end);
		
		//Could be that the filter omits all sites, in which case we just return
		//without doing anything
		if (startSite >= endSite) {
			return;
		}
		
		appendRange(startSite, endSite, id);
	}
	
	public boolean hasMergeableRanges() {
		for(int i=0; i<rangeCount()-1; i++) {
			if (sites[2*i] == sites[2*i+1]) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Temporarily filter this list of ranges so that only those sites/ranges between begin and end are 'visible'
	 * No modifications are permitted while the filter is in place, but the filter can be undone via unapplyFilter,
	 * and may then be modified
	 * @param begin
	 * @param end
	 */
	public void applyFilter(int begin, int end) {
		if (isFiltered)
			throw new IllegalStateException("Cannot apply a new filter to a range list while a filter is applied");
		
		isFiltered = true;
				
		prevRangeMax = rangeMax;
		prevMinSite = -1;
		prevMaxSite = -1;
		prevMinSiteIndex = -1;
		prevMaxSiteIndex = -1;
		
		//If there's no intersection just clear and return immediately
		if (begin >= getMax() || end <= getMin()) {
			rangeMin = 0;
			rangeMax = 0;
			return;
		}
		
		
		int lower = siteIndexForSite(begin);
		int upper = siteIndexForSite(end, lower, 2*rangeMax); //constrain search to start at lower 
		
		if (lower>-1) {
			rangeMin = lower/2;
			if (lower%2==0) {
				prevMinSite = sites[lower];
				prevMinSiteIndex = lower;
				sites[lower] = begin;
			}
			else
				rangeMin++;
			//if (lower was odd, then truncation fell in between ranges and thus lops off a whole range, but doesn't change sites[]
		}
		
		if (upper>-1) {
			rangeMax = upper/2+1;
			if (upper%2==0) {
				prevMaxSite = sites[upper+1];
				prevMaxSiteIndex = upper+1;
				sites[upper+1] = end;
				
				if (sites[upper+1] == sites[upper])
					rangeMax--;
			}
			
		}
		
	}
	
	/**
	 * Removes any filtering applied to this range, restoring all
	 * previous settings. Has no effect if no filter was applied. 
	 */
	public void unapplyFilter() {
		if (!isFiltered) {
			return;
		}
		
		
		rangeMin = 0;
		if (prevMaxSite > -1) {
			sites[prevMaxSiteIndex] = prevMaxSite;
		}
		rangeMax = prevRangeMax;
		
		if (prevMinSite > -1) {
			sites[prevMinSiteIndex] = prevMinSite;
		}
		isFiltered = false;
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
	
	public SiteRangeList copy(SiteRangeList dest) {
		if (isFiltered)
			throw new IllegalStateException("Cannot copy a range list while a filter is applied");

		dest.clear();
		
		if (sites != null) {
			dest.allocateArrays(refIDs.length);
			System.arraycopy(sites, 0, dest.sites, 0, 2*rangeMax);
			System.arraycopy(refIDs, 0, dest.refIDs, 0, rangeMax);
			dest.rangeMin = 0;
			dest.rangeMax = rangeMax;
		}
		return dest;		
	}
	

	
	/**
	 * Allocate integer arrays used to store data. We do this only on the first call
	 * to appendRange so that empty range lists dont maintain lots of arrays
	 * @param maxRangeCount
	 */
	private void allocateArrays(int maxRangeCount) {
		sites = new int[2*maxRangeCount];
		refIDs = new int[maxRangeCount];
       // System.out.println("Allocating arrays for site range list");
	}
	
	/**
	 * Replace all arrays with bigger ones and copy all info from old to new arrays
	 * @param newRangeCount
	 */
	private void expandArrays(int newRangeCount) {
		int[] newSites = new int[2*newRangeCount];
		int[] newIDs = new int[newRangeCount];
		
		System.arraycopy(sites, 0, newSites, 0, 2*rangeMax);
		System.arraycopy(refIDs, 0, newIDs, 0, rangeMax);
		
		sites = newSites;
		refIDs = newIDs;
	}
	

	
	/**
	 * Returns a new SiteRangeList that is a copy of this list between the specified sites. If dest is
	 * provided, range info is entered into dest
	 * @param startFilter
	 * @param endFilter
	 * @param dest
	 * @return
	 */
	public SiteRangeList copyFilter(int startFilter, int endFilter, SiteRangeList dest) {		
		dest.clear();
				
		if (sites == null)
			return dest;
		
		//No intersection, return an empty list
		int max = getMax();
		if (startFilter >= max || endFilter <= getMin()) {
			return dest;
		}
		
		
		int i = siteIndexForSite(startFilter);		
		if (i%2==1)
			i++;

		if (i<0)
			i=0;

		
		int begin;
		int end;
		
		begin = Math.max(sites[i], startFilter);
		end = Math.min(sites[i+1], endFilter);
		int rangeIndex = i/2;
		dest.appendRange(begin, end, refIDs[rangeIndex]);
		i+=2;
		
		for(; i<maxSiteIndex() && begin < endFilter; i+=2) {
			begin = sites[i];
			end = Math.min(sites[i+1], endFilter);
			if (end > begin) {
				rangeIndex = i/2;
				dest.appendRange(begin, end, refIDs[rangeIndex]);
			}
		}
				
		return dest;
	}

	
	/**
	 * Return the number of ranges stores in this range list
	 * @return
	 */
	public int rangeCount() {
		return rangeMax-rangeMin;
	}
	
	/**
	 * Return the number of ranges in this range list. Identical to rangeCount()
	 * @return
	 */
	public int size() {
		return rangeMax-rangeMin;
	}
	
	/**
	 * Return the max index of sites accessible from this range. This is always 2*(rangeMax)
	 * @return
	 */
	public int maxSiteIndex() {
		return 2*rangeMax;
	}
	
	public int getFirstSiteIndex() {
		return 2*rangeMin;
	}
	
	/**
	 * Return the compute node id of this range. 
	 * @param range
	 * @return
	 */
	public int getRefID(int range) {
		//Check turned off for optimization purposes, this code gets called a bunch in some very performance-sensitive methods and
		//the check hasn't caught any errors in a while, so it's off for now. 
//		if (range < rangeMin || range >= rangeMax)
//			throw new IllegalArgumentException("Cannot access ID for range " + range + ", there are only " + rangeMax);
		return refIDs[range];
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
	 * Clears the list so that it contains so ranges.
	 */
	public void clear() {
		isFiltered = false;
		prevMinSite = -1;
		prevMaxSite = -1;
		prevMinSiteIndex = -1;
		prevMaxSiteIndex = -1;
		rangeMin = 0;
		rangeMax = 0;
//		if (sites != null)
//			sites = new int[maxRanges];
	}
	
	/**
	 * Returns true if the list contains no ranges
	 * @return
	 */
	public boolean isEmpty() {
		return rangeMax-rangeMin == 0;
	}
	
	/**
	 * Return the first site of the lowest range in this range list
	 * @return
	 */
	public int getMin() {
		if (rangeMax == 0)
			return -1;
		else 
			return sites[2*rangeMin];
	}
	
	/**
	 * Return the end site of the highest range in this left
	 * @return
	 */
	public int getMax() {
		if (rangeMax == 0)
			return -1;
		else
			return sites[2*rangeMax-1];
	}
	
	/**
	 * Return the site at which the given range begins
	 * @param range
	 * @return
	 */
	public int getRangeBegin(int range) {
		return at(range*2);
	}

	/**
	 * Return the site at which the given range ends
	 * @param range
	 * @return
	 */
	public int getRangeEnd(int range) {
		return at(range*2+1);
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
		return r;
	}
	
	/**
	 * Returns true if the range start and end in this range list at RANGE index
	 * thisRange are equal to the range start and end in compList  
	 * @param coalescingSites
	 * @param i
	 * @return
	 */
	public boolean rangeEquals(int thisRange, SiteRangeList compList, int i) {
		int siteStart = thisRange*2;
		int siteEnd = siteStart+1;
		try {
			return sites[siteStart] == compList.at(siteStart)
					&& sites[siteEnd] == compList.at(siteEnd);
		}
		catch(Exception ex) {
			return false;
		}
	}
	
	/**
	 * Set the refID for the given range to be the given id
	 * @param range
	 * @param id
	 */
	public void setRefID(int range, int id) {
		if (range < rangeMin || range >= rangeMax)
			throw new IllegalArgumentException("Cannot access ID for range " + range );
		refIDs[range] = id;
	}
	/**
	 * Returns true if this site range contains the given site
	 * @param site
	 * @return
	 */
	public boolean contains(int site) {
		if (isEmpty())
			return false;
		int index = siteIndexForSite(site);
		boolean c = index%2==0;
                //Debugging stuff below, turned off since it hasn't broken in a long time
//		if (c && rangeMax==0) {
//			System.err.println("Something is really wrong here");
//			System.exit(0);
//			//throw new IllegalArgumentException("Hmm, rangeMax is zero, but we are reporting that we contain a site");
//		}
		return c;
	}
	
	public String toString() {
		StringBuilder strB = new StringBuilder();
		strB.append("Site range list with " + rangeMax + " ranges rangeMin: " + rangeMin + " rangeMax: " + rangeMax + "\n");
		for(int i=rangeMin; i<rangeMax; i++) {
			int index = i*2;
			strB.append(sites[index] + " .. " + sites[index+1] + "\t id: " + refIDs[i] + "\t lChild: " + "\n");
		}
		return strB.toString();
	}
	
	public void checkValidity() {		
		for(int i=0; i<(2*rangeMax)-1; i++) {
			if (sites[i+1] < sites[i]) {
				System.out.println("List is : " + this);
				throw new IllegalStateException("Site boundaries are not in order");
				
			}
		}
		
	}
	
	public static void main(String[] args) {
		SiteRangeList a = new SiteRangeList();
		SiteRangeList b = new SiteRangeList();
				
		a.appendRange(10, 50, 1);
		a.appendRange(50, 50, 2);
		a.appendRange(51, 51, 3);
		a.appendRange(51, 100, 4);
		System.out.println("Ranges before filter: " + a);

		a.copyFilter(0, 51, b);		
		System.out.println("Ranges after copy : " + b);
		
		
		a.copyFilter(5, 15, b);
		System.out.println("Ranges after second copy : " + b);
		
		
//		int site = 40;
//		int index = b.rangeForSite(site);
//		System.out.println("Index of range for site " + site + " : " + index);
//		
//		boolean c = b.contains(site);
//		System.out.println("Ranges contains site " + site + " : " + c);
		
	}



}
