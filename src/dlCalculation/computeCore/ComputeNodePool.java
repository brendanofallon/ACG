package dlCalculation.computeCore;

import java.util.Stack;

/**
 * Storage for a bunch of ComputeNodes that are used by MultiRateCore. The idea here is that we store a bunch of available nodes
 * in a stack ('pool'), and when nodes are used we hand out a reference to them and also store the node in an array indexed by the
 * node's refID, which is unique. When the compute core is done with a node, it calls retireNode(), and we remove the node from 
 * the array and push it back onto the stack. The stack has a maximum size (defined by maxPoolSize), and if the stack is empty we
 * just create a new node from scratch. 
 * 
 * Since we use the node's refID as the index for array lookups, we actually allocate an array of size (tipCount + maxPoolSize)
 * to store the nodes (although nodes are only created as we need them). So the first tipCount array indices are *never* used 
 * right now. This avoids having to do a subtraction every time we want to get a node from the array.  
 * 
 * The entire idea behind this is that a) We're not constantly instantiating new compute nodes, we just pop the stack and b) By keeping
 * track of nodes that are in use and retired, we can recycle old refID's, and thus we'll never run out. In previous long runs we 
 * actually ended up allocating more than Integer.MAX_VALUE nodes. 
 * @author brendano
 *
 */
public class ComputeNodePool {

	final int maxPoolSize = 1600; //Maximum size of node storage pool

	private Stack<ComputeNode> pool = new Stack<ComputeNode>(); //Storage for entirely unused nodes
		
	private ComputeNode[] store = new ComputeNode[maxPoolSize];
	
	//All nodes are assigned a unique, immutable number (their 'refID') upon creation, this field
	//stores the next value to be assigned and is incremented whenever we assign a new id
	private final int firstNodeNumber;
	private int nextNodeNumber;
	
	//Counts number of assigned nodes
	private int numAssignedNodes = 0;
	
	
	final int mapSize;
	final int partialsSize;
	final int rateCategories;
	

	
	public ComputeNodePool(int tipCount, int intArrayLength, int partialsSize, int rateCategories) {
		this.firstNodeNumber = tipCount;
		nextNodeNumber = firstNodeNumber;
		
		this.mapSize = intArrayLength;
		this.partialsSize = partialsSize;
		this.rateCategories = rateCategories;
	}
	
	/**
	 * Remove this node from the assigned nodes map and put it back into the unused node pool (stack)
	 * @param id
	 */
	public void retireNode(int id) {
		ComputeNode node = store[ id ];
		if (node != null) {
			pool.push(node);
			store[id] = null;
			numAssignedNodes--;
		}
	}
		
	public int getPoolSize() {
		return pool.size();
	}
	
	/**
	 * Obtain a new compute node for use. 
	 * @return
	 */
	public ComputeNode assignNode() {
		if (pool.isEmpty()) {
			ComputeNode node = createNewNode();
			store[ node.refID ] = node;
			pool.push(node);	
		}
		ComputeNode newNode = pool.pop();
		store[ newNode.refID ] = newNode;
		numAssignedNodes++;
		//Poison some info so we don't re-use old values. DL will be subtly broken if this gets erased 
		newNode.currentState.lLength = -1;
		newNode.currentState.rLength = -1;
		newNode.activeState.lLength = -2;
		newNode.activeState.rLength = -2;
		return newNode;
	}
	
	public int getAssignedNodeCount() {
		return numAssignedNodes;
	}

	/**
	 * Obtain an already assigned node by number
	 * @param nodeNumber
	 * @return The assigned node with the given number, or null if no such node exists
	 */
	public ComputeNode getAssignedNode(int nodeNumber) {
		return store[ nodeNumber ];
	}
	
	/**
	 * Allocate a new node, assign it a unique id number, and increment nextNodeNumber
	 * @return
	 */
	private ComputeNode createNewNode() {
		if ( nextNodeNumber==maxPoolSize) {
			throw new IllegalStateException("Ran out of node space (using more than " + maxPoolSize + " nodes)");
		}
		ComputeNode newNode = new ComputeNode(nextNodeNumber);
		
		newNode.currentState.subtreePatternIndices = new int[mapSize];
		newNode.proposedState.subtreePatternIndices = new int[mapSize];
		newNode.currentState.subtreePatternMap = new int[mapSize];
		newNode.proposedState.subtreePatternMap = new int[mapSize];
		newNode.currentState.partialsMap = new int[mapSize];
		newNode.proposedState.partialsMap = new int[mapSize];
		
		newNode.currentState.partials = new double[rateCategories][partialsSize][4];
		newNode.proposedState.partials = new double[rateCategories][partialsSize][4];
		
		newNode.currentState.lMatrices = new double[rateCategories][4][4];
		newNode.currentState.rMatrices = new double[rateCategories][4][4];

		newNode.proposedState.lMatrices = new double[rateCategories][4][4];
		newNode.proposedState.rMatrices = new double[rateCategories][4][4];
		
		newNode.currentState.invariantPartials = new double[rateCategories][4][4];
		newNode.proposedState.invariantPartials = new double[rateCategories][4][4];
		
		nextNodeNumber++;
		return newNode;
	}
}
