package gui;

import java.awt.geom.Point2D;

import component.LikelihoodComponent;
import gui.figure.series.XYSeries;
import gui.figure.series.XYSeriesFigure;
import parameter.AbstractParameter;
import parameter.DoubleParameter;

public class StatusFigure extends XYSeriesFigure {

	private AbstractParameter<?> param = null;
	private LikelihoodComponent comp = null;
	
	XYSeries[] series;
	
	public StatusFigure(AbstractParameter<?> param, String[] logKeys) {
		// ???
	}
	
	public StatusFigure(AbstractParameter<?> param) {
		this.param = param;
		Object t = param.getValue();
		if (t instanceof Double) {
			series = new XYSeries[1];
			series[0] = new XYSeries(param.getName());
			addDataSeries(series[0]);
		}
		else {
			if (t instanceof double[]) {
				//also fine, but we'll draw multiple lines
				series = new XYSeries[ ((double[])t).length ];
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
		series = new XYSeries[1];
		series[0] = new XYSeries(comp.getLogHeader());
		addDataSeries(series[0]);
	}
	
	
	public void update(int state) {
		Double val;
		if (param != null) {
			val = ((DoubleParameter)param).getValue();
		}
		else {
			val = comp.getCurrentLogLikelihood();
		}
		Point2D.Double point = new Point2D.Double(state, val);
		series[0].addPointInOrder(point);
		inferBoundsPolitely();
		repaint();
	}
	

}
