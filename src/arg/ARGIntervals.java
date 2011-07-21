package arg;

import java.util.Collections;
import java.util.List;

/**
 * Represents the set of intervals between coalescent and/or recombination events in an arg. Currently this is 
 * used only by CoalescentLikelihood, which uses these to quickly compute the probability of popsize + recomb. rate
 * given an arg. 
 * 
 * @author brendan
 *
 */
public class ARGIntervals {

	public enum IntervalType { COALESCENT, RECOMBINATION /* someday, SAMPLING */};
	
        //Length of arrays used to store counts and times
        private int arraySize = 250;

        private int intervalCount;
	private double[] intervalEndTimes;
	private int[] lineageCounts;
	private IntervalType[] intervalTypes;	
	
	//double maxDLHeight = Double.NEGATIVE_INFINITY;

	/**
	 * Get the number of intervals
	 * @return
	 */
	public int getIntervalCount() {
		return intervalCount;
	}
	
	/**
	 * Obtain the time at which the ith interval ends
	 * @param i
	 * @return
	 */
	public double getIntervalEndTime(int i) {
		return intervalEndTimes[i];
	}
	
	/**
	 * Obtain the time at which the ith interval begins
	 * @param i
	 * @return
	 */
	public double getIntervalStartTime(int i) {
		if (i==0)
			return 0;
		else
			return intervalEndTimes[i-1];
	}
	
	/**
	 * Return the length of the ith interval
	 * @param i
	 * @return
	 */
	public double getIntervalLength(int i) {
		if (i==0)
			return intervalEndTimes[0];
		else
			return intervalEndTimes[i] - intervalEndTimes[i-1];
	}
	
	public IntervalType getIntervalType(int i) {
		return intervalTypes[i];
	}

	/**
	 * Returns the number of lineages in the given interval
	 * @param i
	 * @return
	 */
	public int getLineageCount(int i) {
		return lineageCounts[i];
	}
	
	/**
	 * Returns the height of the deepest coalescent node with site coalescences
	 * @return
	 */
//	public double getMaxDLHeight() {
//		return maxDLHeight;
//	}
	
	/**
	 * Computes the heights, types, and lineage counts in all intervals
	 * @param arg
	 */
	public void assignIntervals(ARG arg) {
		List<ARGNode> nodes = arg.getInternalNodes();
		intervalCount = nodes.size();
		
		if (lineageCounts == null || lineageCounts.length < intervalCount) {
                        int size = Math.max(intervalCount, arraySize);
			intervalEndTimes = new double[size];
			intervalTypes = new IntervalType[size];
			lineageCounts = new int[size];
		}
		
		
		Collections.sort(nodes, arg.getNodeHeightComparator());
		int lineages = arg.getTips().size();
		
		for(int i=0; i<nodes.size(); i++) {
			ARGNode node = nodes.get(i);
			intervalEndTimes[i] = node.getHeight();
			lineageCounts[i] = lineages;
			
			if (node.getNumParents()==2) {
				intervalTypes[i] = IntervalType.RECOMBINATION;
				lineages++;
			}
			else {
				intervalTypes[i] = IntervalType.COALESCENT;
				lineages--;
			}
			
			//Also compute max height of site-containing coal node
//			if (node.getNumParents()==1 && ((CoalNode)node).getCoalescingSites().size()>0) {
//				if (node.getHeight() > maxDLHeight)
//					maxDLHeight = node.getHeight();
//			}
		}
		
		
		
		if (lineages != 1) {
			throw new IllegalStateException("Final number of lineages was not 1, it was " + lineages);
		}
	}

	public String toString() {
		StringBuilder strB = new StringBuilder();
		for(int i=0; i<getIntervalCount(); i++) {
			if (getIntervalType(i)==IntervalType.COALESCENT)
				strB.append("[" + getIntervalEndTime(i) + " " + getLineageCount(i) + " C] ");
			else 
				strB.append("[" + getIntervalEndTime(i) + " " + getLineageCount(i) + " R] ");
		}
		return strB.toString();
	}
	
}
