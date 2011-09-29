/********************************************************************
*
* 	Copyright 2011 Brendan O'Fallon
*
*   Licensed under the Apache License, Version 2.0 (the "License");
*   you may not use this file except in compliance with the License.
*   You may obtain a copy of the License at
*
*       http://www.apache.org/licenses/LICENSE-2.0
*
*   Unless required by applicable law or agreed to in writing, software
*   distributed under the License is distributed on an "AS IS" BASIS,
*   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*   See the License for the specific language governing permissions and
*   limitations under the License.
*
***********************************************************************/


package gui.monitors;


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
