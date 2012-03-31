package newgui.gui.display.resultsDisplay;

import gui.figure.series.HistogramSeries;
import gui.figure.series.XYSeriesElement;
import gui.figure.series.XYSeriesFigure;

import java.awt.BorderLayout;

import javax.swing.JPanel;

import math.Histogram;
import newgui.datafile.resultsfile.LoggerFigInfo;
import newgui.datafile.resultsfile.XYSeriesInfo;

/**
 * A panel that displays the information about the saved result of a logger, typically
 * contained in a LoggerFigInfo object that is read from a ResultsFile 
 * @author brendan
 *
 */
public class LoggerResultDisplay extends JPanel {

	public LoggerResultDisplay() {
		initComponents();
	}
	

	public void initialize(LoggerFigInfo figInfo) {
		fig.setXLabel(figInfo.getxAxisTitle());
		fig.setYLabel(figInfo.getyAxisTitle());
		
		for(XYSeriesInfo series : figInfo.getSeriesInfo()) {
			XYSeriesElement seriesEl = fig.addDataSeries(series.getSeries());
			seriesEl.setLineColor(series.getColor());
			seriesEl.setLineWidth(series.getWidth());
		}
		
		Histogram histo = figInfo.getHisto();
		if (histo != null) {
			fig.addDataSeries(new HistogramSeries(figInfo.getTitle(), histo));
		}
		
		fig.inferBoundsFromCurrentSeries();
		fig.repaint();
	}
	
	private void initComponents() {
		setLayout(new BorderLayout());
		setOpaque(false);
		
		fig = new XYSeriesFigure();
		this.add(fig, BorderLayout.CENTER);
	}
	
	
	XYSeriesFigure fig;
}
