package plugins.treePlugin.treeFigure;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.Timer;

import display.DisplayData;

import plugins.treePlugin.tree.DrawableTree;
import plugins.treePlugin.tree.SquareTree;
import plugins.treePlugin.tree.drawing.TreeDrawer;
import plugins.treePlugin.tree.reading.TreeQueueManager;
import plugins.treePlugin.treeDisplay.MultiTreeDisplay;
import plugins.treePlugin.treeDisplay.TreeDisplay;


public class MultiTreeChart extends Chart implements MouseListener, MouseMotionListener, ComponentListener, ActionListener {

	MultiTreeDisplay display;
	TreeDrawer treeDrawer;

	Rectangle boundsRect;
	double maxTreeHeight;
	boolean drawBoundsSet;
	
	JPopupMenu popup;

	int rows;
	int cols;
	
	TreeQueueManager currentTrees;
	
	//Rectangles used for drawing the selection region
	Rectangle selectRect; //Current selection rectangle
	Rectangle prevRect;  //previous selection rectangle, used for redrawing
	Rectangle2D translatedRect; //Rectangle passed to tree to select nodes

	boolean mouseDrag = false; //True is the mouse is being dragged
	Dimension mouseBegin;
	Dimension mouseEnd;
	
	DrawableTree selectedTree = null;
	java.awt.Point mousePos;
	
	Color highlightSquareColor = new Color(0.3f, 0.5f, 0.8f, 0.3f);
	
	//Following fields are for tree-sliding animation
	Timer timer;				//Timer for controlling tree sliding behavior
	boolean advancing = false;
	boolean retracting = false;
	double advanceOffset = 0;	//Holds current advance offset
	int moveTime = 800; //Time to move advance/retract in ms
	int timerDelay = moveTime/40;		//Delay in ms between calls to change advance offset + redraw tree
	long startTime = 0;
	long elapsedTime = 0;
	
	int calls = 0;
	
	BufferedImage treeImage;

	//Used for drawing small tree numbers in corner of individual trees
	boolean drawTreeNumbers = true;
	Font treeNumberFont = new Font("Sans", Font.PLAIN, 11);
	
	public MultiTreeChart(MultiTreeDisplay display) {
		addMouseListener(this);
		addMouseMotionListener(this);
		addComponentListener(this);
		MouseListener popupListener = new PopupListener();
	    addMouseListener(popupListener);

	    initializePopup();
	    
		this.display = display;
		boundsRect = new Rectangle(0, 0, 0, 0);
		treeDrawer = new TreeDrawer();

		
		mouseBegin = new Dimension(0, 0);
		mouseEnd = new Dimension(0, 0);
		mousePos = new java.awt.Point(-1, -1);
		selectRect = new Rectangle(0, 0, 0, 0);
		prevRect = new Rectangle(0, 0, 0, 0);
		translatedRect = new Rectangle2D.Double(0, 0, 0, 0);
		
		timer = new Timer(timerDelay, this);
		timer.setInitialDelay(10);
		rows = 1;
		cols = 3;
		selectedTree = null;	
		
	}
	
	/**
	 * When true, small numbers indicating the number of the tree are painted
	 * @param draw
	 */
	public void setDrawTreeNumbers(boolean draw) {
		this.drawTreeNumbers = draw;
	}
	
	private void initializePopup() {
		popup = new JPopupMenu();
		
		JMenuItem exportTree = new JMenuItem("Export tree");
		exportTree.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportTree();
            }
        });
		
		JMenuItem flipItem = new JMenuItem("Flip Horizontally");
		flipItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                flipTreeHorizontally();
            }
        });
		
		JMenuItem hideScale = new JMenuItem("Hide scale bar");
		hideScale.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                hideScale();
            }
        });
		
		showScaleBar = new JCheckBoxMenuItem("Show scale bar");
		showScaleBar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showScaleBar();
            }
        });
		
		showScaleAxis = new JCheckBoxMenuItem("Show scale axis");
		showScaleAxis.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showScaleAxis();
            }
        });
		
		
		popup.add(exportTree);
		popup.add(flipItem);
		popup.add(hideScale);
		popup.add(showScaleBar);
		popup.add(showScaleAxis);
		popup.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
	}
	
	protected void exportTree() {
		if (selectedTree != null) {
			String newick = selectedTree.getNewick();
			DrawableTree newTree = new SquareTree(newick);
			DisplayData data = new DisplayData(null, newTree, TreeDisplay.class);
			display.getSunFishParent().displayData(data, "Exported tree");
		}
		repaint();
	}

	protected void showScaleAxis() {
		if (selectedTree != null && showScaleAxis.isSelected()) {
			selectedTree.setScaleType(DrawableTree.SCALE_AXIS);
			showScaleBar.setSelected(false);			
		}
		repaint();
	}

	protected void showScaleBar() {
		if (selectedTree != null  && showScaleBar.isSelected()) {
			selectedTree.setScaleType(DrawableTree.SCALE_BAR);
			showScaleAxis.setSelected(false);			
		}
		repaint();
	}

	protected void hideScale() {
		if (selectedTree != null) {
			selectedTree.setScaleType(DrawableTree.NO_SCALE_BAR);
		}
		
		repaint();
	}

	protected void flipTreeHorizontally() {
		if (selectedTree != null) {
			selectedTree.flipHorizontally();
			repaint();
		}
	}

	public void setRows(int rows) {
		this.rows = rows;
	}
	
	public void setCols(int cols) {
		this.cols = cols;
	}
	
	public int getRows() {
		return rows;
		
	}
	
	public int getCols() {
		return cols;
	}
	
	public int getMatrixSize() {
		return rows*cols;
	}
	
	public int getQueueSize() {
		return currentTrees.size();
	}
	
	public void setTreeQueue(TreeQueueManager trees) {
		currentTrees = trees;
		drawBoundsSet = false;
	}
	
	private void recalculateBounds() {
		
	}
	
	/**
	 * Returns true if this is in the middle of an animation and we can't change state
	 */
	public boolean isBusy() {
		return advancing || retracting;
	}
	
	public void advance() {
		if (isBusy())	//Ignore requests if we're in the middle of something
			return;
		
		int shifted = currentTrees.extendRight(1);
		
		if (shifted == 0)
			return;
		
		if (rows==1) {
			elapsedTime = 0;
			mousePos.x = -1;
			mousePos.y = -1;
			advancing = true;
			advanceOffset = 0;
			
			cols++; //Since we actually want to draw an extra tree, we need to add an extra column
			treeImage = this.getGraphicsConfiguration().createCompatibleImage((int)(getWidth()*(double)currentTrees.size()/(double)(currentTrees.size()-1.0)), getHeight());
			Graphics2D treeGraphics = treeImage.createGraphics();
			paintTrees(treeGraphics, 0, 0, treeImage.getWidth(), treeImage.getHeight());
			cols--; //.. subtract off the extra column
			
			timer.start();
		}
		else {
			currentTrees.shrinkLeft(1);
		}
		repaint();
	}
	
	public void goback() {
		if (isBusy()) //We're busy right now 
			return;
		
		int shifted = currentTrees.extendLeft(1);
		if (shifted == 0)
			return;
		
		if (rows ==1) {
			mousePos.x = -1;
			mousePos.y = -1;
			elapsedTime = 0;
			//System.out.println("Adding to beginning, current size is : " + currentTrees.size());
			advanceOffset = 0;
			retracting = true;
			
			cols++; //Since we actually want to draw an extra tree, we need to add an extra column
			treeImage = this.getGraphicsConfiguration().createCompatibleImage((int)(getWidth()*(double)currentTrees.size()/(double)(currentTrees.size()-1.0)), getHeight());
			Graphics2D treeGraphics = treeImage.createGraphics();
			paintTrees(treeGraphics, 0, 0, treeImage.getWidth(), treeImage.getHeight());
			cols--; //.. subtract off the extra column
			
			timer.start();
		}
		else {
			currentTrees.shrinkRight(1);
		}
		repaint();
	}
	

	/**
	 * Called by the timer while we're advancing (moving to the next tree)
	 * or retracting (moving to the previous tree)
	 */
	public void actionPerformed(ActionEvent arg0) {
		if (advancing) {
			calls++;
			if (advanceOffset>=0.99) {
				elapsedTime = 0;
				advanceOffset = 0;
				advancing = false;
				timer.stop();
				currentTrees.shrinkLeft(1);
				calls = 0;
				treeImage = null;
			}
			repaint();
		}
		
		if (retracting) {
			calls++;
			if (Math.abs(advanceOffset) > 0.99) {
				elapsedTime = 0;
				retracting = false;
				advanceOffset = 0;
				timer.stop();
				currentTrees.shrinkRight(1);
				calls=0;
			}
			repaint();
		}
	}

	/**
	 * Draws the trees at the specified x and y coordinates onto
	 * the provided graphics object
	 * @param g2d
	 * @param x
	 * @param y
	 */
	private void paintTrees(Graphics2D g2d, int x, int y, int width, int height) {
		int boxWidth = (int)(width/(double)cols);
		int boxHeight = (int)(height/(double)rows);
		Iterator<DrawableTree> treeit = currentTrees.iterator();
		int count = currentTrees.getLeftIndex();
		
		for(int row=0; row<rows; row++) {
			boundsRect.y = row*boxHeight;
			for(int col=0; col<cols; col++) {
				boundsRect.x = col*boxWidth;

				if (treeit.hasNext()) {
					DrawableTree tree = treeit.next();
					treeDrawer.setTree(tree);
					treeDrawer.initializeBoundaries(boundsRect, g2d);
					double twidth = (double)selectRect.width/(double)treeDrawer.getDrawingWidth(); //Used as width and height for translatedRect, which is used to select nodes
					double theight = selectRect.height/(double)treeDrawer.getDrawingHeight();

					if (boundsRect.intersects(selectRect) && mouseDrag) {
						double tx = treeDrawer.translatePixelToTreeX(selectRect.x);
						double ty = treeDrawer.translatePixelToTreeY(selectRect.y - boundsRect.y);
						translatedRect.setRect(tx, ty, twidth, theight);
						tree.setSelectedNodes(translatedRect);
					}
					
					treeDrawer.paint(g2d, boundsRect);
					
					if (drawTreeNumbers) {
						g2d.setFont(treeNumberFont);
						g2d.setColor(Color.GRAY);
						g2d.drawString(String.valueOf(count+1), boundsRect.x+4, boundsRect.y+boundsRect.height-5);
					}
					count++;
					
				}
			}//cols
		}//rows

	}
	
	public void paintComponent(Graphics g) {
			Graphics2D g2d = (Graphics2D)g;
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
	                RenderingHints.VALUE_ANTIALIAS_ON);
			//Rectangle treeSelectionBox = null;

			
			if (mouseDrag) {
				g2d.setColor(Color.white);
				g2d.fill(prevRect);
			}
			
			int boxWidth = (int)(getWidth()/(double)cols);
			int boxHeight = (int)(getHeight()/(double)rows);
			boundsRect.width = boxWidth;
			boundsRect.height = boxHeight;
			if (currentTrees == null) 
				return;
			
			if ((retracting || advancing) && rows ==1) { //We're in the middle of an advancing move, so ignore selection, etc..
				if (elapsedTime==0) {
					startTime = System.currentTimeMillis();
					elapsedTime = 1;
				}
				else
					elapsedTime = System.currentTimeMillis()-startTime;
				
				double fraction = (double)(elapsedTime)/(double)moveTime; 
				double oneMinusFraction = 1.0-fraction;
				advanceOffset = 1.0-oneMinusFraction*oneMinusFraction*oneMinusFraction;
				
				if (advanceOffset>1)
					advanceOffset = 1;
				
				boundsRect.y = 0;
 
				int x;
				if (advancing)
					x = (int)(boxWidth*(1-advanceOffset)-boxWidth);
				else 
					x = (int)(-boxWidth+advanceOffset*boxWidth );
				g2d.drawImage(treeImage, x, 0, null);
				
			}
			else { 
				paintTrees(g2d, 0, 0, getWidth(), getHeight());
			}//Not advancing or retracting, just draw trees normally			

			if (mouseDrag) {
				g2d.setColor(highlightSquareColor);
				g2d.fill(selectRect);
				g2d.setColor(Color.DARK_GRAY);
				g2d.drawRect(selectRect.x, selectRect.y, selectRect.width-1, selectRect.height-1);
				prevRect.x = selectRect.x;
				prevRect.y = selectRect.y;
				prevRect.width = selectRect.width;
				prevRect.height = selectRect.height;
			}

	}
	
	private void drawSelectionRectangle(Graphics2D g2d, Rectangle bounds) {
		g2d.setColor(Color.LIGHT_GRAY);
		g2d.drawRoundRect(bounds.x+1, bounds.y+1, bounds.width-2, bounds.height-2, 10, 10);
	}

	public void unselect() {
		selectedTree = null;
		mousePos.x = 0;
		mousePos.y = 0;
	}

	public void removeTrees(int num) {
		currentTrees.shrinkRight(num);
		repaint();
	}
	
	public void addTrees(int howmany) {
		int tot = currentTrees.extendRight(howmany);
		if (tot<howmany)
			currentTrees.extendLeft(howmany-tot);
	}
	
	public void addTreeLast() {
		currentTrees.extendRight(1);
	}
	
	public void addTreeFirst() {
		currentTrees.extendLeft(1);
	}
	
	public List<DrawableTree> getCurrentQueue() {
		ArrayList<DrawableTree> trees = new ArrayList<DrawableTree>();
		Iterator<DrawableTree> it = currentTrees.iterator();
		while(it.hasNext()) {
			trees.add(it.next());
		}
		return trees;
	}	
	
	/**
	 * Return the tree that is in some broad sense "underneath" the given point and return it. It may be null. 
	 * @param pos
	 * @return The tree corresponding to the given point
	 */
	private DrawableTree findSelectedTree(Point pos) {
		double boxWidth = this.getWidth()/(double)cols;
		double boxHeight = this.getHeight()/(double)rows;
				
		int c = (int) Math.floor((double)pos.getX() / boxWidth);
		int r = (int) Math.floor((double)pos.getY() / boxHeight);
		
		int treeIndex = r*cols + c;
		if (treeIndex < currentTrees.size()) {
			return currentTrees.get(treeIndex);
		}
		

		return null;
	}
	
	public void mousePressed(MouseEvent e) {
		mouseDrag = true;
		mouseBegin.width = e.getX();
		mouseBegin.height = e.getY();		
		selectedTree = findSelectedTree(e.getPoint());
	}

	public void mouseReleased(MouseEvent e) {
		mouseDrag = false;		
		repaint();
	}

	public void mouseDragged(MouseEvent e) {
		mouseEnd.width = e.getX();
		mouseEnd.height = e.getY();
		int fromX;
		int distX;
		if (mouseBegin.width > mouseEnd.width){
			fromX = mouseEnd.width;
			distX = mouseBegin.width - mouseEnd.width;
		}
		else {
			fromX = mouseBegin.width;
			distX = mouseEnd.width - mouseBegin.width;
		}
		
		int fromY; 
		int distY; 
		if (mouseBegin.height > mouseEnd.height){
			fromY = mouseEnd.height;
			distY = mouseBegin.height - mouseEnd.height;
		}
		else {
			fromY = mouseBegin.height;
			distY = mouseEnd.height - mouseBegin.height;
		}
		
		selectRect.x = fromX;
		selectRect.y = fromY;
		selectRect.width = distX;
		selectRect.height = distY;
		
		repaint();
	}

	public void mouseMoved(MouseEvent arg0) {	}
	
	public void mouseClicked(MouseEvent arg0) {
		selectRect.x = 0;
		selectRect.y = 0;
		selectRect.width = 0;
		selectRect.height = 0;
		Iterator<DrawableTree> treeIt = currentTrees.iterator();
		while(treeIt.hasNext()) {
			DrawableTree tree = treeIt.next();
			tree.unselectAllNodes();
		}
		mousePos.x = arg0.getX();
		mousePos.y = arg0.getY();
		repaint();
	}

	
	public void mouseEntered(MouseEvent arg0) {	}
	
	public void mouseExited(MouseEvent arg0) { 	}

	public void componentHidden(ComponentEvent arg0) {	}

	public void componentMoved(ComponentEvent arg0) {	}

	public void componentResized(ComponentEvent arg0) {	}

	public void componentShown(ComponentEvent arg0) {	}	
	
	class PopupListener extends MouseAdapter {
	    public void mousePressed(MouseEvent e) {
	        maybeShowPopup(e);
	    }

	    public void mouseReleased(MouseEvent e) {
	        maybeShowPopup(e);
	    }

	    private void maybeShowPopup(MouseEvent e) {
	        if (e.isPopupTrigger()) {
        		selectedTree = findSelectedTree(e.getPoint());
	        	if (selectedTree != null) {
	        		if (selectedTree.getScaleType() == DrawableTree.NO_SCALE_BAR) {
	        			showScaleBar.setSelected(false);
	        			showScaleAxis.setSelected(false);
	        		}
	        		
	        		if (selectedTree.getScaleType() == DrawableTree.SCALE_AXIS) {
	        			System.out.println("Selected tree scale type is AXIS");
	        			showScaleBar.setSelected(false);
	        			showScaleAxis.setSelected(true);
	        		}
	        		
	        		if (selectedTree.getScaleType() == DrawableTree.SCALE_BAR) {
	        			System.out.println("Selected tree scale type is BAR");
	        			showScaleBar.setSelected(true);
	        			showScaleAxis.setSelected(false);
	        		}
	        		
	        	}
	        	
	            popup.show(e.getComponent(),
	                       e.getX(), e.getY());
	        }
	    }
	}
	
	
	
	private JCheckBoxMenuItem showScaleBar;
	private JCheckBoxMenuItem showScaleAxis;


	
}
