package newgui.datafile.resultsfile;

import java.awt.Color;

import gui.figure.series.XYSeries;

/**
 * A small container class for an XYSeries and some associated plotting info such as color, width, etc. 
 * @author brendano
 *
 */
public class XYSeriesInfo {

	XYSeries series = null;
	Color color = Color.blue;
	float width = 1.0f;
	
	public XYSeries getSeries() {
		return series;
	}
	
	public void setSeries(XYSeries series) {
		this.series = series;
	}
	
	public Color getColor() {
		return color;
	}
	
	public void setColor(Color color) {
		this.color = color;
	}
	
	public float getWidth() {
		return width;
	}
	
	public void setWidth(float width) {
		this.width = width;
	}

}
