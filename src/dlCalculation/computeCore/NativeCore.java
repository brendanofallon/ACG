package dlCalculation.computeCore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import sequence.DNAUtils;
import sequence.DataMatrix;
import dlCalculation.DoubleStore;
import dlCalculation.IntegerStore;
import dlCalculation.substitutionModels.MutationModel;
import gnu.trove.map.hash.TIntIntHashMap;

public abstract class NativeCore extends AbstractComputeCore {

	//Map so we can quickly find nodes using their number
	private Map<Integer, ComputeNode> nodeMap = new HashMap<Integer, ComputeNode>();
	
	//Used for pattern collecting, this version of maps is based all on primitives and 
	//is hence faster than standard Java classes which use objects. Doesn't require boxing / unboxing
	private TIntIntHashMap patternCache = new TIntIntHashMap(); //Default values seem to work just fine
	
	//For storing known patterns above tips
	//private PatternStore tipPatterns = new PatternStore();
	
	//Tips always have indices form 0..number of tips -1. 
	private TipComputeNode[] tips;
	
	//List of all internal nodes for fast traversing 
	private List<ComputeNode> allNodes = new ArrayList<ComputeNode>(256);
	
	private MutationModel matL; 
	private MutationModel matR; 

	
	//Emit debugging info to stdout
	final boolean verbose = false;
	
	private List<ComputeNode> rootRanges = new ArrayList<ComputeNode>(100);
	
	private int nodesAdded = 0;
	
	private int calls = 0;
	
	//The global maximum number of patterns, we never allocate nodes with more space than this
	private final int maxPatterns;

	private final int maxSites; //Total number of sites
	IntegerStore intStore;
	DoubleStore partialsStore;
	
	int[] polymorphicSites; //Global indices of all sites which are polymorphic, in increasing order
	int[] invariantAAccumulator; //Counts of invariant sites with index less than i, so we can quickly compute numbers of invariant sites in a range
	int[] invariantGAccumulator;
	int[] invariantCAccumulator;
	int[] invariantTAccumulator;
	
	//Multiplies all times, mostly for debugging
	final double branchRate = 1.0;
	
	 static {
		  try {
		    System.loadLibrary("nativelib");
		    System.out.println("Appear to have loaded nativelib without exceptions");
		  }
		  catch (Exception ex) {
			  System.err.println("Caught exception loading library : \n " + ex);
		  }
	  }
	
	 public NativeCore() {
		 //this one is just for testing native stuff
		 maxSites = 10;
		 maxPatterns = 10;
		 
		 int[] testArr = new int[]{0, 1, 2, 3, 4, 5};
		 nativeTest(testArr);
		 
		 System.out.println("Native test has completed");
	 }
	 

	 public native void nativeTest(int[] testarr);
		
	 
	public NativeCore(int numTips, MutationModel model, int totalSites, int maxPatterns, DataMatrix dataMatrix) {
		//Map<String, String> attrs = new HashMap<String, String>();
		//attrs.put("stationaries", "0.25 0.25 0.25 0.25");
		matL = model;
		//matR = model.getNew();
		this.maxPatterns = maxPatterns;
		this.maxSites = totalSites;
		tips = new TipComputeNode[numTips];
		nextNodeNumber = numTips;

        polymorphicSites = dataMatrix.getPolymorphicSites();
        
        invariantAAccumulator = dataMatrix.getInvariantCumulator('A');
        invariantGAccumulator = dataMatrix.getInvariantCumulator('G');
        invariantCAccumulator = dataMatrix.getInvariantCumulator('C');
        invariantTAccumulator = dataMatrix.getInvariantCumulator('T');
        
        if (polymorphicSites.length < (maxPatterns-4)) {
        	throw new IllegalArgumentException("There can't be fewer polymorphic sites (" + polymorphicSites.length + ") than patterns (" + maxPatterns +") ");
        }
        
        intStore = new IntegerStore(polymorphicSites.length, 200);
        partialsStore = new DoubleStore(maxPatterns+10, 100);
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
				
				ComputeNode lNode = nodeMap.get(node.activeState.leftNode);
				if (lNode != null)
					stack.push(lNode);
				ComputeNode rNode = nodeMap.get(node.activeState.rightNode);
				if (rNode != null)
					stack.push(rNode);
				}
		}
		
		return sum;
	}
	
	
	public int getNodeCount() {
		return allNodes.size();
	}
	
	
	public int countPatterns(int[] nodeIDs) {
		int sum = 0;
		for(int i=0; i<nodeIDs.length; i++) {
			ComputeNode node = nodeMap.get(nodeIDs[i]);
			sum += node.activeState.subtreeUniquePatternCount;
		}
		return sum;
	}
	
	/**
	 * Eliminate all nodes that have IDs not existing in the array provided
	 * @param existingNodeIDs
	 */
	public void collectGarbage(int[] existingNodeIDs) {
		Map<Integer, ComputeNode> newMap = new HashMap<Integer, ComputeNode>();
		allNodes.clear();
		
		for(int i=0; i<existingNodeIDs.length; i++) {
			int id = existingNodeIDs[i];
			ComputeNode node = nodeMap.get( id );
			newMap.put(id, node);
			allNodes.add(node);
		}
		
		//System.out.println("** ComputeCore GC : reducing map from " + nodeMap.size() + " to " + newMap.size());
		
		
		Set<Integer> usedKeys = newMap.keySet();
		for(Integer usedKey : usedKeys) {
			nodeMap.remove(usedKey);
		}
		
		for(Integer key : nodeMap.keySet()) {
			nodeMap.get(key).disposeArrays();
		}
		
		nodeMap = newMap;
	}
	
	/**
	 * Remove all ComputeNodes that are no longer referenced. We do this ONLY AFTER ACCEPT steps, in which we traverse from
	 * the root ranges downward, flagging all nodes that can be reached. We then discard all of the rest. 
	 */
	private void collectGarbage() {
		Stack<ComputeNode> stack = new Stack<ComputeNode>();
		Map<Integer, ComputeNode> newMap = new HashMap<Integer, ComputeNode>();
		//System.out.println("Collecting garbage...");
		allNodes.clear();
		
		for(ComputeNode rootRange : rootRanges) {
			stack.clear();
			stack.push(rootRange);
			while(! stack.isEmpty()) {
				ComputeNode node = stack.pop();
				
				//If node is not in the new map, then add it to the new map and to allNodes, and push its kids
				if ( newMap.get( node.refID ) == null) {
					newMap.put(node.refID, node);
					allNodes.add(node);
					
					ComputeNode lNode = nodeMap.get(node.activeState.leftNode);
					if (lNode != null)
						stack.push(lNode);
					ComputeNode rNode = nodeMap.get(node.activeState.rightNode);
					if (rNode != null)
						stack.push(rNode);
				}
			}
		}
		
		//if (verbose)
			System.out.println("** ComputeCore GC : reducing map from " + nodeMap.size() + " to " + newMap.size());

		//We need to know which nodes are no longer used, these are all of the nodes that were not put 
		//into the new map. This is clumsy, but for now we go through all nodes in the new map, and remove
		//them from the old map, and those entries left over are the ones that we can dispose of
		Set<Integer> usedKeys = newMap.keySet();
		for(Integer usedKey : usedKeys) {
			nodeMap.remove(usedKey);
		}
		
		for(Integer key : nodeMap.keySet()) {
			nodeMap.get(key).disposeArrays();
		}
		
		nodeMap = newMap;
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
		
		//We also initialize the aliases scheme here
		int[] firstSite = new int[4]; //
		for(int i=0; i<firstSite.length; i++)
			firstSite[i] = -1;
		
		tips[tipNumber].aliases = new int[polymorphicSites.length];
		int[] aliases = tips[tipNumber].aliases;
		
		for(int i=0; i<polymorphicSites.length; i++) {
			int state = tip.states[i];
			int alias = firstSite[state];
			if (alias < 0) {
				aliases[i] = i;
				firstSite[state] = i;
			}
			else {
				aliases[i] = alias;
			}
		}
	}
	
	public void proposeAll() {
		for(ComputeNode node : allNodes) {
			proposeNode(node.refID, node.activeState.height);
		}
	}
	
	public void accept() {
		calls++;
		for(ComputeNode node : allNodes) {
			if (isNodeProposed(node)) {
				node.currentState.rootDL = node.activeState.rootDL;
				node.currentState.startIndex = node.activeState.startIndex;
				node.currentState.endIndex = node.activeState.endIndex;
				node.currentState.globalStartSite = node.activeState.globalStartSite;
				node.currentState.globalEndSite = node.activeState.globalEndSite;
				node.currentState.height = node.activeState.height;
				
				node.currentState.leftNode = node.activeState.leftNode;
				node.currentState.rightNode = node.activeState.rightNode;				
				
				if (node.activeState.partials == node.proposedState.partials) {
					double[][] tmp = node.currentState.partials;
					node.currentState.partials = node.proposedState.partials;
					node.proposedState.partials = tmp;
					
					tmp = node.currentState.invariantPartials;
					node.currentState.invariantPartials = node.proposedState.invariantPartials;
					node.proposedState.invariantPartials = tmp;
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
		}
		
		
//		if (allNodes.size() > 100 || nodesAdded % GCFrequency == 0) {
//			collectGarbage();
//		}
	}
	
	/**
	 * Reject current state. All activeState refs are set to currentState
	 */
	public void reject() {
		calls++;
		for(ComputeNode node : allNodes) {
			if (isNodeProposed(node)) {
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

			}
		}
	}

	/**
	 * Just propose a new height for the given node. This assumes the node already exists and has the
	 * partials initialized to the correct number
	 * @param nodeNumber
	 * @param nodeHeight
	 */
	public void proposeNode(int nodeNumber, double nodeHeight) {
		ComputeNode node = nodeMap.get(nodeNumber);
		if (node == null)
			throw new IllegalStateException("No node with id #" + nodeNumber);
		
		if (verbose)
			System.out.println("Proposing node " + nodeNumber + " new height " + nodeHeight);
		
		
		node.activeState.leftNode = node.currentState.leftNode;
		node.activeState.rightNode = node.currentState.rightNode;
		node.activeState.startIndex = node.currentState.startIndex;
		node.activeState.endIndex = node.currentState.endIndex;
		node.activeState.height = nodeHeight;
		
		node.activeState.subtreePatternIndices = node.currentState.subtreePatternIndices;
		node.activeState.subtreePatternMap = node.currentState.subtreePatternMap;
		node.activeState.subtreeUniquePatternCount = node.currentState.subtreeUniquePatternCount;
		node.activeState.partialsMap = node.currentState.partialsMap;
		node.activeState.partials = node.proposedState.partials;
	}

	
	public void proposeRange(int nodeNumber, 
								double nodeHeight,
								int globalMin, 
								int globalMax, 
								int lNode,
								int rNode) {
		//If this node is not new, then we can search around for this node and overwrite the range
		ComputeNode node = nodeMap.get(nodeNumber);
		boolean newNode = false;
		if (node == null) {
			//Make a new node, and be sure to put it in the map
			node = new ComputeNode(nodeNumber);
			nodeMap.put(nodeNumber, node);
			allNodes.add(node);

			node.currentState.subtreePatternIndices = intStore.getArray();
			node.proposedState.subtreePatternIndices = intStore.getArray();
			node.currentState.subtreePatternMap = intStore.getArray();
			node.proposedState.subtreePatternMap = intStore.getArray();
			node.proposedState.partialsMap = intStore.getArray();
			node.currentState.partialsMap = intStore.getArray();
			nodesAdded++;
			newNode = true;
			if (verbose) {
				System.out.println("ComputeCore allocating node " + nodeNumber);
			}
		}
		

		if (verbose)
			System.out.println("Proposing node " + nodeNumber + " height : " + nodeHeight + " start: " + globalMin + " end: " + globalMax);
		
		node.proposedState.leftNode = lNode;
		node.proposedState.rightNode = rNode;
		node.proposedState.startIndex = findIndexForSite(polymorphicSites, globalMin);
		node.proposedState.endIndex = findIndexForSite(polymorphicSites, globalMax);
		node.proposedState.globalStartSite = globalMin;
		node.proposedState.globalEndSite = globalMax;
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
		
		//Only collect patterns if our structure has changed or if either daughter node's patterns have changed
		boolean leftHasChanged = false;
		if (node.activeState.leftNode > tips.length) {
			ComputeNode leftChild = nodeMap.get(node.activeState.leftNode);
			leftHasChanged = leftChild.activeState.subtreePatternIndices != leftChild.currentState.subtreePatternIndices;
		}
		boolean rightHasChanged = false;
		if (node.activeState.rightNode > tips.length) {
			ComputeNode rightChild = nodeMap.get(node.activeState.rightNode);
			rightHasChanged = rightChild.activeState.subtreePatternIndices != rightChild.currentState.subtreePatternIndices;
		}
		
		
		if (newNode
				|| node.activeState.leftNode != node.currentState.leftNode 
				|| node.activeState.rightNode != node.currentState.rightNode
				|| leftHasChanged
				|| rightHasChanged) {
			collectPatterns(node);	
		}

	} 

	public void emitState() {
		System.out.println("ComputeCore tracking " + allNodes.size() + " ComputeNodes...");
		for(Integer num : nodeMap.keySet()) {
			ComputeNode node = nodeMap.get(num);
			System.out.println("#" + num + "  height=" + node.activeState.height + " sites=[" + node.activeState.startIndex + " - " + node.activeState.endIndex +")");
		}
	}
	
	private void makeNewPartials(ComputeNode node) {
		if (node.proposedState.partials == null) {
			node.proposedState.partials = partialsStore.getArray(); //new double[maxPatterns+10][4];
		}
	}

	
	private boolean isNodeProposed(ComputeNode node) {
		return (node.activeState.partials == node.proposedState.partials 
				|| node.activeState.height != node.currentState.height
				|| node.activeState.leftNode != node.currentState.leftNode 
				|| node.activeState.rightNode != node.currentState.rightNode
				|| node.activeState.subtreePatternIndices != node.currentState.subtreePatternIndices);	
	}

	
	
	public void clearRootRanges() {
		rootRanges.clear();
	}

	/**
	 * Add the ComputeNode with the given ID to the list of nodes to compute rootDL over 
	 * @param refNodeID
	 */
	public void setRootRangeNode(int refNodeID) {
		ComputeNode node = nodeMap.get(refNodeID);
		if (node == null) {
			throw new IllegalStateException("There is no node with id #" + refNodeID);
		}
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
		if (calls < 1000 || calls % 100==0) //Periodically occurring validity check
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
			System.out.println("Node #" + numberForNode(node) + " height: " + node.activeState.height + " start: " + node.activeState.startIndex + "  end: " + node.activeState.endIndex);
		}
	}
	
	/**
	 * Perform a reverse lookup on the node-map to determine the reference value for a node 
	 * @param node
	 * @return
	 */
	private int numberForNode(ComputeNode node) {
		for(Integer key : nodeMap.keySet()) {
			ComputeNode valNode = nodeMap.get(key);
			if (valNode == node) {
				return key;
			}
		}
		return -1;
	}

        private static Map<Integer, Integer> createCardinalityMap(int[] aliases, int start, int end) {
            Map<Integer, Integer> map = new HashMap<Integer, Integer>();
            for(int i=start; i<end; i++) {
                Integer count = map.get(aliases[i]);
                if (count == null)
                    map.put(aliases[i], 1);
                else
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
		final int stateCount = 4;
		boolean rememberDL = false;
		
		
		//Before anything happens, intersect this node's global sites with the requested global sites, and return 0
		//if there's no overlap. This is actually critical otherwise we will try to compute invariants for sites that
		//aren't in our range
		int globalTrueStart = Math.max(globalStartSite, rootNode.activeState.globalStartSite); //As with polymorphic sites above, we must intersect
		int globalTrueEnd = Math.min(globalEndSite, rootNode.activeState.globalEndSite);	//The requested site range with our own range
		
		//If we're computing for the whole range, we may have already stored the DL
		//in state.rootDL, if so, just return it. If not, set a flag so that when we do
		//compute it we will store it
//		if (!isNodeProposed(rootNode)) {
//			return rootNode.activeState.rootDL;
//		}
//		else {
//			rememberDL = true;
//		}
		
		if (globalTrueEnd <= globalTrueStart) {
			if (rememberDL)
				rootNode.activeState.rootDL = 0.0;
			return 0.0;
		}
		
		//System.out.println("Computing root DL with matrices : \n" + matR + matL );
		
		//Convert from global sites to indices in polymorphic sites
		int start = findIndexForSite(polymorphicSites, globalStartSite);
		int end = findIndexForSite(polymorphicSites, globalEndSite);
		
		
		
		double[][] partials = rootNode.activeState.partials; //Should be active, not proposed state, because not all root ranges will be activeProposed (some may not have been changed by the last modification)
		int polyStart = Math.max(start, rootNode.activeState.startIndex); //Index is in terms of the polymorphic sites array, not global sites
		int polyEnd = Math.min(end, rootNode.activeState.endIndex);
		
		int startIndex = polyStart - rootNode.activeState.startIndex; //First index in partials where we begin calculating
		int endIndex = polyEnd - rootNode.activeState.startIndex;	   //One after last index of partials to calculate

		//If there are no polymorphic sites in this region then skip this part.....
		if (endIndex > startIndex) {
			Map<Integer, Integer> cardinalityMap = createCardinalityMap(rootNode.activeState.subtreePatternMap, startIndex, endIndex); //Counts the number of times the pattern appears

			for(int i=0; i<rootNode.activeState.subtreeUniquePatternCount; i++) {
				int pattern = rootNode.activeState.subtreePatternIndices[i];
				int aliasedSite = rootNode.activeState.subtreePatternMap[pattern];

				Integer cardinality = cardinalityMap.get(aliasedSite);
				
				//Sometimes no sites are within range, don't attempt computing if so
				if (cardinality == null || cardinality ==0) { 
					continue;   				
				}

				
				double siteProb = 0;
				for(int j=0; j<stateCount; j++) {
					siteProb += partials[ i ][j] * matL.getStationaryForState(j);
				}

				if (Double.isNaN(siteProb)) {
					System.out.println("Prob is NaN for pattern compute node !" );
				}
				if (siteProb==0) {
					System.out.println("Prob is zero for pattern !");
				}


				logDL += Math.log(siteProb) * cardinality; 

				if (Double.isInfinite(logDL)) {
					System.out.println("Prob is infinite for pattern at site #" + aliasedSite);
				}

			}
		}
		
		

		//Now do the invariant sites. These are always indexed with the global sites system, not the polymorphic sites
		//indices
		int[] invariantCounts = countInvariants(globalStartSite, globalEndSite);
		
		for(int i=0; i<4; i++) {
			double siteProb = 0;
			for(int j=0; j<stateCount; j++) {
				siteProb += rootNode.activeState.invariantPartials[ i ][j] * matL.getStationaryForState(j);
			}

			if (Double.isNaN(siteProb)) {
				System.out.println("Prob is NaN for pattern compute node !" );
			}
			if (siteProb==0) {
				System.out.println("Prob is zero for pattern !");
			}

			logDL += Math.log(siteProb) * invariantCounts[i];
                        
			//System.out.println("Invariants: " + Math.log(siteProb) + " cardinality: " + invariantCounts[i]);
			if (Double.isInfinite(logDL)) {
				System.out.println("Prob is infinite for invariant pattern at site #" + i);
			}

		}
		

		if (rememberDL) {
			rootNode.activeState.rootDL = logDL;
		}
		return logDL;
	}

	/**
	 * Returns an array of ints such that array[DNAUtils.X] is the number of invariant sites with symbol X between start and end
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

	/**
	 * Counts the number of times the given key appears in the list
	 * @param pattern
	 * @param map
	 * @return
	 */
	private double countCardinality(int key, int[] map, int start, int end) {
		int sum = 0;
		for(int i=start; i<end; i++) {
			if (map[i] == key)
				sum++;
		}
		return sum;
	}



	private double computeLogRootDLForRange(ComputeNode rootNode) {
//		int globalStart = polymorphicSites[ rootNode.activeState.startIndex ];
//		int globalEnd = polymorphicSites[ rootNode.activeState.endIndex-1 ]+1;
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
		double distL = state.height - tipL.activeState.height;
		double distR = state.height - tipR.activeState.height;
		
		matL.setBranchLength(distL*branchRate);
		matR.setBranchLength(distR*branchRate);
		
		
		int[] tipLStates = tipL.states;
		int[] tipRStates = tipR.states;
		makeNewPartials(node);
		if (node.proposedState.partials.length < node.activeState.subtreeUniquePatternCount) {
			System.out.println("Hmmm, node " + node + " has partials with length " + node.proposedState.partials.length + ", which is less than its pattern count : " + node.activeState.subtreeUniquePatternCount);
		}
		
		double[][] partials = node.proposedState.partials;
		
		computeForTwoStatesWithMap(matL.getMatrix(), matR.getMatrix(), 
				state.subtreeUniquePatternCount, state.subtreePatternIndices, 
				tipLStates,
				tipRStates, 
				partials, state.subtreePatternMap, state.startIndex);
		
		if (node.proposedState.invariantPartials==null) {
			node.proposedState.invariantPartials = new double[4][4];
		}
		computeInvariantsForTwoStates(matL.getMatrix(), matR.getMatrix(), node.proposedState.invariantPartials); //Thats right, doesn't need any maps or to know any tips states
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
			int pattern = patternIndices[index];
			//double[] resultProbs = resultStates[ resultPatternMap[pattern]]; 
			double[] resultProbs = resultStates[index];
			int index1 = tipStates1[pattern+resultOffset];
			int index2 = tipStates2[pattern+resultOffset];

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
	
	private void computePartialsForOneTip(ComputeNode node,
											TipComputeNode tip,
											ComputeNode childNode) {
		NodeState state = node.activeState;
		double distL = state.height - tip.activeState.height;
		double distR = state.height - childNode.activeState.height;
		
		matL.setBranchLength(distL*branchRate);
		matR.setBranchLength(distR*branchRate);
		
		int[] tipStates = tip.states;
		double[][] childPartials = childNode.activeState.partials;
		
		makeNewPartials(node);
		if (node.proposedState.partials.length < node.activeState.subtreeUniquePatternCount) {
			System.out.println("Hmmm, node " + node + " has partials with length " + node.proposedState.partials.length + ", which is less than its pattern count : " + node.activeState.subtreeUniquePatternCount);
		}
		
		double[][] partials = node.proposedState.partials; //We always write new partials into the proposed state
		
		computeProbsForStateWithMap(matR.getMatrix(), matL.getMatrix(), 
									state.subtreeUniquePatternCount, state.subtreePatternIndices,
									childPartials, childNode.activeState.subtreePatternMap, childNode.activeState.partialsMap, childNode.activeState.startIndex, 
									tipStates,
									partials, state.subtreePatternMap, state.startIndex);
		
		if (node.proposedState.invariantPartials == null) {
			node.proposedState.invariantPartials = new double[4][4];
		}
		computeInvariantsForState(matR.getMatrix(), matL.getMatrix(), childNode.activeState.invariantPartials, node.proposedState.invariantPartials);
			
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
	
	
	/**
	 * Compute new partials for range of proposed sites
	 */
	public void computePartials(int nodeNumber) {
		ComputeNode node = nodeMap.get(nodeNumber);
				
		NodeState state = node.activeState;
		
		int leftNodeNum = state.leftNode;
		int rightNodeNum = state.rightNode;
		
		if (leftNodeNum < tips.length) {
			TipComputeNode tipL = tips[leftNodeNum];
			
			if (rightNodeNum < tips.length) {
				if (!isNodeProposed(node) ) //If we're above two tips, then don't compute only if we are not proposed
					return;
				
				//We're above two tips
				TipComputeNode tipR = tips[rightNodeNum];
				
				if (verbose) {
					System.out.println("ComputeNode #" + nodeNumber + " above tip " + state.leftNode + " and tip " + state.rightNode + " is computing");
				}
				
				computePartialsForTwoTips(node, tipL, tipR);
				node.activeState.partials = node.proposedState.partials;
				node.activeState.invariantPartials = node.proposedState.invariantPartials;
				return;
			}
			else {
				
				//Left is tip but right is not
				ComputeNode rChild = nodeMap.get(state.rightNode);
				
				if (!isNodeProposed(node) && (!isNodeProposed(rChild)) ) //Dont compute if both node and rChild are not proposed
					return;
				
				
				if (verbose) {
					System.out.println("ComputeNode #" + nodeNumber + " above tip " + state.leftNode + " and node " + state.rightNode + " is computing");
				}
				
				computePartialsForOneTip(node, tipL, rChild);
				node.activeState.partials = node.proposedState.partials;
				node.activeState.invariantPartials = node.proposedState.invariantPartials;
			}
			
		}
		else {
			
			if (rightNodeNum < tips.length) {
				//Right is tip but left is not
				TipComputeNode tipR = tips[rightNodeNum];
				ComputeNode lChild = nodeMap.get(state.leftNode);
				
				if (!isNodeProposed(node) && (!isNodeProposed(lChild)) ) //Dont compute if both node and lChild are not proposed
					return;
				
				if (verbose) {
					System.out.println("ComputeNode #" + nodeNumber + " above tip " + state.rightNode + " and node " + state.leftNode + " is computing");
				}
				
				computePartialsForOneTip(node, tipR, lChild);
				node.activeState.partials = node.proposedState.partials;
				node.activeState.invariantPartials = node.proposedState.invariantPartials;
			}
			else {
				//Neither node is a tip
				ComputeNode rChild = nodeMap.get(state.rightNode);
				ComputeNode lChild = nodeMap.get(state.leftNode);
				
				if (!isNodeProposed(node) && (!isNodeProposed(rChild)) && (!isNodeProposed(lChild)) ) //Dont compute this and both descendants have not been altered
					return;
				
				
				double distL = state.height - lChild.activeState.height;
				double distR = state.height - rChild.activeState.height;
				
				
				matL.setBranchLength(distL*branchRate);
				double[][] matrixL = matL.getMatrix();
				double[][] matrixR;
				if (distL != distR) {
					matR.setBranchLength(distR*branchRate);
					matrixR = matR.getMatrix();
				}
				else {
					matrixR = matrixL;
				}
				
				if (verbose) {
					System.out.println("ComputeNode #" + nodeNumber + " above node " + state.leftNode + " and node " + state.rightNode + " is computing");
				}
				
				makeNewPartials(node);
				if (node.proposedState.partials.length < node.activeState.subtreeUniquePatternCount) {
					System.out.println("Hmmm, node " + node + " has partials with length " + node.proposedState.partials.length + ", which is less than its pattern count : " + node.activeState.subtreeUniquePatternCount);
				}
				computeProbsWithMap(matrixL, matrixR, 
									state.subtreeUniquePatternCount, state.subtreePatternIndices,
									lChild.activeState.partials, lChild.activeState.subtreePatternMap, lChild.activeState.partialsMap, lChild.activeState.startIndex,
									rChild.activeState.partials, rChild.activeState.subtreePatternMap, rChild.activeState.partialsMap, rChild.activeState.startIndex,
									node.proposedState.partials, node.activeState.subtreePatternMap, node.activeState.startIndex);

				
				if (node.proposedState.invariantPartials == null) {
					node.proposedState.invariantPartials = new double[4][4];
				}
				computeInvariants(matL.getMatrix(), matR.getMatrix(), lChild.activeState.invariantPartials, rChild.activeState.invariantPartials, node.proposedState.invariantPartials);

				
				node.activeState.partials = node.proposedState.partials;
				node.activeState.invariantPartials = node.proposedState.invariantPartials;
			}
			
		}
		
	}
	
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
	
	/**
	 * Compute subtree aliases for this node based on the state of the two children...
	 * Returns true if the patterns have changed for this node
	 */
	private void collectPatterns(ComputeNode node) {
				
		node.activeState.subtreePatternIndices = node.proposedState.subtreePatternIndices;
		node.activeState.subtreePatternMap = node.proposedState.subtreePatternMap;
		node.activeState.partialsMap = node.proposedState.partialsMap;
		
		//System.out.println("Collecting patterns for compute node : " + node);
		
		//Don't try to collect patterns if there are no polymorphic sites
		if (polymorphicSites.length==0)
			return;
		
		patternCache.clear();
		
				
		int[] lAliases;
		int[] rAliases;
		int lOffset = 0;
		int rOffset = 0;
		if (node.activeState.leftNode < tips.length)
			lAliases = tips[node.activeState.leftNode].aliases;
		else {
			ComputeNode lNode = nodeMap.get(node.activeState.leftNode);
			lAliases = lNode.activeState.subtreePatternMap;
			lOffset = lNode.activeState.startIndex;
		}
		
		if (node.activeState.rightNode < tips.length)
			rAliases = tips[node.activeState.rightNode].aliases;
		else {
			ComputeNode rNode = nodeMap.get(node.activeState.rightNode);
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
														polymorphicSites);

		node.proposedState.subtreeUniquePatternCount = res.uniquePatternCount;
		node.activeState.subtreeUniquePatternCount = node.proposedState.subtreeUniquePatternCount;

		
		//If no patterns have changed, bump all refs back to current state. This prevents 
		//rootward nodes from recomputing their patterns for no reason
		if (!res.patternsChanged 
				&& node.currentState.globalStartSite == node.proposedState.globalStartSite 
				&& node.currentState.globalEndSite == node.proposedState.globalEndSite) {

			//System.out.println("Patterns for node " + node + " appear not to have changed");
			node.activeState.subtreePatternIndices = node.currentState.subtreePatternIndices;
			node.activeState.subtreePatternMap = node.currentState.subtreePatternMap;
			node.activeState.partialsMap = node.currentState.partialsMap;
			node.activeState.subtreeUniquePatternCount = node.currentState.subtreeUniquePatternCount;
		}
		
		//Check to see if patterns have changed, if not, set refs back to current state to avoid having rootward nodes recalculate their patterns too
//		if (node.currentState.globalStartSite == node.proposedState.globalStartSite && node.currentState.globalEndSite == node.proposedState.globalEndSite) {
//			//Scan everything again to see if the patterns have changed!
////			boolean changed = false;
////			for(int i=0; i<node.proposedState.subtreePatternMap.length; i++) {
////				if (node.currentState.subtreePatternMap[i] != node.proposedState.subtreePatternMap[i]) {
////					changed = true;
////					break;
////				}
////			}
//
//
//			
//		}


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
	private static CollectionResult staticCollectPatterns(TIntIntHashMap map,
			int[] lAliases, int lOffset,
			int[] rAliases, int rOffset,
			int[] targetIndices, int[] targetMap, int targetStart, int targetStop,
			int[] partialsMap,
			int[] currentMap,
			int[] polymorphicSites) {

		int uniquePatternCount = 0;
		boolean changed = false;

		final int arrayEnd = targetStop - targetStart;

		for(int i=0; i<arrayEnd; i++) {
			final int globalSite = i+targetStart;
			final int lIndex = lAliases[globalSite - lOffset]; //subtree index of global pattern for left child
			final int rIndex= rAliases[globalSite - rOffset];  //subtree index of global pattern for right child
			int alias = i;
			final int patternKey = 10000*lIndex + rIndex;
			int match; 

			match = map.get(patternKey);

			if (match==0) {
				map.put(patternKey, i);
				alias = i;
				targetIndices[uniquePatternCount] = i;
				partialsMap[i] = uniquePatternCount;
				uniquePatternCount++;
			}
			else {
				alias = match;
			}

			targetMap[i] = alias;		
			if (alias != currentMap[i]) 
				changed = true;
		}		

		CollectionResult res = new CollectionResult();
		res.patternsChanged = changed;
		res.uniquePatternCount = uniquePatternCount;
		
		return res;
	}
	
	/**
	 * A single range of coalescing sites, with a unique number ("refID"), a height, and references to
	 * two other descendant nodes (which may be tips).   
	 * @author brendano
	 *
	 */
	class ComputeNode {
		
		final int refID;
		
		NodeState activeState = new NodeState();
		NodeState currentState = new NodeState();
		NodeState proposedState = new NodeState();
		
		public ComputeNode(int id) {
			refID = id;
		}
		
		
		public String toString() {
			return "ID=" + refID + " active: " + activeState.toString();
		}
		
		/**
		 * Return all buffers to a store we we're not constantly reallocating them
		 */
		public void disposeArrays() {
			intStore.add(currentState.subtreePatternIndices);
			intStore.add(proposedState.subtreePatternIndices);
			intStore.add(currentState.subtreePatternMap);
			intStore.add(proposedState.subtreePatternMap);
			intStore.add(currentState.partialsMap);
			intStore.add(proposedState.partialsMap);
			if (currentState.partials != null)
				partialsStore.add(currentState.partials);
			if (proposedState.partials != null)
				partialsStore.add(proposedState.partials);
		}
	}
	
	class NodeState {
		
		double rootDL = Double.NaN;
		
		double[][] partials;
		double[][] invariantPartials; //Partials for invariant sites
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
			return " start=" + startIndex + " end=" + endIndex + " left=" + leftNode + " right=" + rightNode + " sites=" + subtreeUniquePatternCount;
		}

	}

	
	class TipComputeNode {
		
		int[] states;
		int[] aliases;
		
		TipNodeState currentState = new TipNodeState();
		TipNodeState activeState = currentState;
		
	}
	
	class TipNodeState {
		
		double height = 0;
		
	}
	
	
	class ComputeRangeComparator implements Comparator<ComputeNode> {

		@Override
		public int compare(ComputeNode a, ComputeNode b) {
			return a.activeState.globalStartSite < b.activeState.globalStartSite ? -1 : 1;
		}
		
	}
	
	public static class CollectionResult {
		int uniquePatternCount;
		boolean patternsChanged;
	}
	
	
//	public static void main(String[] args) {
//		NativeCore core = new NativeCore();
//	}
}
