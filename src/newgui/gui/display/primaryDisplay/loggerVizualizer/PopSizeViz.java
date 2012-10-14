package newgui.gui.display.primaryDisplay.loggerVizualizer;

import gui.figure.TextElement;
import gui.figure.series.ConstSizeSeries;
import gui.figure.series.XYSeriesElement;

import java.awt.Color;

import logging.PopSizeLogger;
import logging.RootHeightDensity;

public class PopSizeViz extends AbstractLoggerViz {

	@Override
	public void initialize() {
		this.popSizeLogger = (PopSizeLogger)logger;
		
		
		burninMessage = new TextElement("Burnin period (" + logger.getBurnin() + ") not exceeded", seriesFig);
		burninMessage.setPosition(0.45, 0.5);
		seriesFig.addElement(burninMessage);
		seriesFig.setXLabel("Time in past (subs. / site)");
		seriesFig.setYLabel("Scaled population size");
	}

	@Override
	public String getDataString() {
		String data = logger.getSummaryString();
		return data;
	}	
	
	@Override
	public void update() {
		
		if (burninMessage != null && logger.getBurninExceeded()) {
			seriesFig.removeElement(burninMessage);
			
			burninMessage = null;
		}
		if (logger.getBurninExceeded()) {
			seriesFig.inferBoundsFromCurrentSeries();
			
			if (meanSeries == null) {
				meanSeries = new ConstSizeSeries("Mean size", popSizeLogger.getMeans(), popSizeLogger.getBinPositions() );
				XYSeriesElement meanEl = new XYSeriesElement(meanSeries, seriesFig.getAxes(), seriesFig);
				meanEl.setLineColor(Color.blue);
				meanEl.setLineWidth((float) 1.5);
				meanEl.setCanConfigure(true);
				seriesFig.addSeriesElement(meanEl);
			}
			
			meanSeries.setYVals(popSizeLogger.getMeans());
			
			if (upper95Series == null && popSizeLogger.getHistoTriggerReached()) {
				upper95Series = new ConstSizeSeries("Upper 95%", popSizeLogger.getUpper95s(), popSizeLogger.getBinPositions() );
				XYSeriesElement upperEl = new XYSeriesElement(upper95Series, seriesFig.getAxes(), seriesFig);
				upperEl.setLineColor(Color.blue);
				upperEl.setLineWidth(0.75f);
				upperEl.setCanConfigure(true);
				seriesFig.addSeriesElement(upperEl);
			}
			
			if (lower95Series == null && popSizeLogger.getHistoTriggerReached()) {
				lower95Series = new ConstSizeSeries("Lower 95%", popSizeLogger.getLower95s(), popSizeLogger.getBinPositions() );
				XYSeriesElement lowerEl = new XYSeriesElement(lower95Series, seriesFig.getAxes(), seriesFig);
				lowerEl.setLineColor(Color.blue);
				lowerEl.setLineWidth(0.75f);
				lowerEl.setCanConfigure(true);
				seriesFig.addSeriesElement(lowerEl);
			}
			if (upper95Series != null)
				upper95Series.setYVals(popSizeLogger.getUpper95s());
			if (lower95Series != null) {
				lower95Series.setYVals(popSizeLogger.getLower95s());
			}
		}
		seriesFig.repaint();
	}

	private ConstSizeSeries upper95Series;
	private ConstSizeSeries lower95Series;
	private ConstSizeSeries meanSeries;
	private PopSizeLogger popSizeLogger;
	private TextElement burninMessage;
	
}
