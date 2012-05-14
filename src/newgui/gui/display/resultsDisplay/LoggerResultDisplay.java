package newgui.gui.display.resultsDisplay;

import gui.figure.series.AbstractSeries;
import gui.figure.series.HistogramSeries;
import gui.figure.series.XYSeriesElement;
import gui.figure.series.XYSeriesFigure;

import java.awt.BorderLayout;
import java.util.List;

import javax.swing.JPanel;

import math.Histogram;
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
	
}
