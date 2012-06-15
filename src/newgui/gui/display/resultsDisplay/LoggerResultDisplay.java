package newgui.gui.display.resultsDisplay;

import gui.figure.series.AbstractSeries;
import gui.figure.series.HistogramSeries;
import gui.figure.series.XYSeriesElement;
import gui.figure.series.XYSeriesFigure;

import java.awt.BorderLayout;
import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

import math.Histogram;
import newgui.UIConstants;
import newgui.datafile.resultsfile.XYSeriesInfo;
import newgui.gui.widgets.AbstractFigurePanel;
import newgui.gui.widgets.AbstractSeriesPanel;

/**
 * A panel that displays the information about the saved result of a logger, typically
 * contained in a LoggerFigInfo object that is read from a ResultsFile 
 * @author brendan
 *
 */
public abstract class LoggerResultDisplay extends AbstractFigurePanel {

	//Not a lot of functionality here, really, just a marker for things that we use
	//to show the results of loggers...
	protected ImageIcon icon = UIConstants.grayRightArrow;
	/**
	 * Obtain an icon that represents this display
	 * @return
	 */
	public ImageIcon getIcon() {
		return icon;
	}
	
	/**
	 * Sets the icon that will be used in the SideTabPane to represent this logger
	 * @param icon
	 */
	public void setIcon(ImageIcon icon) {
		this.icon = icon;
	}
	
}
