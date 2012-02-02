package plugins.treePlugin.treeFigure;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import plugins.treePlugin.tree.drawing.TreeDrawer;

import figure.Figure;
import guiWidgets.StringUtilities;

/**
 * A element that paints a scale bar somewhere on the tree. 
 * 
 * @author brendan
 *
 */
public class ScaleBarElement extends figure.FigureElement {

	TreeDrawer treeDrawer;
	private boolean scaleBarCalculated;
	private String scaleBarLabel;
	int scaleBarPixels = 100; //The (horizontal) length of the bar (in pixel) to draw
	
	Font scaleLabelFont;
	Font selectedFont;
	
	Stroke highlightStroke = new BasicStroke(3.0f);
	Stroke normalStroke = new BasicStroke(1.0f);
	
	
	public ScaleBarElement(Figure parent) {
		super(parent);
		scaleLabelFont = new Font("Sans", Font.PLAIN, 11);
		selectedFont = new Font("Sans", Font.BOLD, 11);
	}


	public void setTreeDrawer(TreeDrawer td) {
		this.treeDrawer = td;
	}
	
	public void paint(Graphics2D g2d) {
		xFactor = parent.getWidth();	//Kind of a hack to have this here... but since these objects are changed
		yFactor = parent.getHeight();   //on the fly they aren't aware of usual rescale() calls
		
		
		if (! scaleBarCalculated ) {
			double branchScale = parent.getWidth()-treeDrawer.getLeftPadding()-treeDrawer.getRightPadding()-treeDrawer.getLabelSpace();
			scaleBarPixels = (int)Math.round(branchScale / 10);
			if (scaleBarPixels < 50)
				scaleBarPixels = 50;
			if (scaleBarPixels > branchScale) 
				scaleBarPixels = (int)branchScale;
			
			double fraction = scaleBarPixels / branchScale; //The fraction of the  total tree drawing height subtended by 100 pixels
			double heightFraction = fraction * treeDrawer.getTreeMaxHeight();

			scaleBarCalculated = true;
			scaleBarLabel = StringUtilities.format(heightFraction);
		}
		
		
		

		int labelWidth = (g2d.getFontMetrics().stringWidth(scaleBarLabel));
		
		int leftEdge = (int)Math.round(bounds.x*xFactor); // treeDrawer.getBoundaries().x + parent.getWidth()/2-30; 
		int top = (int)Math.round(bounds.y*yFactor);
		
		if (isSelected()) {
			g2d.setStroke(highlightStroke);
			g2d.setColor(highlightColor);
			g2d.drawLine(leftEdge, top, leftEdge+scaleBarPixels, top); //upper crossbar
			g2d.drawLine(leftEdge, top, leftEdge, top+5);
			g2d.drawLine(leftEdge+scaleBarPixels, top, leftEdge+scaleBarPixels, top+5);
			g2d.setFont(selectedFont);
		}
		else {
			g2d.setFont(scaleLabelFont);
		}
		
		
		g2d.setStroke(normalStroke);
		g2d.setColor(Color.black);
		g2d.drawLine(leftEdge, top, leftEdge+scaleBarPixels, top); //upper crossbar
		g2d.drawLine(leftEdge, top, leftEdge, top+5);
		g2d.drawLine(leftEdge+scaleBarPixels, top, leftEdge+scaleBarPixels, top+5);
		
		int rightEdge = leftEdge + scaleBarPixels;
		g2d.drawString(scaleBarLabel, round((leftEdge+rightEdge)/2.0-labelWidth/2-4), top+12);
		
	}

}
