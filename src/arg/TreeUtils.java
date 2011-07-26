package arg;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import math.RandomSource;
import cern.jet.random.Exponential;

/**
 * A bunch of static utilities that have to do with ARG reading, writing, and random generation 
 * @author brendan
 *
 */
public class TreeUtils {

	/**
	 * Read a string from the file and assuming it's in newick form return it as a tree
	 * @param file
	 * @return
	 */
	public static CoalNode buildTreeFromNewick(File file) {
		StringBuilder str = new StringBuilder();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line = reader.readLine();
			while (line != null) {
				str.append( line.trim().replaceAll("\\n", "") );
				line = reader.readLine();
			}
			
			return buildTreeFromNewick(str.toString());
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	/**
	 * Read a string from the file and assuming it's in newick form return it as an ARG
	 * @param file
	 * @return
	 */
	public static ARG buildARGFromNewick(File file) {
		StringBuilder str = new StringBuilder();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line = reader.readLine();
			while (line != null) {
				str.append( line.trim().replaceAll("\\n", "") );
				line = reader.readLine();
			}
			
			return newickToARG(str.toString());
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	/**
	 * Constructs a new tree from the given String and returns the root of the tree.
	 * @param treeStr
	 * @return
	 */
	public static CoalNode buildTreeFromNewick(String treeStr, ARG arg) {
		CoalNode root = new CoalNode(arg);
		buildSubtreeFromNewick(root, treeStr, arg);
		List<ARGNode> nodes = collectAllNodes(root);
		normalizeHeights(nodes);
		return root;
	}
	
	public static CoalNode buildTreeFromNewick(String treeStr) {
		return buildTreeFromNewick(treeStr, new ARG());
	}
	
	public static ARG newickToARG(String newick) {
		ARG arg = new ARG();
		CoalNode root = buildTreeFromNewick(newick, arg);
		List<ARGNode> nodes = collectAllNodes(root);
		arg.initializeFromNodeList(nodes, 1, false);
		return arg;
	}
	
	/**
	 * Traverses the collection of nodes, adding every node to the a list and returning it. This should work fine
	 * for ARGs, but it cannot contain cycles in time.  
	 * @param root
	 * @return
	 */
	public static List<ARGNode> collectAllNodes(ARGNode root) {
		Stack<ARGNode> stack = new Stack<ARGNode>();
		List<ARGNode> allNodes = new ArrayList<ARGNode>(50);
		stack.push(root);
		
		while(! stack.isEmpty()) {
			ARGNode node = stack.pop();
			if (! allNodes.contains(node)) {
				allNodes.add(node);
			}
			
			for(int i=0; i<node.getNumOffspring(); i++) {
				stack.push(node.getOffspring(i));
			}
		}
		
		return allNodes;
	}
	
	/**
	 * Shifts all node heights such that the minimum height is zero
	 * @param root
	 */
	public static void normalizeHeights(List<ARGNode> nodes) {
		double minHeight = Double.POSITIVE_INFINITY;
		for(ARGNode node : nodes) {
			if (node.getHeight() < minHeight)
				minHeight = node.getHeight();
		}
		
		//Subtract minimum height from all nodes, so tips will have heights very close to zero.
		//Since its actually an error for tips to have a nonzero height, we actually do a 
		//bit of fudging here and round very small numbers (less than 1e-14) to zero. 
		for(ARGNode node : nodes) {
			if (node.getHeight() - minHeight > 1e-14)
				node.proposeHeight(node.getHeight() - minHeight);
			else {
				node.proposeHeight(0.0);
			}
		}
	}

	/**
	 * Helper function for constructing an ARG from a newick string, this constructs a subtree
	 * based on the partial newick string provided. 
	 * @param root
	 * @param treestr
	 */
	protected static void buildSubtreeFromNewick(CoalNode root, String treestr, ARG owner) {
		if (treestr==null || treestr.equals("") || !hasKids(treestr)) {
			return;
		}
		
		List<String> kidStrs = getOffspringStrings(0, treestr);
		if (kidStrs.size()==2) {
			String kidStr1 = kidStrs.get(0);
			ARGNode kid;
			if (hasKids(kidStr1))
				kid = new CoalNode(owner);
			else {
				kid = new TipNode(owner);
			}
			double dist = subTreeDistFromParent(kidStr1);
			kid.proposeParent(0, root);
			kid.proposeHeight(root.getHeight() - dist);
			root.proposeOffspring(0, kid);
			String label = subTreeLabel(kidStr1);
			if (label.trim().length()>0) {
				kid.setLabel( label );
			}
			
			if (kid instanceof CoalNode)
				buildSubtreeFromNewick((CoalNode)kid, kidStr1, owner);
			
			String kidStr2 = kidStrs.get(1);
			ARGNode kid2;
			if (hasKids(kidStr2)) 
				kid2 = new CoalNode(owner);
			else { 
				kid2 = new TipNode(owner);
			}
				
			dist = subTreeDistFromParent(kidStr2);
			kid2.proposeParent(0, root);
			kid2.proposeHeight(root.getHeight() - dist);
			root.proposeOffspring(1, kid2);
			label = subTreeLabel(kidStr2);
			if (label.trim().length()>0) {
				kid2.setLabel( label );
			}
			
			if (kid2 instanceof CoalNode)
				buildSubtreeFromNewick((CoalNode)kid2, kidStr2, owner);
			
			return;
		}
		
		if (kidStrs.size()!=0) {
			for(String str : kidStrs) {
				System.out.println(str);	
			}
			
			throw new IllegalStateException("Incorrect number of kids in newick string for binary tree reading. \n kid num = " + kidStrs.size());
		}
		
	}

	protected static boolean hasKids(String subtree) {
		if (subtree.indexOf(",")<0)
			return false;
		else
			return true;
	}

	/**
	 * Return a list of strings representing the children descending from the subtree that
	 * begins at the position startIndex
	 * @param startIndex
	 * @param treeStr
	 * @return String representing the subtrees that start at the given position
	 */
	protected static List<String> getOffspringStrings(int startIndex, String treeStr) {
		ArrayList<String> children = new ArrayList<String>();
		treeStr = treeStr.trim();
		int lastParen = matchingParenIndex(startIndex, treeStr);
		
		//Scan along subtree string, create new strings separated by commas *at 'highest level'*
		//not all commas, of course
		int i=startIndex+1;
		StringBuffer cstr = new StringBuffer();
		
		int count = 0;
		for(i=startIndex+1; i<lastParen; i++) {	
			if (treeStr.charAt(i)=='(')
				count++;
			if (treeStr.charAt(i)==')')
				count--;
			if (treeStr.charAt(i)==',' && count==0) {
				children.add(cstr.toString());
				cstr = new StringBuffer();
			}
			else	
				cstr.append( treeStr.charAt(i));
			
		}
		
		children.add(cstr.toString());
		
		return children;
	}
	
	/**
	 * Find the index of the parenthesis that matches the given paren
	 * @param firstPos Index of opening paren
	 * @param str String to search in
	 * @return Index of closing paren that matches the given start paren
	 */
	private static int matchingParenIndex(int firstPos, String str) {
		int i = firstPos;
		if (str.charAt(i) != '(') {
			System.err.println("Got a non-paren for first index in find matching paren");
			System.err.println(" Char is : |" + str.charAt(i) +"|");
			return -1;
		}
		int count = 0;
		for(i=firstPos; i<str.length(); i++) {
			if (str.charAt(i) == '(')
				count++;
			if (str.charAt(i) == ')')
				count--;
			if (count==0)
				return i;
		}
		
		System.err.println("Couldn't find matching paren for this string : |" + str + "|");
		System.err.println(" First paren index : " + firstPos);
		return -1;
	}
	
	/**
	 * Extracts the distance of this subtree from it's parent, which is typically given immediately after a colon
	 * that follows the parenthesis that closes this subtree. 
	 * @param subtree
	 * @return The subtree distance to its parent, or Double.NaN if it's not given. 
	 */
	protected static double subTreeDistFromParent(String subtree) {
		int lastColon = Math.max(subtree.lastIndexOf(":"), subtree.lastIndexOf(']')); //The square bracket is for beast-style annotations, which precede the branch length
		int lastParen = subtree.lastIndexOf(")");
		if (lastParen<lastColon)
			lastParen=subtree.length();
		if (lastColon > lastParen)
			System.err.println("Uh-oh, found bad values for branch length positions on this substring : " + subtree);
		
		if (lastColon==-1) {
			//System.out.println("No branch length found for this subtree, returning NaN");
			return Double.NaN;
		}
		String branchLengthStr = subtree.substring(lastColon+1, lastParen);
		double branchLength = Double.valueOf(branchLengthStr);
		return branchLength;
	}

	//Returns the distance of the subtree from a parent (just the number following the final colon) 
	protected static String subTreeLabel(String subtree) {
		int lastPos = subtree.lastIndexOf(":");
		if (lastPos==-1) 
			lastPos = subtree.length();
		
		boolean hasParens = subtree.indexOf(")") > 0;
		if (! hasParens && lastPos > 0) {
			return subtree.substring(0, lastPos).trim();
		}
		else  {
			int pos = subtree.indexOf('(');
			if (pos < 0) {
				System.out.println("Found a broken subtree : " + subtree);
				return "";
			}
			int matchingParen = matchingParenIndex(pos, subtree);
			String label;
			if (lastPos > matchingParen)
				label = subtree.substring(matchingParen+1, lastPos);
			else 
				label = "";
			return label;
		}
	}
	
	public static String getNewick(CoalNode root, boolean includeBranchLengths) {
		return getScaledNewick(root, includeBranchLengths, 1.0);
	}
	
	public static String getScaledNewick(CoalNode root, boolean includeBranchLengths, double scaleFactor) {
		StringBuffer str = new StringBuffer("(");
		int i;

		String lStr = getNewickSubtree( root.getLeftOffspring(), includeBranchLengths, scaleFactor);
		String rStr = getNewickSubtree( root.getRightOffspring(), includeBranchLengths, scaleFactor);
		
		if (lStr.hashCode() < rStr.hashCode()) {
			str.append( lStr + ", ");
			str.append( rStr );			
		}
		else {
			str.append( rStr + ", ");
			str.append( lStr );			
		}

		str.append(");");
		return str.toString();		
	}

	
	/**
	 * Returns a canonical string form of the tree, including branch lengths  
	 */
	public static String getScaledNewick(CoalNode root, double scaleFactor) {
		return getScaledNewick(root, true, scaleFactor);
	}
	
	/**
	 * Returns a canonical string form of the tree, including branch lengths  
	 */
	public static String getNewick(CoalNode root) {
		return getNewick(root, true);
	}
	
	/**
	 * Returns a canonical newick string for the subtree descending from the given node. 
	 * @param n The node which defines the clade to generate tre newick string for 
	 * @return
	 */
	private static String getNewickSubtree(ARGNode n, boolean includeBranchLengths, double scaleFactor) {
		String branchLengthStr  = "";
		if (includeBranchLengths) {
			branchLengthStr = ":" + ((n.getParent(0).getHeight() - n.getHeight())*scaleFactor);
		}
		
		if (n.getNumOffspring()==0) {
			if (n.getLabel() != null) {
				return new String(n.getLabel() + branchLengthStr );
			}
			else {
				return new String("ind?:" + branchLengthStr );
			}
		}
		
		if (n.getNumOffspring()==1) {
			System.err.println("Huh? This node has only one child! Newick representation may not be correct. Also, tree may be screwed up.");
			System.exit(1);
			return "ERROR"; //Code will never be reached, obviously, but apparently it's an error NOT to have this here
		}
		else {
			StringBuffer str = new StringBuffer("(");
			int i;
			String lStr = getNewickSubtree( n.getOffspring(0), includeBranchLengths, scaleFactor);
			String rStr = getNewickSubtree( n.getOffspring(1), includeBranchLengths, scaleFactor);
			
			if (lStr.hashCode() < rStr.hashCode()) {
				str.append( lStr + ", ");
				str.append( rStr );			
			}
			else {
				str.append( rStr + ", ");
				str.append( lStr );				
			}
			
			str.append(")" + /*n.getLabel() + */ branchLengthStr);
			return str.toString();
		}	
	}

	/**
	 * Given an ARG, we create a new tree that reflects the structure of the ARG at the given site.
	 * The ARG is not altered in any way, all new CoalNodes and Tips are created that mimic the structure
	 * @param arg
	 * @param bp
	 * @return
	 */
	public static CoalNode createMarginalTree(ARG arg, Integer site) {
		List<ARGNode> treeNodes = new ArrayList<ARGNode>();
		ARG owner = new ARG(); //Need an arg to assign node numbers
		for(TipNode tip : arg.getTips()) {
			addNodesFromTip(tip, site, treeNodes, owner);
		}
		
		
		//find and return root
		double maxHeight = 0;
		ARGNode rootNode = treeNodes.get(0);
		for(ARGNode node : treeNodes) {
			if (node.getHeight() > maxHeight) {
				maxHeight = node.getHeight();
				rootNode = node;
			}
		}
		if (rootNode.getParent(0)!=null) {
			throw new IllegalArgumentException("Bad ARG structure: deepest node does not have a null parent?");
		}

		//Root may have only one offspring.. advance it toward tips while this is true
		rootNode = moveRootTipward(rootNode);
		
		//Above procedure makes a tree, but it may have some nodes with only one descendant. We need to strip these out to
		//make sure we have a nice bifurcating tree with only CoalNodes 
		stripNodesWithOneOffspring(rootNode);
		
		
		return (CoalNode)rootNode;
	}
	
	/**
	 * Return, as a newick-formatted string, the marginal tree at the given site
	 */
	public static String getMarginalNewickTree(ARG arg, int site) {
		CoalNode root = TreeUtils.createMarginalTree(arg, site);
		String newick = TreeUtils.getNewick(root);
		return newick;
	}
	
	/**
	 * The marginal tree creation procedure may create a tree with a root that only has one offspring. This function
	 * finds the node that's the MRCA of everyone and makes that the root (by advancing the root tipward while it
	 * has only one offspring). 
	 * @param root
	 */
	private static ARGNode moveRootTipward(ARGNode root) {
		while( hasOneOffspring(root)) {
			root = getSingleOffspring(root);
			root.proposeParent(0, null);
		}
		return root;
	}
	
	
	/**
	 * Recursive function that modifies tree structure by removing interior nodes that have
	 * exactly one offspring, and connecting their children to the parents
	 * @param parent
	 */
	private static void stripNodesWithOneOffspring(ARGNode parent) {
		if (parent.getNumOffspring()==0)
			return;
		
		if ( hasOneOffspring(parent) ) {
			//this is an error, we must resolve these before we get here
		}
		
		ARGNode kid0 = parent.getOffspring(0);
		ARGNode kid1 = parent.getOffspring(1);
		
		if ( hasOneOffspring(kid0)) {
			ARGNode grandKid = getSingleOffspring(kid0);
			while (hasOneOffspring(grandKid)) {
				grandKid = getSingleOffspring(grandKid);
			}
			//System.out.println("Connecting parent " + parent + " to descendant " + grandKid);
			grandKid.proposeParent(0, parent);
			parent.proposeOffspring(0, grandKid);
		}
		
		if ( hasOneOffspring(kid1)) {
			ARGNode grandKid = getSingleOffspring(kid1);
			while (hasOneOffspring(grandKid)) {
				grandKid = getSingleOffspring(grandKid);
			}
			//System.out.println("Connecting parent " + parent + " to descendant " + grandKid);
			grandKid.proposeParent(0, parent);
			parent.proposeOffspring(1, grandKid);
		}
		
		
		if (parent.getNumOffspring()==2) {
			stripNodesWithOneOffspring( parent.getOffspring(0));
			stripNodesWithOneOffspring( parent.getOffspring(1));
		}
		
	}

	private static ARGNode getSingleOffspring(ARGNode node) {
		if (node instanceof RecombNode) 
			return node.getOffspring(0);
		if (node instanceof CoalNode) {
			if (node.getOffspring(0)==null)
				return node.getOffspring(1);
			else
				return node.getOffspring(0);
		}
		
		//we should never get here
		throw new IllegalArgumentException("Cannot find single offspring for node that is not a Recomb or Coal node");
	}
	
	
	private static boolean hasOneOffspring(ARGNode node) {
		if (node instanceof RecombNode)
			return true;
		if (node instanceof CoalNode) {
			int kids = 0;
			if (((CoalNode) node).getLeftOffspring()==null)
				kids++;
			if (((CoalNode) node).getRightOffspring()==null)
				kids++;
			return kids==1;
		}
		
		return false;
	}
	
	/**
	 * Trace rootward from the given tip, duplicating the ancestral structure of the nodes found and adding them to treeNodes,
	 * until we reach a node whose parent is already in the list of treeNodes, in which we just attach to that node and then
	 * return.  If we do this for every tip in the an ARG, always using getParentForSite, we end up with a marginal tree. 
	 * @param tip
	 * @param treeNodes
	 */
	private static void addNodesFromTip(TipNode tip, int site, List<ARGNode> treeNodes, ARG owner) {
		ARGNode kid = tip;
		ARGNode parent = kid.getParentForSite(site); //Tips only have one parent
		ARGNode treeKid = new TipNode(owner);
		
		treeKid.setLabel(kid.getLabel());
		treeKid.proposeHeight(kid.getHeight());
		
		ARGNode treeParent = findNodeByLabel(treeNodes, "" + parent.getNumber());
		
		while(treeParent == null && parent != null) {
			if (parent instanceof CoalNode)
				treeParent = new CoalNode(owner);
			else
				treeParent = new RecombNode(owner);
			
			treeParent.setLabel("" + parent.getNumber());
			treeParent.proposeHeight(parent.getHeight());
			treeKid.proposeParent(0, treeParent);
			treeParent.proposeOffspring(0, treeKid);
			//Clone annotations from parent
//			for(String key : parent.getAnnotationKeys()) {
//				treeParent.addAnnotation(key, parent.getAnnotation(key));
//			}
			
			kid = parent;
			parent = kid.getParentForSite(site);
			treeNodes.add(treeParent); // MUST come before we look for the new treeParent
			
			if (parent != null) {
				treeKid = treeParent;
				treeParent = findNodeByLabel(treeNodes, "" + parent.getNumber());
			}
		}
		
		if (parent != null) {
			treeParent.proposeOffspring(1, treeKid);
			treeKid.proposeParent(0, treeParent);
		}
		
	}

	/**
	 * Returns the node in the list whose label.equals(label), or null if none are found
	 * @param list
	 * @param label
	 * @return
	 */
	private static ARGNode findNodeByLabel(List<ARGNode> list, String label) {
		for(ARGNode node : list) {
			if (node.getLabel().equals(label)) {
				return node;
			}
		}
		return null;
	}

	
	/**
	 * Return a random node from the given list of nodes, where all nodes are selected with equal probability
	 * @param list
	 * @return
	 */
	private static ARGNode pickNode(List<ARGNode> list) {
		int which = RandomSource.getNextIntFromTo(0, list.size()-1);
		return list.get(which);
	}
	
	/**
	 * Pick a node from among the list with probability relative to the number of available/unassigned parents
	 * for each node. Typically, coalescent and tip nodes will have one available parent, but recomb nodes
	 * may have one or two, and hence may be picked with higher probability than a coalescent node. This whole
	 * procedure makes more sence if available parents are thought of as lineages, and we pick a random lineage
	 * 
	 * @param list
	 * @return
	 */
	private static ARGNode pickLineage(List<ARGNode> list) {
		int availableLineageCount = countLineages(list);
		
		int lineage = RandomSource.getNextIntFromTo(0, availableLineageCount-1);
		for(ARGNode node : list) {
			for(int i=0; i<node.getNumParents(); i++) {
				if (node.getParent(i)==null) {
					if (lineage == 0)
						return node;
					lineage--;
				}
			}
		}		
		throw new IllegalStateException("Failed to find an available lineage");
	}
	
	/**
	 * Pick a parent number to be the slot for a new parent. Always zero for tip and coal nodes, may be zero or
	 * one for recomb nodes. 
	 * @param node
	 * @return
	 */
	private static int pickParentNum(ARGNode node) {
		if (node.getNumParents()==1) {
			if (node.getParent(0)!=null)
				throw new IllegalStateException("Child does not have an available parent!");
			return 0;
		}
		else {
			if (node.getParent(0)==null) {
				return 0;
			}
			if (node.getParent(1)==null)
				return 1;
			throw new IllegalStateException("Recomb. dhild does not have an available parent!");
		}
	}
	
	/**
	 * Counts the number of unassigned parents among the the list of nodes. This is the number of lineages
	 * that may coalesce during tree simulation
	 * @param nodes
	 * @return
	 */
	private static int countLineages(List<ARGNode> nodes) {
		int lineages = 0;
		for(ARGNode node : nodes) {
			for(int i=0; i<node.getNumParents(); i++)
				if (node.getParent(i)==null)
					lineages++;
		}
		return lineages;
	}
	
	/**
	 * Discard all current topology and branch length data and generate a new random tree, given the 
	 * supplied value of theta (the pairwise coalescent rate will be 1/(2*theta)
	 * 
	 * This should probably be in some factory class that makes new trees
	 * 
	 * @return The root of the newly created tree
	 */
	public static CoalNode generateRandomTree(List<TipNode> tips, double theta, ARG owner) {
		double rate = 1.0/theta;
		Exponential expRng = new Exponential(rate, RandomSource.getEngine());
			
		int lineages = tips.size();
		double sumTime = 0;
		
		List<ARGNode> activeNodes = new ArrayList<ARGNode>();
		for(int i=0; i<tips.size(); i++) {
			activeNodes.add(tips.get(i));
		}

		
		while(lineages>1) {
			
			int pairs = lineages*(lineages-1)/2;
			double nextTime = expRng.nextDouble(pairs*rate); //Argument is the rate, not the mean (
			sumTime += nextTime; //This is the height of the new node 'parent'
			
			ARGNode nodeA = pickNode(activeNodes);
			ARGNode nodeB = pickNode(activeNodes);
			while (nodeB == nodeA) {
				nodeB = pickNode(activeNodes);
			}
			
			CoalNode parent = new CoalNode(owner);
			parent.proposeHeight(sumTime);
			parent.proposeOffspring(0, nodeA);
			parent.proposeOffspring(1, nodeB);
			nodeA.proposeParent(0, parent);
			nodeB.proposeParent(0, parent);
			
			
			activeNodes.remove(nodeA);
			activeNodes.remove(nodeB);
			activeNodes.add(parent);
			lineages = activeNodes.size();
		}
	
		return (CoalNode)activeNodes.get(0);
	}
	
	/**
	 * Construct a random ARG with the given number of tips, theta, and rho
	 * @param tipCount
	 * @param theta
	 * @param rho
	 * @param sites
	 * @return
	 */
	public static ARG generateRandomARG(int tipCount, double popSize, double rho, int sites) {
		ARG arg = new ARG();
		List<TipNode> tips = new ArrayList<TipNode>();
		for(int j=0; j<tipCount; j++) {
			TipNode tip = new TipNode(arg);
			tip.proposeHeight(0.0);
			tip.setLabel("tip" + j);
			tips.add(tip);
		}
		
		return generateRandomARG(tips, popSize, rho, sites, arg);
	}
	
	public static ARG generateRandomARG(List<TipNode> tips, double popSize, double rho, int sites) {
		ARG arg = new ARG();
		return generateRandomARG(tips, popSize, rho, sites, arg);
	}
	
	/**
	 * Create a random ARG from the given list of tips
	 * @param tips
	 * @param popSize The mean expected time until two lineages coalesce, this is also the inverse of the pairwise rate of coalescence 
	 * @param rho The rate at which a single lineage splits into two
	 * @param sites
	 * @return
	 * @throws IOException 
	 */
	public static ARG generateRandomARG(List<TipNode> tips, double popSize, double rho, int sites, ARG owner) {
		List<ARGNode> allNodes = new ArrayList<ARGNode>();
		Exponential expRng = new Exponential(1.0, RandomSource.getEngine());
		
		//ActiveNodes is a list of nodes that do not have all parents assigned, and hence can
		//be picked to be the children of newly created nodes. 
		List<ARGNode> activeNodes = new ArrayList<ARGNode>();
		for(int i=0; i<tips.size(); i++) {
			activeNodes.add(tips.get(i));
			allNodes.add(tips.get(i));
		}
		
		double sumTime = 0;
		
		int coals = 0;
		int recs = 0;
		
		while(activeNodes.size()>1) {
			int lineages = countLineages(activeNodes);
			double pairs = lineages*(lineages-1)/2.0;
			double coalRate = pairs/popSize;
			double recRate = (double)lineages * rho/(popSize);
			double rateSum = coalRate + recRate;
			double nextTime = expRng.nextDouble(rateSum); //Waiting time until next event
			
			double coalProb = coalRate / (coalRate + recRate); //Probability next event is a coalescent
			sumTime += nextTime; //This is the height of the new node 'parent'
			
			if (RandomSource.getNextUniform() < coalProb) { //Event is a coalescent event
				CoalNode parent = new CoalNode(owner);
				parent.proposeHeight(sumTime);
				
				ARGNode nodeA = pickLineage(activeNodes);
				int parA = pickParentNum(nodeA);
				parent.proposeOffspring(0, nodeA);
				nodeA.proposeParent(parA, parent);
				
				//Remove the child if it no longer has any active lineages
				if (nodeA.getNumParents()==1 || parA==1)
					activeNodes.remove(nodeA);
								
				ARGNode nodeB = pickLineage(activeNodes); //Order important here, activeNodes was modified in above operation
				int parB = pickParentNum(nodeB);
				parent.proposeOffspring(1, nodeB);
				nodeB.proposeParent(parB, parent);
				
				if (nodeB.getNumParents()==1 || parB==1)
					activeNodes.remove(nodeB);

				activeNodes.add(parent);
				allNodes.add(parent);
				coals++;
			}
			else { //Event is a recombination event
				RecombNode parent = new RecombNode(owner);
				parent.proposeHeight(sumTime);
				
				ARGNode recChild = pickLineage(activeNodes);
				int par = pickParentNum(recChild);
				
				recChild.proposeParent(par, parent);
				parent.proposeOffspring(0, recChild);
				
				if (recChild.getNumParents()==1 || par==1)
					activeNodes.remove(recChild);
				
				parent.proposeRecombRange(new BiRange(0, RandomSource.getNextIntFromTo(1, sites-1), sites));
				activeNodes.add(parent);
				allNodes.add(parent);
				recs++;
			}

		}
		

		
		if (coals != (tips.size()-1)+recs) {
			System.err.println("Incorrect number of coalescences vs. recombinations:");
			System.err.println(" Tips : " + tips);
			System.err.println(" Coals: " + coals);
			System.err.println(" Recs : " + recs);
			System.exit(0);
			
		}
		
		owner.initializeFromNodeList(allNodes, sites);
		return owner;
	}

}
