package gui.figure.treeFigure;

import java.util.ArrayList;
import java.util.List;

/**
 * A type of drawable tree that arranges nodes in 'square' format. The 'orientation' field
 * specifies the tip direction, currently we have up, down, left, and right as options. 
 * 
 * @author brendan
 *
 */
public class SquareTree extends DrawableTree {
	
	public SquareTree(DrawableNode root) {
		super(root);
		orientation = Direction.RIGHT;
		//calculateNodePositions();
	}
	
	public SquareTree(String treeStr) {
		super();
		orientation = Direction.RIGHT;
		buildTreeFromNewick(treeStr);
		collapseZeroBranchLengths();
		//calculateNodePositions();
		assignLabels((DrawableNode)root);
	}
	
	
	public DrawableTree clone() {
		DrawableNode newRoot = ((DrawableNode)root).cloneWithDescendants();
		DrawableTree newTree = new SquareTree(newRoot);
		newTree.setOrientation(getOrientation());
		return newTree;
	}
	
	/**
	 * The primary method for causing nodes to be 'placed', which amounts to calculating an
	 * x and y position for each node in the tree. We do this differently for each orientation,
	 * although the code differs only a little in each case. 
	 */
	public void calculateNodePositions() {
		switch(orientation) {
		case RIGHT: 
			calculateNodePositionsRight();
			break;
		case LEFT: 
			calculateNodePositionsLeft();
			break;
		case UP: 
			calculateNodePositionsUp();
			break;
		case DOWN: 
			calculateNodePositionsDown();
			break;
			
		}
		
		moveAllBetweenOffspring();
	}
	

	private void calculateNodePositionsRight() {
		((DrawableNode)root).setX(0);
		((DrawableNode)root).setY(0.5);

		int totTips = getNumLeaves();
		
		double maxTreeHeight;
		if (ignoreBranchLengths)
			maxTreeHeight = maxNodeDepth();
		else
			maxTreeHeight = getMaxHeight();
		
		double yDist = 0.1; //What should this be?
		for(Node kid : root.getOffspring()) {
			int kidTips = getNumTips(kid);
			double frac = (double)kidTips/(double)totTips;
			//System.out.println("Kid with " + kidTips + " tips gets fraction : " + frac + " tot ydist: " + yDist + " y pos: " + (yDist+frac/2.0));
			calculateNodePositionsRight((DrawableNode)kid, yDist+frac/2.0, maxTreeHeight, kidTips, totTips);
			yDist += frac;
		}
		//System.out.println("Root level final ydist : " + yDist);
		hasCalculatedNodePositions = true;
		rescale();
	}
	
	
	private void calculateNodePositionsRight(DrawableNode n, double yDist, double maxTreeHeight, int myTips, int totTips) { 
		double myFrac = (double)myTips/(double)totTips;
		if (ignoreBranchLengths || (! hasBranchLengths()))
			n.setX( getNodeDepth(n)/(double)getMaxDescendentNodeDepth(n));			
		else
			n.setX( ((DrawableNode)n.getParent()).getX() + n.getDistToParent()/maxTreeHeight );
		
		n.setY(yDist);

		double dist = -myFrac/2.0;;
		for(Node kid : n.getOffspring()) {
			int kidTips = getNumTips(kid);
			double frac = (double)kidTips/(double)totTips;
			
			calculateNodePositionsRight((DrawableNode)kid, yDist+dist+frac/2.0, maxTreeHeight, kidTips, totTips);
			dist+=frac;	
		}

	}
	
	
	private void calculateNodePositionsLeft() {
		((DrawableNode)root).setX(1.0);
		((DrawableNode)root).setY(0.5);
		//System.out.println("Calculating node positions!");
		int totTips = getNumLeaves();
		
		double maxTreeHeight;
		if (ignoreBranchLengths)
			maxTreeHeight = maxNodeDepth();
		else
			maxTreeHeight = getMaxHeight();
		
		double yDist = 0.1; //What should this be?
		for(Node kid : root.getOffspring()) {
			int kidTips = getNumTips(kid);
			double frac = (double)kidTips/(double)totTips;
			//System.out.println("Kid with " + kidTips + " tips gets fraction : " + frac + " tot ydist: " + yDist + " y pos: " + (yDist+frac/2.0));
			calculateNodePositionsLeft((DrawableNode)kid, yDist+frac/2.0, maxTreeHeight, kidTips, totTips);
			yDist += frac;
		}
		//System.out.println("Root level final ydist : " + yDist);
		hasCalculatedNodePositions = true;
		rescale();
	}
	
	
	private void calculateNodePositionsLeft(DrawableNode n, double yDist, double maxTreeHeight, int myTips, int totTips) { 
		double myFrac = (double)myTips/(double)totTips;
		if (ignoreBranchLengths || (! hasBranchLengths()))
			n.setX(1.0- getNodeDepth(n)/(double)getMaxDescendentNodeDepth(n));			
		else
			n.setX( ((DrawableNode)n.getParent()).getX() - n.getDistToParent()/maxTreeHeight );
		
		n.setY(yDist);

		double dist = -myFrac/2.0;;
		for(Node kid : n.getOffspring()) {
			int kidTips = getNumTips(kid);
			double frac = (double)kidTips/(double)totTips;
			
			calculateNodePositionsLeft((DrawableNode)kid, yDist+dist+frac/2.0, maxTreeHeight, kidTips, totTips);
			dist+=frac;	
		}

	}
	
	
	
	private void calculateNodePositionsUp() {
		((DrawableNode)root).setX(0.5);
		((DrawableNode)root).setY(1.0);
		//System.out.println("Calculating node positions!");
		int totTips = getNumLeaves();
		
		double maxTreeHeight;
		if (ignoreBranchLengths)
			maxTreeHeight = maxNodeDepth();
		else
			maxTreeHeight = getMaxHeight();
		
		double xDist = 0.1; //What should this be?
		for(Node kid : root.getOffspring()) {
			int kidTips = getNumTips(kid);
			double frac = (double)kidTips/(double)totTips;
			//System.out.println("Kid with " + kidTips + " tips gets fraction : " + frac + " tot ydist: " + yDist + " y pos: " + (yDist+frac/2.0));
			calculateNodePositionsUp((DrawableNode)kid, xDist+frac/2.0, maxTreeHeight, kidTips, totTips);
			xDist += frac;
		}
		//System.out.println("Root level final ydist : " + yDist);
		hasCalculatedNodePositions = true;
		rescale();
	}
	
	
	private void calculateNodePositionsUp(DrawableNode n, double xDist, double maxTreeHeight, int myTips, int totTips) { 
		double myFrac = (double)myTips/(double)totTips;
		if (ignoreBranchLengths || (! hasBranchLengths())) {
			n.setY(1.0- getNodeDepth(n)/(double)getMaxDescendentNodeDepth(n));
		}
		else {
			n.setY( ((DrawableNode)n.getParent()).getY() - n.getDistToParent()/maxTreeHeight );
		}
		
		n.setX(xDist);

		double dist = -myFrac/2.0;;
		for(Node kid : n.getOffspring()) {
			int kidTips = getNumTips(kid);
			double frac = (double)kidTips/(double)totTips;
			
			calculateNodePositionsUp((DrawableNode)kid, xDist+dist+frac/2.0, maxTreeHeight, kidTips, totTips);
			dist+=frac;	
		}

	}	


	private void calculateNodePositionsDown() {
		((DrawableNode)root).setX(0.5);
		((DrawableNode)root).setY(1.0);
		//System.out.println("Calculating node positions!");
		int totTips = getNumLeaves();
		
		double maxTreeHeight;
		if (ignoreBranchLengths)
			maxTreeHeight = maxNodeDepth();
		else
			maxTreeHeight = getMaxHeight();
		
		double xDist = 0.1; //What should this be?
		for(Node kid : root.getOffspring()) {
			int kidTips = getNumTips(kid);
			double frac = (double)kidTips/(double)totTips;
			//System.out.println("Kid with " + kidTips + " tips gets fraction : " + frac + " tot ydist: " + yDist + " y pos: " + (yDist+frac/2.0));
			calculateNodePositionsDown((DrawableNode)kid, xDist+frac/2.0, maxTreeHeight, kidTips, totTips);
			xDist += frac;
		}
		//System.out.println("Root level final ydist : " + yDist);
		hasCalculatedNodePositions = true;
		rescale();
	}
	
	
	private void calculateNodePositionsDown(DrawableNode n, double xDist, double maxTreeHeight, int myTips, int totTips) { 
		double myFrac = (double)myTips/(double)totTips;
		if (ignoreBranchLengths || (! hasBranchLengths())) {
			n.setY( getNodeDepth(n)/(double)getMaxDescendentNodeDepth(n));
		}
		else {
			n.setY( ((DrawableNode)n.getParent()).getY()+ n.getDistToParent()/maxTreeHeight );
		}
		
		n.setX(xDist);

		double dist = -myFrac/2.0;;
		for(Node kid : n.getOffspring()) {
			int kidTips = getNumTips(kid);
			double frac = (double)kidTips/(double)totTips;
			
			calculateNodePositionsDown((DrawableNode)kid, xDist+dist+frac/2.0, maxTreeHeight, kidTips, totTips);
			dist+=frac;	
		}

	}	

	
	
	private void scaleCladeY(double factor, DrawableNode n) {
		n.setY( n.getY()*factor );
		for(Node kid : n.getOffspring()) {
			scaleCladeY(factor, (DrawableNode)kid );
		}
	}
	
	private void scaleCladeX(double factor, DrawableNode n) {
		n.setX( n.getX()*factor );
		for(Node kid : n.getOffspring()) {
			scaleCladeX(factor, (DrawableNode)kid );
		}
	}
	
	private void normalizeTipSpacing() {
		rescale();
		List<Node> tips = getAllTips();
		sortByY(tips);
		
		double tipSpacing = 1.0/(tips.size()-1);
		int count = 0;
		for(Node tip : tips) {
			DrawableNode dTip = (DrawableNode)tip;
			dTip.setY( count*tipSpacing );
			count++;
		}
		
		for(Node tip : tips) {
			DrawableNode dTip = (DrawableNode)tip;
			moveBetweenOffspringY( (DrawableNode)dTip.getParent() );
		}
	}
	
	private void moveAllBetweenOffspring() {
		moveAllBetweenOffspring(root);
	}
	
	private void moveAllBetweenOffspring(Node n) {
		if (n.numOffspring()==0)
			return;
		else {
			for(Node kid : n.getOffspring()) {
				moveAllBetweenOffspring(kid);
			}
			if (orientation == Direction.LEFT || orientation == Direction.RIGHT)
				moveBetweenOffspringY( (DrawableNode)n);
			else 
				moveBetweenOffspringX( (DrawableNode)n);
		}
	}

	protected void moveBetweenOffspringY(DrawableNode n) {
		double dist = 0;
		for(Node kid : n.getOffspring()) {
			dist += ((DrawableNode)kid).getY();
		}
		
		n.setY( dist/n.numOffspring() );
	}
	
	protected void moveBetweenOffspringX(DrawableNode n) {
		double dist = 0;
		for(Node kid : n.getOffspring()) {
			dist += ((DrawableNode)kid).getX();
		}
		
		n.setX( dist/n.numOffspring() );
	}
	
	private void sortByY(List<Node> nodes) {
		for(int i=0; i<nodes.size(); i++) {
			for(int j=(i+1); j<nodes.size(); j++) {
				DrawableNode one = (DrawableNode)nodes.get(i);
				DrawableNode two = (DrawableNode)nodes.get(j);
				if ( one.getY() > two.getY() ) {
					DrawableNode tmp = one;
					nodes.set(i, two);
					nodes.set(j, tmp);
				}
			}
		}
	}
	
	public void rescale() {
		if (root!=null && root.numOffspring()>=1) {

			double minY = getMinY((DrawableNode)root);
			nudgeY(-minY, (DrawableNode)root);
			double maxY = getMaxY((DrawableNode)root);
			scaleCladeY(1.0/maxY, (DrawableNode)root);

			double minX = getMinX((DrawableNode)root);
			nudgeX(-minX, (DrawableNode)root);
			double maxX = getMaxX((DrawableNode)root);
			scaleCladeX(1.0/maxX, (DrawableNode)root);				

		}
		else {
			System.out.println("Aborting rescale \n");
			if (root==null) {
				System.out.println(".. because root is null");
			}
			else if (root.numOffspring()<2) {
				System.out.println("..because root has " + root.numOffspring() + " offspring");
			}
		}
	}
	
	
	public void rotateNode(DrawableNode n) {
		if (n.numOffspring()>1) {
			DrawableNode kid1 = (DrawableNode)n.getOffspring().remove(0);
			n.addOffspring(kid1);
			calculateNodePositions();
			moveAllBetweenOffspring();
		}
	}
}
