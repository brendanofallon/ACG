package newgui.trackView;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * A discrete (and immutable) list of adjacent ranges in a sub-interval of the 
 * whole sequence space. This is basically a node in a tree, in which each node has
 * links to descendant nodes as well as a list of all Range objects belonging to all
 * sub nodes 
 * @author brendan
 *
 */
public class RangeBlock {

	public static int blockCount = 0;
	public static int searchCount = 0;
	final int blockBegin;
	final int blockEnd;
	
	final int subBlockCount = 16;
	private RangeBlock[] ranges;
	
	//Storage for all ranges in this block
	private List<Range> myRanges = new ArrayList<Range>(4);
	
	public RangeBlock(List<Range> rangeList, int blockBegin, int blockEnd) {
		this.blockBegin = blockBegin;
		this.blockEnd = blockEnd;
			
		//Assign range list 
		for(Range range : rangeList) {
			if (range.intersects(blockBegin, blockEnd)) {
				myRanges.add(range);
			}
		}
	
		blockCount++;
		//System.out.println("Creating #" + blockCount + " block for range: " + blockBegin + " - " + blockEnd + " containing " + myRanges.size() + " ranges");
		if (myRanges.size()==0) {
			return;
		}
		
		ranges = new RangeBlock[subBlockCount];
		
		if (blockEnd - blockBegin < 256) {
			myRanges.addAll(rangeList);
			return;
		}
		
		//Create sub blocks
		int begin = blockBegin;
		int step = (int)Math.round( (double)(blockEnd - blockBegin) / (double)subBlockCount );
		if (step < 2) {
			return;
		}
		
		for(int i=0; i<ranges.length; i++) {
			int end = begin + step;
			ranges[i] = new RangeBlock(myRanges, begin, end);
			begin = end;
		}
		
	}
	
	/**
	 * Adds ranges that intersect the given given range (in begin-end) to the list provided
	 * Note that this adds all ranges that intersect.. and also a few more! 
	 * This is suitable for providing a list of ranges to draw onto a graphics object, where
	 * high performance is required but its OK to have some ranges that aren't really in the
	 * interval we want to draw 
	 * @param begin
	 * @param end
	 * @param list
	 */
	public void getRanges(int begin, int end, Set<Range> set) {
		searchCount++;
		//System.out.println("Getting ranges range: " + blockBegin + " - " + blockEnd + " containing " + myRanges.size() + " ranges");
		if (myRanges.size() == 0)
			return;
		
		if ( (begin <= blockBegin && end >= blockEnd) ) {
			set.addAll(myRanges);
		}
		else {
			if (myRanges.size() < 2) {
				set.addAll(myRanges);
				return;
			}
			
			for(int i=0; i<ranges.length; i++) {
				if (ranges[i] != null && ranges[i].intersects(begin, end)) {
					ranges[i].getRanges(begin, end, set);
				}
			}	
		}	
		
	}
	
	/**
	 * Get the number of Ranges contained in this block
	 * @return
	 */
	public int getRangeCount() {
		return myRanges.size();
	}
	
	public boolean intersects(int start, int end) {
		return end <= blockBegin || start >= blockEnd ? false : true;
	}
	
	
	public static void main(String[] args) {
		List<Range> ranges = new ArrayList<Range>(2048);
		
		System.out.println("Creating ranges...");
		int intMax = 10000000;
		for(int i=0; i<50000; i++) {
			int begin = (int)Math.round( intMax*Math.random() );
			int length = (int)Math.round( 400.0 * Math.random() );
			ranges.add(new AbstractRange(begin, begin+length));
		}
		
		System.out.println("Creating range block...");
		RangeBlock block = new RangeBlock(ranges, 0, intMax);
		Set<Range> list = new HashSet<Range>();
		
		
		System.out.println("Grabbing ranges in interval...");
		int min = 230000;
		int max = 8400000;
		
		Date begin = new Date();
		block.getRanges(min, max, list);
		Date end = new Date();
		//System.out.println("Elapsed ms: " + (end.getTime() - begin.getTime()));
		
		//System.out.println("Found " + list.size() +" ranges in interval ");
		int misses = 0;
		for(Range r : list) {
			//System.out.println(r);
			if (! r.intersects(min, max)) {
				System.out.println("Hmm, range " + r + " does not intersect request interval");
				misses++;
			}
		}

		for(Range r : ranges) {
			if (r.intersects(min, max)) {
				if (! list.contains(r)) {
					System.out.println("\n ** Uh-oh, found a missed range: " + r + "! \n");
				}
			}
		}
		
		System.out.println("Created " + RangeBlock.blockCount + " total blocks");
		System.out.println(RangeBlock.searchCount + " blocks searched ");
		System.out.println("Found " + list.size() +" ranges in interval ");
		System.out.println("Total misses : " + misses);
		System.out.println("Fraction of misses : " + (double)misses / (double)list.size());
		System.out.println("Search time ms: " + (end.getTime() - begin.getTime()));
		
	}
}
