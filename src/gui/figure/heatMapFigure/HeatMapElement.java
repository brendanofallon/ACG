package gui.figure.heatMapFigure;

import java.awt.Color;
import java.awt.Graphics2D;

import gui.figure.Figure;
import gui.figure.FigureElement;

/**
 * A type of figure that dispays a HeatMapElement and, perhaps, a color bar.
 * 
 * @author brendan
 *
 */
public class HeatMapElement extends FigureElement {
	
	
	protected int xAxisHeight = 5;
	protected int yAxisWidth = 5; 
	
	protected double[][] heats = null;
	
	protected Color coldColor = Color.blue;
	protected Color hotColor = Color.red;
	
	protected double coldTemp = 0.0; //The temperature that corresponds to the cold color
	protected double hotTemp = 1.0; //Temp that corresponds to the hot color
	protected int colorBinCount = 20;
	protected Color[] colors = new Color[colorBinCount];
	
	public HeatMapElement(Figure parentFig) {
		super(parentFig);
	}
	
	public HeatMapElement(Figure parentFig, double[][] heats) {
		this(parentFig);
		this.heats = heats;
		createColorArray();
	}

	private void createColorArray() {
		colors = new Color[colorBinCount];
		double dr = (hotColor.getRed()  - coldColor.getRed())/(double)colors.length;
		double dg = (hotColor.getGreen()  - coldColor.getGreen())/(double)colors.length;
		double db = (hotColor.getBlue()  - coldColor.getBlue())/(double)colors.length;
		
		for(int i=0; i<colors.length; i++) {
			colors[i] = new Color( (int)Math.round(coldColor.getRed() + dr*i), (int)Math.round(coldColor.getGreen() + dg*i),  (int)Math.round(coldColor.getBlue() + db*i));
		}
	}

	public void setData(double[][] heats) {
		this.heats = heats;
	}
	
	public int getNumRows() {
		if (heats == null)
			return 0;
		return heats.length;
	}
	
	public int getNumCols() {
		if (heats == null)
			return 0;
		return heats[0].length;
	}
	
	@Override
	public void paint(Graphics2D g) {
		if (heats == null) {
			g.drawString("Heats is null", (int)Math.floor(xFactor/2), (int)Math.round(yFactor/2));
			return;
		}

		int left = (int)Math.round(bounds.x*xFactor);
		int top = (int)Math.round(bounds.y*yFactor);
		int width = (int)Math.round(bounds.width*xFactor);
		int height = (int)Math.round(bounds.height*yFactor);
		g.setColor(Color.white);
		g.fillRect(left, top, width, height);
		
		g.setColor(Color.LIGHT_GRAY);
		int xStep = width / 4;
		int yStep = height / 4;
		for(int i=left; i<=left+width; i+=xStep) {
			g.drawLine(i, top, i, top+height);
		}
		
		for(int i=top; i<=top+height; i+=yStep) {
			g.drawLine(left, i, left+width, i);
		}
		
		for(int row=0; row<getNumRows(); row++) {
			for(int col=0; col<getNumCols(); col++) {
				if (heats[row][col]>0)
				drawBox(g, row, col, heats[row][col]);
			}
		}
	}

	private void drawBox(Graphics2D g, int row, int col, double heat) {
		
		int boxWidth = (int)Math.round(bounds.width/(double)getNumRows() * xFactor);
		int boxHeight = (int)Math.round(bounds.height/(double)getNumCols() * yFactor);
		int boxTop = (int)Math.round( (bounds.height+bounds.y)*yFactor - (bounds.y*yFactor + boxHeight * col));
		int boxLeft = (int)Math.round(bounds.x*xFactor + boxWidth * row);
		
		g.setColor(colorForHeat(heat));
//		if (heat > 0)
//			System.out.println("Drawing box : " + boxLeft + ", " + boxTop + " heat: " + heat  + " color: " + colorForHeat(heat));
		g.fillRect(boxLeft, boxTop, boxWidth+1, boxHeight+1);
	}

	private Color colorForHeat(double heat) {
		double val = (double)(heat-coldTemp)/(double)(hotTemp - coldTemp);
		int bin = (int)Math.round(Math.max(val*colors.length, 0.0));
		if (bin > colors.length-1)
			bin = colors.length-1;
		return colors[bin];
	}

	/**
	 * Sets the temperature that corresponds to the 'hot' color
	 * @param max
	 */
	public void setHeatMax(double max) {
		this.hotTemp = max;
		createColorArray();
	}

	public void setHeatMin(double min) {
		this.coldTemp = min;
		createColorArray();
	}
	
	public void setColdColor(Color coldColor) {
		this.coldColor = coldColor;
		createColorArray();
	}
	
	public void setHotColor(Color hotColor) {
		this.hotColor = hotColor;
		createColorArray();
	}
}
