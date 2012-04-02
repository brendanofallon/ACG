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

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import gui.ErrorWindow;
import gui.document.ACGDocument;

import javax.swing.SwingWorker;

import xml.InvalidInputFileException;

import jobqueue.JobState.State;

import mcmc.MCMC;
import mcmc.MCMCListener;
import mcmc.mc3.MC3;

/**
 * Wraps a runnable MCMC chain or MC3 object so that it may be run in the background.
 * as well as paused and restarted. 
 * @author brendan
 *
 */
public class ExecutingChain extends SwingWorker implements MCMCListener, ACGJob {

	protected MCMC chain = null; //Will be null if user supplies an MC3 object to constructor
	protected MC3 mc3 = null;		//Will be null if user supplies MCMC object to constructor
	protected MCMC coldChain = null; //Reference to cold chain, only non-null if we're in MC3 mode 
	private boolean paused = false;
	
	//Keep track of start and end time for logging purposes
	private Date startTime =  null;
	private Date endTime = null;
	
	/**
	 * Create a new ExecutingChain that can run the analysis described in the ACGDocument provided.
	 * This constructor immediately calls doc.loadAndVerifyClasses(), doc.turnOffMCMC() and doc.instantiateAll()
	 * to ensure that we can load classes and create objects, and that the chain is not started immediately 
	 * @param doc
	 * @throws Exception
	 */
	public ExecutingChain(ACGDocument doc) throws Exception {
		doc.loadAndVerifyClasses();
		doc.turnOffMCMC(); //Make sure we don't start running the chain immediately, which is the default behavior
		if (! doc.objectsCreated())
			doc.instantiateAll();
		
		List<String> mcLabels = doc.getMCMCLabels();
		if (mcLabels.size()==0) {
			throw new InvalidInputFileException("Could not find any MCMC objects");
		}
		
		List<String> mcmcmcLabels = doc.getLabelForClass(MC3.class);
		if (mcmcmcLabels.size() > 0) {
			try {
				mc3 = (MC3)doc.getObjectForLabel(mcmcmcLabels.get(0));
				this.coldChain = mc3.getColdChain();
				coldChain.addListener(this);
			} catch (InstantiationException e) {
				throw new InvalidInputFileException("Could not create mc3 object : " + e.getMessage());
			} catch (IllegalAccessException e) {
				throw new InvalidInputFileException("Could not create mc3 object : " + e.getMessage());
			} catch (InvocationTargetException e) {
				throw new InvalidInputFileException("Could not create mc3 object : " + e.getMessage());
			}
			
		}
		
		if (mcLabels.size()==1) {
			try {
				chain = (MCMC)doc.getObjectForLabel(mcLabels.get(0));
				chain.addListener(this);
			} catch (InstantiationException e) {
				throw new InvalidInputFileException("Could not create mcmc object : " + e.getMessage());
			} catch (IllegalAccessException e) {
				throw new InvalidInputFileException("Could not create mcmc object : " + e.getMessage());
			} catch (InvocationTargetException e) {
				throw new InvalidInputFileException("Could not create mcmc object : " + e.getMessage());
			}
			
		}
	}
	
//	public ExecutingChain(MCMC chain) {
//		this.chain = chain;
//		chain.addListener(this);
//	}
//	
//	
//	public ExecutingChain(MC3 mc3) {
//		this.mc3 = mc3;
//		this.coldChain = mc3.getColdChain();
//		coldChain.addListener(this);
//	}

	/**
	 * Returns true if we're using an MC3 (Metropolis-coupled) model 
	 * with multiple chains
	 */
	public boolean isMC3() {
		return mc3 != null;
	}
	
	/**
	 * Get the total number of MCMC chains running, this is always one
	 * unless we're using MC3
	 * @return
	 */
	public int getChainCount() {
		if (mc3 == null)
			return 1;
		else {
			return mc3.getChainCount();
		}
	}
	
	/**
	 * Return the number of threads employed in the MC3 run, or 1 if there's 
	 * no MC3 model 
	 * @return
	 */
	public int getThreadCount() {
		if (mc3 == null)
			return 1;
		else {
			return mc3.getThreadCount();
		}
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
	
//	public void done() {
//		this.done = true;
//	}
	
	/**
	 * We actually retain the ability to add listeners to the chain at any time
	 * @param l
	 */
	public void addListener(MCMCListener l) {
		if (mc3 == null)
			chain.addListener(l);
		else {
			mc3.addListener(l);
		}
	}
	
	/**
	 * Set the paused state to the given value. When paused the thread (or MC3) sleeps, but
	 * wakes up periodically to see if it's been unpaused
	 * @param paused
	 */
	public void setPaused(boolean paused) {
		this.paused = paused;
		if (state.getState() != State.ERROR && state.getState() != State.COMPLETED) {
			if (paused)
				state.setState(State.PAUSED);
			else 
				state.setState(State.RUNNING);
		}
		if (mc3 != null) {
			mc3.setPaused(paused);
		}
		fireStatusUpdate();

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
		endTime = new Date();
		fireStatusUpdate();
	}
	
	/**
	 * Obtain a date object representing the time at which this job was executed.
	 * This is null if the job has not been started
	 * @return
	 */
	public Date getStartTime() {
		return startTime;
	}
	
	/**
	 * Obtain date object representing the completion date of this chain (actually reflects
	 * whenever chainIsFinished is called)
	 * @return
	 */
	public Date getEndTime() {
		return endTime;
	}

	@Override
	public void setMCMC(MCMC chain) { 
		if (mc3 != null)
			this.coldChain = chain;
	}


	@Override
	public void beginJob() {
		state.setState(State.RUNNING);

		startTime = new Date();
		System.out.println("Running job, start time is:" + startTime);
		try {

			if (mc3 != null) {
				mc3.run();
			}
			else {
				chain.run();
			}
			
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
		endTime = new Date();
		System.out.println("Job is done time is:" + endTime);

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
	
	
	@Override
	public void pause() {
		setPaused(true); 
	}

	@Override
	public void resume() {
		setPaused(false);
	}

	@Override
	public void abort() {
		if (state.getState() != State.COMPLETED)
			endTime = new Date();
		state.setState(State.COMPLETED);
		if (mc3 != null) {
			mc3.abort();
		}
		else {
			chain.abort();
		}
	}

	private String jobTitle = "Unknown job";
	private JobState state = new JobState(this);
	private List<JobListener> listeners = new ArrayList<JobListener>();

	
}
