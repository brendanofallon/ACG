package gui;

/**
 * A simple monitor that displays the "speed" (mc states / sec) of the chains
 * @author brendan
 *
 */
public class SpeedMonitor extends MonitorPanel {

	private long lastTime;
	private int lastState = 0;
	int count = 0;
	
	public SpeedMonitor(int burnin) {
		super(burnin);
		initializeFigure();		
		initializeSeries(1);
		super.addSeries("mc.rate");
		lastTime = System.currentTimeMillis();
		setShowMeans(false);
		setShowStdevs(false);
		setHeaderLabel("MC.Rate");
	}
	
	@Override
	protected void update(int state) {
		long timeNow = System.currentTimeMillis();
		double speed = (double)(state - lastState)/((double)(timeNow-lastTime)/1000.0) ;
		lastState = state;
		lastTime = timeNow;
		addPointToSeries(0, state, speed);
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
