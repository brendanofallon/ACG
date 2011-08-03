package gui;

import java.awt.geom.Point2D;

import gui.figure.series.XYSeries;
import gui.figure.series.XYSeriesElement;

/**
 * A simple monitor that displays the "speed" (mc states / sec) of the chains
 * @author brendan
 *
 */
public class SpeedMonitor extends MonitorPanel {

	private long lastTime;
	private int lastState = 0;
	double[] means = new double[1];
	int count = 0;
	
	public SpeedMonitor() {
		initializeFigure();
		series = new XYSeries[1];
		series[0] = new XYSeries("mc.rate");
		titles = new String[1];
		titles[0] = "Log likelihood";
		XYSeriesElement serEl = traceFigure.addDataSeries(series[0]);
		serEl.setLineWidth(defaultLineWidth);
		lastTime = System.currentTimeMillis();
	}
	
	@Override
	protected void update(int state) {
		long timeNow = System.currentTimeMillis();
		double speed = (double)(state - lastState)/((double)(timeNow-lastTime)/1000.0) ;
		lastState = state;
		lastTime = timeNow;
		
		Point2D.Double point = new Point2D.Double(state, speed);
		series[0].addPointInOrder(point);
		count++;
		
		if (histoSeries != null) {
			createHistograms();
			histoSeries[0].addValue(speed);
		}
		traceFigure.inferBoundsPolitely();
		repaint();
	}

	@Override
	public double[] getMean() {
		means[0] = series[0].getYMean();
		return means;
	}

	@Override
	public int getCalls() {
		return count;
	}

	@Override
	public double getAcceptanceRate() {
		return 1.0;
	}

}
