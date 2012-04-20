package gui.figure.treeFigure.drawing;

import gui.figure.treeFigure.*;
import gui.figure.treeFigure.drawing.*;
import gui.figure.treeFigure.DrawableNode;
import gui.figure.treeFigure.DrawableTree;
import gui.figure.treeFigure.DrawableTree.Direction;
import gui.figure.treeFigure.Node;
import gui.figure.treeFigure.TreeListener;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import tools.StringUtilities;


/**
 * This class draws DrawableTrees by using a branchPainter to stamp out the branches, and
 * then adding the node labels and drawing a scale bar or scale axis, if necessary. It does
 * not change the (relative) position of any node, all positioning is done in subclasses of
 * DrawableTree (SquareTree, etc). This class is used independently to stamp out trees in a 
 * MultiTreeChart, and within a TreeElement in the TreeFigure, and behaves slightly differently
 * in each (basically, it doesn't ever draw a scale bar or axis in TreeFigure, since those 
 * are their own elements in figures).
 * 
 * The boundaries of the tree are defined by 4 'padding' variables which function like insets;
 * they measure the number of pixels of additional whitespace in from the sides of the containing 
 * rectangle (passed in to the paint( ) method) in which the tree is drawn. In addition, a 
 * fifth variable "labelSpace" determines the area used to draw the tip labels. Exactly where
 * this extra space appears depends on tree orientation, if the tips point to the right 
 * (tree.getOrientation()==RIGHT), then the label space appears at right. 
 *  
 * 
 * @author brendan
 *
 */
public class TreeDrawer implements TreeListener {
	
	private int leftPadding;
	private int bottomPadding;
	private int topPadding;
	private int rightPadding;
	private int labelSpace;
	
	DrawableTree tree;
	
	//private boolean drawBoundsSet = false;
	private java.awt.Point workingPoint;

	boolean drawScaleBar = false;
	boolean drawScaleAxis = false;

	Color normalColor;
	Color highlightColor;
	Color cartoonColor;
	Stroke highlightStroke;
	Color errorBarColor = Color.LIGHT_GRAY;
	
	int scaleBarPixels = 0;
	String scaleBarLabel = "";
	
	boolean drawYGrid = false;
	int scaleAxisTicks = 4;
	boolean scaleAxisDistFromTips = true;
	
	boolean leaveBottomSpace = false;
	
	boolean clearBeforeDrawing = true;
	
	Rectangle boundaries;
	private boolean showErrorBars = true;
	private boolean scaleBarCalculated = false;
	private Font scaleLabelFont;
	DrawableNode root = null;
	private double branchScale;
	private double heightScale;
	private double treeMaxHeight;
	
	BranchPainter defaultBranchPainter;
	Stack<DrawableNode> nodeStack;
	
	boolean forceRecalculateBounds = true; //Forces a call to initializeBoundaries before painting
	
	NumberFormat formatter = new DecimalFormat("####0.0####");
	
	AffineTransform textTransform; //Used for drawing text labels at various rotations
	
	double eaTreeScaleFactor = 1.0; //A factor used to shrink equal angle trees so the labels will fit inside the bounds
	
	
	public TreeDrawer() {
		leftPadding = 0;
		rightPadding = 0;
		topPadding = 0;
		bottomPadding = 20;
		workingPoint = new java.awt.Point();
		scaleLabelFont = new Font("Sans", Font.PLAIN, 10);
		highlightColor = new Color(10, 225, 225);
		highlightStroke = new BasicStroke(3.25f); 
		normalColor = Color.black;
		cartoonColor = Color.red;
		defaultBranchPainter = new SquareBranchPainter();
		nodeStack = new Stack<DrawableNode>();
		boundaries = new Rectangle(0, 0, 0, 0);
		textTransform = new AffineTransform();
		textTransform.rotate(Math.PI*1.5);
		
	}
	
	
	public Rectangle getBoundaries() {
		return boundaries;
	}
	
	/**
	 * Set the tree to be drawn by this treedrawer. This forces a recalculation of bounds.
	 * @param tree
	 */
	public void setTree(DrawableTree tree) {
		//Make sure we don't end up listening to events from multiple trees
		if (this.tree != null) {
			this.tree.removeTreeListener(this);
		}
		
		this.tree = tree;
		tree.addTreeListener(this);
		forceRecalculateBounds = true;
		root = (DrawableNode)tree.getRoot();
		treeMaxHeight = tree.getHeight();
		if (tree instanceof SquareTree) {
			defaultBranchPainter = new CurvedBranchPainter(0.95);
			textTransform = new AffineTransform();
			textTransform.rotate(Math.PI*1.5);
		}
		
	}

	/**
	 * Returns the width of the rectangle in pixels in which the tree is actually drawn. If
	 * the tree orientation is vertical (i.e. right or left), this measures  the number of 
	 * pixels from the root to the farthest-out tip. If the tree orientation is horizontal, then
	 * this measures the (approximate) number of pixels between the most widely separated tips.  
	 */
	public int getDrawingWidth() {
		if (tree.getOrientation()==DrawableTree.Direction.LEFT || tree.getOrientation()==DrawableTree.Direction.RIGHT)
			return getWidth()-leftPadding-rightPadding-labelSpace;
		else
			return getWidth()-leftPadding-rightPadding;
	}
	
	/**
	 * Returns the height of the rectangle in pixels in which the tree is actually drawn. If
	 * the tree orientation is horizontal (i.e. right or left), this measures  the number of 
	 * pixels from the root to the farthest-out tip. If the tree orientation is up or down, then
	 * this measures the (approximate) number of pixels between the most widely separated tips.  
	 * 
	 */
	public int getDrawingHeight() {
		if (tree.getOrientation()==DrawableTree.Direction.LEFT || tree.getOrientation()==DrawableTree.Direction.RIGHT)
			return getHeight()-topPadding-bottomPadding;
		else
			return getHeight()-topPadding-bottomPadding - labelSpace;
	}
	
	
	/**
	 * Used by Scale elements
	 * @return The maximum height of the tree in branch length units
	 */
	public double getTreeMaxHeight() {
		return treeMaxHeight;
	}
	
	
	/**
	 * Returns the tree this drawer is currently drawing. 
	 * @return A drawable tree to draw
	 */
	public DrawableTree getTree() {
		return tree;
	}
	
	
	/**
	 * This method chooses the appropriate values for the padding fields, which are the insets for
	 * the tree drawing area. It's done a bit differently if we're using an equal angle tree as opposed
	 * to a square tree. This occurs only when the drawing area is resized. This should not be done
	 * without also calling initializeBoundaries.  
	 */
	private void setDrawBounds() {
		if (tree.getOrientation()==Direction.UP || tree.getOrientation()==Direction.DOWN) {
			leftPadding = 60; //Leave some space for the scale axis
		}
		else
			leftPadding = 20;

		//LeaveBottomSpace is used by TreeFigure, which draws its own scale bar and axis, and 
		//therefore requires this thing to leave a bit of extra space at the bottom
		if (drawScaleAxis || drawScaleBar || leaveBottomSpace)
			bottomPadding = 40;
		else
			bottomPadding = 20;
		topPadding = 15;
		rightPadding = 15;

		((SquareTree)tree).rescale();

		scaleBarCalculated = false;
	}
	
	private int getWidth() {
		return boundaries.width;
	}
	
	private int getHeight() {
		return boundaries.height;
	}

	/**
	 * Recalculate the branchscale, heightscale, and labelSpace quantities. These need to be recalculated
	 * whenever the boundaries have changed. This also sets the current boundaries values to 
	 * those in the provided bounds object. We also need a graphics object here so we can calculate
	 * how big the labels are, which is necessary for the labelSpace calculation.  
	 * @param bounds
	 * @param g2d
	 */
	public void initializeBoundaries(Rectangle bounds, Graphics2D g2d) {
		boundaries.x = bounds.x;
		boundaries.y = bounds.y;
		boundaries.width = bounds.width;
		boundaries.height = bounds.height;

		if (drawScaleBar || drawScaleAxis) {
			leaveBottomSpace = true;
		}
		else {
			leaveBottomSpace = false;
		}
		
		setDrawBounds();
		
		
		//System.out.println("Minimum tip+label pos: " + maxWidth + " x(0): " + translateTreeToPixelX(0));
		
		
			FontMetrics fm = g2d.getFontMetrics();
			List<Node> tips = tree.getAllTips();
			int maxWidth = 0;
			if (tree.getOrientation()==Direction.LEFT || tree.getOrientation()==Direction.UP) 
				maxWidth = Integer.MAX_VALUE;

			for(Node tip : tips) {
				int labelWidth = fm.stringWidth( ((DrawableNode)tip).getCurrentLabel());
				int tipPos = 0;
				if (tree.getOrientation()==Direction.LEFT || tree.getOrientation()==Direction.RIGHT)
					tipPos = translateTreeToPixelX( ((DrawableNode)tip).getX() );
				else
					tipPos = translateTreeToPixelY( ((DrawableNode)tip).getY() );

				//If direction is left (or up), we want to identify the leftmost (or upmost) point
				//of all the tree tips PLUS the width the their label. This is the minimum of all
				//all X (or Y) values. Later, we subtract from this translateTreeToPixelX(0), to
				//find the labelSpace value
				if (tree.getOrientation()==Direction.LEFT || tree.getOrientation()==Direction.UP) {
					int x = tipPos-labelSpace - labelWidth;
					if (x<maxWidth)
						maxWidth = x;
				}
				else { //On the other hand, if direction is right (or down), we keep the max X (or Y) value
					int x = tipPos + labelWidth;
					if (x>maxWidth)
						maxWidth = x;
				}

			}


			if (tree.getOrientation()==Direction.LEFT) 
				labelSpace = translateTreeToPixelX(0)-labelSpace-maxWidth;
			if (tree.getOrientation()==Direction.UP) 
				labelSpace = translateTreeToPixelY(0)-labelSpace-maxWidth;
			if (tree.getOrientation()==Direction.DOWN) 
				labelSpace = maxWidth-translateTreeToPixelY(1.0);
			if (tree.getOrientation()==Direction.RIGHT) 
				labelSpace = maxWidth-translateTreeToPixelX(1.0);



			if (tree.getOrientation()==DrawableTree.Direction.RIGHT || tree.getOrientation()==DrawableTree.Direction.LEFT) {
				branchScale = (getWidth()-leftPadding-rightPadding-labelSpace);
				heightScale = getHeight()-topPadding-bottomPadding;
			}
			else {
				branchScale = getWidth()-leftPadding-rightPadding;
				heightScale = getHeight()-topPadding-bottomPadding-labelSpace;
			}
		
		//System.out.println("Boundaries initialized, branch scale: " + branchScale + " width: " + getWidth() + "  leftpadding : " + leftPadding + " label space: " + labelSpace);
		scaleBarCalculated = false;
		forceRecalculateBounds = false;
	}

	/**
	 * Draw the Y gridlines for a scale axis using the specified graphics object.
	 * @param g2d
	 */
	private void drawYGridlines(Graphics2D g2d) {
		int leftEdge = boundaries.x+leftPadding;
		int drawWidth = getWidth()-leftPadding-rightPadding-labelSpace;
		int bottomDist = 25; //This must be exactly the same value as in drawScaleAxis... watch out
		int topEdge = boundaries.y+getHeight()-bottomDist;
		g2d.setColor(Color.LIGHT_GRAY);
		int ticks = scaleAxisTicks;
		double tickStep = drawWidth/ticks;
		for(int i=0; i<ticks; i++) {
			g2d.drawLine(leftEdge+(int)Math.round(i*tickStep), boundaries.y+topPadding, leftEdge+(int)Math.round(i*tickStep), topEdge-1);
		}
		
		g2d.drawLine(leftEdge+drawWidth, boundaries.y+topPadding, leftEdge+drawWidth, topEdge-1);
		g2d.setColor(Color.black);
	}
	
	public void setClearBeforeDrawing(boolean clear) {
		this.clearBeforeDrawing = clear;
	}
	
	/**
	 * Paint the tree using the specified graphics object and using the specified rectangle as 
	 * the boundaries. 
	 * @param g2d
	 * @param bounds
	 */
	public void paint(Graphics2D g2d, Rectangle bounds) {
		if (tree == null) {
			System.err.println("Tree is null!");
			return;
		}
			
		if (! tree.getHasCalculatedNodePositions()) {
			tree.calculateNodePositions();
		}
				
		if (clearBeforeDrawing) {
			g2d.setColor(Color.white);
			g2d.fill(bounds);
		}
		
		if (tree.getScaleType()==DrawableTree.NO_SCALE_BAR) {
			drawScaleBar = false;
			drawScaleAxis = false;
			drawYGrid = false;
			removeScaleBarSpace();
		}
		if (tree.getScaleType()==DrawableTree.SCALE_BAR) {
			drawScaleBar = true;
			drawScaleAxis = false;
			drawYGrid = false;
			makeSpaceForScaleBar();
		}
		if (tree.getScaleType()==DrawableTree.SCALE_AXIS) {
			drawScaleAxis = true;
			drawScaleBar = false;
			//drawYGrid = true;
			makeSpaceForScaleBar();
		}
		
		if ( (! bounds.equals( boundaries)) || forceRecalculateBounds) {
			initializeBoundaries(bounds, g2d);
		}
		
//		g2d.setColor(Color.RED);
//		g2d.drawRect(bounds.x+leftPadding, bounds.y+topPadding, bounds.width-rightPadding, bounds.height-bottomPadding);
//		System.out.println("Bounds width: " + bounds.width + " padding: " + rightPadding);
//		g2d.setColor(Color.green);
//		g2d.fillRect(translateTreeToPixelX(0.5)-1, translateTreeToPixelY(0.5)-1, 3, 3);
//		g2d.drawRect(translateTreeToPixelX(0.0), translateTreeToPixelY(0.0), translateTreeToPixelX(1.0)-translateTreeToPixelX(0.0), translateTreeToPixelY(1.0)-translateTreeToPixelY(0.0));
			
		
		if (tree == null) {
			return;
		}
		
		
		if (tree.getOrientation() == Direction.RIGHT || tree.getOrientation() == Direction.LEFT) {
			textTransform.setToIdentity();
		}
		if (tree.getOrientation() == Direction.UP) {
			textTransform = new AffineTransform();
			textTransform.rotate(Math.PI*1.5);
		}
		if (tree.getOrientation() == Direction.DOWN) {
			textTransform = new AffineTransform();
			textTransform.rotate(Math.PI*-1.5);
		}
		
		

		
		if (drawYGrid) {
			drawYGridlines(g2d);
		}
		
		if (root==null) {
			System.err.println("Yikes! Root=null! Not drawing tree...");
		}
		else {
			
			if (showErrorBars)
				drawErrorBars(g2d, root); //This comes before everything else
			drawHighlightedNodes(g2d, root);
			
			drawTree(g2d, root);
			
			//Draw scale bar/axis only if tree has branch lengths and we're not ignoring them
			if (drawScaleBar && (tree.hasBranchLengths() && !tree.getIgnoreBranchLengths())) {
				drawScaleBar(g2d);
			}
			if (drawScaleAxis && (tree.hasBranchLengths() && !tree.getIgnoreBranchLengths())) {
				drawScaleAxis(g2d);
			}
		}

	}
	


	public int getLeftPadding() {
		return leftPadding;
	}


	public void setLeftPadding(int leftPadding) {
		this.leftPadding = leftPadding;
	}


	public int getTopPadding() {
		return topPadding;
	}


	public void setTopPadding(int topPadding) {
		this.topPadding = topPadding;
	}


	public int getRightPadding() {
		return rightPadding;
	}


	public void setRightPadding(int rightPadding) {
		this.rightPadding = rightPadding;
	}


	public int getLabelSpace() {
			return labelSpace;
	}


	public void setLabelSpace(int labelSpace) {
		this.labelSpace = labelSpace;
	}


	public Color getNormalColor() {
		return normalColor;
	}


	public void setNormalColor(Color normalColor) {
		this.normalColor = normalColor;
	}


	public Color getErrorBarColor() {
		return errorBarColor;
	}


	public void setErrorBarColor(Color errorBarColor) {
		this.errorBarColor = errorBarColor;
	}


	public boolean isShowingErrorBars() {
		return showErrorBars;
	}


	public int getBottomPadding() {
		return bottomPadding;
	}

	public void setBottomPadding(int bottomPadding) {
		this.bottomPadding = bottomPadding;
	}

	public boolean isShowErrorBars() {
		return showErrorBars;
	}


	public void setShowErrorBars(boolean showErrorBars) {
		this.showErrorBars = showErrorBars;
	}
	
	public boolean isDrawYGrid() {
		return drawYGrid;
	}


	public void setDrawYGrid(boolean drawYGrid) {
		this.drawYGrid = drawYGrid;
	}


	public boolean isScaleAxisDistFromTips() {
		return scaleAxisDistFromTips;
	}


	public void setScaleAxisDistFromTips(boolean scaleAxisDistFromTips) {
		this.scaleAxisDistFromTips = scaleAxisDistFromTips;
	}

	
	protected void drawCartoon(Graphics2D g2d, int x, int y, int maxX, int minY, int maxY, boolean selected) {
		int[] xvals = {x, maxX, maxX, x};
		int[] yvals = {y, minY, maxY, y};
		Shape cartoon = new Polygon(xvals, yvals, 4);
		
		if (selected) {
			g2d.setColor(highlightColor);
			g2d.drawLine(x-1, y, maxX+1, minY-1);
			g2d.drawLine(x-1, y, maxX+1, maxY+1);
			g2d.drawLine(maxX+1, minY-1, maxX+1, maxY+1);
		}
		g2d.setColor(cartoonColor);
		g2d.fill(cartoon);
		g2d.setColor(normalColor);
		
	}
	
	protected java.awt.Point calculateLabelXY(DrawableNode n, int x, int y, FontMetrics fm) {
		int width = fm.stringWidth(n.getCurrentLabel());
		int height = n.getFont().getSize();
		
		if (n.getLabelPosition()==TreeFigure.RIGHT_POSITION) {
			if (tree.getOrientation()== DrawableTree.Direction.RIGHT) {
				workingPoint.x = x+3;				
			}
			else {
				workingPoint.x = x-width-2;
			}
				
			workingPoint.y = y+height/2-1;
			return workingPoint;
		}
		
		
		
		if (tree.getOrientation()== DrawableTree.Direction.RIGHT ) {
			workingPoint.x = x-width-2;				
		}
		else{
			workingPoint.x = x+2;
		}
		
		if (n.getLabelPosition()==TreeFigure.UPPER_LEFT_POSITION) {
			workingPoint.y = y - 1;
			return workingPoint;
		}
		if (n.getLabelPosition()==TreeFigure.LOWER_LEFT_POSITION) {	
			workingPoint.y = y+height+1;
			return workingPoint;
		}
		
		return workingPoint;
	}
	
	/**
	 * DrawableTrees use a 0..1 coordinate system to place their nodes, this translates from
	 * these 'tree coordinates' into pixels. This also respects the 'labelSpace' area, which
	 * moves depending on tree orientation. 
	 * 
	 * @param x
	 * @return
	 */
	public int translateTreeToPixelX(double x) {
		if (tree.getOrientation()==DrawableTree.Direction.LEFT)
			return (int)Math.round( x*branchScale)+leftPadding+boundaries.x+labelSpace;
		else
			return (int)Math.round( x*branchScale)+leftPadding+boundaries.x;
	}

	/**
	 * Translates a coordinate in pixel space back to tree space
	 * @param x
	 * @return
	 */
	public double translatePixelToTreeX(int x) {
		if (tree.getOrientation()==DrawableTree.Direction.LEFT)
			return (double)(x-(leftPadding+boundaries.x+labelSpace))/(double)(branchScale);
		else
			return (double)(x-(leftPadding+boundaries.x))/(double)(branchScale);
	}
	
	/**
	 * DrawableTrees use a 0..1 coordinate system to place their nodes, this translates from
	 * these 'tree coordinates' into pixels. This also respects the 'labelSpace' area, which
	 * moves depending on tree orientation.
	 * @param y in 'tree coordinates'
	 * @return y value in pixels
	 */
	public int translateTreeToPixelY(double y) {
		if (tree.getOrientation()==DrawableTree.Direction.UP)
			return (int)Math.round( y*heightScale )+topPadding + boundaries.y+labelSpace;
		else
			return (int)Math.round( y*heightScale )+topPadding + boundaries.y;
	}

	/**
	 * DrawableTrees use a 0..1 coordinate system to place their nodes, this translates from
	 * the pixel coordinates (whatever the bounding rectangle is described in, presumably) back
	 * to 'tree' units
	 * @param y in 'tree coordinates'
	 * @return y value in pixels
	 */
	public double translatePixelToTreeY(int y) {
		if (tree.getOrientation()==DrawableTree.Direction.UP)
			return (y-(topPadding + boundaries.y+labelSpace))/((double)heightScale); 
		else
			return (y-(topPadding + boundaries.y))/((double)heightScale);		
	}
	
	
	protected void drawHighlightedNodes(Graphics2D g2d, DrawableNode root) {
		nodeStack.clear();
		nodeStack.push(root);
		
		g2d.setColor(highlightColor);
		while(!nodeStack.isEmpty()) {
			DrawableNode n = nodeStack.pop();	
			int x = translateTreeToPixelX(n.getX());
			int y = translateTreeToPixelY(n.getY());
			
			if (n.isSelected()) {
				g2d.setColor(highlightColor);
				g2d.fillRect(x-2, y-2, 5, 5);
				if (n.getParent()!=null) {
					int x1 = translateTreeToPixelX(((DrawableNode)n.getParent()).getX() );
					int y1 = translateTreeToPixelY(((DrawableNode)n.getParent()).getY() );
					BranchPainter branchPainter = n.getBranchPainter();
					if (branchPainter == null)
						branchPainter = defaultBranchPainter;
					//We paint the branches differently depending on the orientation of the tree
					if (tree.getOrientation()==DrawableTree.Direction.RIGHT || tree.getOrientation()==DrawableTree.Direction.LEFT) {
						branchPainter.setOrientation(BranchPainter.Direction.YFIRST);
					}
					else {
						branchPainter.setOrientation(BranchPainter.Direction.XFIRST);
					}
					branchPainter.paintBranch(g2d, x1, y1, x, y, highlightStroke, highlightColor);	
				}
			}
			
			for(Node kid : n.getOffspring()) {
				nodeStack.push( (DrawableNode)kid);
			}	
		}
		
	}
	
	protected void drawTree(Graphics2D g2d, DrawableNode root) {
		drawTree(g2d, root, true);
	}

	/**
	 * Draw a label at the tip of this tree. We call the node's getCurrentLabel() to decide what
	 * string to draw.  
	 * @param g2d
	 * @param node
	 * @param x
	 * @param y
	 */
	protected void drawTipLabel(Graphics2D g2d, DrawableNode node, int x, int y) {
		String theLabel = node.getCurrentLabel();

		if (node.isSelected()) {
			g2d.setFont(node.getFont().deriveFont(Font.BOLD));
		}
		else {
			g2d.setFont(node.getFont());
			//System.out.println("Setting tip label font to : " +  node.getFont());
		}
		
		int labX = x;
		int labY = y+node.getFont().getSize()/2-1;
		
		if (tree.getOrientation()== DrawableTree.Direction.RIGHT)
			labX = x+3;
		if (tree.getOrientation() == DrawableTree.Direction.LEFT) {
			FontMetrics fm = g2d.getFontMetrics();
			int width = (int)Math.round(fm.getStringBounds(theLabel, 0, theLabel.length(), g2d).getWidth());
			labX = x-3-width;
		}
		if (tree.getOrientation() == DrawableTree.Direction.UP) {
			Font origFont = g2d.getFont();
			g2d.setFont(origFont.deriveFont(textTransform));
			FontMetrics fm = g2d.getFontMetrics();
			int height = (int)Math.round(fm.getStringBounds(theLabel, 0, theLabel.length(), g2d).getHeight());
			labY = y-2;
			labX = x + height/3;
		}
		if (tree.getOrientation() == DrawableTree.Direction.DOWN) {
			Font origFont = g2d.getFont();
			g2d.setFont(origFont.deriveFont(textTransform));
			FontMetrics fm = g2d.getFontMetrics();
			int height = (int)Math.round(fm.getStringBounds(theLabel, 0, theLabel.length(), g2d).getHeight());
			labX = x -height/3;
		}

		g2d.setColor(Color.BLACK);
		g2d.drawString(theLabel, labX, labY);
	}
	
	/**
	 * Draw the branches and labels. We use a stack-based tree traversal hoping that it will be
	 * a bit faster than a recursive algorithm
	 * 
	 * @param g2d
	 * @param root
	 * @param drawLabels
	 */
	protected void drawTree(Graphics2D g2d, DrawableNode root, boolean drawLabels) {
		nodeStack.clear();
		
		nodeStack.push(root);
		
		//Draw root label, we do this separately....
		if (drawLabels) {
			g2d.setFont(root.getFont());
			String label = root.getCurrentLabel();
			if (label!=null) {
				java.awt.Point labelPos = calculateLabelXY(root, translateTreeToPixelX(root.getX()), translateTreeToPixelY(root.getY()), g2d.getFontMetrics());
				g2d.setColor(Color.BLACK);
				
				g2d.drawString( label, labelPos.x , labelPos.y);
			}
		}
		
		while(!nodeStack.isEmpty()) {
			DrawableNode node = nodeStack.pop();
			int x = translateTreeToPixelX(node.getX());
			int y = translateTreeToPixelY(node.getY());

			if (node.isCollapsed()) {
				int maxX = translateTreeToPixelX(tree.getMaxX(node)); 
				int maxY = translateTreeToPixelY(tree.getMaxY(node)); 
				int minY = translateTreeToPixelY(tree.getMinY(node)); 

				drawCartoon(g2d, x, y, maxX, minY, maxY, node.isSelected());
			}
			else {
				for(Node kid : node.getOffspring()) {
					DrawableNode dKid = (DrawableNode)kid;
					int x1 = translateTreeToPixelX(dKid.getX() ); 
					int y1 = translateTreeToPixelY(dKid.getY() ); 
					BranchPainter branchPainter = dKid.getBranchPainter();
					if (branchPainter == null)
						branchPainter = defaultBranchPainter;
					
					//We paint the branches differently depending on the orientation of the tree
					if (tree.getOrientation()==DrawableTree.Direction.RIGHT || tree.getOrientation()==DrawableTree.Direction.LEFT) {
						branchPainter.setOrientation(BranchPainter.Direction.YFIRST);
					}
					else {
						branchPainter.setOrientation(BranchPainter.Direction.XFIRST);
					}
					branchPainter.paintBranch(g2d, x, y, x1, y1, dKid.getStroke(), dKid.getColor());
					//System.out.println("Drawing branch from tree: " + node.getY() + " dKid y: " + dKid.getY() + " y: " + y + " to y1: " + y1);
				}

				for(Node kid : node.getOffspring()) {
					nodeStack.push((DrawableNode)kid);
				}

			}	
			
			if (node.numOffspring()==0 && drawLabels) {
				drawTipLabel(g2d, node, x, y);
			}//if this node is a tip
			else { //Node has at least one offspring
				g2d.setFont(node.getFont());
				String label = node.getCurrentLabel();
				if (label!=null) {
					java.awt.Point labelPos = calculateLabelXY(node, x, y, g2d.getFontMetrics());
					g2d.setColor(Color.BLACK);
					g2d.drawString( label, labelPos.x , labelPos.y);
				}
			}
		}
	}
		
	/**
	 * Draw the error bars for each node, but only if the node has its error
	 * bars value set
	 * @param g2d
	 * @param node
	 */
	private void drawErrorBars(Graphics2D g2d, DrawableNode node) {
		int x = translateTreeToPixelX(node.getX());
		int y = translateTreeToPixelY(node.getY());
		g2d.setColor(errorBarColor);
		double error = 0;
		

		if (node.hasErrorBars()) {
			error = node.getErrorBarLength();
			if (error > 0) {
				int pixError = (int)Math.round(error*branchScale);
				g2d.fillRect(x-pixError, y-2, 2*pixError, 5);
			}

		}
		
		for(Node kid : node.getOffspring()) {
			drawErrorBars(g2d, (DrawableNode)kid);
		}

	}

	public void drawScaleAxis(Graphics2D g2d) {
		int drawWidth = getWidth()-leftPadding-rightPadding-labelSpace;
		FontMetrics fm = g2d.getFontMetrics();
		g2d.setFont(scaleLabelFont);
		g2d.setColor(Color.black);
		int bottomDist = 25;
		String label;		
		int leftEdge = boundaries.x+leftPadding;
		int topEdge = boundaries.y+getHeight()-bottomDist;
		g2d.drawLine(leftEdge, topEdge, leftEdge+drawWidth, topEdge);
		int ticks = scaleAxisTicks;
		double tickStep = drawWidth/ticks;
		
		if (treeMaxHeight>0.5)
			formatter.setMaximumFractionDigits(2);
		if (treeMaxHeight>100)
			formatter.setMaximumFractionDigits(1);
		
		for(int i=0; i<ticks; i++) {
			g2d.drawLine(leftEdge+(int)Math.round(i*tickStep), topEdge, leftEdge+(int)Math.round(i*tickStep), topEdge+5);
			double scaleVal;
			if (scaleAxisDistFromTips)
				scaleVal = treeMaxHeight*(1.0-(i*tickStep)/branchScale);
			else
				scaleVal = treeMaxHeight*i*tickStep/branchScale;
				

			label = formatter.format(scaleVal);
			
			int width = (int)Math.round(fm.getStringBounds(label, 0, label.length(), g2d).getWidth());
			
			
			g2d.drawString(label, leftEdge+(int)Math.round(i*tickStep)-width/2, topEdge+20);
		}
		
		
		g2d.drawLine(leftEdge+drawWidth, topEdge, leftEdge+drawWidth, topEdge+5);
		if (scaleAxisDistFromTips)
			label = formatter.format(treeMaxHeight*(1.0-(ticks*tickStep)/branchScale));
		else
			label = formatter.format(treeMaxHeight*ticks*tickStep/branchScale);

		int width = (int)Math.round(fm.getStringBounds(label, 0, label.length(), g2d).getWidth());
		g2d.drawString(label, leftEdge+(int)Math.round(ticks*tickStep-width/1.55), topEdge+20);		
	}
	
	public void drawScaleBar(Graphics2D g2d) {
		if (! scaleBarCalculated ) {
			int treeLengthPixels = getWidth()-leftPadding-rightPadding-labelSpace;			
			int barLengthPixels = Math.max(treeLengthPixels/5, 28);
			
			double treeHeightFraction = (double)barLengthPixels / (double)treeLengthPixels;
			double barLengthBranchUnits = treeHeightFraction * treeMaxHeight;
			
			scaleBarCalculated = true;
			scaleBarPixels = barLengthPixels;
			scaleBarLabel = StringUtilities.format( barLengthBranchUnits );
		}
		
		
		g2d.setColor(Color.black);
		g2d.setFont(scaleLabelFont);
		int labelWidth = g2d.getFontMetrics().stringWidth(scaleBarLabel);
		int bottomDist = 20;
		int leftEdge = boundaries.x + getWidth()/2-30; 
		int top = boundaries.y + getHeight()-bottomDist;
		g2d.drawLine(leftEdge, top, leftEdge+scaleBarPixels, top); //upper crossbar
		g2d.drawLine(leftEdge, top, leftEdge, top+5);
		g2d.drawLine(leftEdge+scaleBarPixels, top, leftEdge+scaleBarPixels, top+5);
		g2d.drawString(scaleBarLabel, leftEdge-labelWidth/2+scaleBarPixels/2, top+12);
	}


	public void makeSpaceForScaleBar() {
		leaveBottomSpace = true;
		forceRecalculateBounds = true;
	}


	public void removeScaleBarSpace() {
		leaveBottomSpace = false;
		forceRecalculateBounds = true;
	}


	/**
	 * Called when the tree has changed, we set flags that mark various fields for updating 
	 * @param type
	 */
	public void treeChanged(ChangeType type) {
		//System.out.println("Got tree changed event, flagging recalc bounds");
		forceRecalculateBounds = true;
	}


	/**
	 * Flag the bounds recalculation to occur on the next repaint (or not)
	 * @param b 
	 */
	public void setRecalculateBounds(boolean b) {
		forceRecalculateBounds = b;
	}

}
