package newgui.gui.display.resultsDisplay;

import gui.figure.series.AbstractSeries;
import gui.figure.series.HistogramSeries;
import gui.figure.series.XYSeriesElement;
import gui.figure.series.XYSeriesFigure;

import java.awt.BorderLayout;
import java.util.List;

import javax.swing.JPanel;

import math.Histogram;
import newgui.datafile.resultsfile.LoggerFigInfo;
import newgui.datafile.resultsfile.XYSeriesInfo;
import newgui.gui.widgets.AbstractSeriesPanel;

/**
 * A panel that displays the information about the saved result of a logger, typically
 * contained in a LoggerFigInfo object that is read from a ResultsFile 
 * @author brendan
 *
 */
public class LoggerResultDisplay extends AbstractSeriesPanel {

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
		
		fig.inferBoundsFromCurrentSeries();
		fig.repaint();
	}
	

	@Override
	protected String getDataString() {
		StringBuilder strB = new StringBuilder();
		String sep = System.getProperty("line.separator");
		List<AbstractSeries> series = fig.getAllSeries();
		
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
}
