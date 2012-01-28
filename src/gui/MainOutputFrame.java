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


package gui;

import gui.monitors.LikelihoodMonitor;
import gui.monitors.MonitorPanel;
import gui.monitors.ParamMonitor;

import java.awt.Color;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import mcmc.MCMC;
import mcmc.MCMCListener;

import component.LikelihoodComponent;

import parameter.AbstractParameter;

/**
 * Panel that displays various output charts
 * @author brendano
 *
 */
public class MainOutputFrame extends JPanel implements MCMCListener {
	
	//Maximum size we allow data series to get before they're thinned
	public final int MAX_SERIES_SIZE = 8000;
	
	private int rows = 2;
	private int cols = 2;
	private MCMC mcmc = null;
	private int frequency;
	private boolean initialized = false;
	
	private List<MonitorPanel> figureList = new ArrayList<MonitorPanel>();
	final int burnin;
	
	public MainOutputFrame(MCMC chain, int frequency, int burnin) {
		this.mcmc = chain;
		chain.addListener(this);
		this.frequency = frequency;
		initComponents();
		setMCMC(chain);
		this.burnin = burnin;
	}
	
	public void addChart(AbstractParameter<?> param, String logKey) {		
		MonitorPanel figure;
		if (logKey != null)
			 figure = new ParamMonitor(param, logKey, burnin);
		else
			 figure = new ParamMonitor(param, burnin);
		
		figureList.add(figure);
		this.add(figure);
	}
	
	/**
	 * Create and add a new MonitorPanel to this component that displays information
	 * from the given parameter
	 * @param param
	 */
	public void addMonitor(AbstractParameter<?> param) {
		addChart(param, null);
	}
	
	/**
	 * Create and add a new MonitorPanel to this component that displays information
	 * from the given Likelihood
	 * @param param
	 */
	public void addMonitor(LikelihoodComponent comp) {
		MonitorPanel figure = new LikelihoodMonitor(comp, burnin);	
		figureList.add(figure);
		this.add(figure);
	}
	
	/**
	 * Add a the given MonitorPanel to this component 
	 * @param param
	 */
	public void addMonitor(MonitorPanel panel) {	
		figureList.add(panel);
		this.add(panel);
	}
	
	
	private void initComponents() {
		//For reasons I don't understand at all, setting a layout here is required in order
		//for figure legend element to be drawn, even though we set another
		//layout later (in initializeMatrix). Your guess is as good as mine. 
		GridLayout layout = new GridLayout(rows, cols, 2, 2);
		this.setLayout(layout);
		this.setBackground(Color.LIGHT_GRAY);
	}

	@Override
	public void setMCMC(MCMC chain) {
		this.mcmc = chain;
		for(MonitorPanel fig : figureList) {
			fig.setChain(mcmc);
		}
	}

	@Override
	public void newState(int stateNumber) {
		if (!initialized) {
			initializeMatrix();
		}
		
		if (stateNumber % frequency == 0) {
			for(MonitorPanel fig : figureList) {
				fig.updateMonitor(stateNumber);
			}
			
			//If any figures have more than MAX_SERIES_SIZE data points, remove half of the data points from all
			//series and multiply frequency by two (so we collect less frequently)
			boolean needsThinning = false;
			for(MonitorPanel fig : figureList) {
				needsThinning = fig.getSeriesSize() > MAX_SERIES_SIZE;
				if (needsThinning)
					break;
			}
			
//			if (needsThinning) {
//				System.out.println("Thinning all series.....");
//				for(MonitorPanel fig : figureList) { 
//					fig.thinSeries();
//				}
//				frequency *= 2;
//			}
		}
	}

	private void initializeMatrix() {
		initialized = true;
		int numFigs = figureList.size();
		if (numFigs < 4) {
			rows = 1;
			cols = numFigs;
		}
		if (numFigs == 4) {
			rows = 2;
			cols = 2;
		}
		if (numFigs > 4 && numFigs < 7) {
			rows = 2;
			cols = 3;
		}
		
		if (numFigs == 7 || numFigs == 8) {
			rows = 2;
			cols = 4;
		}
		
		if (numFigs == 9) {
			rows = 3;
			cols = 3;
		}
		
		GridLayout layout = new GridLayout(rows, cols, 1, 1);
		this.setLayout(layout);
		for(MonitorPanel fig : figureList) {
			this.add(fig);
		}
		
		this.revalidate();
		initialized = true;
	}

	@Override
	public void chainIsFinished() {
		// TODO Auto-generated method stub
	}

	/**
	 * Return the number of (Monitors) that have been added to this pane
	 * @return
	 */
	public int getMonitorCount() {
		return figureList.size();
	}
}
