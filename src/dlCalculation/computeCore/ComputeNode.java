package dlCalculation.computeCore;

/**
 * A contiguous range of sites over which the partial 'data likelihood' can be calculated.  
 * @author brendano
 *
 */
public class ComputeNode {
	
	static final int dimension = 4;
	
	//A unique, immutable number identifying this node
	final int refID;
	
	NodeState activeState = new NodeState();
	NodeState currentState = new NodeState();
	NodeState proposedState = new NodeState();
	
	public ComputeNode(int id) {
		this.refID = id;
	}

	public ComputeNode(int id, int mapSize, int rateCategories, int partialsSize) {
		this.refID = id;

		currentState.partials = new double[rateCategories][partialsSize][dimension];
		proposedState.partials = new double[rateCategories][partialsSize][dimension];
		
		currentState.lMatrices = new double[rateCategories][dimension+1][dimension+1];
		currentState.rMatrices = new double[rateCategories][dimension+1][dimension+1];

		proposedState.lMatrices = new double[rateCategories][dimension+1][dimension+1];
		proposedState.rMatrices = new double[rateCategories][dimension+1][dimension+1];
		
		//Another speed/memory tradeoff taking place here, and we're opting for speed. 
		//Gapped sites are given a state index of 5, and their tip probabilities are {1.0, 1.0, 1.0, 1.0}
		//When we're computing partials we'll sometimes come across sites with stateIndex=5, and we can either
		// a) Throw in an if/else to do something special at those sites. or
		// b) Change the transition matrix so that it handles that case appropriately. 
		// Right now we're choosing b, and we fill the 5th column of all transition matrices with ones
		for(int j=0; j<rateCategories; j++) {
			for(int i=0; i<(dimension+1); i++) {
				currentState.lMatrices[j][i][dimension] = 1.0;
				currentState.lMatrices[j][i][dimension] = 1.0;
				currentState.rMatrices[j][dimension][i] = 1.0;
				currentState.rMatrices[j][dimension][i] = 1.0;
				
				proposedState.lMatrices[j][i][dimension] = 1.0;
				proposedState.lMatrices[j][i][dimension] = 1.0;
				proposedState.rMatrices[j][dimension][i] = 1.0;
				proposedState.rMatrices[j][dimension][i] = 1.0;
			}
		}
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
