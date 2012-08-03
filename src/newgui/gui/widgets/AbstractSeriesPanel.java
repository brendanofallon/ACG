package newgui.gui.widgets;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import gui.figure.series.AbstractSeries;
import gui.figure.series.UnifiedConfigFrame;
import gui.figure.series.XYSeries;
import gui.figure.series.XYSeriesElement;
import gui.figure.series.XYSeriesFigure;

import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import newgui.ErrorWindow;
import newgui.UIConstants;
import newgui.datafile.XMLConversionError;
import newgui.datafile.resultsfile.XYSeriesInfo;

/**
 * A generic panel that contains an XYSeriesFigure and a toolbar with a few useful buttons
 * @author brendan
 *
 */
public abstract class AbstractSeriesPanel extends AbstractFigurePanel {

	protected XYSeriesFigure seriesFig;
	protected UnifiedConfigFrame configFrame = null;
	
	
	public void initializeFigure() {
		fig = new XYSeriesFigure();
		seriesFig = (XYSeriesFigure)fig;

		seriesFig.setAllowMouseDragSelection(false);
		seriesFig.getAxes().setNumXTicks(4);
		seriesFig.getAxes().setNumYTicks(4);
		seriesFig.setAxisLabelFont(UIConstants.sansFont.deriveFont(14f));
		seriesFig.setLegendFont(UIConstants.sansFont.deriveFont(13f));
	}

	/**
	 * Obtain the figure used to draw the data
	 * @return
	 */
	public XYSeriesFigure getSeriesFigure() {
		return seriesFig;
	}
	
	/**
	 * Remove all series from the current figure
	 */
	public void clearSeries() {
		seriesFig.removeAllSeries();
	}
	
	public void setXLabel(String xLabel) {
		seriesFig.setXLabel(xLabel);
		seriesFig.repaint();
	}
	
	public void setYLabel(String yLabel) {
		seriesFig.setYLabel(yLabel);
		seriesFig.repaint();
	}
	
	/**
	 * Add the series to those drawn by the figure
	 * @param series
	 */
	public XYSeriesElement addSeries(XYSeries series) {
		XYSeriesElement el = seriesFig.addDataSeries(series);
		repaint();
		return el;
	}
	
	/**
	 * Add the given series to the figure and set its color to the given color
	 * @param series
	 * @param col
	 * @return
	 */
	public XYSeriesElement addSeries(XYSeries series, Color col) {
		XYSeriesElement el = seriesFig.addDataSeries(series);
		el.setLineColor(col);
		repaint();
		return el;
	}
	
	/**
	 * Add the given series to the figure and set its color and width to the given params
	 * @param series
	 * @param col
	 * @return
	 */
	public XYSeriesElement addSeries(XYSeries series, Color col, float width) {
		XYSeriesElement el = seriesFig.addDataSeries(series);
		el.setLineColor(col);
		el.setLineWidth(width);
		repaint();
		return el;
	}
	
	

	public void showConfigFrame() {
		if (configFrame == null)
			configFrame = new UnifiedConfigFrame(seriesFig);
		
		configFrame.readSettingsFromFigure();
		configFrame.setVisible(true);
	}


}
