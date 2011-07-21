package dlCalculation.computeCore;

import dlCalculation.IntegerStore;
import dlCalculation.PartialsStore;

/**
 * A contiguous range of sites over which the partial 'data likelihood' can be calculated. Used by CompressionCore2 only. 
 * @author brendano
 *
 */
public class ComputeNode {
	final int refID;
	
	NodeState activeState = new NodeState();
	NodeState currentState = new NodeState();
	NodeState proposedState = new NodeState();
	
	public ComputeNode(int id) {
		this.refID = id;
	}

	public ComputeNode(int id, int mapSize, int rateCategories, int partialsSize) {
		this.refID = id;

		currentState.partials = new double[rateCategories][partialsSize][4];
		proposedState.partials = new double[rateCategories][partialsSize][4];
		
		currentState.lMatrices = new double[rateCategories][4][4];
		currentState.rMatrices = new double[rateCategories][4][4];

		proposedState.lMatrices = new double[rateCategories][4][4];
		proposedState.rMatrices = new double[rateCategories][4][4];
		
		currentState.invariantPartials = new double[rateCategories][4][4];
		proposedState.invariantPartials = new double[rateCategories][4][4];
		
		currentState.subtreePatternIndices = new int[mapSize];
		proposedState.subtreePatternIndices = new int[mapSize];
		currentState.subtreePatternMap = new int[mapSize];
		proposedState.subtreePatternMap = new int[mapSize];
		currentState.partialsMap = new int[mapSize];
		proposedState.partialsMap = new int[mapSize];
	}
	
	public String toString() {
		return "ID=" + refID + " active: " + activeState.toString();
	}
	
	/**
	 * Return all buffers to a store we we're not constantly reallocating them
	 */
//	public void disposeArrays(IntegerStore intStore, PartialsStore partialsStore) {
//		intStore.add(currentState.subtreePatternIndices);
//		intStore.add(proposedState.subtreePatternIndices);
//		intStore.add(currentState.subtreePatternMap);
//		intStore.add(proposedState.subtreePatternMap);
//		intStore.add(currentState.partialsMap);
//		intStore.add(proposedState.partialsMap);
//		if (currentState.partials != null)
//			partialsStore.add(currentState.partials);
//		if (proposedState.partials != null)
//			partialsStore.add(proposedState.partials);
//	}
}

class NodeState {
	
	double rootDL = Double.NaN;
	
	double[][][] partials;
	double[][][] invariantPartials; //Partials for invariant sites
	double[][][] lMatrices; //Transition matrices for left child, first index is rate category
	double[][][] rMatrices; //Transition matrices for right child, first index is rate category
	double lLength = -1;	//Branch length of left edge
	double rLength = -1;	//Branch length of right edge

	int startIndex = -1; //Stores index in polymorphic sites where this region begins
	int endIndex = -1;  //Stores index in polymorphic sites where region ends
	int globalStartSite = -1; //Stores global site where this region begins
	int globalEndSite = -1;  //Stores global site where this regions ends
	int leftNode = -1;
	int rightNode = -1;
	double height = -1;
		
	//A list of the global pattern indices that, when combined, account for all the patterns seen in this subtree
	//The length of this is always equal to the number of global patterns, but we only use the first subtreePatternCount
	//of these for lookups (the actual number will vary with the tree node), a Set would be a more understandable type for
	//this, but we use an array for speed purposes (we need to be able to iterate through the indices quickly)
	int[] subtreePatternIndices; 
	int subtreeUniquePatternCount = 0; //The addTipStates method requires this to be zero initialized here

	//A mapping from the global pattern index to the subtree pattern index. This always has length = global pattern count, but maps onto the states list, which has length = subtree pattern count
	int[] subtreePatternMap;
	
	//Mapping from site to an index in partials
	int[] partialsMap;
	
	public String toString() {
		return " start=" + globalStartSite + " end=" + globalEndSite + " left=" + leftNode + " right=" + rightNode + " sites=" + subtreeUniquePatternCount;
	}

}
