package gui;

import javax.swing.SwingWorker;

import mcmc.MCMC;
import mcmc.MCMCListener;

public class ExecutingChain extends SwingWorker implements MCMCListener {

	MCMC chain;
	private boolean paused = false;
	
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
	
	public void setPaused(boolean paused) {
		this.paused = paused;
	}
	
	@Override
	public void newState(int stateNumber) {
		while(paused) {
			try {
				Thread.sleep(500); //Wake up every 0.5 seconds to see if we're unpaused
			} catch (InterruptedException e) {
				//Dont do anything
			} 
		}
	}

	@Override
	public void chainIsFinished() {
		// TODO Auto-generated method stub
		
	}
	

	@Override
	public void setMCMC(MCMC chain) { }
}
