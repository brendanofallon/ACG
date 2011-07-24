package gui;

import javax.swing.SwingWorker;

import mcmc.MCMC;
import mcmc.MCMCListener;

/**
 * Wraps a running MCMC chain so that it may be run in the background
 * as well as paused and restarted
 * @author brendan
 *
 */
public class ExecutingChain extends SwingWorker implements MCMCListener {

	MCMC chain;
	private boolean paused = false;
	private boolean done = false;
	
	public ExecutingChain(MCMC chain) {
		this.chain = chain;
		chain.addListener(this);
	}
	
	protected Object doInBackground() throws Exception {
		chain.run();
		return null;
	}

	public boolean isPaused() {
		return paused;
	}
	
	public void done() {
		this.done = true;
	}
	
	
	/**
	 * Set the paused state to the given value. When paused the thread sleeps, but
	 * wakes up periodically to see if it's been unpaused
	 * @param paused
	 */
	public void setPaused(boolean paused) {
		this.paused = paused;
	}
	
	@Override
	public void newState(int stateNumber) {
		while(paused) {
			try {
				Thread.sleep(500); //Wake up every 0.5 seconds to see if we're unpaused
			} catch (InterruptedException e) {
				
			} 
		}
		if (this.isCancelled()) {
			chain.abort();
		}
	}

	@Override
	public void chainIsFinished() {
		// TODO Auto-generated method stub
		
	}
	

	@Override
	public void setMCMC(MCMC chain) { }
}
