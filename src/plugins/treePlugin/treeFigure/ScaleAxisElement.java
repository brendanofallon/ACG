package plugins.treePlugin.treeFigure;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import plugins.treePlugin.tree.DrawableTree;
import plugins.treePlugin.tree.drawing.TreeDrawer;
import plugins.treePlugin.treeFigure.ScaleAxisConfigFrame.ScaleAxisOptions;

import figure.Figure;
import guiWidgets.StringUtilities;

/**
 * A element that paints a scale axis at the bottom of a tree. 
 * 
 * @author brendan
 *
 */
public class ScaleAxisElement extends figure.FigureElement {
	
	Font scaleLabelFont;
	TreeDrawer treeDrawer;

	private int scaleAxisTicks = 6;
	private boolean useCustomDistance = false;
	private double tickDistance = -1;
	
	Stroke highlightStroke;
	Stroke normalStroke;
	
	ScaleAxisConfigFrame configFrame;
	Color normalColor = Color.black;
	
	boolean drawGridLines = false;
	
	Color gridColor = Color.LIGHT_GRAY;
	
	boolean reverse = false;
	
	public ScaleAxisElement(Figure parent) {
		super(parent);
		scaleLabelFont = new Font("Sans", Font.PLAIN, 11);
		highlightStroke = new BasicStroke(3.0f);
		normalStroke = new BasicStroke(1.0f);
		bounds.width = 0.95;
		bounds.height = 0.10;
		canConfigure = true;
		configFrame = new ScaleAxisConfigFrame(this);
	}

	public void popupConfigureTool(java.awt.Point pos) {
		ScaleAxisConfigFrame.ScaleAxisOptions ops = configFrame.getOptions();
		ops.numberOfTicks = scaleAxisTicks;
		ops.reverse = reverse;
		configFrame.display(ops);
	}

	public void setTreeDrawer(TreeDrawer td) {
		this.treeDrawer = td;	
	}
	
	private void drawHorizontalAxis(Graphics2D g2d, int drawWidth, int leftEdge, int topEdge, int bottomDist) {
		int ticks = scaleAxisTicks;
		double maxHeight = treeDrawer.getTreeMaxHeight();
		
		double tickStep;
		if (useCustomDistance)
			tickStep = tickDistance; 
		else
			tickStep = maxHeight/(double)(ticks-1.0);
		
		g2d.drawLine(leftEdge, topEdge, leftEdge+drawWidth, topEdge);

		double tickX = 0;
		while(tickX <= maxHeight) {
			
			int pixX;
			if (reverse)
				pixX = treeDrawer.translateTreeToPixelX(tickX/maxHeight);
			else 
				pixX = treeDrawer.translateTreeToPixelX((maxHeight-tickX)/maxHeight);
			
			if (drawGridLines) {
				g2d.setColor(gridColor);
				g2d.drawLine(pixX, 1, pixX, topEdge);
				g2d.setColor(normalColor);
			}

			g2d.drawLine(pixX, topEdge, pixX, topEdge+5);			
			
			tickX += tickStep;
		}
		
//		for(double i=0; i<ticks; i++) {
//			if (drawGridLines) {
//				g2d.setColor(gridColor);
//				g2d.drawLine((int)Math.round(leftEdge+i*tickStep), 1, (int)Math.round(leftEdge+i*tickStep), topEdge);
//				g2d.setColor(normalColor);
//			}
//
//			g2d.drawLine((int)Math.round(leftEdge+i*tickStep), topEdge, leftEdge+(int)Math.round(i*tickStep), topEdge+5);			
//		}
		
		
//		if (drawGridLines) {
//			g2d.setColor(gridColor);
//			g2d.drawLine(leftEdge+drawWidth, 1, leftEdge+drawWidth, topEdge);
//			g2d.setColor(normalColor);
//		}
//
//		g2d.drawLine(leftEdge+drawWidth, topEdge, leftEdge+drawWidth, topEdge+5);
	}
	
	protected void drawHorizontalLabels(Graphics2D g2d, int drawWidth, int leftEdge, int topEdge, int bottomDist) {
		String label;
		double maxHeight = treeDrawer.getTreeMaxHeight();
		
		//int ticks = scaleAxisTicks;
		
		double tickStep;
		if (useCustomDistance)
			tickStep = tickDistance; 
		else
			tickStep = maxHeight/((double)scaleAxisTicks-1.0);
		
		double tickX = 0;
		while(tickX <= maxHeight) {
			int pixX;
			if (reverse) {
				pixX = treeDrawer.translateTreeToPixelX(tickX/maxHeight);
				label = StringUtilities.format(tickX);
			}
			else { 
				pixX = treeDrawer.translateTreeToPixelX((maxHeight-tickX)/maxHeight);
				label = StringUtilities.format(tickX);
			}
			
			
			int width = g2d.getFontMetrics().stringWidth(label);
			g2d.drawString(label, Math.max(1, pixX-width/2), topEdge+20);			
			
			tickX += tickStep;
		}

//		if (! reverse)
//			label = "0.0"; 
//		else
//			label = StringUtilities.format(treeDrawer.getTreeMaxHeight()*ticks*tickStep/drawWidth);
//
//		int width = g2d.getFontMetrics().stringWidth(label);
//		g2d.drawString(label, leftEdge+(int)Math.round(ticks*tickStep-width/1.55), topEdge+20);		
	}
	
	
	private void drawVerticalAxis(Graphics2D g2d, int drawHeight, int leftEdge, int topEdge, int bottomEdge) {
		
		double maxHeight = treeDrawer.getTreeMaxHeight();
		
		double tickStep;
		if (useCustomDistance) 
			tickStep = tickDistance;
		else
			tickStep = maxHeight / (double)(scaleAxisTicks-1.0); 
		
		g2d.drawLine(leftEdge, topEdge, leftEdge, bottomEdge); //Main vertical line
		//System.out.println("Drawing line from " + leftEdge + ", " + topEdge + " to bottom edge : " + bottomEdge);
		double tickY = 0; //Cumulative distance in tree units
		
		while(tickY <= maxHeight) {
			int pixY;
			if (reverse)
				pixY = treeDrawer.translateTreeToPixelY(tickY/maxHeight); //Y coord in pixels
			else
				pixY = treeDrawer.translateTreeToPixelY((maxHeight-tickY)/maxHeight); //Y coord in pixels
			
			if (drawGridLines) {
				g2d.setColor(gridColor);
				g2d.drawLine(leftEdge, pixY, treeDrawer.getDrawingWidth()+leftEdge, pixY);
				g2d.setColor(normalColor);
			}
			
			g2d.drawLine(leftEdge-5,  pixY, leftEdge, pixY);
			tickY += tickStep;
		}
		
	}
	
	
	protected void drawVerticalLabels(Graphics2D g2d, int drawWidth, int leftEdge, int topEdge, int bottomEdge) {
		String label;
		double maxHeight = treeDrawer.getTreeMaxHeight();
		
		double tickStep;
		if (useCustomDistance) 
			tickStep = tickDistance;
		else
			tickStep = maxHeight / (double)(scaleAxisTicks-1.0);
		
		double tickY = 0;
		while(tickY <= maxHeight) {
			int pixY;
			if (reverse)
				pixY = treeDrawer.translateTreeToPixelY(tickY/maxHeight); //Y coord in pixels
			else
				pixY = treeDrawer.translateTreeToPixelY((maxHeight-tickY)/maxHeight); //Y coord in pixels
			
	
			label = StringUtilities.format(tickY);
			int width = g2d.getFontMetrics().stringWidth(label);
			g2d.drawString(label, leftEdge-5-width, pixY+5 );
			tickY += tickStep;
		}

	}
	
	public void paint(Graphics2D g2d) {
		xFactor = parent.getWidth();	//Kind of a hack to have this here... but since these objects are changed
		yFactor = parent.getHeight();   //on the fly they aren't aware of usual rescale() calls
		

		g2d.setFont(scaleLabelFont);
		g2d.setColor(Color.black);
		
		if (treeDrawer.getTree().getOrientation()==DrawableTree.Direction.RIGHT || treeDrawer.getTree().getOrientation()==DrawableTree.Direction.LEFT) {
				
			int drawWidth = treeDrawer.getDrawingWidth();
			//double tickStep = drawWidth/ticks;

			int bottomDist = 25;
			
			int leftEdge = treeDrawer.translateTreeToPixelX(0);
			int topEdge = treeDrawer.translateTreeToPixelY(1.1);
			
			this.bounds.x = leftEdge / xFactor;
			this.bounds.y = topEdge / yFactor;
			this.bounds.width = drawWidth / xFactor;
			this.bounds.height = 10.0 / yFactor;

			if (isSelected()) {
				g2d.setStroke(highlightStroke);
				g2d.setColor(highlightColor);
				drawHorizontalAxis(g2d, drawWidth, leftEdge, topEdge, bottomDist);
				g2d.setStroke(normalStroke);
				g2d.setColor(Color.black);
			}
			
			drawHorizontalAxis(g2d, drawWidth, leftEdge, topEdge, bottomDist);
			drawHorizontalLabels(g2d, drawWidth, leftEdge, topEdge, bottomDist);

		}
		else { //Orientation is vertical 
			int leftEdge = Math.max(10, treeDrawer.getLeftPadding()-10);
			int drawHeight = treeDrawer.getDrawingHeight();
			
			this.bounds.x = (leftEdge-20.0)/xFactor;
			this.bounds.y = treeDrawer.translateTreeToPixelY(0)/yFactor;
			this.bounds.width = 20.0/xFactor;
			this.bounds.height = (double)drawHeight / (double)yFactor;
			
			if (isSelected()) {
				g2d.setStroke(highlightStroke);
				g2d.setColor(highlightColor);
				drawVerticalAxis(g2d, drawHeight, leftEdge, treeDrawer.translateTreeToPixelY(0), treeDrawer.translateTreeToPixelY(1.0));
				g2d.setStroke(normalStroke);
				g2d.setColor(Color.black);
			}

			
			
			drawVerticalAxis(g2d, drawHeight, leftEdge, treeDrawer.translateTreeToPixelY(0), treeDrawer.translateTreeToPixelY(1.0));
			drawVerticalLabels(g2d, drawHeight, leftEdge, treeDrawer.translateTreeToPixelY(0), treeDrawer.translateTreeToPixelY(1.0));
		}
		
	}

	public void setOptions(ScaleAxisOptions ops) {
		scaleAxisTicks = ops.numberOfTicks;
		this.drawGridLines = ops.drawGridLines;
		this.reverse = ops.reverse;
		this.useCustomDistance = ops.useTickDistance;
		this.tickDistance = ops.tickDistance;
		parent.repaint();
	}
	
	

}
