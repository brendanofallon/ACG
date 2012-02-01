package plugins.treePlugin.treeFigure;

import java.awt.Graphics2D;
import java.awt.Rectangle;

import plugins.treePlugin.tree.DrawableTree;
import plugins.treePlugin.tree.drawing.TreeDrawer;

import figure.Figure;
import figure.FigureElement;


/**
 * Draws a tree, FigureElement-style. Basically just a wrapper for a tree drawer, and many
 * methods here are delegated directly to the tree drawer.
 * @author brendan
 *
 */
public class TreeElement extends FigureElement {

	TreeDrawer treeDrawer;
	Rectangle pixelBounds;

	public TreeElement(Figure parent) {
		super(parent);
		treeDrawer = new TreeDrawer();
//		treeDrawer.setDrawScaleAxis(false);
//		treeDrawer.setDrawScaleBar(false);
//		treeDrawer.setDrawYGrid(false);
		treeDrawer.setClearBeforeDrawing(false);
		pixelBounds = new Rectangle(0, 0, 0, 0);
	}

	public void setTree(DrawableTree tree) {
		treeDrawer.setTree(tree);
	}

	public DrawableTree getTree() {
		return treeDrawer.getTree();
	}
	
	/**
	 * Mouse clicks can ge 'though' trees to underlying objects. This is because scaleAxisElements must
	 * be underneath the tree so their grid lines aren't on top, but oftentimes their bounds overlap
	 * the tree bounds, meaning we can't select the axis if we want to. 
	 * @return
	 */
	public boolean consumesMouseClick() {
		return false;
	}
	
	public void makeSpaceForScaleBar() {
		treeDrawer.makeSpaceForScaleBar();
		parent.repaint();
	}	
	
	public void removeScaleBarSpace() {
		treeDrawer.removeScaleBarSpace();
		parent.repaint();
	}

	
	/**
	 * Potential for a lot of confusion here regarding 'bounds' . In FigureElements, bounds describes
	 * the rectangle boundaries of an object in 0..1, 0..1 space. But A treeDrawer needs the bounds
	 * in pixel (component) space. 
	 */
	public void paint(Graphics2D g) {
		//System.out.println("painting tree, xFactor: " + xFactor + " bounds x: " + bounds.x + " bounds width: " + bounds.width);
		pixelBounds.x = round(xFactor*getX());
		pixelBounds.y = round(yFactor*getY());
		pixelBounds.width =round(bounds.width*xFactor);
		pixelBounds.height = round(bounds.height*yFactor);
		//drawBorder(g);
		treeDrawer.paint(g, pixelBounds);
	}

	public void setShowErrorBars(boolean showErrorBars) {
		treeDrawer.setShowErrorBars(showErrorBars);
	}

	public int getDrawingHeight() {
		return treeDrawer.getDrawingHeight();
	}
	
	public int getDrawingWidth() {
		return treeDrawer.getDrawingWidth();
	}
	
	public void setDrawBounds() {
		treeDrawer.setRecalculateBounds(true);
	}

	public int getTopPadding() {
		return treeDrawer.getTopPadding();
	}

	public int getBottomPadding() {
		return treeDrawer.getBottomPadding();
	}

	public int getLeftPadding() {
		return treeDrawer.getLeftPadding();
	}

	public int getRightPadding() {
		return treeDrawer.getRightPadding();
	}

	public int getLabelSpace() {
		return treeDrawer.getLabelSpace();
	}

	public TreeDrawer getTreeDrawer() {
		return treeDrawer;
	}


}
