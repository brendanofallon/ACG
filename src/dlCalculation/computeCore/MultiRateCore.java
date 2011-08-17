package dlCalculation.computeCore;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TIntIntHashMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Stack;

import sequence.DNAUtils;
import sequence.DataMatrix;
import dlCalculation.siteRateModels.SiteRateModel;
import dlCalculation.substitutionModels.MutationModel;

public class MultiRateCore extends AbstractComputeCore {
	
	//Used for pattern collecting, this version of maps is based all on primitives and 
	//is hence faster than standard Java classes which use objects. Doesn't require boxing / unboxing
	private TIntIntHashMap patternCache = new TIntIntHashMap(); //Default values seem to work just fine
		
	//Tips always have indices form 0..number of tips -1. 
	protected TipComputeNode[] tips;
		
	private MutationModel mutModel;

	//Emit debugging info to stdout
	final boolean verbose = false;
	
	protected List<ComputeNode> rootRanges = new ArrayList<ComputeNode>(100);
	
	//List of nodes that aer in proposed state, so we can quickly call accept or reject on them
	private List<ComputeNode> proposedNodes = new ArrayList<ComputeNode>(1000);
	
	protected int calls = 0;
	
	private final int maxSites; //Total number of sites

	final int[] polymorphicSites; //Global indices of all sites which are polymorphic, in increasing order
	final int[] invariantAAccumulator; //Counts of invariant sites with index less than i, so we can quickly compute numbers of invariant sites in a range
	final int[] invariantGAccumulator;
	final int[] invariantCAccumulator;
	final int[] invariantTAccumulator;
	
	
	//Storage for ComputeNodes so we dont have to reallocate them and so we dont run 
	//out of unique numbers for them
	protected ComputeNodePool nodePool;
	
	//Used to store results from a pattern collection
	private CollectionResult collectionResult = new CollectionResult();

	//Number of categories in rate model
    private final int rateCategories;
	
	//Description of how branch rate varies over sites
	SiteRateModel siteRateModel;	

	//For debugging
    private int computedNodeCount = 0;

    //Flag set when we want to force a recomputation of all transition matrices (for instance, if 
    //the base frequencies have changed
	protected boolean updateAllMatrices = true;
    
	public MultiRateCore(int numTips, MutationModel model, DataMatrix dataMatrix, SiteRateModel siteRateModel) {
		mutModel = model;
		this.maxSites = dataMatrix.getTotalColumnCount();
		tips = new TipComputeNode[numTips];
        this.rateCategories = siteRateModel.getCategoryCount();
		
        polymorphicSites = dataMatrix.getPolymorphicSites();
        
        nodePool = new ComputeNodePool(numTips, polymorphicSites.length, dataMatrix.getNumberOfPatterns()+10, siteRateModel.getCategoryCount());
        
        invariantAAccumulator = dataMatrix.getInvariantCumulator('A');
        invariantGAccumulator = dataMatrix.getInvariantCumulator('G');
        invariantCAccumulator = dataMatrix.getInvariantCumulator('C');
        invariantTAccumulator = dataMatrix.getInvariantCumulator('T');
        
//        if (polymorphicSites.length < (dataMatrix.getNumberOfPatterns()-4)) {
//        	throw new IllegalArgumentException("There can't be fewer polymorphic sites (" + polymorphicSites.length + ") than patterns (" + dataMatrix.getNumberOfPatterns() +") ");
//        }
        
        this.siteRateModel = siteRateModel;
                
        nextNodeNumber = -1; //We don't use this so make it invalid
	}
	
	/**
	 * Force a full recomputation of all transition matrices on the next call of updatePartials
	 *  ..this must be sure to recalculate the various intermediates as well, not just the branch-length dependent stuff...
	 */
	public void setUpdateAllMatrices() {
		updateAllMatrices = true;
	}
	
	/**
	 * Count the total number of patterns counted in the current state
	 * @return
	 */
	public int countPatterns() {
		int sum = 0;
		Stack<ComputeNode> stack = new Stack<ComputeNode>();
		for(ComputeNode rootRange : rootRanges) {
			stack.clear();
			stack.push(rootRange);
			while(! stack.isEmpty()) {
				ComputeNode node = stack.pop();
				sum += node.activeState.subtreeUniquePatternCount + 4; //Four for the invariant sites - we assume there's always all of these
				
				if (node.activeState.leftNode >= tips.length) {
					ComputeNode lNode = nodePool.getAssignedNode(node.activeState.leftNode);
					if (lNode != null)
						stack.push(lNode);
				}
				
				if (node.activeState.rightNode >= tips.length) {
					ComputeNode rNode = nodePool.getAssignedNode(node.activeState.rightNode);
					if (rNode != null)
						stack.push(rNode);
				}
			}
		}
		
		return sum;
	}
	
	
	public int getNodeCount() {
		return nodePool.getAssignedNodeCount();
	}
	
	
	public int countPatterns(TIntArrayList nodeIDs) {
		int sum = 0;
		for(int i=0; i<nodeIDs.size(); i++) {
			ComputeNode node = nodePool.getAssignedNode(nodeIDs.get(i));
			sum += node.activeState.subtreeUniquePatternCount;
		}
		return sum;
	}

	public void releaseNode(int nodeID) {
		nodePool.retireNode(nodeID);
	}

	
	/**
	 * Get compute node index of left child of given node
	 * @param nodeNumber
	 * @return
	 */
	public int getLeftChildForNode(int nodeNumber) {
		ComputeNode node = nodePool.getAssignedNode(nodeNumber);
		if (node == null) {
			return -1;
		}
		else {
			return node.activeState.leftNode;
		}
	}
	
	
	/**
	 * Get compute node index of right child of given node
	 * @param nodeNumber
	 * @return
	 */
	public int getRightChildForNode(int nodeNumber) {
		ComputeNode node = nodePool.getAssignedNode(nodeNumber);
		if (node == null) {
			return -1;
		}
		else {
			return node.activeState.rightNode;
		}
	}
	
	/**
	 * Obtain height of given node
	 * @param nodeNumber
	 * @return
	 */
	public double getHeightForNode(int nodeNumber) {
		ComputeNode node = nodePool.getAssignedNode(nodeNumber);
		if (node == null) {
			return -1;
		}
		else {
			return node.activeState.height;
		}
	}
	
	public int getStartSiteForNode(int nodeNumber) {
		ComputeNode node = nodePool.getAssignedNode(nodeNumber);
		if (node == null) {
			return -1;
		}
		else {
			return node.activeState.globalStartSite;
		}
	}

	/**
	 * The end site in the range of sites coalescing at the given node
	 * @param nodeNumber
	 * @return
	 */
	public int getEndSiteForNode(int nodeNumber) {
		ComputeNode node = nodePool.getAssignedNode(nodeNumber);
		if (node == null) {
			return -1;
		}
		else {
			return node.activeState.globalEndSite;
		}
	}

	
	/**
	 * True if any component of the given node is in the proposed state
	 * @param nodeNumber
	 * @return
	 */
	public boolean isNodeProposed(int nodeNumber) {
		throw new IllegalStateException("Does anything ever use this?");
//		ComputeNode node = nodePool.getAssignedNode(nodeNumber);
//		if (node == null) {
//			throw new IllegalStateException("Node " + nodeNumber + " does not exist");
//		}
//		else {
//			return node.activeState.leftNode != node.currentState.leftNode
//					|| node.activeState.rightNode != node.currentState.rightNode
//					|| node.activeState.subtreePatternMap != node.currentState.subtreePatternMap
//					|| node.activeState.height != node.currentState.height
//					|| node.activeState.globalStartSite != node.currentState.globalStartSite
//					|| node.activeState.globalEndSite != node.currentState.globalEndSite;
//		}
	}
	
	/**
	 * Create a new tipNode with the given tip number / index, and assign it the given vector of states. 
	 * @param tipNumber
	 * @param states
	 * @return id 
	 */
	public void initializeTipState(int tipNumber, int[] states) {
		TipComputeNode tip = new TipComputeNode(); 
		tips[tipNumber] = tip;
		tip.states = new int[polymorphicSites.length];
		for(int i=0; i<polymorphicSites.length; i++) {
			tip.states[i] = states[ polymorphicSites[i]];
		}
		
		tip.activeState.height = 0;
		
		//If there are no polymorphic sites, dont attempt to compute aliases
		if (polymorphicSites.length == 0)
			return;
	}
	
	public void proposeAll() {
		updateAllMatrices = true;
	}

	
	public void accept() {
		calls++;

		//System.out.println("Calculated dl for " + computedNodeCount + " nodes");
//		System.out.println("Core has allocated " + nodePool.allocatedNodeCount + " nodes, allocated nodes " + nodePool.getAssignedNodeCount() + " pool size: " + nodePool.getPoolSize());
//		nodePool.allocatedNodeCount = 0;
//		computedNodeCount = 0;

		for(ComputeNode node : proposedNodes) {
			node.currentState.rootDL = node.activeState.rootDL;
			node.currentState.startIndex = node.activeState.startIndex;
			node.currentState.endIndex = node.activeState.endIndex;
			node.currentState.globalStartSite = node.activeState.globalStartSite;
			node.currentState.globalEndSite = node.activeState.globalEndSite;
			node.currentState.height = node.activeState.height;

			node.currentState.leftNode = node.activeState.leftNode;
			node.currentState.rightNode = node.activeState.rightNode;				

			if (node.activeState.partials == node.proposedState.partials) {
				double[][][] tmp = node.currentState.partials;
				node.currentState.partials = node.proposedState.partials;
				node.proposedState.partials = tmp;

				tmp = node.currentState.invariantPartials;
				node.currentState.invariantPartials = node.proposedState.invariantPartials;
				node.proposedState.invariantPartials = tmp;
			}

			
			if (node.activeState.lMatrices == node.proposedState.lMatrices) {
				double[][][] tmp = node.currentState.lMatrices;
				node.currentState.lMatrices = node.proposedState.lMatrices;
				node.proposedState.lMatrices = tmp;
				node.currentState.lLength = node.activeState.lLength;
				node.activeState.lMatrices = node.currentState.lMatrices;
			}
			
			if (node.activeState.rMatrices == node.proposedState.rMatrices) {
				double[][][] tmp = node.currentState.rMatrices;
				node.currentState.rMatrices = node.proposedState.rMatrices;
				node.proposedState.rMatrices = tmp;
				node.currentState.rLength = node.activeState.rLength;
				node.activeState.rMatrices = node.currentState.rMatrices;
			}
			
			if (node.activeState.subtreePatternIndices == node.proposedState.subtreePatternIndices) {
				int[] tmpi = node.currentState.subtreePatternIndices;
				node.currentState.subtreePatternIndices = node.proposedState.subtreePatternIndices;
				node.proposedState.subtreePatternIndices = tmpi;

				tmpi = node.currentState.subtreePatternMap;
				node.currentState.subtreePatternMap = node.proposedState.subtreePatternMap;
				node.proposedState.subtreePatternMap = tmpi;

				tmpi = node.currentState.partialsMap;
				node.currentState.partialsMap = node.proposedState.partialsMap;
				node.proposedState.partialsMap = tmpi;

				node.currentState.subtreeUniquePatternCount = node.proposedState.subtreeUniquePatternCount;
			}

		}

		updateAllMatrices = false;
		proposedNodes.clear();
	}
	
	/**
	 * Reject current state. All activeState refs are set to currentState
	 */
	public void reject() {
		calls++;

		//System.out.println("Calculated dl for " + computedNodeCount + " nodes");
//		System.out.println("Core has allocated " + nodePool.allocatedNodeCount + " nodes, allocated nodes " + nodePool.getAssignedNodeCount() + " pool size: " + nodePool.getPoolSize());                nodePool.allocatedNodeCount = 0;
//		computedNodeCount = 0;
//		nodePool.allocatedNodeCount = 0;

		for(ComputeNode node : proposedNodes) {
			node.activeState.startIndex = node.currentState.startIndex;
			node.activeState.endIndex = node.currentState.endIndex;
			node.activeState.height = node.currentState.height;
			node.activeState.rightNode = node.currentState.rightNode;
			node.activeState.leftNode = node.currentState.leftNode;
			node.activeState.partials = node.currentState.partials;
			node.activeState.rootDL = node.currentState.rootDL;

			node.activeState.globalStartSite = node.currentState.globalStartSite;
			node.activeState.globalEndSite = node.currentState.globalEndSite;

			node.activeState.subtreePatternIndices = node.currentState.subtreePatternIndices;
			node.activeState.subtreePatternMap = node.currentState.subtreePatternMap;
			node.activeState.subtreeUniquePatternCount = node.currentState.subtreeUniquePatternCount;

			node.activeState.invariantPartials = node.currentState.invariantPartials;

			node.activeState.partialsMap = node.currentState.partialsMap;
			
			node.activeState.lMatrices = node.currentState.lMatrices;
			node.activeState.rMatrices = node.currentState.rMatrices;
			node.activeState.lLength = node.currentState.lLength;
			node.activeState.rLength = node.currentState.rLength;
		}

		updateAllMatrices = false;
		proposedNodes.clear();
	}

	/**
	 * Just propose a new height for the given node. This assumes the node already exists and has the
	 * partials initialized to the correct number
	 * @param nodeNumber
	 * @param nodeHeight
	 */
	public void proposeNode(int nodeNumber, double nodeHeight) {
		ComputeNode node = nodePool.getAssignedNode(nodeNumber);
		
		if (verbose)
			System.out.println("Proposing node " + nodeNumber + " new height " + nodeHeight);
		
		node.proposedState.height = nodeHeight;
		node.activeState.height = nodeHeight;
		node.activeState.partials = node.proposedState.partials;
	}

	
	public void proposeRange(int nodeNumber, 
								double nodeHeight,
								int globalMin, 
								int globalMax, 
								int lNode,
								int rNode) {

		ComputeNode node = nodePool.getAssignedNode(nodeNumber);
		
		if (verbose)
			System.out.println("Proposing node " + nodeNumber + " height : " + nodeHeight + " start: " + globalMin + " end: " + globalMax);
		
		node.proposedState.leftNode = lNode;
		node.proposedState.rightNode = rNode;
		node.proposedState.globalStartSite = globalMin;
		node.proposedState.globalEndSite = globalMax;
		
		if (node.proposedState.globalStartSite == node.currentState.globalStartSite)
			node.proposedState.startIndex = node.currentState.startIndex;
		else
			node.proposedState.startIndex = findIndexForSite(polymorphicSites, globalMin);
		
		if (node.proposedState.globalEndSite == node.currentState.globalEndSite)
			node.proposedState.endIndex = node.currentState.endIndex;
		else
			node.proposedState.endIndex = findIndexForSite(polymorphicSites, globalMax);

		node.proposedState.height = nodeHeight;
		
		node.activeState.leftNode = node.proposedState.leftNode;
		node.activeState.rightNode = node.proposedState.rightNode;
		node.activeState.startIndex = node.proposedState.startIndex;
		node.activeState.endIndex = node.proposedState.endIndex;
		node.activeState.globalStartSite = node.proposedState.globalStartSite;
		node.activeState.globalEndSite = node.proposedState.globalEndSite;
		node.activeState.rootDL = node.proposedState.rootDL;
		node.activeState.height = node.proposedState.height;
		node.activeState.partials = node.proposedState.partials;
		
		ComputeNode leftChild = null;
		if (node.activeState.leftNode >= tips.length)
			leftChild = nodePool.getAssignedNode(node.activeState.leftNode);

		ComputeNode rightChild = null;
		if (node.activeState.rightNode >= tips.length)
			rightChild = nodePool.getAssignedNode(node.activeState.rightNode);
		
		//Only collect patterns if our structure has changed or if either daughter node's patterns have changed
		if ( node.activeState.leftNode != node.currentState.leftNode 
				|| node.activeState.rightNode != node.currentState.rightNode
				|| (rightChild != null && rightChild.activeState.subtreePatternIndices != rightChild.currentState.subtreePatternIndices)
				|| (leftChild != null && leftChild.activeState.subtreePatternIndices != leftChild.currentState.subtreePatternIndices)
				|| node.activeState.startIndex != node.currentState.startIndex
				|| node.activeState.endIndex != node.currentState.endIndex) {
			collectPatterns(node);	
		}
	} 

	/**
	 * Returns a ref ID corresponding to an unused compute node. Unlike other compute cores, here we maintain
	 * a pool of unused nodes so we can easily know which nodes are unused. Other ComputeCores fail
	 * when the number of allocated nodes exceeds Integer.MAX_VALUE, which may happen in really long runs.  
	 */
	public int nextNumber() {
		ComputeNode newNode = nodePool.assignNode();
		return newNode.refID;
	}

	public void emitState() {
		System.out.println("ComputeCore tracking " + nodePool.getAssignedNodeCount() + " ComputeNodes...");
	}

	
	private boolean isNodeProposed(ComputeNode node) {
		return (node.activeState.partials == node.proposedState.partials); 
	}

	public void clearRootRanges() {
		rootRanges.clear();
	}

	/**
	 * Add the ComputeNode with the given ID to the list of nodes to compute rootDL over 
	 * @param refNodeID
	 */
	public void setRootRangeNode(int refNodeID) {
		ComputeNode node = nodePool.getAssignedNode(refNodeID);
		rootRanges.add(node);
	}

	
	/**
	 * Debugging (slow) function which returns the rootDL for range of sites. This is useful if we want
	 * to compute the DL for a marginal tree, for instance. 
	 * @param startSite
	 * @param endSite
	 * @return Root DL for a range of sites
	 */
	public double computeRootLogDLForRange(int startSite, int endSite) {
		double logDL = 0;
		for(ComputeNode rootNode : rootRanges) {
			logDL += computeLogRootDLForRange(rootNode, startSite, endSite);
		}
		return logDL;
	}
	
	public double computeRootLogDL() {
		if (calls < 10000 || calls % 1000==0) //Periodically occurring validity check
			checkRootRanges();
		
		double logDL = 0;
		for(ComputeNode rootNode : rootRanges) {
			logDL += computeLogRootDLForRange(rootNode);
		}

		return logDL;
	}
	
	/**
	 * Debugging function to check the consistency of the root ranges - they should all be adjacent and not overlap and
	 * cover all sites
	 */
	private void checkRootRanges() {
		List<ComputeNode> sortedRanges = new ArrayList<ComputeNode>();
		sortedRanges.addAll(rootRanges);
		
		Collections.sort(sortedRanges, new ComputeRangeComparator());
		
		if (sortedRanges.size()==0) {
			emitRanges(sortedRanges);
			throw new IllegalStateException("No root ranges have been assiged");
		}
		
		if (sortedRanges.get(0).activeState.startIndex != 0) {
			emitRanges(sortedRanges);
			throw new IllegalStateException("First root range first site is not zero (it's " + sortedRanges.get(0).activeState.startIndex + ")");	
		}
		
		
		for(int i=1; i<sortedRanges.size(); i++) {
			if (sortedRanges.get(i).activeState.globalStartSite != sortedRanges.get(i-1).activeState.globalEndSite) {
				emitRanges(sortedRanges);
				throw new IllegalStateException("Root ranges are not adjacent");
			}
		}
		
		ComputeNode last = sortedRanges.get( sortedRanges.size()-1);
		if (last.activeState.endIndex != polymorphicSites.length || last.activeState.globalEndSite != maxSites)
			throw new IllegalStateException("Root ranges do not go to last site (just " + last.activeState.endIndex + ")");
				
	}

	private void emitRanges(List<ComputeNode> nodes) {
		System.out.println("Ranges are : \n");
		for(ComputeNode node : nodes) {
			System.out.println("Node #" + node.refID + " height: " + node.activeState.height + " start: " + node.activeState.globalStartSite + "  end: " + node.activeState.globalEndSite + " l=" + node.activeState.leftNode + " r=" + node.activeState.rightNode);
		}
	}
	


	private TIntIntHashMap createCardinalityMap(int[] aliases, int start, int end) {
		TIntIntHashMap map = patternCache;
		map.clear();
		for(int i=start; i<end; i++) {
			int count = map.get(aliases[i]); //Returns 0 if element is not in there
			map.put(aliases[i], count+1);
		}
		return map;
	}

	/**
	 * Compute and return the root DL for the specified node 
	 * @param rootNode
	 * @return
	 */
	private double computeLogRootDLForRange(ComputeNode rootNode, int globalStartSite, int globalEndSite) {
		double logDL = 0;
		
		//Before anything happens, intersect this node's global sites with the requested global sites, and return 0
		//if there's no overlap. This is actually critical otherwise we will try to compute invariants for sites that
		//aren't in our range
		int globalTrueStart = Math.max(globalStartSite, rootNode.activeState.globalStartSite); //As with polymorphic sites above, we must intersect
		int globalTrueEnd = Math.min(globalEndSite, rootNode.activeState.globalEndSite);	//The requested site range with our own range
		
		
		if (globalTrueEnd <= globalTrueStart) {
			return 0.0;
		}
		
		
		//If we're computing for the whole range, we may have already stored the DL
		//in state.rootDL, if so, just return it. If not, set a flag so that when we do
		//compute it we will store it. This yields a decent speedup, actually
		if (!isNodeProposed(rootNode) 
				&& !Double.isNaN(rootNode.currentState.rootDL)
				&& globalStartSite == rootNode.activeState.globalStartSite
				&& globalEndSite == rootNode.activeState.globalEndSite) {
			return rootNode.currentState.rootDL;
		}
		
		
		//Convert from global sites to indices in polymorphic sites
		int start = findIndexForSite(polymorphicSites, globalStartSite);
		int end = findIndexForSite(polymorphicSites, globalEndSite);
		
		
		double[][][] partials = rootNode.activeState.partials; //Should be active, not proposed state, because not all root ranges will be activeProposed (some may not have been changed by the last modification)
		int polyStart = Math.max(start, rootNode.activeState.startIndex); //Index is in terms of the polymorphic sites array, not global sites
		int polyEnd = Math.min(end, rootNode.activeState.endIndex);
		
		int startIndex = polyStart - rootNode.activeState.startIndex; //First index in partials where we begin calculating
		int endIndex = polyEnd - rootNode.activeState.startIndex;	   //One after last index of partials to calculate

		//Lookup stationaries so we're not constantly finding them again
		final double stat0 = mutModel.getStationaryForState(0);
		final double stat1 = mutModel.getStationaryForState(1);
		final double stat2 = mutModel.getStationaryForState(2);
		final double stat3 = mutModel.getStationaryForState(3);
		
		
		//If there are no polymorphic sites in this region then skip this part.....
		if (endIndex > startIndex) {
			
			TIntIntHashMap cardinalityMap = createCardinalityMap(rootNode.activeState.subtreePatternMap, startIndex, endIndex); //Counts the number of times the pattern appears

			for(int i=0; i<rootNode.activeState.subtreeUniquePatternCount; i++) {
				int pattern = rootNode.activeState.subtreePatternIndices[i];
				int aliasedSite = rootNode.activeState.subtreePatternMap[pattern];

				int cardinality = cardinalityMap.get(aliasedSite);
				
				//Sometimes no sites are within range, don't attempt computing if so
				if (cardinality ==0) { 
					continue;   				
				}

				double siteProb = 0;
				for(int j=0; j<rateCategories; j++) {
					double[] sitePartials = partials[j][i];
					if (siteRateModel.getProbForCategory(j)==0)
						throw new IllegalStateException("Site prob is zero for category " + j);
					siteProb +=  siteRateModel.getProbForCategory(j)*( sitePartials[0] * stat0
										 + sitePartials[1] * stat1
										 + sitePartials[2] * stat2
										 + sitePartials[3] * stat3);
					
//					if (siteProb == 0) {
//						throw new IllegalStateException("Site prob is zero for index " + i + " site partials: " + sitePartials + " prob: " + siteRateModel.getProbForCategory(j));
//					}
				}
				logDL +=  Math.log(siteProb) * cardinality; 		
			}
			
			if (Double.isInfinite(logDL) || Double.isNaN(logDL)  ) {
				throw new IllegalStateException("Invalid logDL calculated for root node " + rootNode + "\n DL: " + logDL);
			}
		}

		//Now do the invariant sites. These are always indexed with the global sites system, 
		//not the polymorphic sites indices
		int[] invariantCounts = countInvariants(globalStartSite, globalEndSite);
		
		for(int i=0; i<4; i++) {
			double siteProb = 0;
			for(int j=0; j<rateCategories; j++) {
				double[] iPartials = rootNode.activeState.invariantPartials[j][i];
				siteProb +=  siteRateModel.getProbForCategory(j)*(iPartials[0] * stat0
						+ iPartials[1] * stat1
						+ iPartials[2] * stat2
						+ iPartials[3] * stat3);
			}

			logDL += Math.log(siteProb) * invariantCounts[i];                        
		}

		
		if (Double.isInfinite(logDL)) {
			System.out.println("Prob is infinite for invariant patterns");
		}

		
		if (rootNode.activeState.globalStartSite == globalTrueStart
				&& rootNode.activeState.globalEndSite == globalTrueEnd) {
			rootNode.proposedState.rootDL = logDL;
			rootNode.activeState.rootDL = rootNode.proposedState.rootDL;
		}
		else {
			//We must poison the root dl if we don't remember it
			rootNode.proposedState.rootDL = Double.NaN;
			rootNode.activeState.rootDL = Double.NaN;
		}
		return logDL;
	}


	
	/**
	 * Returns an array of ints such that array[DNAUtils.X] is the number of invariant sites 
	 * with symbol X between start and end
	 * @param globalStart
	 * @param globalEnd
	 * @return
	 */
	private int[] countInvariants(int globalStart, int globalEnd) {
		int[] invarCounts = new int[4];
		invarCounts[DNAUtils.A] = invariantAAccumulator[globalEnd] -   invariantAAccumulator[globalStart];
		invarCounts[DNAUtils.G] = invariantGAccumulator[globalEnd] -   invariantGAccumulator[globalStart];
		invarCounts[DNAUtils.C] = invariantCAccumulator[globalEnd] -   invariantCAccumulator[globalStart];
		invarCounts[DNAUtils.T] = invariantTAccumulator[globalEnd] -   invariantTAccumulator[globalStart];
		return invarCounts;
	}


	private double computeLogRootDLForRange(ComputeNode rootNode) {
		return computeLogRootDLForRange(rootNode, rootNode.activeState.globalStartSite, rootNode.activeState.globalEndSite);
	}
	
	/**
	 * Compute partials for a node that is above 
	 * @param node
	 * @param tipL
	 * @param tipR
	 */
	private void computePartialsForTwoTips(ComputeNode node, TipComputeNode tipL, TipComputeNode tipR) {
		NodeState state = node.activeState;
		
		final int[] tipLStates = tipL.states;
		final int[] tipRStates = tipR.states;
		
		for(int i=0; i<rateCategories; i++) {
			double[][] partials = node.proposedState.partials[i];

			computeForTwoStatesWithMap(state.lMatrices[i], state.rMatrices[i], 
					state.subtreeUniquePatternCount, state.subtreePatternIndices, 
					tipLStates,
					tipRStates, 
					partials, state.subtreePatternMap, state.startIndex);

			computeInvariantsForTwoStates(state.lMatrices[i], state.rMatrices[i], node.proposedState.invariantPartials[i]);
		}
	}
	
	/**
	 * Compute partials for a node whose left child is a tip (with known states)
	 * @param node
	 * @param tip
	 * @param childNode
	 */
	private void computePartialsForLTip(ComputeNode node,
			TipComputeNode tip,
			ComputeNode childNode) {
		NodeState state = node.activeState;

		final int[] tipStates = tip.states;

		for(int i=0; i<rateCategories; i++) {
			double[][] childPartials = childNode.activeState.partials[i];

			double[][] partials = node.proposedState.partials[i]; //We always write new partials into the proposed state

			computeProbsForStateWithMap(state.rMatrices[i], state.lMatrices[i], 
					state.subtreeUniquePatternCount, state.subtreePatternIndices,
					childPartials, childNode.activeState.subtreePatternMap, childNode.activeState.partialsMap, childNode.activeState.startIndex, 
					tipStates,
					partials, state.subtreePatternMap, state.startIndex);

			computeInvariantsForState(state.rMatrices[i], state.lMatrices[i], 
					childNode.activeState.invariantPartials[i], node.proposedState.invariantPartials[i]);
		}

	}


	/**
	 * Compute partials for a node whose right child is a tip (with known states)
	 * @param node
	 * @param tip
	 * @param childNode
	 */
	private void computePartialsForRTip(ComputeNode node,
			TipComputeNode tip,
											ComputeNode childNode) {
		NodeState state = node.activeState;
		
		final int[] tipStates = tip.states;
		
		for(int i=0; i<rateCategories; i++) {
			double[][] childPartials = childNode.activeState.partials[i];

			double[][] partials = node.proposedState.partials[i]; //We always write new partials into the proposed state

			computeProbsForStateWithMap(state.lMatrices[i], state.rMatrices[i], 
					state.subtreeUniquePatternCount, state.subtreePatternIndices,
					childPartials, childNode.activeState.subtreePatternMap, childNode.activeState.partialsMap, childNode.activeState.startIndex, 
					tipStates,
					partials, state.subtreePatternMap, state.startIndex);

			computeInvariantsForState(state.lMatrices[i], state.rMatrices[i], 
					childNode.activeState.invariantPartials[i], node.proposedState.invariantPartials[i]);
		}
			
	}
	
	/**
	 * Checks to see if either transition matrix for the given node needs to be updated, if so we calculate
	 * a new one and store it in the proposed state
	 * @param node
	 */
	private void checkUpdateMatrices(ComputeNode node) {
		ComputeNode leftChild = null; 
		ComputeNode rightChild = null; 
		
		if (node.activeState.leftNode < tips.length) {
			node.proposedState.lLength = node.activeState.height - 0; //Change this someday if we want tips to have nonzero height
		}
		else {
			leftChild = nodePool.getAssignedNode(node.activeState.leftNode);
			node.proposedState.lLength = node.activeState.height - leftChild.activeState.height;
		}
		
		if (node.activeState.rightNode < tips.length) {
			node.proposedState.rLength = node.activeState.height - 0; //Change this someday if we want tips to have nonzero height
		}
		else {
			rightChild = nodePool.getAssignedNode(node.activeState.rightNode);
			node.proposedState.rLength = node.activeState.height - rightChild.activeState.height;
		}
		
		node.activeState.lLength = node.proposedState.lLength;
		node.activeState.rLength = node.proposedState.rLength;
		
		final boolean recalcLeft = updateAllMatrices || (node.currentState.lLength != node.activeState.lLength);
		final boolean recalcRight = updateAllMatrices || (node.currentState.rLength != node.activeState.rLength);

		if (recalcLeft) {
			for(int i=0; i<siteRateModel.getCategoryCount(); i++) {
				double branchRate = siteRateModel.getRateForCategory(i);
				mutModel.setMatrix(node.activeState.lLength*branchRate, node.proposedState.lMatrices[i]);
			}
			//System.out.println("Updating left matrix for node " + node.refID + " old dist: " + node.currentState.lLength + " new dist: " + node.proposedState.lLength);
			node.activeState.lMatrices = node.proposedState.lMatrices;
		}
		else {
			//System.out.println("Not updating left matrix for node " + node.refID);
			node.activeState.lMatrices = node.currentState.lMatrices;
		}
		
		if (recalcRight) {
			for(int i=0; i<siteRateModel.getCategoryCount(); i++) {
				double branchRate = siteRateModel.getRateForCategory(i);
				mutModel.setMatrix(node.activeState.rLength*branchRate, node.proposedState.rMatrices[i]);
			}
			//System.out.println("Updating right matrix for node " + node.refID + " old dist: " + node.currentState.rLength + " new dist: " + node.proposedState.rLength);
			node.activeState.rMatrices = node.proposedState.rMatrices;
		}
		else {
			//System.out.println("Not updating right matrix for node " + node.refID);
			node.activeState.rMatrices = node.currentState.rMatrices;
		}
	}

	/**
	 * Compute new partials for range of proposed sites
	 */
//	public void computePartials(int nodeNumber) {
//        ComputeNode node = nodePool.getAssignedNode(nodeNumber);
//		
//        checkUpdateMatrices(node);
//        
//		NodeState state = node.activeState;
//		
//		
//		int leftNodeNum = state.leftNode;
//		int rightNodeNum = state.rightNode;
//		
//		if (leftNodeNum < tips.length) {
//			TipComputeNode tipL = tips[leftNodeNum];
//			
//			if (rightNodeNum < tips.length) {
//				if (!updateAllMatrices && !isNodeProposed(node) ) //If we're above two tips, then don't compute only if we are not proposed
//					return;
//				
//				//We're above two tips
//				TipComputeNode tipR = tips[rightNodeNum];
//				
//				if (verbose) {
//					System.out.println("ComputeNode #" + nodeNumber + " above tip " + state.leftNode + " and tip " + state.rightNode + " min: " + node.activeState.globalStartSite + " max: " + node.activeState.globalEndSite + " is computing");
//				}
//				computedNodeCount++;
//				proposedNodes.add(node);
//				computePartialsForTwoTips(node, tipL, tipR);
//				node.activeState.partials = node.proposedState.partials;
//				node.activeState.invariantPartials = node.proposedState.invariantPartials;
//				return;
//			}
//			else {
//				
//				//Left is tip but right is not
//				ComputeNode rChild = nodePool.getAssignedNode(state.rightNode);
//				
//				if (!updateAllMatrices && !isNodeProposed(node) && (!isNodeProposed(rChild)) ) //Dont compute if both node and rChild are not proposed
//					return;
//				
//				
//				if (verbose) {
//					System.out.println("ComputeNode #" + nodeNumber + " above tip " + state.leftNode + " and node " + state.rightNode + " min: " + node.activeState.globalStartSite + " max: " + node.activeState.globalEndSite + " is computing");
//				}
//				computedNodeCount++;
//				proposedNodes.add(node);
//				computePartialsForLTip(node, tipL, rChild);
//				node.activeState.partials = node.proposedState.partials;
//				node.activeState.invariantPartials = node.proposedState.invariantPartials;
//			}
//			
//		}
//		else {
//			
//			if (rightNodeNum < tips.length) {
//				//Right is tip but left is not
//				TipComputeNode tipR = tips[rightNodeNum];
//				ComputeNode lChild = nodePool.getAssignedNode(state.leftNode);
//				
//				if (!updateAllMatrices && !isNodeProposed(node) && (!isNodeProposed(lChild)) ) //Dont compute if both node and lChild are not proposed
//					return;
//				
//				if (verbose) {
//					System.out.println("ComputeNode #" + nodeNumber + " above tip " + state.rightNode + " and node " + state.leftNode + " min: " + node.activeState.globalStartSite + " max: " + node.activeState.globalEndSite + " is computing");
//				}
//				computedNodeCount++;
//				proposedNodes.add(node);
//				computePartialsForRTip(node, tipR, lChild);
//				node.activeState.partials = node.proposedState.partials;
//				node.activeState.invariantPartials = node.proposedState.invariantPartials;
//			}
//			else {
//				//Neither node is a tip
//				ComputeNode rChild = nodePool.getAssignedNode(state.rightNode);
//				ComputeNode lChild = nodePool.getAssignedNode(state.leftNode);
//				
//				if (!updateAllMatrices && !isNodeProposed(node) && (!isNodeProposed(rChild)) && (!isNodeProposed(lChild))) //Dont compute this and both descendants have not been altered
//					return;
//				
//			
//				computedNodeCount++;
//
//				proposedNodes.add(node);
//		
//				if (verbose) {
//					System.out.println("ComputeNode #" + nodeNumber + " above node " + state.leftNode + " and node " + state.rightNode + " min: " + node.activeState.globalStartSite + " max: " + node.activeState.globalEndSite + " is computing");
//				}
//				
//				
//				for(int i=0; i<siteRateModel.getCategoryCount(); i++) {
//					computeProbsWithMap(state.lMatrices[i], state.rMatrices[i], 
//							state.subtreeUniquePatternCount, state.subtreePatternIndices,
//							lChild.activeState.partials[i], lChild.activeState.subtreePatternMap, lChild.activeState.partialsMap, lChild.activeState.startIndex,
//							rChild.activeState.partials[i], rChild.activeState.subtreePatternMap, rChild.activeState.partialsMap, rChild.activeState.startIndex,
//							node.proposedState.partials[i], node.activeState.subtreePatternMap, node.activeState.startIndex);
//
//
//					computeInvariants(state.lMatrices[i], state.rMatrices[i], 
//										lChild.activeState.invariantPartials[i], 
//										rChild.activeState.invariantPartials[i], 
//										node.proposedState.invariantPartials[i]);
//
//
//					node.activeState.partials = node.proposedState.partials;
//					node.activeState.invariantPartials = node.proposedState.invariantPartials;
//				}
//			}
//			
//		}
//		
//	}
	
	
	/**
	 * Updates transition matrices for the left branch from this node if the active lLength field is not 
	 * equal to the current lLength
	 * @param node
	 */
	private void updateLeftMatrix(ComputeNode node) {
		if (updateAllMatrices || node.activeState.lLength != node.currentState.lLength) {
			for(int i=0; i<siteRateModel.getCategoryCount(); i++) {
				double branchRate = siteRateModel.getRateForCategory(i);
				mutModel.setMatrix(node.activeState.lLength*branchRate, node.proposedState.lMatrices[i]);
			}
			node.activeState.lMatrices = node.proposedState.lMatrices;
		}
		else {
			node.activeState.lMatrices = node.currentState.lMatrices;
		}
	}
	
	/**
	 * Updates transition matrices for the right branch from this node if the active rLength field is not 
	 * equal to the current rLength
	 */
	private void updateRightMatrix(ComputeNode node) {
		if (updateAllMatrices || node.activeState.rLength != node.currentState.rLength) {
			for(int i=0; i<siteRateModel.getCategoryCount(); i++) {
				double branchRate = siteRateModel.getRateForCategory(i);
				mutModel.setMatrix(node.activeState.rLength*branchRate, node.proposedState.rMatrices[i]);
			}
			node.activeState.rMatrices = node.proposedState.rMatrices;
		}
		else {
			node.activeState.rMatrices = node.currentState.rMatrices;
		}
	}

	/**
	 * Compute new partials for range of proposed sites
	 */
	public void computePartials(int nodeNumber) {
        ComputeNode node = nodePool.getAssignedNode(nodeNumber);
		NodeState state = node.activeState;
		
		
		int leftNodeNum = state.leftNode;
		int rightNodeNum = state.rightNode;
		
		if (leftNodeNum < tips.length) {
			TipComputeNode tipL = tips[leftNodeNum];
			
			node.proposedState.lLength = node.activeState.height;
			node.activeState.lLength = node.proposedState.lLength;
			updateLeftMatrix(node);
			
			
			if (rightNodeNum < tips.length) {
				
				//We're above two tips
				TipComputeNode tipR = tips[rightNodeNum];
				
				node.proposedState.rLength = node.activeState.height;
				node.activeState.rLength = node.proposedState.rLength;
				updateRightMatrix(node);
				
				if (!updateAllMatrices && !isNodeProposed(node) ) //If we're above two tips, then don't compute only if we are not proposed
					return;
				
				
				if (verbose) {
					System.out.println("ComputeNode #" + nodeNumber + " above tip " + state.leftNode + " and tip " + state.rightNode + " min: " + node.activeState.globalStartSite + " max: " + node.activeState.globalEndSite + " is computing");
				}
				computedNodeCount++;
				proposedNodes.add(node);
				computePartialsForTwoTips(node, tipL, tipR);
				node.activeState.partials = node.proposedState.partials;
				node.activeState.invariantPartials = node.proposedState.invariantPartials;
				return;
			}
			else {
				
				//Left is tip but right is not
				ComputeNode rChild = nodePool.getAssignedNode(state.rightNode);
				
				node.proposedState.rLength = node.activeState.height - rChild.activeState.height;
				node.activeState.rLength = node.proposedState.rLength;
				updateRightMatrix(node);
				
				if (!updateAllMatrices && !isNodeProposed(node) && (!isNodeProposed(rChild)) ) //Dont compute if both node and rChild are not proposed
					return;
				
				
				if (verbose) {
					System.out.println("ComputeNode #" + nodeNumber + " above tip " + state.leftNode + " and node " + state.rightNode + " min: " + node.activeState.globalStartSite + " max: " + node.activeState.globalEndSite + " is computing");
				}
				computedNodeCount++;
				proposedNodes.add(node);
				computePartialsForLTip(node, tipL, rChild);
				node.activeState.partials = node.proposedState.partials;
				node.activeState.invariantPartials = node.proposedState.invariantPartials;
			}
			
		}
		else {
			//Left child is not a tip

			ComputeNode lChild = nodePool.getAssignedNode(state.leftNode);
			node.proposedState.lLength = node.activeState.height - lChild.activeState.height;
			node.activeState.lLength = node.proposedState.lLength;
			updateLeftMatrix(node);
			
			if (rightNodeNum < tips.length) {
				//Right is tip but left is not
				TipComputeNode tipR = tips[rightNodeNum];
				
				node.proposedState.rLength = node.activeState.height;
				node.activeState.rLength = node.proposedState.rLength;
				updateRightMatrix(node);
				
				if (!updateAllMatrices && !isNodeProposed(node) && (!isNodeProposed(lChild)) ) //Dont compute if both node and lChild are not proposed
					return;
				
				if (verbose) {
					System.out.println("ComputeNode #" + nodeNumber + " above tip " + state.rightNode + " and node " + state.leftNode + " min: " + node.activeState.globalStartSite + " max: " + node.activeState.globalEndSite + " is computing");
				}
				computedNodeCount++;
				proposedNodes.add(node);
				computePartialsForRTip(node, tipR, lChild);
				node.activeState.partials = node.proposedState.partials;
				node.activeState.invariantPartials = node.proposedState.invariantPartials;
			}
			else {
				//Neither node is a tip
				ComputeNode rChild = nodePool.getAssignedNode(state.rightNode);
				node.proposedState.rLength = node.activeState.height - rChild.activeState.height;
				node.activeState.rLength = node.proposedState.rLength;
				updateRightMatrix(node);
				
				if (!updateAllMatrices && !isNodeProposed(node) && (!isNodeProposed(rChild)) && (!isNodeProposed(lChild))) //Dont compute this and both descendants have not been altered
					return;
				
				computedNodeCount++;
				proposedNodes.add(node);
		
				if (verbose) {
					System.out.println("ComputeNode #" + nodeNumber + " above node " + state.leftNode + " and node " + state.rightNode + " min: " + node.activeState.globalStartSite + " max: " + node.activeState.globalEndSite + " is computing");
				}
				
				
				for(int i=0; i<rateCategories; i++) {
					computeProbsWithMap(state.lMatrices[i], state.rMatrices[i], 
							state.subtreeUniquePatternCount, state.subtreePatternIndices,
							lChild.activeState.partials[i], lChild.activeState.subtreePatternMap, lChild.activeState.partialsMap, lChild.activeState.startIndex,
							rChild.activeState.partials[i], rChild.activeState.subtreePatternMap, rChild.activeState.partialsMap, rChild.activeState.startIndex,
							node.proposedState.partials[i], node.activeState.subtreePatternMap, node.activeState.startIndex);


					computeInvariants(state.lMatrices[i], state.rMatrices[i], 
										lChild.activeState.invariantPartials[i], 
										rChild.activeState.invariantPartials[i], 
										node.proposedState.invariantPartials[i]);


					node.activeState.partials = node.proposedState.partials;
					node.activeState.invariantPartials = node.proposedState.invariantPartials;
				}
			}
			
		}
		
	}

	
	/**
	 * Compute subtree aliases for this node based on the state of the two children...
	 * Returns true if the patterns have changed for this node
	 */
	private void collectPatterns(ComputeNode node) {
				
		node.activeState.subtreePatternIndices = node.proposedState.subtreePatternIndices;
		node.activeState.subtreePatternMap = node.proposedState.subtreePatternMap;
		node.activeState.partialsMap = node.proposedState.partialsMap;
		
		//Don't try to collect patterns if there are no polymorphic sites
		if (polymorphicSites.length==0)
			return;
		
		patternCache.clear();
		
		//If true, tip patterns are collected in 'reciprocal' style, so that nodes above two tips will have a maximum 
		//of only 4 patterns, not 16. 
		final boolean reciprocalTipPatterns = true;
				
		int[] lAliases;
		int[] rAliases;
		int lOffset = 0;
		int rOffset = 0;
		
		final boolean leftIsTip = node.activeState.leftNode < tips.length;
		final boolean rightIsTip = node.activeState.rightNode < tips.length;
		



		if (leftIsTip)
			lAliases = tips[node.activeState.leftNode].states;
		else {
			ComputeNode lNode = nodePool.getAssignedNode(node.activeState.leftNode);
			//lPatterns = lNode.activeState.subtreeUniquePatternCount;
			lAliases = lNode.activeState.subtreePatternMap;
			lOffset = lNode.activeState.startIndex;
		}

		if (rightIsTip)
			rAliases = tips[node.activeState.rightNode].states;
		else {
			ComputeNode rNode = nodePool.getAssignedNode(node.activeState.rightNode);
			//rPatterns = rNode.activeState.subtreeUniquePatternCount;
			rAliases = rNode.activeState.subtreePatternMap;
			rOffset = rNode.activeState.startIndex;
		}



		//This bit will make it so things should break immediately if the map is referenced inappropriately
		//It hasn't tripped up any errors in a while, so it's turned off for now
		//		for(int i=0; i<node.activeState.partialsMap.length; i++) {
		//			node.activeState.partialsMap[i] = -1;
		//		}


		CollectionResult res = staticCollectPatterns(patternCache, 
				lAliases, lOffset, 
				rAliases, rOffset,
				node.activeState.subtreePatternIndices, 
				node.activeState.subtreePatternMap,
				node.activeState.startIndex, node.activeState.endIndex,
				node.activeState.partialsMap,
				node.currentState.subtreePatternMap,
				polymorphicSites,
				reciprocalTipPatterns && leftIsTip && rightIsTip);



		node.proposedState.subtreeUniquePatternCount = res.uniquePatternCount;
		node.activeState.subtreeUniquePatternCount = node.proposedState.subtreeUniquePatternCount;

		//If no patterns have changed, bump all refs back to current state. This prevents 
		//rootward nodes from recomputing their patterns for no reason
		if (!res.patternsChanged 
				&& node.currentState.globalStartSite == node.proposedState.globalStartSite 
				&& node.currentState.globalEndSite == node.proposedState.globalEndSite) {

			node.activeState.subtreePatternIndices = node.currentState.subtreePatternIndices;
			node.activeState.subtreePatternMap = node.currentState.subtreePatternMap;
			node.activeState.partialsMap = node.currentState.partialsMap;
			node.activeState.subtreeUniquePatternCount = node.currentState.subtreeUniquePatternCount;
		}
	}

	/**
	 * Algorithm for computing aliases, the indices of unique sites, and the mapping from sites to partials is all in here. 
	 * The inputs are two descendant lists of aliases in which all aliasable sites are indexed to the lowest index, which
	 * implies that there is not site where aliases[i] > i. 
	 * @param map Map from integer-integer, used to compute some intermediates
	 * @param lAliases Aliases from one descendant child
	 * @param lOffset Starting 'global' site for left aliases
	 * @param rAliases Aliases from other descendant child
	 * @param rOffset Start 'global' site for right aliases
	 * @param targetIndices This gets filled with the the indices at which unique sites exist - partials will be calculated for these sites
	 * @param targetMap The resulting list of aliases, 
	 * @param targetStart Offset for target alias list
	 * @param targetStop
	 * @param partialsMap Mapping from indices at which we calculate partials to the actual position in the partials array
	 * @return
	 */
	private CollectionResult staticCollectPatterns(TIntIntHashMap map,
			int[] lAliases, int lOffset,
			int[] rAliases, int rOffset,
			int[] targetIndices, int[] targetMap, int targetStart, int targetStop,
			int[] partialsMap,
			int[] currentMap,
			int[] polymorphicSites,
			boolean useReciprocality) {

		int uniquePatternCount = 0;
		boolean changed = false;

		final int arrayEnd = targetStop - targetStart;

		for(int i=0; i<arrayEnd; i++) {
			final int globalSite = i+targetStart;
			final int lIndex = lAliases[globalSite - lOffset]; //subtree index of global pattern for left child
			final int rIndex= rAliases[globalSite - rOffset];  //subtree index of global pattern for right child
			int alias = i;
			final int patternKey = 10000*lIndex + rIndex;
			int match = map.get(patternKey);

			//Pattern was not in map, so add it and mark this spot as the first of a new pattern
			if (match==0) {
				map.put(patternKey, i);
				if (useReciprocality)
					map.put(10000*rIndex+lIndex, i);
				
				alias = i;
				targetIndices[uniquePatternCount] = i;
				partialsMap[i] = uniquePatternCount;
				uniquePatternCount++;
			}
			else {
				alias = match;
			}

			targetMap[i] = alias;		
			if ((!changed) && alias != currentMap[i]) 
				changed = true;
		}		


		collectionResult.patternsChanged = changed;
		collectionResult.uniquePatternCount = uniquePatternCount;
		
		return collectionResult;
	}
	

	class TipComputeNode {
		
		int[] states;
		
		TipNodeState currentState = new TipNodeState();
		TipNodeState activeState = currentState;
		
	}
	
	class TipNodeState {
		
		double height = 0;
		
	}
	
	public class CollectionResult {
		int uniquePatternCount;
		boolean patternsChanged;
	}
	
	
	class ComputeRangeComparator implements Comparator<ComputeNode> {

		@Override
		public int compare(ComputeNode a, ComputeNode b) {
			return a.activeState.globalStartSite < b.activeState.globalStartSite ? -1 : 1;
		}
		
	}
	

	/*************************************** Some static DL computation stuff *************************************/
	
	
	private static void computeProbsWithMap(double[][] transMat1, double[][] transMat2,
			int maxPatternCount, int[] patternIndices,
			double[][] leftStates, int[] leftPatternMap, int[] leftPartialsMap, int leftOffset,
			double[][] rightStates, int[] rightPatternMap, int[] rightPartialsMap, int rightOffset,
			double[][] resultStates, int[] resultPatternMap, int resultOffset) {

		for(int index = 0; index<maxPatternCount; index++) {
			int pattern = patternIndices[index];
			int globalPattern = pattern+resultOffset;

			final int rightAlias = rightPatternMap[globalPattern - rightOffset];
			final int rightPartialsIndex = rightPartialsMap[rightAlias];
			
			final int leftAlias = leftPatternMap[globalPattern - leftOffset];
			final int leftPartialsIndex = leftPartialsMap[leftAlias];
			
			
			double[] resultProbs = resultStates[ index ];
			
			double[] leftProbs = leftStates[ leftPartialsIndex  ];
			double[] rightProbs = rightStates[ rightPartialsIndex  ]; 

			computeProbabilities(transMat1, transMat2, leftProbs, rightProbs, resultProbs);
		}

	}
	
	private static void computeInvariants(double[][] transMat1, double[][] transMat2,
			double[][] leftStates, double[][] rightStates, double[][] resultStates) {

		for(int index = 0; index<4; index++) {
			double[] resultProbs = resultStates[ index ];
			
			double[] leftProbs = leftStates[ index  ];
			double[] rightProbs = rightStates[ index ]; 

			computeProbabilities(transMat1, transMat2, leftProbs, rightProbs, resultProbs);
		}

	}
	
	/**
	 * Returns the index in an alias list where we should begin looking if we want to start at *global* site i.
	 * If there are no breakpoints, this is always zero. In the first-gen CompressionCore alias lists were 
	 * always a simple subrange of the global sites, so if we wanted global site i, we just looked up
	 * the element at index - startSite. In this version, the alias list is a subrange with all invariant
	 * sites removed 
	 * @param globalSite
	 * @return
	 */
	private static int findIndexForSite(int[] sites, int globalSite) {
		if (globalSite == 0)
			return 0;
		else {
			int index = Arrays.binarySearch(sites, globalSite);
			if (index < 0) {
				index *=-1;
				index--;
			}
			return index;
		}	
	}
	
	
	
	private static void computeProbsForStateWithMap(double[][] transMat1, double[][] transMat2, 
			int maxPatternCount,	int[] patternIndices, 
			double[][] probStates,	int[] probPatternMap, int[] probPartialsMap, int probOffset,
			int[] tipStates, 
			double[][] resultStates, int[] resultPatternMap, int resultOffset) {

		for(int index = 0; index<maxPatternCount; index++) {
			final int pattern = patternIndices[index];
			final int globalPattern = pattern + resultOffset;
			
			int probAlias = probPatternMap[globalPattern - probOffset];
			int probPartialsIndex = probPartialsMap[ probAlias ];
			double[] probs = probStates[ probPartialsIndex ]; 
			double[] resultProbs = resultStates[ index ]; 

			computeProbsForOneState(transMat2, transMat1, tipStates[globalPattern], probs, resultProbs);
		}
	}
	
	/**
	 * Compute invariant partials for a node above one tip and one non-tip
	 * @param transMat1
	 * @param transMat2
	 * @param probStates
	 * @param resultStates
	 */
	private static void computeInvariantsForState(double[][] transMat1, double[][] transMat2,  
										double[][] probStates,	double[][] resultStates) {

		for(int index = 0; index<4; index++) {
			double[] probs = probStates[index]; 
			double[] resultProbs = resultStates[ index ]; 

			computeProbsForOneState(transMat2, transMat1, index, probs, resultProbs);
		}
	}
	
	private static void computeForTwoStatesWithMap(double[][] mat1, double[][] mat2, 
			int maxPatternCount,	int[] patternIndices, 
			int[] tipStates1, 
			int[] tipStates2, 
			double[][] resultStates, int[] resultPatternMap, int resultOffset) {

		double[] m10 = mat1[0];
		double[] m11 = mat1[1];
		double[] m12 = mat1[2];
		double[] m13 = mat1[3];

		double[] m20 = mat2[0];
		double[] m21 = mat2[1];
		double[] m22 = mat2[2];
		double[] m23 = mat2[3];

		for(int index = 0; index<maxPatternCount; index++) {
			int patternIndex = resultOffset + patternIndices[index];
			double[] resultProbs = resultStates[index];
			int index1 = tipStates1[patternIndex];
			int index2 = tipStates2[patternIndex];

			resultProbs[0] = m10[ index1] * m20[index2];
			resultProbs[1] = m11[ index1] * m21[index2];
			resultProbs[2] = m12[ index1] * m22[index2];
			resultProbs[3] = m13[ index1] * m23[index2];
		}
	}

	/**
	 * Computes partial probabilities for all invariant sites for a node above two tips at which particular
	 * states (not partial probabilities) are observed. 
	 * @param mat1
	 * @param mat2
	 * @param resultStates
	 */
	private static void computeInvariantsForTwoStates(double[][] mat1, double[][] mat2, double[][] resultStates) {
		double[] m10 = mat1[0];
		double[] m11 = mat1[1];
		double[] m12 = mat1[2];
		double[] m13 = mat1[3];

		double[] m20 = mat2[0];
		double[] m21 = mat2[1];
		double[] m22 = mat2[2];
		double[] m23 = mat2[3];

		for(int index = 0; index<4; index++) {
			double[] resultProbs = resultStates[index];

			resultProbs[0] = m10[index] * m20[index];
			resultProbs[1] = m11[index] * m21[index];
			resultProbs[2] = m12[index] * m22[index];
			resultProbs[3] = m13[index] * m23[index];
		}
	}




}
