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
		
		
		burninMessage = new TextElement("Burnin period (" + logger.getBurnin() + ") not exceeded", fig);
		burninMessage.setPosition(0.45, 0.5);
		fig.addElement(burninMessage);
	}

	@Override
	public String getDataString() {
		String data = logger.getSummaryString();
		return data;
	}	
	
	@Override
	public void update() {
		
		if (burninMessage != null && logger.getBurninExceeded()) {
			fig.removeElement(burninMessage);
			
			burninMessage = null;
		}
		if (logger.getBurninExceeded()) {
			fig.inferBoundsFromCurrentSeries();
			
			if (meanSeries == null) {
				meanSeries = new ConstSizeSeries("Mean size", popSizeLogger.getMeans(), popSizeLogger.getBinPositions() );
				XYSeriesElement meanEl = new XYSeriesElement(meanSeries, fig.getAxes(), fig);
				meanEl.setLineColor(Color.blue);
				meanEl.setLineWidth((float) 1.5);
				meanEl.setCanConfigure(true);
				fig.addSeriesElement(meanEl);
			}
			
			meanSeries.setYVals(popSizeLogger.getMeans());
			
			if (upper95Series == null && popSizeLogger.getHistoTriggerReached()) {
				upper95Series = new ConstSizeSeries("Upper 95%", popSizeLogger.getUpper95s(), popSizeLogger.getBinPositions() );
				XYSeriesElement upperEl = new XYSeriesElement(upper95Series, fig.getAxes(), fig);
				upperEl.setLineColor(Color.blue);
				upperEl.setLineWidth(0.75f);
				upperEl.setCanConfigure(true);
				fig.addSeriesElement(upperEl);
			}
			
			if (lower95Series == null && popSizeLogger.getHistoTriggerReached()) {
				lower95Series = new ConstSizeSeries("Lower 95%", popSizeLogger.getLower95s(), popSizeLogger.getBinPositions() );
				XYSeriesElement lowerEl = new XYSeriesElement(lower95Series, fig.getAxes(), fig);
				lowerEl.setLineColor(Color.blue);
				lowerEl.setLineWidth(0.75f);
				lowerEl.setCanConfigure(true);
				fig.addSeriesElement(lowerEl);
			}
			if (upper95Series != null)
				upper95Series.setYVals(popSizeLogger.getUpper95s());
			if (lower95Series != null) {
				lower95Series.setYVals(popSizeLogger.getLower95s());
			}
		}
		fig.repaint();
	}

	private ConstSizeSeries upper95Series;
	private ConstSizeSeries lower95Series;
	private ConstSizeSeries meanSeries;
	private PopSizeLogger popSizeLogger;
	private TextElement burninMessage;
	
}
