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


package jobqueue;

import java.util.ArrayList;
import java.util.List;

import gui.ErrorWindow;

import javax.swing.SwingWorker;

import jobqueue.JobState.State;

import mcmc.MCMC;
import mcmc.MCMCListener;
import mcmc.mc3.MC3;

/**
 * Wraps a running MCMC chain or MC3 object so that it may be run in the background
 * as well as paused and restarted. 
 * @author brendan
 *
 */
public class ExecutingChain extends SwingWorker implements MCMCListener, ACGJob {

	protected MCMC chain = null; //Will be null if user supplies an MC3 object to constructor
	protected MC3 mc3 = null;		//Will be null if user supplies MCMC object to constructor
	protected MCMC coldChain = null; //Reference to cold chain, only non-null if we're in MC3 mode 
	private boolean paused = false;
	private boolean done = false;
	
	public ExecutingChain(MCMC chain) {
		this.chain = chain;
		chain.addListener(this);
	}
	
	
	public ExecutingChain(MC3 mc3) {
		this.mc3 = mc3;
		this.coldChain = mc3.getColdChain();
		coldChain.addListener(this);
	}
	
	/**
	 * Begin execution of MCMC / MC3
	 */
	protected Object doInBackground() throws Exception {
		
		try {
			beginJob();
		}
		catch (Exception ex) {
			ErrorWindow.showErrorWindow(ex);
			state.setState(State.ERROR);
		}
		return null;
	}

	public boolean isPaused() {
		return paused;
	}
	
	public void done() {
		this.done = true;
	}
	
	
	/**
	 * Set the paused state to the given value. When paused the thread (or MC3) sleeps, but
	 * wakes up periodically to see if it's been unpaused
	 * @param paused
	 */
	public void setPaused(boolean paused) {
		this.paused = paused;
		if (mc3 != null) {
			mc3.setPaused(paused);
			
			state.setState(State.PAUSED);
			fireStatusUpdate();
		}
	}
	
	@Override
	public void newState(int stateNumber) {
		while(paused && mc3==null) {			
			try {
				Thread.sleep(500); //Wake up every 0.5 seconds to see if we're unpaused
			} catch (InterruptedException e) {
				
			} 
		}
		
		if (this.isCancelled()) {
			if (mc3 != null)
				mc3.abort();
			
			if (chain != null)
				chain.abort();
			
			state.setState(State.COMPLETED);
			fireStatusUpdate();
		}
	}

	@Override
	public void chainIsFinished() {
		state.setState(State.COMPLETED);
		fireStatusUpdate();
	}
	

	@Override
	public void setMCMC(MCMC chain) { 
		if (mc3 != null)
			this.coldChain = chain;
	}


	@Override
	public void beginJob() {
		System.out.println("Beginning job");
		state.setState(State.RUNNING);
		fireStatusUpdate();
		if (mc3 != null) {
			mc3.run();
		}
		else {
			chain.run();
		}
	}
	
	/**
	 * Set a title for this job that will appear in some UI components displaying the job
	 * @param title
	 */
	public void setJobTitle(String title) {
		this.jobTitle = title;
	}

	////////////////////////////////    ACGJob implementation     ///////////////////////////////////

	@Override
	public void addListener(JobListener listener) {
		listeners.add(listener);
	}


	@Override
	public void removeListener(JobListener listener) {
		listeners.remove(listener);
	}
	
	public void fireStatusUpdate() {
		System.out.println("Firing state update, new state is: " + state.getState() + " listeners size is : " + listeners.size());
		for(JobListener l : listeners) {
			l.statusUpdated(this);
		}
	}


	@Override
	public JobState getJobState() {
		return state;
	}


	@Override
	public String getJobTitle() {
		return jobTitle;
	}
	
	@Override
	public int getTotalRunLength() {
		if (mc3 != null) {
			return mc3.getRunLength();
		}
		else {
			return chain.getUserRunLength();
		}
	}


	@Override
	public int getCurrentStep() {
		if (mc3 != null) {
			return mc3.getColdChain().getCurrentState();
		}
		else {
			return chain.getCurrentState();
		}
	}
	
	private String jobTitle = "Unknown job";
	private JobState state = new JobState(this);
	private List<JobListener> listeners = new ArrayList<JobListener>();

	
	
}
