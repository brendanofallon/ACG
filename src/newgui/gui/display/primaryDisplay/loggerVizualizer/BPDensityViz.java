package newgui.gui.display.primaryDisplay.loggerVizualizer;

import gui.figure.TextElement;
import gui.figure.series.HistogramSeries;
import gui.figure.series.XYSeriesElement;

import java.awt.Color;

import logging.BreakpointDensity;

/**
 * Visualizer for BPDensity logger - basically we just show a single histogram 
 * @author brendano
 *
 */
public class BPDensityViz extends AbstractLoggerViz {
	
	@Override
	public void update() {		
		seriesFig.repaint();
		if (burninMessage != null && logger.getBurninExceeded()) {
			seriesFig.removeElement(burninMessage);
			burninMessage = null;
		}
		if (logger.getBurninExceeded()) {
			seriesFig.inferBoundsFromCurrentSeries();
		}
	}
	
	@Override
	public void initialize() {
		this.bpLogger = (BreakpointDensity)logger;
		HistogramSeries histoSeries = new HistogramSeries("Breakpoint density", bpLogger.getHistogram());
		XYSeriesElement histoEl = new XYSeriesElement(histoSeries, seriesFig.getAxes(), seriesFig);
		histoEl.setLineColor(Color.blue);
		histoEl.setLineWidth((float) 1.25);
		histoEl.setCanConfigure(true);
		seriesFig.addSeriesElement(histoEl);
		seriesFig.setXLabel("Sequence Position");
		seriesFig.setYLabel("Breakpoint Density");
		
		burninMessage = new TextElement("Burnin period (" + logger.getBurnin() + ") not exceeded", seriesFig);
		burninMessage.setPosition(0.45, 0.5);
		seriesFig.addElement(burninMessage);
	}


	@Override
	public String getDataString() {
		String data = logger.getSummaryString();
		return data;
	}
	
	private BreakpointDensity bpLogger;
	private TextElement burninMessage;

	
}
