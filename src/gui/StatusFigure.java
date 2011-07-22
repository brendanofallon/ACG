package gui;

import java.awt.geom.Point2D;

import component.LikelihoodComponent;
import gui.figure.series.XYSeries;
import gui.figure.series.XYSeriesElement;
import gui.figure.series.XYSeriesFigure;
import parameter.AbstractParameter;
import parameter.DoubleParameter;

public class StatusFigure extends XYSeriesFigure {

	private AbstractParameter<?> param = null;
	private LikelihoodComponent comp = null;
	
	private float defaultLineWidth = 1.1f;
	
	XYSeries[] series;
	String logKey = null;
	
	public StatusFigure(AbstractParameter<?> param, String logKey) {
		this.param = param;
		this.logKey = logKey;
		
		this.setAllowMouseDragSelection(false);
		this.setXLabel("MCMC state");
		this.setYLabel(null);
		this.getAxes().setNumXTicks(4);
		this.getAxes().setNumYTicks(4);
		
		//This ALL NEEDS TO BE REFACTORED BADLY! BUT how do we know if things should be grouped together, or not? 
		String[] logKeyItems = logKey.split("\\t");
		
		
		String logStr = (param.getLogItem(logKey)).toString();
		String[] logToks = logStr.split("\\t");
		series = new XYSeries[logToks.length];
		for(int i=0; i<Math.min(logToks.length, logKeyItems.length); i++) {
			String displayName = logKeyItems[i];
			if (i>0)
				displayName = logKeyItems[i] + "(" + i + ")";
			series[i] = new XYSeries(displayName);
			XYSeriesElement serEl = addDataSeries(series[i]);
			serEl.setLineWidth(defaultLineWidth);	
		}
	}
	
	public StatusFigure(AbstractParameter<?> param) {
		this.param = param;
		Object t = param.getValue();
		
		this.setAllowMouseDragSelection(false);
		this.setXLabel("MCMC state");
		this.setYLabel(null);
		if (t instanceof Double) {
			series = new XYSeries[1];
			series[0] = new XYSeries(param.getName());
			XYSeriesElement serEl = addDataSeries(series[0]);
			serEl.setLineWidth(defaultLineWidth);	
		}
		else {
			if (t instanceof double[]) {
				//also fine, but we'll draw multiple lines
				double[] vals = (double[])t;
				series = new XYSeries[ vals.length ];
				for(int i=0; i<vals.length; i++) {
					series[i] = new XYSeries(param.getName() + "(" + i + ")");
					XYSeriesElement serEl = addDataSeries(series[i]);
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
	
	public StatusFigure(LikelihoodComponent comp) {
		this.comp = comp;
		this.setAllowMouseDragSelection(false);
		this.setXLabel("MCMC state");
		this.getAxes().setNumXTicks(4);
		this.getAxes().setNumYTicks(4);
		this.setYLabel(null);
		series = new XYSeries[1];
		series[0] = new XYSeries(comp.getLogHeader());
		XYSeriesElement serEl = addDataSeries(series[0]);
		serEl.setLineWidth(defaultLineWidth);
	}
	
	
	public void update(int state) {
		if (param != null) {
			String logStr = (param.getLogItem(logKey)).toString();
			String[] logToks = logStr.split("\\t");
			for(int i=0; i<logToks.length; i++) {
				try {
					Double val = Double.parseDouble(logToks[i]);
					Point2D.Double point = new Point2D.Double(state, val);
					series[i].addPointInOrder(point);
				}
				catch (NumberFormatException nex) {
					//don't worry about it
				}
			}
		}
		else {
			Double val = comp.getCurrentLogLikelihood();
			Point2D.Double point = new Point2D.Double(state, val);
			series[0].addPointInOrder(point);
		}
		inferBoundsPolitely();
		repaint();
	}
	

}
