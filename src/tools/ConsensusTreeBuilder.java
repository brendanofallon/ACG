package tools;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tools.Tree.Node;

public class ConsensusTreeBuilder {

	final String labelSep = "&@&"; 	// Just for debugging! Use this later :"%#&"; //Something unlikely to ever appear in a real label
	private final String emptyString = "";
	int maxTrees = 100000;
	int subsampleRate = 1;
	int burninTrees = 0;
	int totalClades = 0;
	//int numInputTrees = 0;
	double targetFraction = 0.50; 	//Specifies the fraction of input trees containing clade required for 
								   //representation in the consensus. This must be at least 0.50

	StringBuilder merge  = new StringBuilder(); //working object for clade counting
	private boolean DEBUG = false;

	Map<String, TreeItem> clades = new HashMap<String, TreeItem>();
	
	Tree consensusTree = null; //Gets created when we call buildConsensus();
	
	public ConsensusTreeBuilder()  {
	}
	
	public void clear() {
		consensusTree = null;
		clades = new HashMap<String, TreeItem>();
		merge = new StringBuilder();
	}
	
	public Tree buildConsensusFromFile(File file) throws IOException {
		TreeReader reader = new TreeReader(file);
		int numInputTrees = tabulateTrees(reader);
		ArrayList<TreeItem> majorityClades = buildMajorityCladeList(numInputTrees);
		consensusTree = new Tree(); 
		mergeClades(consensusTree, majorityClades, numInputTrees);
		List<Node> nodes = consensusTree.getAllNodes();
		for(Node node : nodes) {
			node.removeAnnotation("tips");
		}
		return consensusTree;
	}

	public String getConsensusNewick() {
		if (consensusTree == null)
			return null;
		else
			return consensusTree.getNewick();
	}
	
	/**
	 * Reads through the input buffer to parse out trees, then calls countClades on each
	 * tree to add all the clades to the hash table
	 * @param buf Input buffer
	 * @param clades HashTable to add clade info to
	 * @return Number of trees counted
	 * @throws IOException
	 */
	private int tabulateTrees(TreeReader reader) throws IOException {
		int countedTrees = 0;
		int examinedTrees =0;

		Node treeRoot = reader.getNextTreeRoot(); //This is the slow part and we do it for every single tree, regardless of subSampleRate
		
		while(treeRoot!=null && countedTrees < maxTrees) {
			examinedTrees++;
			//System.out.println("Examining tree #" + examinedTrees );
			if (treeRoot!=null && (double)examinedTrees % subsampleRate == 0.0 && examinedTrees > burninTrees) {
				//System.out.println("Counting tree #" + examinedTrees + " val : " + (double)examinedTrees/subsampleRate );
				if (treeRoot.getNumOffspring()<2) {
					System.err.println("Error reading tree, found less than two two tips from root, not tabulating clades...");
				}
				else {
					if (DEBUG)
						System.out.println("Counting tree #" + examinedTrees);
					countClades(treeRoot);
					countedTrees++;
				}
				

			}

			treeRoot = reader.getNextTreeRoot();
		}
		
		//System.out.println("Counted " + countedTrees + " of " + examinedTrees + " trees = " + (double)countedTrees/(double)examinedTrees + "%");
		return countedTrees;
	}
	
	/**
	 * This recursive function is responsible for adding all of the nodes in a tree to the growing 
	 * hash table. 
	 * @param root
	 * @param clades
	 * @return
	 */
	public ArrayList<String> countClades(Node root) {
		ArrayList<String> tipLabels = new ArrayList<String>();
		if (root.getNumOffspring()==0) {
			tipLabels.add(root.getLabel());
		}
		else {
			tipLabels = countClades(root.getOffspring(0));
			for (int i=1; i<2; i++) {
				tipLabels.addAll(countClades((Node)root.getOffspring(i)));
			}
		}
							
		Collections.sort(tipLabels);
		merge.replace(0, merge.length(), emptyString);
		
		for(String label : tipLabels) {
			merge.append(label + labelSep);
		}

		root.addAnnotation("tips", merge.toString()); 
		String key = merge.toString();
		TreeItem hashItem = clades.get(key);
		totalClades++;
		if (hashItem==null) {
			TreeItem newItem = new TreeItem();
			newItem.count = 1;
			newItem.cardinality = tipLabels.size();
			newItem.height = root.getHeight();
			newItem.M2 = 0;	//Running total of variance in dist to parent
			newItem.clade = key;
			clades.put(key, newItem);
		}
		else {
			hashItem.count++;
			double height = root.getHeight();
			double delta = height-hashItem.height;
			hashItem.height += delta/(double)hashItem.count;
			hashItem.M2 += delta*(height-hashItem.height);
		}
		return tipLabels;
	}
	
	/**
	 * Extracts the majority clades from the hashtable of all clades and, then sorts it by
	 * clade cardinality, then returns it
	 * 
	 * @param clades
	 * @return
	 */
	public ArrayList<TreeItem> buildMajorityCladeList(int inputTreeCount) {
		ArrayList<TreeItem> cladeList = new ArrayList<TreeItem>();
		int numKeys = clades.size();
		int currentKey = 0;
		
		for(String key : clades.keySet()) {
			TreeItem cladeInfo = clades.get(key);
			//System.out.println("Clade : " + clade + " frequency : " + cladeInfo.count/(double)numInputTrees);
			if ((double)cladeInfo.count/(double)inputTreeCount > targetFraction) {
				cladeList.add(cladeInfo);
			}

			currentKey++;
		}
		
		Collections.sort(cladeList);
		return cladeList;
	}


	/**
	 * Reads through a list of clade info items and adds clades successively to tree
	 * Clade info list MUST be sorted by clade cardinality (with biggest cardinality--
	 * the clade corresponding to the root, first)
	 * 
	 * @param majorityClades
	 * @return
	 */
	public Node mergeClades(Tree tree, ArrayList<TreeItem> majorityClades, int inputTreeCount) {
		Node root = tree.createNode();
		tree.setRoot(root);
		if (majorityClades.size()==0) {
			throw new IllegalStateException("Error building majority tree: Could not identify majority clades");
		}
		
		if (majorityClades.size()<3) {
			System.out.println("Hmmm... majority clades is very small, majority tree builder may have been messed up..");
		}
		
		TreeItem cladeInfo = majorityClades.get(0);
		root.addAnnotation("tips", cladeInfo.clade);
		root.addAnnotation("height", new Double(cladeInfo.height).toString());
		root.setHeight(cladeInfo.height);
		double var = cladeInfo.M2/(double)(cladeInfo.count-1.0);
		//System.out.println("Root node var : " + var);
		if (var>0) {
			//System.out.println("Root node error : " + Math.sqrt(var));
			root.addAnnotation("error", new Double(Math.sqrt(var)).toString());
		}
		for(int i=1; i<majorityClades.size(); i++) {
			addClade(tree, root, majorityClades.get(i), inputTreeCount);
		}

		if (root.getNumOffspring()<2) {
			System.out.println("Root has only one or two offspring, merge clades must have failed somehow");
		}
		if (root==null) {
			System.out.println("merge clades is returning a null node");
		}
		return root;
	}
	
	
	/**
	 * Recursive (pre-order traversal) function which identifies the right spot in the tree to which to add a new clade and
	 * then adds it to the root
	 * @param root
	 * @param cladeInfo
	 */
	private void addClade(Tree tree, Node root, TreeItem cladeInfo, int inputTreeCount) {
		//We traverse the tree and look for a node that contains this clade, but
		//that does not have any children that contain this clade
		
		if (containsClade(root, cladeInfo.clade)) {
			boolean found = false;
			for(Node kid : root.getOffspring()) {
				if (containsClade(kid, cladeInfo.clade)) {
					found = true;
					
					addClade(tree, kid, cladeInfo, inputTreeCount);
				}
			}
			if (!found) {
				//Here the root contains this clade, but none of root's kids do, so add the clade here
				//System.out.println("Clade " + cladeInfo.clade + " was not found in any kids, but was contained in this node, so adding here");
				Node newNode = tree.createNode(cladeInfo.clade);
				newNode.addAnnotation("tips", cladeInfo.clade);
				newNode.addAnnotation("support", new Double((double)cladeInfo.count/(double)inputTreeCount).toString());
				double height = cladeInfo.height;
				newNode.addAnnotation("height", new Double(height).toString());
				double var = cladeInfo.M2/(double)(cladeInfo.count-1.0);
				//System.out.print("Var: " + var);
				if (var>0) {
					double stdev = Math.sqrt(var);
					//System.out.println(" stdev : " + stdev);
					newNode.addAnnotation("error", new Double(stdev).toString());	
				}
					
				
				newNode.setParent(root);
				root.addOffspring(newNode);
				try {
					//double parentHeight = Double.parseDouble( root.getAnnotation("height") ) ;
					newNode.setHeight(cladeInfo.height);
				}
				catch (Exception nfe) {
					System.err.println("Could not read height value from root node, this means we can't set the node height for a node. Uh oh");
				}
				
				if (cladeInfo.cardinality==1) {
					newNode.setLabel( cladeInfo.clade.replaceAll(labelSep, "") );
				}
			}
		}
	}
	


	private boolean containsClade(Node root, String clade) {
		String[] rootTips = root.getAnnotation("tips").split(labelSep);
		String[] cladeTips = clade.split(labelSep);

		for(int i=0; i<cladeTips.length; i++) {
			String tip = cladeTips[i];
			boolean foundMatch = false;
			for(int j=0; j<rootTips.length; j++) {
				if (tip.equals(rootTips[j])) {
					foundMatch = true;
					break;
				}	
			}
			
			if (!foundMatch) {
				return false;
			}
			
		}
		return true;
	}
	
	/**
	 * Holds some info regarding a clade 
	 * @author brendan
	 *
	 */
	public class TreeItem implements Comparable<TreeItem> {
		
		public int count = 0;
		public double height = 0;
		public int cardinality = 0;
		public double M2 = 0;
		public String clade = null;
		
		public int compareTo(TreeItem c) {
			return c.cardinality - cardinality;
		}
	}
	
//	public static void main(String[] args) {
//		File input = new File("badTree.trees");
//		ConsensusTreeBuilder builder;
//		try {
//			builder = new ConsensusTreeBuilder(input);
//			builder.buildConsensus();
//			String newick = builder.getConsensusNewick();
//			System.out.println("Got newick : \n" + newick);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//	}
}
