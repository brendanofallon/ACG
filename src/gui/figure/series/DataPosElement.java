package figure.series;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.text.DecimalFormat;
import java.text.NumberFormat;


import figure.Figure;
import guiWidgets.StringUtilities;

/**
 * A small element that paints the indicates the current data position of the mouse pointer if the pointer is over the the associated axes element
 * @author brendan
 *
 */
public class DataPosElement extends figure.FigureElement {

	Point mousePos;
	AxesElement axes;
	double dataX = 0;
	double dataY = 0;
	boolean draw = false;
	
	
	
	Font font = new Font("Sans", Font.PLAIN, 10);
	
	public DataPosElement(AxesElement axes, Figure parent) {
		super(parent);
		parent.addMouseListeningElement(this);
		this.axes = axes;
		bounds.x = axes.getBounds().x+0.05;
		bounds.y = Math.min(1.0, axes.getBounds().y+axes.getBounds().height+0.05);
		bounds.height = 0.05;
		bounds.width = 0.1;
	}

	/**
	 * This is called by figure when the mouse has moved. 
	 * @param pos
	 */
	protected void mouseMoved(element.Point pos) {
		//System.out.println("Got poition x: " + pos.x + " y: " + pos.y);
		if (axes.getBounds().contains(pos)) {
			dataX = axes.boundsXtoDataX(pos.x);
			dataY = axes.boundsYtoDataY(pos.y);
			if (dataX>= axes.minXVal && dataX <= axes.maxXVal && dataY >= axes.minYVal && dataY <= axes.maxYVal) {
				draw = true;
			}
			else {
				draw = false;
			}
		}
		else {
			draw = false;
		}
		 
	}
	

	
	public void paint(Graphics2D g) {
		g.setColor(Color.GRAY);
		g.setFont(font);
		if (draw) {
			
			g.drawString(StringUtilities.format(dataX) + ", " + StringUtilities.format(dataY), round(bounds.x*xFactor), round(bounds.y*yFactor));
			//g.drawString("Bounds: " + formatter.format(pos.x) + " fig: " + formatter.format(axes. formatter.format(dataX) + ", " + formatter.format(dataY), round(bounds.x*xFactor), round(bounds.y*yFactor));
		}
	}

}
