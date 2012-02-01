package gui.figure.treeFigure;

import gui.figure.treeFigure.drawing.BranchPainter;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * A Node that knows it's position relative to the root and a few things about
 * how to draw itself.. right now it is permissible to have Node offspring that are not
 * DrawableNodes, but this probably leads to a big performance hit since anytime we need
 * to traverse a drawabletree we have to cast all nodes to drawable nodes for every operation, 
 * which is potentially expensive. A faster method may just be to enfore drawable trees and drawable
 * nodes to only contain drawablenodes, and forget all the casting
 * 
 * @author brendan
 *
 */
public class DrawableNode extends Node {
	
	Point pos;
	boolean selected;
	Color branchColor;
	BasicStroke branchStroke;
	Font font;
	String labelPosition;
	String currentLabel = "";
	boolean collapse = false;
	
	HashMap<String, String> annotations;
	
	boolean hasErrorBars = false;


	double errorBarLength = 0;
	
	BranchPainter branchPainter = null;


	public DrawableNode() {
		pos = new Point(0, 0);
		selected = false;
		branchColor = Color.black;
		font = new Font("Sans", Font.PLAIN, 11);
		branchStroke = new BasicStroke(1.0f);
		annotations = new HashMap<String, String>();
		currentLabel = "";
		labelPosition = TreeFigure.RIGHT_POSITION;
		
	}

	
	protected DrawableNode(Point pos, boolean sel, Color col, BasicStroke strk, boolean coll, Font font) {
		this.pos = pos;
		this.selected = sel;
		this.branchColor = col;
		this.branchStroke = strk;
		this.collapse = coll;
		this.font = font;
		annotations = new HashMap<String, String>();
	}

	public boolean hasErrorBars() {
		return hasErrorBars;
	}


	public void setHasErrorBars(boolean hasErrorBars) {
		this.hasErrorBars = hasErrorBars;
	}
	
	public double getErrorBarLength() {
		return errorBarLength;
	}
	
	public void setErrorBarLength(double length) {
		errorBarLength = length;
	}
	
	public String getCurrentLabel() {
		return currentLabel;
	}
	
	public Font getFont() {
		return font;
	}
	
	public void setFontSize(float size) {
		font = font.deriveFont(size);
	}
	
	public void setFontStyle(int style) {
		font = font.deriveFont(style);
	}
	
	public void setFontFace(String face) {
		int currentSize = font.getSize();
		int style = font.getStyle();
		font = new Font(face, style, currentSize);
	}
	
	
	public BranchPainter getBranchPainter() {
		return branchPainter;
	}


	public void setBranchPainter(BranchPainter branchPainter) {
		this.branchPainter = branchPainter;
	}
	
	/**
	 * This cloner adds clones of all this nodes descendants, but not the parent;
	 * @return
	 */
	public DrawableNode cloneWithDescendants() {
		Font newFont = font.deriveFont((float)font.getSize());
		DrawableNode newnode = new DrawableNode(new Point(pos.x, pos.y),
												selected,
												branchColor,
												new BasicStroke(1.0f),
												collapse,
												newFont);
//		if (parent!=null) {
//			DrawableNode newParent = new DrawableNode();
//			newParent.setLabel(parent.getLabel());
//			newnode.setDistToParent(distToParent);
//			newnode.setParent(newParent);
//		}

		newnode.setLabel(label);
		newnode.setCurrentLabel(currentLabel);
		if (annotations != null)
			newnode.setAllAnnotations( annotations );

		for(Node kid : getOffspring()) {
			Node clonedKid = ((DrawableNode)kid).cloneWithDescendants();
			newnode.addOffspring( clonedKid );
			clonedKid.setParent(newnode);
			clonedKid.setDistToParent(kid.getDistToParent());

		}
		return newnode;
	}

	
	
	//////////// Annotation Stuff //////////////////////////

	public boolean addAnnotation(String key, String value) {
        boolean alreadyHas = annotations.containsKey(key);
		annotations.put(key, value);
        return alreadyHas;
	}

    public String getAnnotationValue(String key) {
		return (String)annotations.get(key);
	}

    public boolean hasAnnotations() {
        return annotations.size()>0;
    }

    public Set<String> getAnnotationKeys() {
        return annotations.keySet();
    }

    
	public boolean containsAnnotationKey(String key) {
		return annotations.containsKey(key);
	}
	

	
	public void setAllAnnotations(Map<String, String> newAnnotations) {
		annotations.clear();
		for(String key : newAnnotations.keySet()) {
			annotations.put(key, newAnnotations.get(key));
		}
	}
	
	public void removeAnnotation(String key) {
		annotations.remove(key);
	}
	
	public Map<String, String> getAnnotations() {
		return annotations;
	}
	
	
	
	
	
	
	public boolean isCollapsed() {
		return collapse;
	}


	public void setCollapse(boolean collapse) {
		this.collapse = collapse;
	}

	
	public void setLineWidth(float width) {
		branchStroke = new BasicStroke(width);
	}
	
	public void setBranchColor(Color c) {
		branchColor = c;
	}
	
	public boolean isSelected() {
		return selected;
	}


	public void setSelected(boolean selected) {
		this.selected = selected;
	}


	public void setLabel(String label) {
		this.label = label;
		addAnnotation("Tip", label);
	}
	
	public void setX(double newX) {
		pos.setLocation(newX, pos.getY());
	}
	
	public void setY(double newY) {
		pos.setLocation(pos.getX(), newY);
	}
	
	public double getX() {
		return pos.x;
	}
	
	public double getY() {
		return pos.y;
	}
	
	public Point getPosition() {
		return new Point(pos.x, pos.y);
	}


	public void setPosition(Point position) {
		pos = position;
	}


	public Color getColor() {
		return branchColor;
	}
	
	public Stroke getStroke() {
		return branchStroke;
	}


	public void setPosition(double x, double y) {
		pos.setLocation(x, y);
	}


	public void setCurrentLabel(String label) {
		currentLabel = label;
	}


	/**
	 * We allow any string here, whatever is setting the position should 
	 * know how to deal with it.
	 * @param labelPosition
	 */
	public void setLabelPosition(String labelPosition) {
		this.labelPosition = labelPosition;
	}
	
	public String getLabelPosition() {
		return labelPosition;
	}
	
}
