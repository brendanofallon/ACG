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
		XYSeriesElement meanEl = new XYSeriesElement(meanSeries, fig.getAxes(), fig);
		meanEl.setLineColor(Color.blue);
		meanEl.setLineWidth((float) 1.25);
		fig.addSeriesElement(meanEl);
		
		burninMessage = new TextElement("(Burnin period not exceeded)", fig);
		burninMessage.setPosition(0.45, 0.5);
		fig.addElement(burninMessage);
	}

	@Override
	public void update() {
		
		
		if (burninMessage != null && logger.getBurninExceeded()) {
			fig.removeElement(burninMessage);
			
			burninMessage = null;
		}
		if (logger.getBurninExceeded()) {
			fig.inferBoundsPolitely();
			meanSeries.setYVals(rhLogger.getMeans());
			
			if (upper95Series == null && rhLogger.getHistoTriggerReached()) {
				upper95Series = new ConstSizeSeries("Upper 95%", rhLogger.getUpper95s(), rhLogger.getBinPositions() );
				XYSeriesElement upperEl = new XYSeriesElement(upper95Series, fig.getAxes(), fig);
				upperEl.setLineColor(Color.blue);
				upperEl.setLineWidth(0.75f);
				fig.addSeriesElement(upperEl);
			}
			
			if (lower95Series == null && rhLogger.getHistoTriggerReached()) {
				lower95Series = new ConstSizeSeries("Lower 95%", rhLogger.getLower95s(), rhLogger.getBinPositions() );
				XYSeriesElement lowerEl = new XYSeriesElement(lower95Series, fig.getAxes(), fig);
				lowerEl.setLineColor(Color.blue);
				lowerEl.setLineWidth(0.75f);
				fig.addSeriesElement(lowerEl);
			}
			if (upper95Series != null)
				upper95Series.setYVals(rhLogger.getUpper95s());
			if (lower95Series != null) {
				lower95Series.setYVals(rhLogger.getLower95s());
			}
		}
		fig.repaint();
	}

	private ConstSizeSeries upper95Series;
	private ConstSizeSeries lower95Series;
	private ConstSizeSeries meanSeries;
	private RootHeightDensity rhLogger;
	private TextElement burninMessage;
}
