package gui.figure.treeFigure;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Base class of all multifurcating trees that are used for drawing purposes. Trees are composed of Nodes, where each Node has one parent and zero
 * or more offspring. 
 * @author brendano
 *
 */
public class Tree {

	static int count = 0;
	Node root = null;
	String originalNewick;
	boolean hasBranchLengths = false;
	
	public Tree() {
		count++;
		originalNewick = "";
	}
	
	public Tree(Node root) {
		count++;
		this.root = root;
		originalNewick = "";
	}

	public Tree(String treeStr) {
		count++;
		buildTreeFromNewick(treeStr);
		originalNewick = treeStr;
	}

	public String getOriginalNewick() {
		return originalNewick;
	}
	
	
	/**
	 * 
	 * @return
	 */
	public boolean restoreFromOriginalNewick() {
		root = null;
		if (originalNewick != "") {
			buildTreeFromNewick(originalNewick);
			return true;
		}
		else
			return false;
	}

	
	public int getTotalNodes() {
		if (root==null)
			return 0;
		else 
			return subTreeSize(root);
	}
	
	
	/**
	 * Returns true if the tips descending from node n have exactly the same
	 * labels as the list of nodes passed in
	 * @param n Node of this tree to compare tips to
	 * @param clade list of nodes to compare
	 * @return true if n's tips have the same labels as those found in clade
	 */
	public boolean containsSubclade(Node n, ArrayList<Node> clade) {
		ArrayList<Node> tips = getTips(n);
		if (tips.size() != clade.size() ) 
			return false;
		else {
			int sameCount = 0;
			for(Node tip : tips) {
				boolean found = false;
				for(Node compTip : clade) {
					if (compTip.getLabel().equals(tip.getLabel())) {
						found = true;
						break;
					}
				}
				if (found)
					sameCount++;
			}
			return sameCount==tips.size();
		}
	}
	
	
	
	/**
	 * Collapses branch lengths of zero length into polytomies, may be required for accurate tree drawing
	 */
	public void collapseZeroBranchLengths() {
		ArrayList<Node> kids = root.getOffspring();
		for(int i=0; i<kids.size(); i++)
			collapseZeros( kids.get(i));
	}
	
	/**
	 * Removes node n from the tree, and adds n's offspring to n's parent. This method is overridden in DrawableTree
	 * to provide support for selected nodes (nodes must be unselected before removal)
	 * @param n Node to be removed
	 */
	public void removeNode(Node n) {
		Node p = n.getParent();
		ArrayList<Node> kids = n.getOffspring();
		if (n.getParent()==null && n!=root) {
			System.err.println("Uh oh, this node is not the root, but is has a null parent. Aborting!");
			return;
		}
		if (p==null) {
			//System.err.println("Remove the root node?");
			root = null;
			return;
		}
		
		boolean ok = p.removeOffspring(n);
		if (!ok) {
			System.out.println("Failed to remove node " + n.getLabel() + " from parent " + p.getLabel());
//			ArrayList<Node> pKids = p.getOffspring();
//			System.out.println("Siblings of node " + n.getLabel() + ", descendants of parent: " + p.getLabel());
//			for(Node kid : pKids) {
//				System.out.println("Kid : " + kid.getLabel());
//			}
			
		}
		for(Node kid : kids) {
			kid.setParent(p);
			kid.setDistToParent(n.getDistToParent()+kid.getDistToParent());
			p.addOffspring(kid);
		}
		
		n.setParent(null);
		n.clearOffspring();
	}
	
	/**
	 * Removes node n and all of it's descendants from the tree. This method is overridden in DrawableTree
	 * to provide support for selected nodes (nodes must be unselected before removal)
	 * @param n Top of clade to be removed
	 */	
	public void removeClade(Node n) {
		Node p = n.getParent();
		if (p==null) {
			root = null;
		}
		else {
			p.removeOffspring(n);
			n.setParent(null);
			n.setDistToParent(-1);
			if (p.getParent()!=null && p.numOffspring()==1) {
				p.getOffspring(0).setParent( p.getParent() );
				p.getParent().addOffspring(p.getOffspring(0));
				p.getOffspring(0).setDistToParent(p.getDistToParent()+p.getOffspring(0).getDistToParent());	
				p.getParent().removeOffspring(p);
				p.setParent(null);
			}

		}
	}
	
	public boolean hasZeroBranchLengths() {
		boolean hasZeros = false;
		for(Node kid : root.getOffspring())
			hasZeros = hasZeros || hasZeroBranchLengths(kid);
		return hasZeros;
	}
	
	public boolean hasZeroBranchLengths(Node n) {
		if (n.getDistToParent()==0) {
			//.out.println("Node : " + n.getLabel() + " has zero dist to parent");
			return true;
		}
		
		boolean hasZeros = false;
		for(int i=0; i<n.numOffspring() && (!hasZeros); i++) 
			hasZeros = hasZeros || hasZeroBranchLengths( n.getOffspring(i));
		
		return hasZeros;
	}
	
	protected void collapseZeros(Node n) {
		ArrayList<Node> kids = new ArrayList<Node>();
		for(Node kid : n.getOffspring())
			kids.add(kid);
		boolean movedANode = false;
		for(int i=0; i<kids.size(); i++) {

			if (kids.get(i).getDistToParent()==0) {
				if (n.getParent()==null) {
					System.err.println("Uh-oh, null parent in collapseZeros...");
				}
				else {
					Node kid = kids.get(i);
					//System.out.println("Tree before rearranging single node " + kid.getLabel() + " : " + getNewick());
					boolean removed = n.removeOffspring( kid );
					if (removed) {
						n.getParent().addOffspring(kid);
						kid.setParent(n.getParent());
						kid.setDistToParent(n.getDistToParent());
						//System.out.println("Tree after rearranging node : " + kid.getLabel() + " : " + getNewick());
						movedANode = true;
					}
					else {
						System.out.println("Kid is not an offspring of parental node, uh-oh CollapseZeros may not work.");
					}
				}
			}
		}
		
		if (n.numOffspring()==1 && movedANode) {
			n.getOffspring(0).setParent( n.getParent() );
			n.getParent().addOffspring(n.getOffspring(0));
			n.getOffspring(0).setDistToParent(n.getDistToParent()+n.getOffspring(0).getDistToParent());
			n.getParent().removeOffspring(n);
		}
		
		
		for(int i=0; i<kids.size(); i++)
			collapseZeros(kids.get(i));
	}
	

		
	public int getNumLeaves() {
		if (root==null)
			return 0;
		else 
			return getNumTips(root);		
	}
	
	public String getNewick() {
		StringBuffer str = new StringBuffer("(");
		int i;
		for(i=0; i<(root.numOffspring()-1); i++)
			str.append( getNewickSubtree(root.getOffspring(i)) + ", ");
		str.append( getNewickSubtree(root.getOffspring(i)) );
		
		str.append(");");
		return str.toString();
	}
	
	public void printTree() {
		if (root==null) {
			System.out.println("Tree has no root.");
		}
		else {
			printDescendents(root, 0);
		}
	}
	
	public void addNodeBeforeParent(Node n) {
		if (n.getParent()==null)
			return;
		
		double half = n.getDistToParent()/2.0;
		Node k = new Node();
		k.setParent(n.getParent());
		k.setDistToParent(half);
		n.getParent().addOffspring(k);
		n.setParent(k);
		k.addOffspring(n);
		n.setDistToParent(half);
	}
	
	/*
	 * This quickie converts n's parent to an offspring, but doesn't assign the moved node's parent to be n
	 * Therefore this function leaves the tree in an inconsistent state! So use carefully.
	 */
	protected void convertParentToOffspring(Node n) {
		Node p = n.getParent();
		n.setParent(null);
		n.addOffspring(p);
		p.setDistToParent(n.getDistToParent());
		n.setDistToParent(Double.NaN);
	}
	
	/**
	 * Reroots the tree using node n as the new root
	 * @param nnewRoot The new tree root (must be in tree)
	 */
	public void reroot(Node newroot) {
		if (newroot == null || newroot == root)
			return;
		
		Node oldParent = newroot.getParent();
		Node trail = newroot;
		newroot.addOffspring(oldParent);
		newroot.setParent(null);
		newroot.setDistToParent(Double.NaN);
		
		//Old parent is now an offspring of newroot, but oldparent's parent is unchanged,
		//So we must track oldParent's original parent, the set oldparent new parent to be newroot
		Node p = oldParent.getParent();
		
		while(p!= null && oldParent!=null) {
			oldParent.addOffspring(p);
			oldParent.setParent(trail);
			oldParent.setDistToParent(trail.getDistToParent());
			
			trail = oldParent;
			oldParent = p;
			p = p.getParent();
		}
		
		root = newroot;
		root.setParent(null);
		root.setDistToParent(Double.NaN);
	}
	
	
	public Node getRoot() {
		return root;
	}
	
	/**
	 * Obtain all nodes in this tree that have zero offspring. 
	 * @return
	 */
	public List<Node> getAllTips() {
		if (root==null)
			return new ArrayList<Node>();
		else {
			ArrayList<Node> tips = new ArrayList<Node>();
			int i;
			for(i=0; i<root.numOffspring(); i++)
				tips.addAll( getTips(root.getOffspring(i)));
				
			return tips;
		}
			
	}
	
	public ArrayList<Node> getAllNodes() {
		ArrayList<Node> Nodes = getNonTips(root);
		Nodes.addAll( getTips(root) );
		return Nodes;
	}
	
	public ArrayList<Node> getInternalNodes() {
		ArrayList<Node> iNodes = getNonTips(root);
		return iNodes;
	}
	
	/**
	 * Traverse all tips in the tree and return the greatest distance to root found among them. 
	 */
	public double getMaxHeight() {
		List<Node> tips = this.getAllTips();
		double maxHeight = 0;
		int i;
		for(i=0; i<tips.size(); i++) {
			double dist = getDistToRoot( tips.get(i) ); 
			if ( dist  > maxHeight )
				maxHeight = dist;
		}
		
		return maxHeight;
	}
	
	
	/**
	 * Not the true 'diameter' in the graph theoretical sense, but the maximum distance between two leaves, conditional
	 * on the path going through the root. 
	 */
	public double getMaxDiameter() {
		ArrayList<Double> heights = getDescendentDepthList(root);
		
		Collections.sort(heights);
		if (heights.size()>1) {
			return heights.get(heights.size()-1)+heights.get( heights.size()-2);
		}
		
		if (heights.size()==1)
			return heights.get(0);
		
		return 0;
	}

	//Returns the maximum 'time back from present' (i.e. distance to current time, or tips)
	//amongst all the leaves descendent of this Node
	public static double getMaxDescendentHeight(Node n, Tree tree) {
		double totalDepth = getMaxDescendentDepth( tree.getRoot() );
		double minDepth = getMinDescendentDepth(n);
		if (totalDepth-minDepth < 0) {
			System.err.println("Uh-oh, total tree depth is : " + totalDepth + " but min. depth from this Node is : " + minDepth);
		}
		
		return totalDepth - minDepth;
	}
	
	//t is measured in units of height, that is, starting at tips and counting toward root
	public int lineageCountAtTime(double t) {
		double totalDepth = getMaxDescendentDepth( root );
		double tDepth = totalDepth - t;
		//time asked for is greater than TMRCA
		if (tDepth < 0) 
			return 1;
		else {
			return lineageCountSubtree(root, tDepth);
		}
	}
	
	
	//Returns the minimum distance to 'current' time amongst all the leaves
	//descendent of this Node
	public static double getMinDescendentHeight(Node n, Tree tree) {
		double totalDepth = getMaxDescendentDepth( tree.getRoot() );
		double maxDepth = getMaxDescendentDepth(n);
		if (totalDepth-maxDepth < 0) {
			System.err.println("Uh-oh, total tree depth is : " + totalDepth + " but max. depth from this Node is : " + maxDepth);
		}
		
		return totalDepth - maxDepth;
	}
	
	
	protected void buildTreeFromNewick(String treeStr) {
		root = new Node();
		buildSubtreeFromNewick(root, treeStr);
	}
	
	protected static void buildSubtreeFromNewick(Node root, String treestr) {
		if (treestr==null || treestr.equals("") || !hasKids(treestr)) {
			return;
		}
		ArrayList<String> kidStrs = getOffspringStrings(0, treestr);
		//System.out.println("Found " + kidStrs.size() + " child strings : " );
		int count = 0;
		for(String kidStr : kidStrs) {
			count++;
			//System.out.println(" kidstr #" + count + " : " + kidStr + " label : '" +  subTreeLabel(kidStr)  + "' dist from parent: " + subTreeDistFromParent(kidStr));
			Node kid = new Node();
			kid.setParent(root);
			root.addOffspring(kid);
			double dist = subTreeDistFromParent(kidStr);
			kid.setDistToParent( dist );
			kid.setLabel( subTreeLabel(kidStr));
			buildSubtreeFromNewick(kid, kidStr);
		}
	}
	
	protected static boolean hasKids(String subtree) {
		if (subtree.indexOf(",")<0)
			return false;
		else
			return true;
	}
	
	protected static ArrayList<String> getOffspringStrings(int startIndex, String treeStr) {
		ArrayList<String> children = new ArrayList<String>();
		treeStr = treeStr.trim();
		int lastParen = matchingParenIndex(startIndex, treeStr);
		
		//Scan along subtree string, create new strings separated by commas *at 'highest level'*
		//not all commas, of course
		int i=startIndex+1;
		StringBuffer cstr = new StringBuffer();
		
		int count = 0;
		boolean reading = true;
		for(i=startIndex+1; i<lastParen; i++) {
			char c = treeStr.charAt(i);
			if (c=='[')
				reading = false;
			if (c==']')
				reading = true;
			if (c=='(')
				count++;
			if (c==')')
				count--;
			if (reading && c==',' && count==0) {
				children.add(cstr.toString());
				cstr = new StringBuffer();
			}
			else	
				cstr.append( treeStr.charAt(i));
			
		}
		
		children.add(cstr.toString());
		
		return children;
	}
	
	//Returns the entire subtree string, don't try it on the whole tree string thoughh (index==0 will error) 
	private static String extractSubtreeString(int startIndex, String treeStr) {	
		int lastParen = matchingParenIndex(startIndex, treeStr);
		int lastPos = Math.min( treeStr.indexOf(",", lastParen),  treeStr.indexOf(")", lastParen+1) );
		System.out.println("Last paren index : " + lastParen);
		System.out.println("First , or ) after lastParen : " + lastPos);
		String subtree = treeStr.substring(startIndex, lastPos);
		return subtree;
	}
	
	//Returns the distance of the subtree from a parent (just the number following the final colon) 
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
	
	protected static int matchingParenIndex(int firstPos, String str) {
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
	
	public boolean hasBranchLengths() {
		ArrayList<Node> nodes = getAllNodes();
		boolean hasEm = true;
		for(Node n : nodes) {
			if (n!=root)
				hasEm = hasEm && n.getDistToParent()>=0 && (! Double.isNaN(n.getDistToParent()));
				if (! hasEm) {	//This function gets called a lot, so it's probably best if we shortcut
					return false;
				}
		}
//		if (hasEm)
//			System.out.println("Tree has branch lengths");
//		else
//			System.out.println("Tree does NOT have branch lengths");
		return hasEm;
	}
	
	
	/**
	 * Returns true if every node has either zero or two children
	 * @return
	 */
	public boolean isBinary() {
		return isBinary(root);
	}
	
	/**
	 * Returns true only if the subtree starting here is binary
	 * @param n
	 * @return
	 */
	protected boolean isBinary(Node n) {
		if (n.isLeaf())
			return true;
		if (n.numOffspring()==2) {
			boolean b = true;
			for(Node k : n.getOffspring())
				 if (! isBinary(k))
					 b = false;
			return b;
		}
		
		//If we're here the number of offspring is not zero or two
		return false;
	}
	
	public double getHeight() {
		double max = 0;
		List<Node> tips = getAllTips();
		for(Node n : tips) {
			if (getDistToRoot(n)>max)
				max = getDistToRoot(n);
		}
		return max;
	}
	
	//Returns distance from Node to farthest out tip (=totaldepth - NodeDepth)
	public static double getNodeHeight(Node n, Tree tree) {
		double totalDepth = getMaxDescendentDepth( tree.getRoot() );
		double NodeDepth = getDistToRoot(n);
		return totalDepth - NodeDepth;
	}
	
	//Returns depth of leaf that is 'farthest' (toward tips) fom n
	public static double getMaxDescendentDepth(Node n) {
		ArrayList<Double> depths = getDescendentDepthList(n);
		int i;
		double maxDepth = 0;
		//System.out.println("Found depths : ");
		for(i=0; i<depths.size(); i++) {
			//System.out.println( depths.get(i));
			if (depths.get(i) > maxDepth)
				maxDepth = depths.get(i);
		}
		//System.out.println(" Returning max dist from root : " + maxDepth);
		return maxDepth;
	}
	
	
	//Returns the depth of the leaf Node that is 'closest' (toward root) to n	
	public static double getMinDescendentDepth(Node n) {
		ArrayList<Double> depths = getDescendentDepthList(n);
		int i;
		double minDepth = Double.MAX_VALUE;
		//System.out.println("Found depths : ");
		for(i=0; i<depths.size(); i++) {
			//System.out.println( depths.get(i));
			if (depths.get(i) < minDepth)
				minDepth = depths.get(i);
		}
		//System.out.println(" Returning min dist from root : " + minDepth);
		return minDepth;
	}
	
	
	/**
	 * Returns the maximum node depth (number of nodes from root to tip) of the tree
	 */
	public int maxNodeDepth() {
		int maxDepth = 0;
		for(Node n : getAllTips()) {
			int depth = getNodeDepth(n);
			if (depth > maxDepth)
				maxDepth = depth;
		}
		return maxDepth;
	}
	
	public int getNodeDepth(Node n) {
		return getNodeDepth(n, 0);
	}
	
	public static int getNodeDepth(Node n, int depth) {
		if (n.getParent()==null)
			return depth;
		else
			return getNodeDepth(n.getParent(), depth+1);
	}
	
	public static double getDistToRoot(Node n) {
		double dist = 0;
		Node p = n;
		//System.out.println("\nNew calc starting with tip: " + n.getLabel());
		while( p.getParent() != null) {
			dist += p.getDistToParent();
			//System.out.println("Dist from " + p.getLabel() + " to parent is : " + p.getDistToParent() + " sum dist : " + dist);
			p = p.getParent();
		}
		return dist;
	}
	
	/**
	 * The number of tips descending from node n
	 * @param n Node at top of clade to count
	 * @return Number of descendants
	 */
	public static int getNumTips(Node n) {
		if (n.isLeaf()) 
			return 1;
		else {
			int sum = 0;
			for(Node kid : n.getOffspring())
				sum += getNumTips(kid);
			return sum;
		}
				
	}

	private static ArrayList getNonTips(Node n) {
		ArrayList<Node> nonTips = new ArrayList<Node>();
		int i;
		if (n.numOffspring()>0) {
			nonTips.add(n);

			for(i=0; i<n.numOffspring(); i++) 
				nonTips.addAll( getNonTips(n.getOffspring(i)));
		}
		
		return nonTips;
	}
	
	
	private static int lineageCountSubtree(Node n, double tDepth) {
		double thisDepth = getDistToRoot(n);
		double parentDepth = getDistToRoot( n.getParent() );
		if (thisDepth >= tDepth && parentDepth <= tDepth)
			return 1;
		
		if (thisDepth < tDepth) {
			int count = 0;
			int i;
			for(i=0; i<n.numOffspring(); i++)
				count += lineageCountSubtree(n.getOffspring(i), tDepth);
			return count;
		}
		else {
			return 0;
		}
	}
	
	protected static ArrayList<Node> getTips(Node n) {
		ArrayList<Node> tips = new ArrayList<Node>();
		int i;
		if (n.numOffspring()==0) {
			tips.add(n);
		}
		else {
			for(i=0; i<n.numOffspring(); i++) {
				tips.addAll( getTips(n.getOffspring(i)));
			}
		}
		return tips;
	}
	
	
	private static String getNewickSubtree(Node n) {
		if (n.numOffspring()==0) {
			if (n.hasLabel())
				return new String(n.getLabel() + ":" + n.getDistToParent() );
			else
				return new String("ind?:" + n.getDistToParent() );
		}
		
		if (n.numOffspring()==1) {
			System.err.println("Huh? This node has only one child! Newick representation may not be correct. Also, tree may be screwed up.");
			return getNewickSubtree(n.getOffspring(0));
		}
		else {
			StringBuffer str = new StringBuffer("(");
			int i;
			for(i=0; i<(n.numOffspring()-1); i++)
				str.append( getNewickSubtree(n.getOffspring(i)) + ", ");
			str.append( getNewickSubtree(n.getOffspring(i)));
			str.append(")" + n.getLabel() + ":" + n.getDistToParent());
			return str.toString();
		}
		
	}	
	
	
	public int getMaxDescendentNodeDepth(Node n) {
		ArrayList<Node> tips = getDescendents(n);
		int max = 0;
		for(Node tip : tips) {
			int depth = getNodeDepth(tip);
			if (depth > max)
				max = depth;
		}
		return max;
	}
	
	public ArrayList<Node> getDescendents(Node n) {
		ArrayList<Node> ds = new ArrayList<Node>();
		if (n.numOffspring()==0)
			ds.add(n);
		else {
			for(Node kid : n.getOffspring())
				ds.addAll( getDescendents(kid));
		}
		return ds;
	}
	
	//Returns a list of the distances to the root of all the tips that descend 
	//from this Node
	public static ArrayList<Double> getDescendentDepthList(Node n) {
		ArrayList<Double> depths = new ArrayList<Double>();
		if (n.numOffspring()==0) {
			depths.add( getDistToRoot(n) );
		}
		else {
			int i;
			for(i=0; i<n.numOffspring(); i++) 
				depths.addAll( getDescendentDepthList(n.getOffspring(i)) );
		}
		return depths;
	}
	
	
	private static void printDescendents(Node n, int padding) {
		int i;
		for(i=0; i<padding; i++)
			System.out.print("  ");
		if (n.hasLabel())
			System.out.println("Node id : " + n.getLabel() + " children : " + n.numOffspring() );
		else
			System.out.println("Node id : (no label)   children : " + n.numOffspring() );
		for(i=0; i<n.numOffspring(); i++ ) {
			printDescendents(n.getOffspring(i), padding+2);
		}
		
	}

			
	private static int subTreeSize(Node n) {
		int count = 1;
		int i;
		for(i=0; i<n.numOffspring(); i++)
			count += subTreeSize(n.getOffspring(i));
		
		return count;
	}
	
	
}

