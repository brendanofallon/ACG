package mcmc;

/**
 * These things are notified when the MCMC state changes. 
 * @author brendan
 *
 */
public interface MCMCListener {
	
	public void setMCMC(MCMC chain);

	/**
	 * Called when a new state has been reached by the chain
	 * @param stateNumber
	 */
	public void newState(int stateNumber);
		
	/**
	 * Called when the chain has completed
	 */
	public void chainIsFinished();
}
