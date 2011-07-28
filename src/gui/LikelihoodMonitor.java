package gui;

import java.awt.geom.Point2D;

import gui.figure.series.XYSeries;
import gui.figure.series.XYSeriesElement;

import component.LikelihoodComponent;

public class LikelihoodMonitor extends MonitorPanel {

	private LikelihoodComponent comp;
	
	public LikelihoodMonitor(LikelihoodComponent comp) {
		this.comp = comp;
		initializeFigure();
		series = new XYSeries[1];
		series[0] = new XYSeries(comp.getLogHeader());
		titles = new String[1];
		titles[0] = "Log likelihood";
		XYSeriesElement serEl = traceFigure.addDataSeries(series[0]);
		serEl.setLineWidth(defaultLineWidth);
	}
	
	
	/**
	 * Called when the mcmc chain fires a new state ot the MainOutputWindow,
	 * this is where we add new data to the chart 
	 * @param state
	 */
	public void update(int state) {
		Double val = comp.getCurrentLogLikelihood();
		Point2D.Double point = new Point2D.Double(state, val);
		series[0].addPointInOrder(point);

		if (histoSeries != null) {
			createHistograms();
			histoSeries[0].addValue(val);
		}
		traceFigure.inferBoundsPolitely();
		repaint();
	}
}
