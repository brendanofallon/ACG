package newgui.gui.display.primaryDisplay.loggerVizualizer;

import gui.figure.TextElement;
import gui.figure.series.ConstSizeSeries;
import gui.figure.series.XYSeriesElement;

import java.awt.Color;

import logging.RootHeightDensity;

public class TMRCAViz extends AbstractLoggerViz {

	@Override
	public void initialize() {
		this.rhLogger = (RootHeightDensity)logger;
		
		meanSeries = new ConstSizeSeries("Mean height", rhLogger.getMeans(), rhLogger.getBinPositions() );
		XYSeriesElement meanEl = new XYSeriesElement(meanSeries, seriesFig.getAxes(), seriesFig);
		meanEl.setLineColor(Color.blue);
		meanEl.setLineWidth((float) 1.5);
		meanEl.setCanConfigure(true);
		seriesFig.addSeriesElement(meanEl);
		seriesFig.setXLabel("Sequence Position");
		seriesFig.setYLabel("Time to most recent common ancestor");
		
		burninMessage = new TextElement("Burnin period (" + logger.getBurnin() + ") not exceeded", seriesFig);
		burninMessage.setPosition(0.45, 0.5);
		seriesFig.addElement(burninMessage);
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
			meanSeries.setYVals(rhLogger.getMeans());
			
			
			if (upper95Series == null && rhLogger.getHistoTriggerReached()) {
				upper95Series = new ConstSizeSeries("Upper 95%", rhLogger.getUpper95s(), rhLogger.getBinPositions() );
				XYSeriesElement upperEl = new XYSeriesElement(upper95Series, seriesFig.getAxes(), seriesFig);
				upperEl.setLineColor(Color.blue);
				upperEl.setLineWidth(0.75f);
				upperEl.setCanConfigure(true);
				seriesFig.addSeriesElement(upperEl);
			}
			
			if (lower95Series == null && rhLogger.getHistoTriggerReached()) {
				lower95Series = new ConstSizeSeries("Lower 95%", rhLogger.getLower95s(), rhLogger.getBinPositions() );
				XYSeriesElement lowerEl = new XYSeriesElement(lower95Series, seriesFig.getAxes(), seriesFig);
				lowerEl.setLineColor(Color.blue);
				lowerEl.setLineWidth(0.75f);
				lowerEl.setCanConfigure(true);
				seriesFig.addSeriesElement(lowerEl);
			}
			if (upper95Series != null)
				upper95Series.setYVals(rhLogger.getUpper95s());
			if (lower95Series != null) {
				lower95Series.setYVals(rhLogger.getLower95s());
			}
		}
		seriesFig.repaint();
	}

	private ConstSizeSeries upper95Series;
	private ConstSizeSeries lower95Series;
	private ConstSizeSeries meanSeries;
	private RootHeightDensity rhLogger;
	private TextElement burninMessage;
}
