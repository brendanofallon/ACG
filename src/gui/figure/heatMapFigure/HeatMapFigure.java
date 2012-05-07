package gui.figure.heatMapFigure;

import gui.figure.Figure;

/**
 * A type of figure that dispays a HeatMapElement and, perhaps, a color bar.
 * 
 * @author brendan
 *
 */
public class HeatMapFigure extends Figure {

//Distances from the edges of the figure to the main element, in "figure" (0..1) coords
	protected double leftPadding = 0.1;
	protected double rightPadding = 0.1;
	protected double topPadding = 0.05;
	protected double bottomPadding = 0.1;
	
	protected HeatMapElement heatMapEl = null;
	
	
	public HeatMapFigure() {
		heatMapEl = new HeatMapElement(this);
		heatMapEl.setBounds(leftPadding, topPadding, 1.0-leftPadding-rightPadding, 1.0-topPadding-bottomPadding);
		heatMapEl.setCanConfigure(false);
		heatMapEl.setMobile(false);
		super.addElement(heatMapEl);
	}
	
	
	public void setData(double[][] heats) {
		heatMapEl.setData(heats);
		repaint();
	}
}
