package newgui.gui.display.primaryDisplay.loggerVizualizer;

import java.awt.Color;

import gui.figure.TextElement;
import gui.figure.series.HistogramSeries;
import gui.figure.series.XYSeriesElement;

import logging.BreakpointDensity;

/**
 * Visualizer for BPDensity logger - basically we just show a single histogram 
 * @author brendano
 *
 */
public class BPDensityViz extends AbstractLoggerViz {

	@Override
	public void update() {		
		fig.repaint();
		if (burninMessage != null && logger.getBurninExceeded()) {
			fig.removeElement(burninMessage);
			burninMessage = null;
		}
		if (logger.getBurninExceeded()) {
			fig.inferBoundsFromCurrentSeries();
		}
	}
	
	@Override
	public void initialize() {
		this.bpLogger = (BreakpointDensity)logger;
		HistogramSeries histoSeries = new HistogramSeries("Breakpoint density", bpLogger.getHistogram());
		XYSeriesElement histoEl = new XYSeriesElement(histoSeries, fig.getAxes(), fig);
		histoEl.setLineColor(Color.blue);
		histoEl.setLineWidth((float) 1.25);
		fig.addSeriesElement(histoEl);
		
		burninMessage = new TextElement("Burnin period (" + logger.getBurnin() + ") not exceeded", fig);
		burninMessage.setPosition(0.45, 0.5);
		fig.addElement(burninMessage);
	}


	@Override
	public String getDataString() {
		String data = logger.getSummaryString();
		return data;
	}
	
	private BreakpointDensity bpLogger;
	private TextElement burninMessage;

	
}
