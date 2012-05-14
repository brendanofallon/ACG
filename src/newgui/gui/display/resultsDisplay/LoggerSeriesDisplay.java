package newgui.gui.display.resultsDisplay;

import gui.figure.series.AbstractSeries;
import gui.figure.series.HistogramSeries;
import gui.figure.series.UnifiedConfigFrame;
import gui.figure.series.XYSeries;
import gui.figure.series.XYSeriesElement;
import gui.figure.series.XYSeriesFigure;

import java.awt.Color;
import java.util.List;

import math.Histogram;
import newgui.UIConstants;
import newgui.datafile.resultsfile.XYSeriesInfo;

/**
 * A loggerResultDisplay with some functionality to display XY series', as well as 
 * read data from LoggerFigInfo objects and turn that into drawn series. 
 * @author brendan
 *
 */
public class LoggerSeriesDisplay extends LoggerResultDisplay {

	public void initialize(LoggerFigInfo figInfo) {
		setXLabel(figInfo.getxAxisTitle());
		setYLabel(figInfo.getyAxisTitle());
		
		for(XYSeriesInfo series : figInfo.getSeriesInfo()) {
			XYSeriesElement seriesEl = addSeries(series.getSeries());
			seriesEl.setLineColor(series.getColor());
			seriesEl.setLineWidth(series.getWidth());
		}
		
		Histogram histo = figInfo.getHisto();
		if (histo != null) {
			addSeries(new HistogramSeries(figInfo.getTitle(), histo));
		}
		
		seriesFig.inferBoundsFromCurrentSeries();
		seriesFig.repaint();
	}
	

	@Override
	protected String getDataString() {
		StringBuilder strB = new StringBuilder();
		String sep = System.getProperty("line.separator");
		List<AbstractSeries> series = seriesFig.getAllSeries();
		
		for(int i=0; i<series.size()-1; i++) {
			strB.append(series.get(i).getName() + "\t");
		}
		strB.append(series.get(series.size()-1).getName() + sep);
		
		boolean cont = true;
		int index = 0;
		while(cont) {
			for(int i=0; i<series.size()-1; i++) {
				strB.append(series.get(i).getX(index) + "\t" + series.get(i).getY(index) + "\t");
			}
			strB.append(series.get(series.size()-1).getX(index) + "\t" + series.get(series.size()-1).getY(index) + sep);
			
			index++;
			cont = index < series.get(0).size();
		}
		
		return strB.toString();
	}


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
