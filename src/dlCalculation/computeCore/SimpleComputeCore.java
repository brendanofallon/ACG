package dlCalculation.computeCore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import dlCalculation.substitutionModels.MutationModel;



/**
 * This object stores partials (arrays of doubles) and performs 'data likelihood' computations on those partials.
 * Contiguous ranges of coalescing sites are described by a ComputeNode, which stores all the information needed to
 * compute it's partials (which includes partials list, a height, and references to the two descending ComputeNodes).
 * Each compute node is indexed by a unique number, so an arg 
 * to contain the info for all its sites, and also maintains a height and two fields which reference other ComputeNodes
 * 
 * This ComputeCore is the "dumb as rocks" reference version, which should be used for debugging and
 * reference purposes only. It performs NO PATTERN COMPRESSION, and doesn't mess around with any 
 * sort of fancy partials-memory management (we just allocate new partials arrays whenever we need do). 
 * Of course, this makes this thing super slow, you'd be ludicrous to use this in a release.   
 * @author brendano
 *
 */
public abstract class SimpleComputeCore extends AbstractComputeCore {
	
	//Map so we can quickly find nodes using their number
	private Map<Integer, ComputeNode> nodeMap = new HashMap<Integer, ComputeNode>();
	
	//Tips always have indices form 0..number of tips -1. 
	private TipComputeNode[] tips;
	
	//List of all internal nodes for fast traversing 
	private List<ComputeNode> allNodes = new ArrayList<ComputeNode>(256);
	
	private MutationModel matL; 
	private MutationModel matR; 

	
	//Emit debugging info to stdout
	final boolean verbose = false;
	
	private List<ComputeNode> rootRanges = new ArrayList<ComputeNode>(100);
	
	private int GCFrequency = 100; //Once every 100 node adds attempt to collect garbage
	private int nodesAdded = 0;
	
	public SimpleComputeCore(int numTips, MutationModel model) {
		Map<String, String> attrs = new HashMap<String, String>();
		attrs.put("stationaries", "0.25 0.25 0.25 0.25");
		//matL = model.getNew();
		//matR = model.getNew();
		
		tips = new TipComputeNode[numTips];
		nextNodeNumber = numTips;
	}
	

	
	/**
	 * Remove all ComputeNodes that are no longer referenced. We do this ONLY AFTER ACCEPT steps, in which we traverse from
	 * the root ranges downward, flagging all nodes that can be reached. We then discard all of the rest. 
	 */
	private void collectGarbage() {
		Stack<ComputeNode> stack = new Stack<ComputeNode>();
		Map<Integer, ComputeNode> newMap = new HashMap<Integer, ComputeNode>();
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

		nodeMap = newMap;
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
	}
	
	public void proposeAll() {
		for(ComputeNode node : allNodes) {
			node.activeState = node.proposedState;
		}
//		for(int i=0; i<tips.length; i++) {
//			TipComputeNode tip = tips[i];
//			tip.activeState = tip.proposedState;
//		}
	}
	
	public void accept() {
		for(ComputeNode node : allNodes) {
			if (isNodeProposed(node)) {

				node.currentState.rootDL = node.proposedState.rootDL;
				node.currentState.startSite = node.proposedState.startSite;
				node.currentState.endSite = node.proposedState.endSite;
				node.currentState.height = node.proposedState.height;
				
				node.currentState.leftNode = node.proposedState.leftNode;
				node.currentState.rightNode = node.proposedState.rightNode;
				node.activeState = node.currentState;
				
				double[][] tmp = node.currentState.partials;
				node.currentState.partials = node.proposedState.partials;
				node.proposedState.partials = tmp;
				
			}
		}
		
		
		if (allNodes.size() > 100 && nodesAdded % GCFrequency == 0 || allNodes.size() > 1000) {
			collectGarbage();
		}
	}
	
	
	/**
	 * Reject current state. All activeState refs are set to currentState
	 */
	public void reject() {
		for(ComputeNode node : allNodes) {
			node.activeState.startSite = node.currentState.startSite;
			node.activeState.endSite = node.currentState.endSite;
			node.activeState.height = node.currentState.height;
			node.activeState.rightNode = node.currentState.rightNode;
			node.activeState.leftNode = node.currentState.leftNode;
			node.activeState.partials = node.currentState.partials;
			node.activeState.rootDL = node.currentState.rootDL;
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
		
		node.activeState = node.proposedState;
		node.proposedState.leftNode = node.currentState.leftNode;
		node.proposedState.rightNode = node.currentState.rightNode;
		node.proposedState.startSite = node.currentState.startSite;
		node.proposedState.endSite = node.currentState.endSite;
		node.proposedState.height = nodeHeight;
	
		node.activeState.partials = node.proposedState.partials;
	}
	
	public void proposeRange(int nodeNumber, double nodeHeight, int globalMin, int globalMax, int lNode, int rNode) {
		//If this node is not new, then we can search around for this node and overwrite the range
		ComputeNode node = nodeMap.get(nodeNumber);
		if (node == null) {
			//Make a new node, and be sure to put it in the map
			node = new ComputeNode(nodeNumber);
			nodeMap.put(nodeNumber, node);
			allNodes.add(node);
			node.currentState.partials = makeNewPartials(globalMax - globalMin);
			node.proposedState.partials = makeNewPartials(globalMax-globalMin);
			nodesAdded++;
			if (verbose) {
				System.out.println("ComputeCore allocating node " + nodeNumber);
			}
		}
		

		if (verbose)
			System.out.println("Proposing node " + nodeNumber + " height : " + nodeHeight + " start: " + globalMin + " end: " + globalMax);
		
		node.activeState = node.proposedState;
		node.proposedState.leftNode = lNode;
		node.proposedState.rightNode = rNode;
		node.proposedState.startSite = globalMin;
		node.proposedState.endSite = globalMax;
		node.proposedState.height = nodeHeight;
	
		node.activeState.partials = node.proposedState.partials;
		if (node.activeState.partials.length != (globalMax - globalMin)) {
			node.activeState.partials = adjustPartials(node.activeState.partials, globalMax-globalMin);
		}
	} 

	public void emitState() {
		System.out.println("ComputeCore tracking " + allNodes.size() + " ComputeNodes...");
		for(Integer num : nodeMap.keySet()) {
			ComputeNode node = nodeMap.get(num);
			System.out.println("#" + num + "  height=" + node.activeState.height + " sites=[" + node.activeState.startSite + " - " + node.activeState.endSite +")");
		}
	}
	
	private double[][] makeNewPartials(int size) {
		return new double[size][4];
	}
	
	private double[][] adjustPartials(double[][] oldPartials, int newSize) {
		return new double[newSize][4];
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

	public boolean isNodeProposed(ComputeNode node) {
		return (node.activeState.partials == node.proposedState.partials 
				|| node.activeState.height != node.currentState.height
				|| node.activeState.leftNode != node.currentState.leftNode 
				|| node.activeState.rightNode != node.currentState.rightNode);	
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
		checkRootRanges();
		
		double logDL = 0;
		for(ComputeNode rootNode : rootRanges) {
			logDL += computeLogRootDLForRange(rootNode);
		}


		return logDL;
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
		if (start == rootNode.activeState.startSite && end == rootNode.activeState.endSite) {
			if (rootNode.activeState == rootNode.currentState) {
				return rootNode.activeState.rootDL;
			}
			else {
				rememberDL = true;
			}
		}
		
		double[][] partials = rootNode.activeState.partials; //Should be active, not proposed state, because not all root ranges will be activeProposed (some may not have been changed by the last modification)
		int globalStart = Math.max(start, rootNode.activeState.startSite);
		int globalEnd = Math.min(end, rootNode.activeState.endSite);
		
		int startIndex = globalStart - rootNode.activeState.startSite; //First index in partials where we begin calculating
		int endIndex = globalEnd - rootNode.activeState.startSite;	   //One after last index of partials to calculate
		
		//System.out.println("Computing root DL for range " + start + " - " + end );
		//System.out.println("Start index : " + startIndex + " end index: " + endIndex);
		for(int index = startIndex; index<endIndex; index++) {
			double siteProb = 0;
			for(int i=0; i<stateCount; i++) {
				siteProb += partials[index][i] * matL.getStationaryForState(i);
			}
			
			
			if (Double.isNaN(siteProb)) {
				System.out.println("Prob is NaN for pattern compute node !" );
			}
			if (siteProb==0) {
				System.out.println("Prob is zero for pattern !");
			}
			//System.out.println("Prob for pattern at pattern index " + pattern + " : " + patternProb);
			logDL += Math.log(siteProb); // * getCardinalityForPatternIndex(pattern)
			//System.out.println("Site prob #" + (index+rootNode.activeState.startSite) + " : " + Math.log(siteProb));
			if (Double.isInfinite(logDL)) {
				System.out.println("Prob is infinite for pattern !");
			}
		}

		if (rememberDL) {
			rootNode.activeState.rootDL = logDL;
		}
		return logDL;
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
		double[][] partials = node.proposedState.partials;
		computeForTwoStates(matrixL, matrixR,
							state.startSite, state.endSite,
							tipLStates, tipRStates,
							partials, node.activeState.startSite);
		
	}
	
	private void computePartialsForOneTip(ComputeNode node,
											TipComputeNode tip,
											ComputeNode childNode) {
		NodeState state = node.activeState;
		double distL = state.height - tip.activeState.height;
		double distR = state.height - childNode.activeState.height;
		
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
		
		int[] tipStates = tip.states;
		double[][] childPartials = childNode.activeState.partials;
		double[][] partials = node.proposedState.partials; //We always write new partials into the proposed state
		computeProbsForState(matL.getMatrix(), matR.getMatrix(), 
							node.activeState.startSite, node.activeState.endSite,
							tipStates, 
							childPartials, childNode.activeState.startSite, 
							partials);
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
				
				int siteIndex = 0;
				for(int i=state.startSite; i<state.endSite; i++) {
						computeProbabilities(matrixL,
											 matrixR, 
											 lChild.getPartialsForSite(i),
											 rChild.getPartialsForSite(i), 
											 node.proposedState.partials[siteIndex] );
						siteIndex++;
				}
			}
			

		}
		
		//CRITICAL! Calls to nodes that are ancestors of this node need to know that they must recompute their partials as well, 
		//to do this they ask descendant nodes if their activeState = proposedState. If this isn't set here, changes won't 
		//propagate to the root
		node.activeState = node.proposedState;		
	}
	
	
	
	@Override
	public int countPatterns() {
		int sum = 0;
		Stack<ComputeNode> stack = new Stack<ComputeNode>();
		for(ComputeNode rootRange : rootRanges) {
			stack.clear();
			stack.push(rootRange);
			while(! stack.isEmpty()) {
				ComputeNode node = stack.pop();
				sum += node.activeState.endSite; //Four for the invariant sites - we assume there's always all of these
				
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

	/**
	 * Compute partial probabilities for 4 states, storing the result in 'result'
	 * @param matL
	 * @param matR
	 * @param leftProbs
	 * @param rightProbs
	 * @param result
	 */
//	protected static void computeProbabilities(double[][] matL,  
//											 double[][] matR, 
//											 double[] leftProbs, 
//											 double[] rightProbs, 
//											 double[] result) {
//		double[] vecL = matL[0];
//		double[] vecR = matR[0];
//
//		double l0 = leftProbs[0];
//		double l1 = leftProbs[1];
//		double l2 = leftProbs[2];
//		double l3 = leftProbs[3];
//
//		double r0 = rightProbs[0];
//		double r1 = rightProbs[1];
//		double r2 = rightProbs[2];
//		double r3 = rightProbs[3];
//
//		double probL = vecL[0]*l0  + vecL[1]*l1 + vecL[2]*l2 + vecL[3]*l3;
//		double probR = vecR[0]*r0 + vecR[1]*r1+ vecR[2]*r2  + vecR[3]*r3;
//		result[0] =  probL*probR;		
//
//		vecL = matL[1];
//		vecR = matR[1];
//		probL = vecL[0]*l0  + vecL[1]*l1 + vecL[2]*l2 + vecL[3]*l3;
//		probR = vecR[0]*r0 + vecR[1]*r1+ vecR[2]*r2  + vecR[3]*r3;
//		result[1] =  probL*probR;
//
//		vecL = matL[2];
//		vecR = matR[2];
//		probL = vecL[0]*l0  + vecL[1]*l1 + vecL[2]*l2 + vecL[3]*l3;
//		probR = vecR[0]*r0 + vecR[1]*r1+ vecR[2]*r2  + vecR[3]*r3;
//		result[2] =  probL*probR;
//
//		vecL = matL[3];
//		vecR = matR[3];
//		probL = vecL[0]*l0  + vecL[1]*l1 + vecL[2]*l2 + vecL[3]*l3;
//		probR = vecR[0]*r0 + vecR[1]*r1 + vecR[2]*r2  + vecR[3]*r3;
//		result[3] =  probL*probR;
//	}
	
	
	/**
	 * Compute partials for a single site when node is above one tip and one non-tip
	 * @param mat1
	 * @param mat2
	 * @param stateIndex
	 * @param probs
	 * @param result
	 */
//	private static void computeProbsForOneState(double[][] mat1,  double[][] mat2, int stateIndex, double[] probs, double[] result) {
//		double p0 = probs[0];
//		double p1 = probs[1];
//		double p2 = probs[2];
//		double p3 = probs[3];
//
//		for(int i=0; i<result.length; i++) {
//			double[] vec = mat2[i];
//			double probR = vec[0]*p0 + vec[1]*p1 + vec[2]*p2 + vec[3]*p3;
//			result[i] = mat1[i][stateIndex]*probR;		
//		}
//	}
	
	/**
	 * Compute partials when a node is above one tip and one non-tip
	 * @param tipMat Transition matrix for branch toward tip
	 * @param nodeMat Transition matrix for branch toward node
	 * @param startSite
	 * @param endIndex
	 * @param probStates
	 * @param tipStates
	 * @param resultStates
	 */
//	public void computeProbsForState(double[][] tipMat, double[][] nodeMat, 
//									 int startSite, int endSite,
//									 int[] tipStates, 
//									 double[][] probStates,	int probOffset,
//									 double[][] resultStates) {
//		int site = startSite;
//		final int siteCount = endSite - startSite;
//		for(int index = startSite; index<endSite; index++) {
//			double[] probs = probStates[ index-probOffset ];
//			double[] resultProbs = resultStates[ index-startSite ]; 
//
//			computeProbsForOneState(tipMat, nodeMat, tipStates[index], probs, resultProbs);
//			site++;
//		}
//	}


//	private void computeForTwoStates(double[][] mat1, double[][] mat2, 
//			int startSite, int endSite,
//			int[] tipStates1, 
//			int[] tipStates2, 
//			double[][] resultStates, int resultOffset ) {
//
//		double[] m10 = mat1[0];
//		double[] m11 = mat1[1];
//		double[] m12 = mat1[2];
//		double[] m13 = mat1[3];
//
//		double[] m20 = mat2[0];
//		double[] m21 = mat2[1];
//		double[] m22 = mat2[2];
//		double[] m23 = mat2[3];
//
//		for(int site = startSite; site<endSite; site++) {
//			double[] resultProbs = resultStates[ site - resultOffset ]; 
//
//			int index1 = tipStates1[site];
//			int index2 = tipStates2[site];
//
//			resultProbs[0] = m10[ index1] * m20[index2];
//			resultProbs[1] = m11[ index1] * m21[index2];
//			resultProbs[2] = m12[ index1] * m22[index2];
//			resultProbs[3] = m13[ index1] * m23[index2];
//		}
//	}
	
	
	
	
	
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
		
	}
	
	class NodeState {
		
		double rootDL = Double.NaN;
		
		double[][] partials;
		int startSite = -1;
		int endSite = -1;
		int leftNode = -1;
		int rightNode = -1;
		double height = -1;
		
	}

	
	class TipComputeNode {
		
		int[] states;
		
		TipNodeState activeState = new TipNodeState();
		TipNodeState currentState = new TipNodeState();
		TipNodeState proposedState = new TipNodeState();
		
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


	public int countPatterns(int[] nodeIDs) {
		return 0;
	}



	@Override
	public int getNodeCount() {
		return allNodes.size();
	}



	@Override
	public boolean isNodeProposed(int nodeNumber) {
		ComputeNode node = nodeMap.get(nodeNumber);
		return isNodeProposed(node);
	}



}
