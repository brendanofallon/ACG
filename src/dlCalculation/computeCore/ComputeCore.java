package dlCalculation.computeCore;

import gnu.trove.list.array.TIntArrayList;

import java.util.List;


/**
 * Interface for all things 
 * @author brendano
 *
 */
public interface ComputeCore {
	
	/**
	 * Called when the proposed state is accepted. 
	 */
	public void accept();
	
	/**
	 * Called when proposed state is rejected. All changes since last call to accept should be reverted. 
	 */
	public void reject();
	
	/**
	 * Force a full recomputation of all transition matrices on the next call of updatePartials
	 *  ..this must be sure to recalculate the various intermediates as well, not just the branch-length dependent stuff...
	 */
	public void setUpdateAllMatrices();
	
	/**
	 * Compute partials for the given list of ids. IDs must be given in ascending
	 * height
	 * @param ids
	 */
	public void computePartialsList(TIntArrayList ids);

    
	public void computePartials(TIntArrayList ids);

	/**
	 * Compute new partials for the given node number
	 */
	public void computePartials(int nodeNumber);
	
	
	/**
	 * Release resources associated with the given nodeID
	 * @param nodeID
	 */
	public void releaseNode(int nodeID);
	
	/**
	 * Return the number of patterns computed for the given nodes
	 * @param nodeIDs
	 * @return
	 */
	public int countPatterns(TIntArrayList ids);


	/**
	 * Compute and return the root data likelihood - the probability of the tree given the data. 
	 * @return
	 */
	public double computeRootLogDL();
	
	/**
	 * Return the number of compute nodes in the core
	 * @return
	 */
	public int getNodeCount();
	
	/**
	 * Compute the root partials over a restricted range of sites
	 * @param startSite
	 * @param endSite
	 * @return
	 */
	public double computeRootLogDLForRange(int startSite, int endSite);
	
	
	/**
	 * Returns the number of patterns (calculations) this core is computing
	 * @return
	 */
	public int countPatterns();
	/**
	 * Initialize the state information for the given tip number. 
	 * State information is a list of integers, such that the state of site i is states[i] 
	 * @param tipNumber
	 * @param states
	 */
	public void initializeTipState(int tipNumber, int[] states);
	
	/**
	 * Get compute node index of left child of given node
	 * @param nodeNumber
	 * @return
	 */
	public int getLeftChildForNode(int nodeNumber);
	
	
	/**
	 * Get compute node index of right child of given node
	 * @param nodeNumber
	 * @return
	 */
	public int getRightChildForNode(int nodeNumber);
	
	/**
	 * Obtain height of given node
	 * @param nodeNumber
	 * @return
	 */
	public double getHeightForNode(int nodeNumber);
	
	/**
	 * The first site in the range of sites coalescing at the given node
	 * @param nodeNumber
	 * @return
	 */
	public int getStartSiteForNode(int nodeNumber);

	/**
	 * The end site in the range of sites coalescing at the given node
	 * @param nodeNumber
	 * @return
	 */
	public int getEndSiteForNode(int nodeNumber);
	
	/**
	 * True if any component of the given node is in the proposed state
	 * @param nodeNumber
	 * @return
	 */
	public boolean isNodeProposed(int nodeNumber);
	
	/**
	 * Propose new information for a node. If no node with the given number is found, one is created. 
	 * @param nodeNumber Reference ID for the node
	 * @param nodeHeight Height of the node
	 * @param globalMin First site in the range of sites associated with this node
	 * @param globalMax One beyond last site in range of sites associated with this node
	 * @param lNode ID of left child of this node
	 * @param rNode ID of right child of this node
	 */
	public void proposeRange(int nodeNumber, double nodeHeight, int globalMin, int globalMax, int lNode, int rNode);
	
	/**
	 * Just propose a new height for the given node. This assumes the node already exists and has the
	 * partials initialized to the correct size
	 * @param nodeNumber
	 * @param nodeHeight
	 */
	public void proposeNode(int nodeNumber, double nodeHeight);
	
	
	/**
	 * Clear all root range information
	 */
	public void clearRootRanges();

	/**
	 * Add the ComputeNode with the given ID to the list of nodes to compute rootDL over 
	 * @param refNodeID
	 */
	public void setRootRangeNode(int refNodeID);
	
	
	/**
	 * Obtain a unique new number for a compute node
	 * @return
	 */
	public int nextNumber();
	
	/**
	 * Returns the next index for a tip (the 'tipID') used by this core to index tips, and advances the counter
	 * by 1. 
	 * @return
	 */
	public int nextTipID();
	
	/**
	 * Print some state information to System.out
	 */
	public void emitState();

}
