package plugins.treePlugin.treeFigure;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;

import plugins.treePlugin.tree.DrawableTree;
import plugins.treePlugin.tree.DrawableTree.Direction;
import plugins.treePlugin.treeDisplay.TreeDisplay;

import element.DoubleRectangle;
import figure.Figure;
import figure.FigureElement;

/**
 * A subclass of Figure that allows for drawing a tree and a scale axis or bar. Trees
 * and the scale elements are represented by independent figure elements. 
 * @author brendan
 *
 */
public class TreeFigure extends Figure {

	int tipLabelType = 1;
	public static final int NO_TIP_LABELS = 0;
	public static final int TIP_LABELS_FROM_TREE = 1;
	public static final int DEPTH_TIP_LABELS = 2;
	public static final int HEIGHT_TIP_LABELS = 3;
	
	
	boolean mouseDrag = false;


	Rectangle2D translatedRect;
	TreeDisplay display;
	Color cartoonColor;
	
	Color errorBarColor = Color.LIGHT_GRAY;
	
	NumberFormat formatter = new DecimalFormat("0.0###");

	java.awt.Point workingPoint;
	Rectangle boundsRect; //Should always store 0, 0, getWidth() getHeight()
	
	List<TreeElement> treeElements = new ArrayList<TreeElement>();
	
	//A listing of all scale elements and their associated TreeElements. Not all TreeElements
	//will have a scale element.
	Map<TreeElement, FigureElement> scaleElements = new HashMap<TreeElement, FigureElement>();
	
	int currentScaleType = DrawableTree.NO_SCALE_BAR;
	
	JPopupMenu popup;
	java.awt.Point lastPopupPosition = new java.awt.Point(0,0);
	
	public TreeFigure(TreeDisplay display) {
		addComponentListener(this);

		setRectangleSelection(true); 	//Turn on rectangle selection
		translatedRect = new Rectangle2D.Double(0, 0, 0, 0);
		this.display = display;
		cartoonColor = Color.red;
		workingPoint = new java.awt.Point(0, 0);

		boundsRect = new Rectangle(0, 0, getWidth(), getHeight());				
		constructPopup();
		
		addMouseListener(new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent e) {
				for(TreeElement treeL : treeElements) {
					treeL.getTree().unselectAllNodes();
				}
				repaint();
			}

			public void mousePressed(MouseEvent e) {	}

			public void mouseReleased(MouseEvent e) {	}

			public void mouseEntered(MouseEvent e) {	}

			public void mouseExited(MouseEvent e) {		}
		});
	}
	
	private void constructPopup() {
		popup = new JPopupMenu();
		popup.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY) );
		popup.setBackground(new Color(100,100,100) );
		JMenuItem popupItemSaveImage = new JMenuItem("Save image");
		popupItemSaveImage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
            	display.saveButtonPressed();
            }
        });
		popup.add(popupItemSaveImage);
		
		JMenuItem popupItemSaveNewick = new JMenuItem("Save newick");
		popupItemSaveNewick.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
            	display.saveNewickButtonPressed();
            }
        });
		popup.add(popupItemSaveNewick);
		

		JMenuItem popupItemRemove = new JMenuItem("Remove tree");
		popupItemRemove.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
            	display.removeButtonPressed();
            }
        });
		popup.add(popupItemRemove);
		
		
		JMenu orientMenu = new JMenu("Orientation");
		popup.add(orientMenu);
		
		
		ButtonGroup bg = new ButtonGroup();
		OrientationListener oListen = new OrientationListener();
		JRadioButtonMenuItem oRight = new JRadioButtonMenuItem("Right");
		oRight.addActionListener(oListen);
		oRight.setSelected(true);
		orientMenu.add(oRight);
		oRight.setActionCommand("Right");
		bg.add(oRight);
		JRadioButtonMenuItem oLeft = new JRadioButtonMenuItem("Left");
		oLeft.addActionListener(oListen);
		orientMenu.add(oLeft);
		oLeft.setActionCommand("Left");
		bg.add(oLeft);
		JRadioButtonMenuItem oUp = new JRadioButtonMenuItem("Up");
		oUp.addActionListener(oListen);
		oUp.setActionCommand("Up");
		orientMenu.add(oUp);
		bg.add(oUp);
		JRadioButtonMenuItem oDown = new JRadioButtonMenuItem("Down");
		oDown.addActionListener(oListen);
		oDown.setActionCommand("Down");
		orientMenu.add(oDown);
		bg.add(oDown);
		
		PopupListener popListener = new PopupListener();
		this.addMouseListener(popListener);
	}

	public void addTree(DrawableTree newTree) {
		TreeElement newElement = new TreeElement(this);
		newTree.setScaleType(DrawableTree.NO_SCALE_BAR);
		newElement.setScale(getWidth(), getHeight(), null);
		newElement.setTree(newTree);
		treeElements.add(newElement);
		super.addElement(newElement);
		
		
		FigureElement scaleElement = null;
		if (currentScaleType == DrawableTree.SCALE_AXIS) {
			scaleElement = new ScaleAxisElement(this);

			scaleElement.setZPosition(-5); //draw underneath the tree so grid lines aren't on top
			((ScaleAxisElement)scaleElement).setTreeDrawer(newElement.getTreeDrawer());
			addElement(scaleElement);
			scaleElements.put(newElement, scaleElement);			
		}
		
		if (currentScaleType == DrawableTree.SCALE_BAR) {
			scaleElement = new ScaleBarElement(this);
			scaleElement.setMobile(true);
			((ScaleBarElement)scaleElement).setTreeDrawer(newElement.getTreeDrawer());
			addElement(scaleElement);
			scaleElements.put(newElement, scaleElement);			
		}
		
		layoutTrees();
		
		if (currentScaleType == DrawableTree.SCALE_AXIS && scaleElement != null) {
			scaleElement.setBounds(newElement.getX(), 0.9, 0.1, 0.01); //Width and height parts are ignored
		}
		
		if (currentScaleType == DrawableTree.SCALE_BAR && scaleElement != null) {
			scaleElement.setBounds(Math.max(0,newElement.getX()+newElement.getWidth()/2.0-0.05), 0.9, 0.1, 0.1); //Width and height parts are ignored
		}
	}
	
	/**
	 * Remove the tree element and associated scale element from this figure.
	 * @param treeToRemove
	 */
	public void removeTree(TreeElement treeToRemove) {
		FigureElement scale = scaleElements.get(treeToRemove);
		if (scale!=null) {
			scaleElements.remove(treeToRemove);
			super.removeElement(scale);
		}
		super.removeElement(treeToRemove);
		treeElements.remove(treeToRemove);
		layoutTrees();
	}
	
	/**
	 * Remove all trees from this figure. 
	 */
	public void removeAllTrees() {
		for(FigureElement figEl : treeElements) {
			super.removeElement(figEl);
			FigureElement scale = scaleElements.get(figEl);
			if (scale!=null)
				super.removeElement(scale);
		}
		treeElements.clear();
		scaleElements.clear();
	}
	
	/**
	 * Called to set the appropriate bounds for all trees. For now we assume they are simply laid out
	 * left-to-right, but this would be easy to change sometime
	 */
	private void layoutTrees() {
		double x = 0;
		double xPadding = 0.02;
		double treeWidth = 1.0/treeElements.size()-xPadding;
		for(int i=0; i<treeElements.size(); i++) {
			treeElements.get(i).setScale(getWidth(), getHeight(), null);
			FigureElement scaleElement = scaleElements.get(treeElements.get(i));
			if (scaleElement==null) { 
				treeElements.get(i).setBounds(x, 0, treeWidth, 1.0);
			}
			else {
				treeElements.get(i).setBounds(x, 0, 1.0/treeElements.size(), 0.9);
				
				if (scaleElement instanceof ScaleAxisElement) {
					scaleElement.setBounds(treeElements.get(i).getX(), 0.9, treeElements.get(i).getWidth(), 0.1);
				}
				if (scaleElement instanceof ScaleBarElement) {
					scaleElement.setBounds(treeElements.get(i).getX()+treeElements.get(i).getWidth()/2.0, 0.9, 0.1, 0.1);
				}
					
			}
			x+= treeWidth+xPadding;
			//System.out.println("Tree : " + i + " x: " + treeElements.get(i).getX() + " width: " + treeElements.get(i).getWidth());
		}
	}
	
	/**
	 * Remove the scale element associated with the given tree element
	 * @param tel
	 */
	private void removeScaleForTree(TreeElement tel) {
		FigureElement el = scaleElements.get(tel);
		if (el!=null) {
			scaleElements.remove(tel);
			super.removeElement(el);
		}
	}
	
	/**
	 * Note that there is a bit of disagreement regarding what controls the 'scale bar' and 
	 * 'scale axis' properties. In MultiTreeDisplays, this is a property of the tree. Here,
	 * since we use the Figure interface, the ScaleBar and ScaleAxis are a type of FigureElement,
	 * and TreeFigure controls which one (or none) is shown.  
	 * 
	 * @param type
	 */
	public void setScaleType(int type) {
		currentScaleType = type;
		//Remove all scale elements first
		for(TreeElement treeElement : treeElements) {
			removeScaleForTree(treeElement);
		}
		
		for(TreeElement treeElement : treeElements) {
			double x = treeElement.getX();
			double y = treeElement.getY();
			double width = treeElement.getWidth();

			if (type == DrawableTree.SCALE_BAR) {
				ScaleBarElement scaleElement = new ScaleBarElement(this);

				scaleElement.setBounds(Math.max(0,x+width/2.0-0.05), 0.9, 0.1, 0.1); //Width and height parts are ignored
				scaleElement.setMobile(true);
				scaleElement.setTreeDrawer(treeElement.getTreeDrawer());
				treeElement.setBounds(x, y, width, 0.9);
				addElement(scaleElement);
				scaleElements.put(treeElement, scaleElement);
			}

			if (type == DrawableTree.SCALE_AXIS) {
				ScaleAxisElement scaleElement = new ScaleAxisElement(this);

				scaleElement.setZPosition(-5); //draw underneath the tree so grid lines aren't on top
				scaleElement.setBounds(x, 0.9, width, 0.075); //Width and height parts are ignored
				scaleElement.setTreeDrawer(treeElement.getTreeDrawer());
				treeElement.setBounds(x, y, width, 0.9);
				addElement(scaleElement);
				scaleElements.put(treeElement, scaleElement);
			}	

		}
	}


	/**
	 * Called when the selection rectangle has changed, happens repeatedly when user
	 * drags the selection rectangle
	 * @param selectRect2
	 */
	public void selectionRectUpdated(Rectangle selRect) {
		boolean anythingIsSelected = false;
		for(TreeElement treeElement : treeElements) {
			double tx = treeElement.getTreeDrawer().translatePixelToTreeX(selRect.x);
			double ty = treeElement.getTreeDrawer().translatePixelToTreeY(selRect.y);
			double twidth = ((double)selRect.width/(double)treeElement.getDrawingWidth());
			double theight = ((double)selRect.height/(double)treeElement.getDrawingHeight());

			translatedRect.setRect(tx, ty, twidth, theight);		
			boolean selected = treeElement.getTree().setSelectedNodes(translatedRect);
			anythingIsSelected = selected || anythingIsSelected;
		}
		display.setSomethingIsSelected(anythingIsSelected);
	}
	
	/**
	 * Set whether or not error bars are drawn
	 * @param showErrorBars
	 */
	public void setShowErrorBars(boolean showErrorBars) {
		for(TreeElement treeElement : treeElements) {
			treeElement.setShowErrorBars(showErrorBars);
		}
	}
	
	/**
	 * Returns a list of all drawable trees currently being handled by this figure.
	 * @return
	 */
	public List<DrawableTree> getTrees() {
		List<DrawableTree> trees = new ArrayList<DrawableTree>();
		for(TreeElement treeElement : treeElements) {
			trees.add(treeElement.getTree());
		}
		return trees;
	}
	
	public void setSelectionMode(int mode) {
		for(TreeElement treeElement : treeElements) {
			treeElement.getTree().setSelectionMode(mode);
		}
	}
	
	public void setTreeOrientation(Direction orientation) {
		for(TreeElement treeElement : treeElements) {
			treeElement.getTree().setOrientation(orientation);
			treeElement.setDrawBounds();
			treeElement.setScale(getWidth(), getHeight(), null);
		}
		repaint();
	}

	/**
	 * Removes the tree element under the popup position, if it exists. 
	 */
	public void removeTreeAtPopup() {
		FigureElement el = getElementForPosition(lastPopupPosition);
		if (el!=null) {
			if (el instanceof TreeElement) {
				removeTree( (TreeElement)el);
				repaint();
			}
		}
	}


	public void componentResized(ComponentEvent arg0) {
		boundsRect.width = getWidth();
		boundsRect.height = getHeight();

		for(TreeElement treeElement : treeElements) {
			treeElement.setDrawBounds();
		}
		
		super.componentResized(arg0);

		repaint();
	}

	public void mouseEntered(MouseEvent e) { }

	public void mouseExited(MouseEvent e) {	}

	public void mouseMoved(MouseEvent arg0) {	}

	
	/**
	 * The orientation radio button group uses this
	 * @author brendan
	 *
	 */
	class OrientationListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			
			//System.out.println("Popup pos is: " + lastPopupPosition.x + " pos y: " + lastPopupPosition.y);
			FigureElement el = getElementForPosition(lastPopupPosition);
			
			if (el instanceof TreeElement) {
				DrawableTree tree = ((TreeElement)el).getTree();
				//DoubleRectangle origBounds = el.getBounds();
				if (e.getActionCommand().contains("Up") ) {
					tree.setOrientation(Direction.UP);
				}
				if (e.getActionCommand().contains("Down") ) {
					tree.setOrientation(Direction.DOWN);
				}
				if (e.getActionCommand().contains("Left") ) {
					tree.setOrientation(Direction.LEFT);
				}
				if (e.getActionCommand().contains("Right") ) {
					tree.setOrientation(Direction.RIGHT);
				}
				//el.setBounds(origBounds.x, origBounds.y, origBounds.width, origBounds.height); 
				repaint();
			}
		}
	}
	
	private class PopupListener extends MouseAdapter {
	    public void mousePressed(MouseEvent e) {
	        maybeShowPopup(e);
	    }

	    public void mouseReleased(MouseEvent e) {
	        maybeShowPopup(e);
	    }

	    private void maybeShowPopup(MouseEvent e) {
	        if (e.isPopupTrigger()) {
	            popup.show(e.getComponent(),
	                       e.getX(), e.getY());
	            lastPopupPosition.x = e.getX();
	            lastPopupPosition.y = e.getY();
	        }
	    }
	}

}
