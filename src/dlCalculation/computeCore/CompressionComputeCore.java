package dlCalculation.computeCore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import dlCalculation.DoubleStore;
import dlCalculation.IntegerStore;
import dlCalculation.substitutionModels.MutationModel;

/**
 * This is very similar to the SimpleComputeCore, but implements pattern compression (nee subtree aliasing of patterns).
 * An excersize in indirection, this class contains many 
 * @author brendano
 *
 */
public abstract class CompressionComputeCore extends AbstractComputeCore {

	
	//Map so we can quickly find nodes using their number
	private Map<Integer, ComputeNode> nodeMap = new HashMap<Integer, ComputeNode>();
	
	//Used for pattern collecting 
	private Map<Integer, Integer> patternCache = new HashMap<Integer, Integer>();
	
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
	
	private int GCFrequency = 50; //Once every 50 node adds attempt to collect garbage
	private int nodesAdded = 0;
	
	private int calls = 0;
	
	//The global maximum number of patterns, we never allocate nodes with more space than this
	private final int maxPatterns;

	private final int maxSites; //Total number of sites
	IntegerStore intStore;
	DoubleStore partialsStore;
	
	
	public CompressionComputeCore(int numTips, MutationModel model, int totalSites, int maxPatterns) {
		Map<String, String> attrs = new HashMap<String, String>();
		attrs.put("stationaries", "0.25 0.25 0.25 0.25");
		matL = model;
		//matR = model.getNew();
		this.maxPatterns = maxPatterns;
		this.maxSites = totalSites;
		tips = new TipComputeNode[numTips];
		nextNodeNumber = numTips;
        intStore = new IntegerStore(totalSites, 200);
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
				sum += node.activeState.subtreeUniquePatternCount;
				
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
		
		if (verbose)
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
	
	public int countPatterns(Integer[] nodeIDs) {
		int sum = 0;
		for(int i=0; i<nodeIDs.length; i++) {
			ComputeNode node = nodeMap.get(nodeIDs[i]);
			sum += node.activeState.subtreeUniquePatternCount;
		}
		return sum;
	}
	
	/**
	 * Create a new tipNode with the given tip number / index, and assign it the given vector of states. 
	 * @param tipNumber
	 * @param states
	 * @return id 
	 */
	public void initializeTipState(int tipNumber, int[] states) {
		tips[tipNumber] = new TipComputeNode();
		tips[tipNumber].states = states;
		tips[tipNumber].activeState.height = 0;
		
		//We also initialize the aliases scheme here
		int[] firstSite = new int[4]; //
		for(int i=0; i<firstSite.length; i++)
			firstSite[i] = -1;
		
		tips[tipNumber].aliases = new int[states.length];
		int[] aliases = tips[tipNumber].aliases;
		
		for(int i=0; i<states.length; i++) {
			int state = states[i];
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
				node.currentState.startSite = node.activeState.startSite;
				node.currentState.endSite = node.activeState.endSite;
				node.currentState.height = node.activeState.height;
				
				node.currentState.leftNode = node.activeState.leftNode;
				node.currentState.rightNode = node.activeState.rightNode;				
				
				if (node.activeState.partials == node.proposedState.partials) {
					double[][] tmp = node.currentState.partials;
					node.currentState.partials = node.proposedState.partials;
					node.proposedState.partials = tmp;
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
		
		
		if (allNodes.size() > 100 || nodesAdded % GCFrequency == 0) {
			collectGarbage();
		}
		
	}
	
	/**
	 * Reject current state. All activeState refs are set to currentState
	 */
	public void reject() {
		calls++;
		for(ComputeNode node : allNodes) {
			if (isNodeProposed(node)) {
				node.activeState.startSite = node.currentState.startSite;
				node.activeState.endSite = node.currentState.endSite;
				node.activeState.height = node.currentState.height;
				node.activeState.rightNode = node.currentState.rightNode;
				node.activeState.leftNode = node.currentState.leftNode;
				node.activeState.partials = node.currentState.partials;
				node.activeState.rootDL = node.currentState.rootDL;

				node.activeState.subtreePatternIndices = node.currentState.subtreePatternIndices;
				node.activeState.subtreePatternMap = node.currentState.subtreePatternMap;
				node.activeState.subtreeUniquePatternCount = node.currentState.subtreeUniquePatternCount;

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
		node.activeState.startSite = node.currentState.startSite;
		node.activeState.endSite = node.currentState.endSite;
		node.activeState.height = nodeHeight;
		
		node.activeState.subtreePatternIndices = node.currentState.subtreePatternIndices;
		node.activeState.subtreePatternMap = node.currentState.subtreePatternMap;
		node.activeState.subtreeUniquePatternCount = node.currentState.subtreeUniquePatternCount;
		node.activeState.partialsMap = node.currentState.partialsMap;
		node.activeState.partials = node.proposedState.partials;
	}
	
	public void proposeRange(int nodeNumber, double nodeHeight, int globalMin, int globalMax, int lNode, int rNode) {
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
		node.proposedState.startSite = globalMin;
		node.proposedState.endSite = globalMax;
		node.proposedState.height = nodeHeight;
		
		node.activeState.leftNode = node.proposedState.leftNode;
		node.activeState.rightNode = node.proposedState.rightNode;
		node.activeState.startSite = node.proposedState.startSite;
		node.activeState.endSite = node.proposedState.endSite;
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
			System.out.println("#" + num + "  height=" + node.activeState.height + " sites=[" + node.activeState.startSite + " - " + node.activeState.endSite +")");
		}
	}
	
	private void makeNewPartials(ComputeNode node) {
		if (node.proposedState.partials == null) {
			node.proposedState.partials = partialsStore.getArray(); //new double[maxPatterns+10][4];
		}
	}

	
	public boolean isNodeProposed(ComputeNode node) {
		return (node.activeState.partials == node.proposedState.partials 
				|| node.activeState.height != node.currentState.height
				|| node.activeState.leftNode != node.currentState.leftNode 
				|| node.activeState.rightNode != node.currentState.rightNode
				|| node.activeState.subtreePatternIndices != node.currentState.subtreePatternIndices);	
	}

	public int getLeftChildForNode(int nodeNumber) {
		ComputeNode node = nodeMap.get(nodeNumber);
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
		ComputeNode node = nodeMap.get(nodeNumber);
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
		ComputeNode node = nodeMap.get(nodeNumber);
		if (node == null) {
			return -1;
		}
		else {
			return node.activeState.height;
		}
	}
	
	public int getStartSiteForNode(int nodeNumber) {
		ComputeNode node = nodeMap.get(nodeNumber);
		if (node == null) {
			return -1;
		}
		else {
			return node.activeState.startSite;
		}
	}

	/**
	 * The end site in the range of sites coalescing at the given node
	 * @param nodeNumber
	 * @return
	 */
	public int getEndSiteForNode(int nodeNumber) {
		ComputeNode node = nodeMap.get(nodeNumber);
		if (node == null) {
			return -1;
		}
		else {
			return node.activeState.endSite;
		}
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
		
		if (sortedRanges.get(0).activeState.startSite != 0) {
			emitRanges(sortedRanges);
			throw new IllegalStateException("First root range first site is not zero (it's " + sortedRanges.get(0).activeState.startSite + ")");	
		}
		
		
		for(int i=1; i<sortedRanges.size(); i++) {
			if (sortedRanges.get(i).activeState.startSite != sortedRanges.get(i-1).activeState.endSite) {
				emitRanges(sortedRanges);
				throw new IllegalStateException("Root ranges are not adjacent");
			}
		}
		
		ComputeNode last = sortedRanges.get( sortedRanges.size()-1);
		if (last.activeState.endSite != tips[0].states.length)
			throw new IllegalStateException("Root ranges do not go to last site (just " + last.activeState.endSite + ")");
				
	}

	private void emitRanges(List<ComputeNode> nodes) {
		System.out.println("Ranges are : \n");
		for(ComputeNode node : nodes) {
			System.out.println("Node #" + numberForNode(node) + " height: " + node.activeState.height + " start: " + node.activeState.startSite + "  end: " + node.activeState.endSite);
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
	private double computeLogRootDLForRange(ComputeNode rootNode, int start, int end) {
		double logDL = 0;
		final int stateCount = 4;
		boolean rememberDL = false;
		
		//If we're computing for the whole range, we may have already stored the DL
		//in state.rootDL, if so, just return it. If not, set a flag so that when we do
		//compute it we will store it
		if (!isNodeProposed(rootNode)) {
			return rootNode.activeState.rootDL;
		}
		else {
			rememberDL = true;
		}
		
		double[][] partials = rootNode.activeState.partials; //Should be active, not proposed state, because not all root ranges will be activeProposed (some may not have been changed by the last modification)
		int globalStart = Math.max(start, rootNode.activeState.startSite);
		int globalEnd = Math.min(end, rootNode.activeState.endSite);
		
		if (globalStart >= globalEnd) //No sites to compute in this range
			return 0.0;

		
		int startIndex = globalStart - rootNode.activeState.startSite; //First index in partials where we begin calculating
		int endIndex = globalEnd - rootNode.activeState.startSite;	   //One after last index of partials to calculate

        Map<Integer, Integer> cardinalityMap = createCardinalityMap(rootNode.activeState.subtreePatternMap, startIndex, endIndex); //Counts the number of times the pattern appears


		for(int i=0; i<rootNode.activeState.subtreeUniquePatternCount; i++) {
			int pattern = rootNode.activeState.subtreePatternIndices[i];
			int aliasedSite = rootNode.activeState.subtreePatternMap[pattern];

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

			logDL += Math.log(siteProb) * cardinalityMap.get(aliasedSite); 
                        
			if (Double.isInfinite(logDL)) {
				System.out.println("Prob is infinite for pattern at site #" + aliasedSite);
			}

		}
		

		if (rememberDL) {
			rootNode.activeState.rootDL = logDL;
		}
		return logDL;
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
		return computeLogRootDLForRange(rootNode, rootNode.activeState.startSite, rootNode.activeState.endSite);
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
		
		matL.setBranchLength(distL);
		double[][] matrixL = matL.getMatrix();
		double[][] matrixR;
		if (distL != distR) {
			matR.setBranchLength(distR);
			matrixR = matR.getMatrix();
		}
		else {
			matrixR = matrixL;
		}
		
		
		int[] tipLStates = tipL.states;
		int[] tipRStates = tipR.states;
		makeNewPartials(node);
		if (node.proposedState.partials.length < node.activeState.subtreeUniquePatternCount) {
			System.out.println("Hmmm, node " + node + " has partials with length " + node.proposedState.partials.length + ", which is less than its pattern count : " + node.activeState.subtreeUniquePatternCount);
		}
		
		double[][] partials = node.proposedState.partials;
		
		computeForTwoStatesWithMap(matrixL, matrixR, 
				state.subtreeUniquePatternCount, state.subtreePatternIndices, 
				tipLStates,
				tipRStates, 
				partials, state.subtreePatternMap, state.startSite);
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

	
	private void computePartialsForOneTip(ComputeNode node,
											TipComputeNode tip,
											ComputeNode childNode) {
		NodeState state = node.activeState;
		double distL = state.height - tip.activeState.height;
		double distR = state.height - childNode.activeState.height;
		
		matL.setBranchLength(distL);
		matR.setBranchLength(distR);
		
		int[] tipStates = tip.states;
		double[][] childPartials = childNode.activeState.partials;
		
		makeNewPartials(node);
		if (node.proposedState.partials.length < node.activeState.subtreeUniquePatternCount) {
			System.out.println("Hmmm, node " + node + " has partials with length " + node.proposedState.partials.length + ", which is less than its pattern count : " + node.activeState.subtreeUniquePatternCount);
		}
		
		double[][] partials = node.proposedState.partials; //We always write new partials into the proposed state
		
		computeProbsForStateWithMap(matR.getMatrix(), matL.getMatrix(), 
									state.subtreeUniquePatternCount, state.subtreePatternIndices,
									childPartials, childNode.activeState.subtreePatternMap, childNode.activeState.partialsMap, childNode.activeState.startSite, 
									tipStates,
									partials, state.subtreePatternMap, state.startSite);
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
			double[] probs = probStates[ /* probAlias */ probPartialsIndex ]; 
			double[] resultProbs = resultStates[ /* resultPatternMap[pattern] */ index ]; 

			computeProbsForOneState(transMat2, transMat1, tipStates[globalPattern], probs, resultProbs);
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
			}
			else {
				//Neither node is a tip
				ComputeNode rChild = nodeMap.get(state.rightNode);
				ComputeNode lChild = nodeMap.get(state.leftNode);
				
				if (!isNodeProposed(node) && (!isNodeProposed(rChild)) && (!isNodeProposed(lChild)) ) //Dont compute this and both descendants have not been altered
					return;
				
				
				double distL = state.height - lChild.activeState.height;
				double distR = state.height - rChild.activeState.height;
				
				
				matL.setBranchLength(distL);
				double[][] matrixL = matL.getMatrix();
				double[][] matrixR;
				if (distL != distR) {
					matR.setBranchLength(distR);
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
									lChild.activeState.partials, lChild.activeState.subtreePatternMap, lChild.activeState.partialsMap, lChild.activeState.startSite,
									rChild.activeState.partials, rChild.activeState.subtreePatternMap, rChild.activeState.partialsMap, rChild.activeState.startSite,
									node.proposedState.partials, node.activeState.subtreePatternMap, node.activeState.startSite);

				node.activeState.partials = node.proposedState.partials;
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
			
			double[] leftProbs = leftStates[ /*leftAlias */ leftPartialsIndex  ];
			double[] rightProbs = rightStates[ /*rightAlias*/ rightPartialsIndex  ];

			//double[] resultProbs = resultStates[ resultPatternMap[pattern] ]; 

			computeProbabilities(transMat1, transMat2, leftProbs, rightProbs, resultProbs);

		}

	}
	
	/**
	 * Compute subtree aliases for this node based on the state of the two children...
	 */
	private int collectPatterns(ComputeNode node) {
				
		node.activeState.subtreePatternIndices = node.proposedState.subtreePatternIndices;
		node.activeState.subtreePatternMap = node.proposedState.subtreePatternMap;
		node.activeState.partialsMap = node.proposedState.partialsMap;
		
		patternCache.clear();
		
		//When set to true we attempt to store the patterns in the tipPatterns store
		boolean remember = false;
		
		ComputeNode compNode = null; 

		
		//If we're above two tips and there's no recombs below us, then try to find our 
		//pattern info in the pattern store
		//TODO another thing: It doesn't really matter which node is the left vs. right - the
		//pattern info will always be the same no matter what. So I think we should look at 
		//both keys 
//		if (node.activeState.leftNode < tips.length 
//				&& node.activeState.rightNode < tips.length
//				&& node.activeState.startSite==0 
//				&& node.activeState.endSite == maxSites) {
//			
//			int key = 10000*Math.max(node.activeState.leftNode, node.activeState.rightNode) + Math.min(node.activeState.leftNode, node.activeState.rightNode);
//			PatternHolder patterns = tipPatterns.get(key);
//			if (patterns != null) {
//				System.out.println("Found patterns for key: " + key);
//				compNode = new ComputeNode(-10); 
//				compNode.activeState.subtreePatternIndices = patterns.indices;
//				compNode.activeState.subtreePatternMap = patterns.patternMap;
//				compNode.activeState.partialsMap = patterns.partialsMap;
//				compNode.proposedState.subtreeUniquePatternCount = patterns.patternCount;
//				compNode.activeState.subtreeUniquePatternCount = patterns.patternCount;
//				
////				node.proposedState.subtreePatternIndices = patterns.indices;
////				node.activeState.subtreePatternIndices = node.proposedState.subtreePatternIndices;
////				node.proposedState.subtreePatternMap = patterns.patternMap;
////				node.activeState.subtreePatternMap = node.proposedState.subtreePatternMap;
////				node.proposedState.partialsMap = patterns.partialsMap;
////				node.activeState.partialsMap = node.proposedState.partialsMap;
////				node.proposedState.subtreeUniquePatternCount = patterns.patternCount;
////				node.activeState.subtreeUniquePatternCount = node.proposedState.subtreeUniquePatternCount;
////				return node.activeState.subtreeUniquePatternCount;
//			}
//			else {
//				remember = true; //This node can be put in store, but it's not there yet, so remember what we compute and add it
//			}
//		}
		
		int[] lAliases;
		int[] rAliases;
		int lOffset = 0;
		int rOffset = 0;
		if (node.activeState.leftNode < tips.length)
			lAliases = tips[node.activeState.leftNode].aliases;
		else {
			ComputeNode lNode = nodeMap.get(node.activeState.leftNode);
			lAliases = lNode.activeState.subtreePatternMap;
			lOffset = lNode.activeState.startSite;
		}
		
		if (node.activeState.rightNode < tips.length)
			rAliases = tips[node.activeState.rightNode].aliases;
		else {
			ComputeNode rNode = nodeMap.get(node.activeState.rightNode);
			rAliases = rNode.activeState.subtreePatternMap;
			rOffset = rNode.activeState.startSite;
		}
		
		//This bit will make it so things should break immediately if the map is referenced inappropriately
		//It hasn't tripped up any errors in a while, so it's turned off for now
//		for(int i=0; i<node.activeState.partialsMap.length; i++) {
//			node.activeState.partialsMap[i] = -1;
//		}
		
		node.proposedState.subtreeUniquePatternCount = staticCollectPatterns(patternCache, 
														lAliases, lOffset, 
														rAliases, rOffset,
														node.activeState.subtreePatternIndices, 
														node.activeState.subtreePatternMap,
														node.activeState.startSite, node.activeState.endSite,
														node.activeState.partialsMap);
		

		node.activeState.subtreeUniquePatternCount = node.proposedState.subtreeUniquePatternCount;

		return node.activeState.subtreeUniquePatternCount;
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
	private static int staticCollectPatterns(Map<Integer, Integer> map, 
			int[] lAliases, int lOffset,
			int[] rAliases, int rOffset,
			int[] targetIndices, int[] targetMap, int targetStart, int targetStop,
			int[] partialsMap) {

		int uniquePatternCount = 0;

		final int arrayEnd = targetStop - targetStart;

		for(int i=0; i<arrayEnd; i++) {
			int globalSite = i+targetStart;
			int lIndex = lAliases[globalSite - lOffset]; //subtree index of global pattern for left child
			int rIndex= rAliases[globalSite - rOffset];  //subtree index of global pattern for right child
			int alias = i;
			//if (lIndex < i && rIndex < i) {
				final Integer patternKey = 10000*lIndex + rIndex;
				Integer match = null; 

				match = map.get(patternKey);

				if (match==null) {
					map.put(patternKey, i);
					alias = i;
					targetIndices[uniquePatternCount] = i;
					partialsMap[i] = uniquePatternCount;
					uniquePatternCount++;
				}
				else {
					alias = match;
				}
			//}
			targetMap[i] = alias;			
		}		

		return uniquePatternCount;
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
		
		public double[] getPartialsForSite(int site) {
			return activeState.partials[site-activeState.startSite];
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
		int startSite = -1;
		int endSite = -1;
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
			return " start=" + startSite + " end=" + endSite + " left=" + leftNode + " right=" + rightNode + " sites=" + subtreeUniquePatternCount;
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
			return a.activeState.startSite < b.activeState.startSite ? -1 : 1;
		}
		
	}


	@Override
	public boolean isNodeProposed(int nodeNumber) {
		ComputeNode node = nodeMap.get(nodeNumber);
		if (node == null) {
			throw new IllegalStateException("Node " + nodeNumber + " does not exist");
		}
		else {
			return node.activeState.leftNode != node.currentState.leftNode
					|| node.activeState.rightNode != node.currentState.rightNode
					|| node.activeState.subtreePatternMap != node.currentState.subtreePatternMap
					|| node.activeState.height != node.currentState.height
					|| node.activeState.startSite != node.currentState.startSite
					|| node.activeState.endSite != node.currentState.endSite;
		}
	}
	
}
