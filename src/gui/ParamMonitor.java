package gui;

import java.awt.geom.Point2D;

import gui.figure.series.XYSeries;
import gui.figure.series.XYSeriesElement;
import parameter.AbstractParameter;

public class ParamMonitor extends MonitorPanel {

	private AbstractParameter<?> param;
	
	public ParamMonitor(AbstractParameter<?> param, String logKey) {
		this.param = param;
		this.logKey = logKey;
		initializeFigure();
		//This ALL NEEDS TO BE REFACTORED BADLY! BUT how do we know if things should be grouped together, or not? 
		String[] logKeyItems = logKey.split("\\t");
		
		
		String logStr = (param.getLogItem(logKey)).toString();
		String[] logToks = logStr.split("\\t");
		series = new XYSeries[logToks.length];
		titles = new String[logToks.length];
		for(int i=0; i<Math.min(logToks.length, logKeyItems.length); i++) {
			String displayName = logKeyItems[i];
			if (i>0)
				displayName = logKeyItems[i] + "(" + i + ")";
			titles[i] = displayName;
			series[i] = new XYSeries(displayName);
			XYSeriesElement serEl = traceFigure.addDataSeries(series[i]);
			serEl.setLineWidth(defaultLineWidth);	
		}
	}
	
	public ParamMonitor(AbstractParameter<?> param) {
		this.param = param;
		Object t = param.getValue();
		
		initializeFigure();
		if (t instanceof Double) {
			series = new XYSeries[1];
			series[0] = new XYSeries(param.getName());
			XYSeriesElement serEl = traceFigure.addDataSeries(series[0]);
			serEl.setLineWidth(defaultLineWidth);	
		}
		else {
			if (t instanceof double[]) {
				//also fine, but we'll draw multiple lines
				double[] vals = (double[])t;
				series = new XYSeries[ vals.length ];
				for(int i=0; i<vals.length; i++) {
					series[i] = new XYSeries(param.getName() + "(" + i + ")");
					XYSeriesElement serEl = traceFigure.addDataSeries(series[i]);
					serEl.setLineWidth(defaultLineWidth);	
				}
			}
			else {
				if (t instanceof Integer) {
					//OK
					//We could theoretically do an array of integers?
					series = new XYSeries[1];
				}
				else {
					throw new IllegalArgumentException("Can't create a Figure for parameter with type " + t.getClass());
				}
			}
		}
	}
	
	/**
	 * Called when the mcmc chain fires a new state ot the MainOutputWindow,
	 * this is where we add new data to the chart 
	 * @param state
	 */
	public void update(int state) {
		if (param != null) {
			String logStr = (param.getLogItem(logKey)).toString();
			String[] logToks = logStr.split("\\t");
			for(int i=0; i<logToks.length; i++) {
				try {
					Double val = Double.parseDouble(logToks[i]);
					Point2D.Double point = new Point2D.Double(state, val);
					series[i].addPointInOrder(point);
					if (histoSeries != null) {
						histoSeries[i].addValue(val);
					}
				}
				catch (NumberFormatException nex) {
					//don't worry about it
				}
			}
		}
		traceFigure.inferBoundsPolitely();
		repaint();
	}
}
