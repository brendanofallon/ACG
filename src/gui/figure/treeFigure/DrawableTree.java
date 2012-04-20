package gui.figure.treeFigure;

import gui.ErrorWindow;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.regex.Pattern;


/**
 * Extends the tree idea to include one whose nodes know their positions, and supplies a few (or many)
 * methods to change/calculate the node positions. Also implements the idea of a selection of nodes,
 * and maintains a list of which nodes are selected. The class is abstract since different types of trees
 * may choose to position their nodes differently (by calling calculateNodePositions)
 * 
 * @author brendan
 *
 */
public abstract class DrawableTree extends Tree {

	//Node annotations begin with the following string
	static String annotationIndicator = "[&"; 
	
	public enum Direction {LEFT, RIGHT, UP, DOWN}; //Possible tree orientations
	
	Direction orientation; //Which way the tips point
	
	static final int NODE_SELECTION = 0;
	static final int CLADE_SELECTION = 1;
	static final int DESCENDANT_SELECTION = 2; 
	static final int UNSELECT_SELECTION = 3;
	
	int scaleType = 1;
	public static final int NO_SCALE_BAR = 0;
	public static final int SCALE_BAR = 1;
	public static final int SCALE_AXIS = 2;
	
	boolean hasCalculatedNodePositions = false;
	boolean ignoreBranchLengths = false;
	
	int selectionMode = NODE_SELECTION;
	final int myCount = count;
	
	boolean hasCalculatedHeight = false;
	double height = 1;
	
	ArrayList<DrawableNode> selectedNodes = new ArrayList<DrawableNode>();
	Stack<DrawableNode> nodeStack = new Stack<DrawableNode>();
	
	//A list of all parties interested in tree changing events. 
	List<TreeListener> treeListeners = new ArrayList<TreeListener>(); 
	
	public DrawableTree() {
		root=null;
	}
	
	public DrawableTree(DrawableNode root) {
		super(root);
		initializeAnnotations();
	}
	
	public DrawableTree(String treeStr) {
		buildTreeFromNewick(treeStr);
		initializeAnnotations();
	}
	
	protected void initializeAnnotations() {
		assignAnnotations((DrawableNode)root);
		double factor = getHeight();
		initializeErrorBars((DrawableNode)root, factor);
	}
	
	/**
	 * Calculates the appropriate lengths for the error bars (right now, only trees built using
	 * the majority tree builder have error bars). 
	 * @param node
	 * @param factor
	 */
	private void initializeErrorBars(DrawableNode node, double factor) {
		if (node.getAnnotationValue("error")!=null) {
			node.setHasErrorBars(true);
			double length = Double.parseDouble(node.getAnnotationValue("error"));
			node.setErrorBarLength(length/factor);
		}
		
		for(Node kid : node.getOffspring()) {
			initializeErrorBars((DrawableNode)kid, factor);
		}
	}

	/**
	 * Computes the height of the tree. This doesn't fully recompute every time - we store the result
	 * in a field and update only when necessary. 
	 */
	public double getHeight() {
		if (! hasCalculatedHeight) {
			height = super.getHeight();
			hasCalculatedHeight = true;
		}
		return height;
	}
	
	public int getScaleType() {
		return scaleType;
	}
	
	public void setScaleType(int type) {
		if (type>2) {
			throw new IllegalArgumentException("Can't set tree scaling type to type #: " + type);
		}
		else 
			scaleType = type;
	}

	
	/**
	 * Add a new tree listener to the list of listeners
	 * @param tl
	 */
	public void addTreeListener(TreeListener tl) {
		treeListeners.add(tl);
	}
	
	/**
	 * Remove this tree listener from the list of listeners.
	 * @param tl
	 */
	public void removeTreeListener(TreeListener tl) {
		treeListeners.remove(tl);
	}
	
	/**
	 * Notify all tree listeners that this tree has changed
	 * @param type
	 */
	public void fireTreeChangedEvent(TreeListener.ChangeType type) {
		for(TreeListener tl : treeListeners) {
			tl.treeChanged(type);
		}
	}
	
	/**
	 * Orientation flag doesn't actually change node positions, it just informs the TreeDrawer about
	 * how to draw this tree.
	 * @return
	 */
	public Direction getOrientation() {
		return orientation;
	}

	public void setOrientation(Direction orientation) {
		this.orientation = orientation;
		calculateNodePositions();
		fireTreeChangedEvent(TreeListener.ChangeType.ORIENTATION);
	}
	
	public void flipHorizontally() {
		if (orientation == Direction.LEFT)
			setOrientation(Direction.RIGHT);
		
		if (orientation == Direction.RIGHT)
			setOrientation(Direction.LEFT);
		
	}

	/**
	 * Returns a new copy of this tree with all properties *except listeners* preserved
	 */
	public abstract DrawableTree clone(); 
	
	
	/**
	 * Assigns the 'current label' value to a reasonable default for all nodes. The 
	 * current scheme is to set the labels of tips to be whatever getLabel() returns,
	 * or the annotation value from key "tip", if getLabel() is null, and "" for all
	 * other nodes
	 */
	protected void assignAnnotations(DrawableNode node) {
		if (node.numOffspring()==0) {
			String label = node.getLabel();
			if (label==null || label=="") {
				label = node.getAnnotationValue("tip");
			}
			if (label == null) 
				node.setCurrentLabel(node.getLabel());
			else
				node.setCurrentLabel(label);
		}
		else {
			node.setCurrentLabel("");
		}
	
		for(Node kid : node.getOffspring()) {
			assignAnnotations((DrawableNode)kid);
		}		
	}
	
	public int getCount() {
		return myCount;
	}
	
	public void setRoot(DrawableNode newRoot) {
		root = newRoot;
		hasCalculatedNodePositions = false;
		hasCalculatedHeight = false;
		fireTreeChangedEvent(TreeListener.ChangeType.NODES_REMOVED);
	}
	
	public abstract void calculateNodePositions();
	
	public abstract void rotateNode(DrawableNode n);
	
	public void setSelectionMode(int mode) {
		selectionMode = mode;
	}
	
	/**
	 * Returns all annotations keys (but not values) available
	 * among the nodes of the tree.
	 * @return
	 */
	public ArrayList<String> collectAnnotationKeys() {
		 ArrayList<String> allAnnotationKeys = new ArrayList<String>();
		 addAllAnnotations(allAnnotationKeys, (DrawableNode)root);
		 return allAnnotationKeys;
	}
	
	/**
	 * Helper for retrieving annotation keys
	 * @param keyList
	 * @param node
	 */
	protected void addAllAnnotations(ArrayList<String> keyList, DrawableNode node) {
		Set keys = node.getAnnotations().keySet();
		Iterator kit = keys.iterator();
		while(kit.hasNext()) {
			String key = (String)kit.next();
			if (! keyList.contains(key)) {
				keyList.add(key);
			}
		}
		
		for(Node kid : node.getOffspring()) {
			addAllAnnotations(keyList, (DrawableNode)kid);
		}
		
	}
	
	
	public ArrayList<DrawableNode> getAllDrawableNodes() {
		ArrayList<DrawableNode> allNodes = new ArrayList<DrawableNode>();
		
		Stack<Node> stack = new Stack<Node>();
		stack.push(root);
		while(stack.size()>0) {
			DrawableNode n = (DrawableNode)stack.pop(); 
			allNodes.add( n );
			stack.addAll( n.getOffspring() );
		}
		return allNodes;
	}
	
	public ArrayList<DrawableNode> getAllInternalDrawableNodes() {
		ArrayList<DrawableNode> allNodes = new ArrayList<DrawableNode>();
		
		Stack<Node> stack = new Stack<Node>();
		stack.push(root);
		while(stack.size()>0) {
			DrawableNode n = (DrawableNode)stack.pop(); 
			if (n.numOffspring()>0)
				allNodes.add( n );
			stack.addAll( n.getOffspring() );
		}
		return allNodes;
	}
	
	
	
	public ArrayList<DrawableNode> getSelectedNodes() {
		return selectedNodes;
	}
	
	public void setSelectedNodeColor(Color c) {
		for(DrawableNode n : selectedNodes)
			n.setBranchColor(c);
	}
	
	public void setSelectedNodeLineWidth(float width) {
		for(DrawableNode n : selectedNodes)
			n.setLineWidth(width);
	}
	
	public boolean hasSelectedNodes() {
		return selectedNodes.size()>0;
	}
	
	/**
	 * Select all nodes contained in the given rectangle
	 * @param selectionRect
	 * @param xFactor
	 * @param yFactor
	 * @return
	 */
	public boolean setSelectedNodes(Rectangle2D selectionRect) {
		if (selectionRect==null) {
			unselectAllNodes();
			return false;
		}

		return setSelectedNodes((DrawableNode)root, selectionRect);
	}
	
	public void unselectAllNodes() {
		for(DrawableNode n : selectedNodes) {
			n.setSelected(false);
		}
		selectedNodes.clear();
	}

	public double nodeDistance(DrawableNode a, DrawableNode b) {
		return Math.sqrt( (a.getX()-b.getX())*(a.getX()-b.getX()) + (a.getY() - b.getY())*(a.getY() - b.getY()));
	}
	
	public double nodeDistance(DrawableNode n, Point2D pos) {
		return nodeDistance(n, pos.getX(), pos.getY());
	}

	public double pointDistance(Point2D a, Point2D b) {
		return Math.sqrt( (a.getX()-b.getX())*(a.getX()-b.getX()) + (a.getY()-b.getY())*(a.getY()-b.getY()));
	}
	
	public double nodeDistance(DrawableNode n, double x, double y) {
		return Math.sqrt( (n.getX()-x)*(n.getX()-x) + (n.getY()-y)*(n.getY()-y));
	}
	

	/**
	 * Could be made iterative for faster execution.. or all nodes could be stored in a list and we could
	 * just traverse the list once
	 * @param n The node to determine the selection status of
	 * @param selRect  
	 * @return If any nodes are selected
	 */
	protected boolean setSelectedNodes(DrawableNode rootNode, Rectangle2D selRect) {
		boolean somethinIsSelected = false;
		boolean contains = false;
		double x;
		nodeStack.clear();
		nodeStack.push((DrawableNode)root);
		
		while(nodeStack.size()>0) {
			DrawableNode n = nodeStack.pop();
			
			x = n.getX();
		
			contains = selRect.contains(x, n.getY());
			
			if (contains) {
				if (selectionMode == UNSELECT_SELECTION) {
					if (n.isSelected()) {
						n.setSelected(false);
						selectedNodes.remove(n);
					}
				}
				
				if (selectionMode == NODE_SELECTION) {
					if (! n.isSelected()) {
						n.setSelected(true);
						somethinIsSelected = true;
						selectedNodes.add(n);
					}
					else {
						somethinIsSelected = true;
					}
				}
				
				if (selectionMode == CLADE_SELECTION) {
					selectClade(n);
					somethinIsSelected = true;
					continue; //Don't need to traverse further into tree
				}
				
				if (selectionMode == DESCENDANT_SELECTION)  {
					selectAllTips(n);
					somethinIsSelected = true;
					continue; //Don't need to traverse further into tree
				}
			}
			else {
				if (n.isSelected()) {
					n.setSelected(false);
					selectedNodes.remove(n);
				}
			}
			
			for(Node kid : n.getOffspring()) {
				nodeStack.push((DrawableNode)kid);
			}

			
		}
			
		return somethinIsSelected;
	}	

	/**
	 * Unselects all nodes descending from the given node
	 * @param n
	 */
	protected void unselectClade(DrawableNode n) {
		if (n.isSelected()) {
			n.setSelected(false);
			selectedNodes.remove(n);
		}
		for(Node kid : n.getOffspring()) 
			unselectClade((DrawableNode)kid);		
	}
	
	public boolean subtreeContainsNodes(DrawableNode n, ArrayList<DrawableNode> sample) {
		if (sample.contains(n))
			return true;
		else {
			boolean contains = false;
			for(Node kid : n.getOffspring()) {
				contains = contains || subtreeContainsNodes((DrawableNode)kid, sample);
			}
			return contains;
		}
	}
	
	public DrawableNode findCommonAncestor(ArrayList<DrawableNode> sample) {
		if (sample.size()==0) //shouldn't happen, but just in case
			return null;
		
		DrawableNode newroot = (DrawableNode)root;
		
		ArrayList<Node> containsSample = new ArrayList<Node>();
		for(Node kid : newroot.getOffspring()) {
			if (subtreeContainsNodes( (DrawableNode)kid, sample))
				containsSample.add(kid);
		}
		
		while(containsSample.size()==1) {
			newroot = (DrawableNode)containsSample.get(0);
			containsSample.clear();
			for(Node kid : newroot.getOffspring()) {
				if (subtreeContainsNodes( (DrawableNode)kid, sample))
					containsSample.add(kid);
			}
			
		}
		
		if (containsSample.size()==0) //This should never happen either
			return null;
		
		if (containsSample.size()>1) {
			//.println("Found common ancestor : " + newroot.getLabel());
			return newroot;
		}
		
		//We should never get here
		System.out.println("Error in finding common ancestor, returning null node");
		return null;
	}
	
	
	/**
	 * Create a clone of the smallest subtree that contains only those nodes that are
	 * currently selected.  
	 * @return
	 */
	public Tree extractSelectedTree() {
		DrawableNode ancestor = null;
		if (selectedNodes.size()==1) {
			DrawableNode node = selectedNodes.get(0);
			ancestor = node;
		}
		else {
			ancestor = findCommonAncestor(selectedNodes);
		}
		if (ancestor==null) {
			ErrorWindow.showErrorWindow(new NullPointerException("Could not find common ancestor of selected nodes"));
			return null;
		}
		DrawableNode newroot = ancestor.cloneWithDescendants();
		newroot.setParent(null);
		newroot.setDistToParent(Double.NaN);
	
		DrawableTree newTree = null;
		Constructor<?>[] conList = this.getClass().getConstructors();
		for(Constructor cons : conList) {
			if (cons.getParameterTypes().length==1 && cons.getParameterTypes()[0]==DrawableNode.class) {
				try {
					//System.out.println("Invoking constructor for class " + this.getClass());
					newTree = (DrawableTree) cons.newInstance(new Object[] {newroot});
				} catch (Exception ex) {
					ErrorWindow.showErrorWindow(ex);
				}
			}
		}

		List<Node> tips = newTree.getAllTips();
		int removed = 0;
		for(Node tip : tips) {
			if ( ! ((DrawableNode)tip).isSelected() ) {
				newTree.removeNode(tip);
				removed++;
			}
		}
		
		return newTree;
	}
	
	protected void selectClade(DrawableNode n) {
		if (! n.isSelected()) {
			n.setSelected(true);
			selectedNodes.add(n);
		}
		//System.out.println("Selecting node " + n.getLabel());
		for(Node kid : n.getOffspring()) 
			selectClade((DrawableNode)kid);
	}
	
	protected void selectAllTips(DrawableNode n) {
		if (n.numOffspring()==0) {
			if (! n.isSelected()) {
				n.setSelected(true);
				selectedNodes.add(n);
			}
			//System.out.println("Selecting tip node " + n.getLabel());
		}
		else {
			for(Node kid : n.getOffspring()) 
				selectAllTips((DrawableNode)kid);
		}
	}

	public void emitNodePositions(DrawableNode n) {
		System.out.println(n.getLabel() + " x: " + n.getX() + " y: " + n.getY());
		for(Node k : n.getOffspring())
			emitNodePositions((DrawableNode)k);
	}
	
	public void emitNodePositions() {
		emitNodePositions((DrawableNode)root);
	}
		
	
	/**
	 * Shifts the clade descending from the supplied node by modY in the Y direction 
	 * @param modY Amount to shift by
	 * @param n Node at top of clade
	 */
	public void nudgeY(double modY, DrawableNode n) {
		n.setY( n.getY() + modY );
		for(Node kid : n.getOffspring()) {
			nudgeY(modY, (DrawableNode)kid );
		}
	}
	
	/**
	 * Shifts the clade descending from the supplied node by modX in the X direction 
	 * @param modX Amount to shift by
	 * @param n Node at top of clade
	 */
	public void nudgeX(double modX, DrawableNode n) {
		n.setX( n.getX() + modX );
		for(Node kid : n.getOffspring()) {
			nudgeX(modX, (DrawableNode)kid );
		}
	}

	protected double getMinX(DrawableNode n, double min) {
		if (n.getX()<min)
			min = n.getX();

		for(Node kid : n.getOffspring()) {
			double val = getMinX( (DrawableNode)kid, min);
			if (val<min)
				min = val;
		}
		return min;
	}	
	
	public double getMinX(DrawableNode n) {
		return getMinX(n, n.getX());
	}
	
	public double getMinX() {
		return getMinX( (DrawableNode)root);
	}
	
	protected double getMinY(DrawableNode n, double min) {
		if (n.getY()<min)
			min = n.getY();

		for(Node kid : n.getOffspring()) {
			double val = getMinY( (DrawableNode)kid, min);
			if (val<min)
				min = val;
		}
		return min;
	}	
	
	public double getMinY(DrawableNode n) {
		return getMinY(n, n.getY());
	}

	public double getMinY() {
		return getMinY( (DrawableNode)root);
	}
	
	protected double getMaxY(DrawableNode n, double max) {
		if (n.getY()>max)
			max = n.getY();
		
		for(Node kid : n.getOffspring()) {
			double val = getMaxY( (DrawableNode)kid, max);
			if (val>max)
				max = val;
		}
		return max;
	}	

	protected double getMaxX(DrawableNode n, double max) {
		if (n.getX()>max)
			max = n.getX();
		
		for(Node kid : n.getOffspring()) {
			double val = getMaxX( (DrawableNode)kid, max);
			if (val>max)
				max = val;
		}
		return max;
	}
	
	public double getMaxX(DrawableNode n) {
		return getMaxX(n, n.getX());
	}
	
	public double getMaxY(DrawableNode n) {
		return getMaxY(n, n.getY());
	}
	
	public double getMaxY() {
		return getMaxY( (DrawableNode)root);
	}
	
	public double getMaxX() {
		return getMaxX( (DrawableNode)root);
	}
	
	protected void rotateBigCladesUp(DrawableNode n) {
		if (n.numOffspring()==2) {
			if (n.getOffspring(0).numOffspring() < n.getOffspring(1).numOffspring()) 
				rotateNode(n);
		}
		for(Node kid : n.getOffspring()) {
			rotateBigCladesUp( (DrawableNode)kid );
		}
	}
	
	protected void rotateBigCladesDown(DrawableNode n) {
		if (n.numOffspring()==2) {
			if (n.getOffspring(0).numOffspring() > n.getOffspring(1).numOffspring()) 
				rotateNode(n);
		}
		for(Node kid : n.getOffspring()) {
			rotateBigCladesUp( (DrawableNode)kid );
		}
	}
	

	
	protected void resolveCladeCollisions(DrawableNode n) {
		if (n.numOffspring()<2) {
			return;
		}
		else {
			DrawableNode upper;
			DrawableNode lower;
			if ( ((DrawableNode)n.getOffspring(0)).getY() < ((DrawableNode)n.getOffspring(1)).getY() ) {
				upper = ((DrawableNode)n.getOffspring(0));
				lower = ((DrawableNode)n.getOffspring(1));
			}
			else {
				upper = ((DrawableNode)n.getOffspring(1));
				lower = ((DrawableNode)n.getOffspring(0));				
			}
			
			double max = getMaxY( upper );
			double min = getMinY( lower );

			
			if (min < (max+0.05)) {
				//System.out.println("Changing Y for clade : " + n.getLabel());
				//System.out.println("Nudging upper by : " + (min-max)/2);
				nudgeY((min-max)/2-0.025, upper);
				//System.out.println("Nudging lower by : " + (max-min)/2);
				nudgeY((max-min)/2+0.025, lower);
	
			}
			
			for(Node kid : n.getOffspring()) {
				resolveCladeCollisions( (DrawableNode)kid );
			}
		}
	}	
	
	/**
	 * Remove all nodes descending from this one from the tree, and fire a tree changed event
	 */
	public void removeClade(Node n) {
		unselectClade( (DrawableNode)n);
		super.removeClade(n);
		fireTreeChangedEvent(TreeListener.ChangeType.NODES_REMOVED);
	}
	
	/**
	 * Remove this node from the tree, and if it has children, connect them to this nodes parent, and
	 * fire a tree changed event.
	 */
	public void removeNode(Node n) {
		//System.out.println("Removing node " + n.getLabel() + " from tree " + getCount());
		if ( ((DrawableNode)n).isSelected() ) {
			((DrawableNode)n).setSelected(false);
			selectedNodes.remove(n);
		}
		super.removeNode(n);
		fireTreeChangedEvent(TreeListener.ChangeType.NODES_REMOVED);
	}
	
	protected void resolveCladeCollisions() {
		resolveCladeCollisions((DrawableNode)root);
	}
	

	/**
	 * Calculate that angle from node n's parent to node n. 
	 * @param n
	 * @return
	 */
	public double angleFromParent(DrawableNode n) {
		if (n.getParent()==null)
			return 0;
		else {
			Point2D pPos = ((DrawableNode)n.getParent()).getPosition();
			if (pPos.getX() == n.getX() && pPos.getY() == n.getY()) {
//				System.out.println("Yikes! Node " + n.getLabel() + " has exactly the same position as it's parent! ");
//				System.out.println("Dist to parent : " + n.getDistToParent());
//				System.out.println("Parent : " + n.getParent().getLabel());
				if (n==n.getParent()) 
					System.out.println("Double yikes, this node is it's own parent!");
			}
			return angleFromPos(n, pPos);
		}
	}
	
	/**
	 * The angle from point 'from', to point 'to', in degrees, where degrees are counted in 
	 * clockwise fashion with straight up being zero, down is 180, and due "west" is 270, etc. 
	 * @param to
	 * @param from
	 * @return The angle in degrees from one point to the other
	 */
	public double angleFromPoint(Point2D to, Point2D from) {
		double val;
		if (from.getY()==to.getY()) {
			if (to.getX()<from.getX())
				return 270.0;
			else
				return 90.0;
		}
			
		double arg = (from.getX()-to.getX() )/(from.getY()-to.getY());
		val =  -57.2957795*Math.atan( arg);
		
		if (Double.isNaN(val)) {
			System.out.println("Got NaN in AngleFromPoint, arg : " + arg + " from y: " + from.getY() + " to y : " + to.getY());
		}
		if (from.getY()<to.getY())
			return 180+val;
		else
			return val;		
	}

	
	protected double angleFromPos(DrawableNode n, Point2D pPos) {
		return angleFromPoint(n.getPosition(), pPos);
	}

	
	/**
	 *  Overrides Tree method and ensures that we get DrawableNodes if we read this tree from a string
	 */
	protected void buildTreeFromNewick(String treeStr) {
		root = new DrawableNode();

		//treeStr = treeStr.replaceAll("\\[&[^\\]]+\\]", ""); //Used to strip off beast-style annotations, this may not be the right spot for this...
		try {
			//System.out.println("Tree str is now: " + treeStr);
			buildSubtreeFromNewick(root, treeStr);
		}
		catch (Exception ex) {
			System.err.println("An error was encountered while attempting to build the tree from a newick string \n Error: " + ex + "\n Tree : " + treeStr);
		}
		
		if (root.numOffspring()==0) {
			System.out.println("Error : Tree was found with zero offspring. String was : " + treeStr);
		}
		if (root.numOffspring()==1) {
			System.out.println("Error : Tree was found with just 1 offspring. String was : " + treeStr);
		}
		
	}
	
	protected static void buildSubtreeFromNewick(Node root, String treestr) {
		if (treestr==null || treestr.equals("") || !hasKids(treestr)) {
			return;
		}
		ArrayList<String> kidStrs = getOffspringStrings(0, treestr);
		
		for(String kidStr : kidStrs) {
			DrawableNode kid = new DrawableNode();
			kid.setParent(root);
			root.addOffspring(kid);
			kid.setDistToParent( subTreeDistFromParent(kidStr) );
			kid.setLabel( subTreeLabel(kidStr));
			addAnnotationsToNode(kid, kidStr);
			buildSubtreeFromNewick(kid, kidStr);
		}
	}

	/**
	 * We must override Tree.subTreeLabel to provide accurate parsing of node annotations
	 * @param subtree
	 * @return
	 */
	protected static String subTreeLabel(String subtree) {
		boolean hasParens = subtree.indexOf(")") > 0;
		int lastColon = subtree.lastIndexOf(":");
		if (lastColon==-1) 
			lastColon = subtree.length();
		int lastAnno = subtree.lastIndexOf(annotationIndicator);
		
		if (! hasParens) {
			int labelEnd = lastColon;
			if (lastAnno > -1)
				labelEnd = Math.min(lastAnno, lastColon);
			return subtree.substring(0, labelEnd).trim();
		}
		else  {
			int pos = subtree.indexOf('(');
			if (pos < 0) {
				System.err.println("Found a broken subtree during newick subtree parse: " + subtree);
				return "";
			}
			int matchingParen = matchingParenIndex(pos, subtree);
			
			int labelEnd = lastColon;
			if (lastAnno > matchingParen)
				labelEnd = Math.min(lastAnno, lastColon);
			
			String label;
			if (lastColon > matchingParen) {
				label = subtree.substring(matchingParen+1, labelEnd);	
			}
			else 
				label = "";
			return label;
		}
	}
	
	/**
	 * Add node annotations to the given node from the string provided. The method used is to identify the last index of the 
	 * annotationIndicator (a static field in this class) and the last index of a closing bracket, and to examine the string
	 * in between for comma separated key=value pairs. For instance, we look for strings like:
	 * [& label=Homo sapiens, color=blue, width=4], and assume 
	 * @param kid
	 * @param kidStr
	 */
	private static void addAnnotationsToNode(Node kid, String kidStr) {
		int parenIndex = kidStr.lastIndexOf(")");
		int annoIndex = kidStr.lastIndexOf(annotationIndicator);
		
		if (annoIndex > parenIndex) {
			DrawableNode dKid = (DrawableNode)kid;
			int lastIndex = kidStr.lastIndexOf("]");
			String annotationString = kidStr.substring(annoIndex+annotationIndicator.length(), lastIndex);
			String[] annotations = annotationString.split(",");
			for(int i=0; i<annotations.length; i++) {
				String[] keyVal = annotations[i].split("=");
				if (keyVal.length == 2) {
					dKid.addAnnotation(keyVal[0].trim(), keyVal[1].trim());
					//.println("Found annotation : " + keyVal[0] + " = " + keyVal[1]);
				}
				else {
					System.err.println("Unrecognized node annotation : " + keyVal);
				}
			}
		}
	}
	
	public boolean getIgnoreBranchLengths() {
		return ignoreBranchLengths;
	}

	public void setIgnoreBranchLengths(boolean ignoreBranchLengths) {
		if (this.ignoreBranchLengths != ignoreBranchLengths)
			setHasCalculatedNodePositions(false);
		
		this.ignoreBranchLengths = ignoreBranchLengths;
		fireTreeChangedEvent(TreeListener.ChangeType.NODES_MOVED);
	}
	
	public boolean getHasCalculatedNodePositions() {
		return hasCalculatedNodePositions;
	}

	public void setHasCalculatedNodePositions(boolean hasCalculatedNodePositions) {
		this.hasCalculatedNodePositions = hasCalculatedNodePositions;
	}

	public void changeCollapsedState() {
		for(DrawableNode n : selectedNodes) {
			if (n.isCollapsed())
				n.setCollapse(false);
			else
				n.setCollapse(true);
		}
		fireTreeChangedEvent(TreeListener.ChangeType.COLLAPSED);
	}
	
	public void uncollapseSelectedNodes() {
		for(DrawableNode n : selectedNodes) {
			n.setCollapse(false);
		}
		fireTreeChangedEvent(TreeListener.ChangeType.COLLAPSED);
	}

	public void setNodeLabelType(String type) {
		
	}

	
}
