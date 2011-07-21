package mcmc;

/**
 * These listen to accept / reject events from an MCMC chain
 * @author brendano
 *
 */
public interface AcceptRejectListener {

	/**
	 * Called when a new state is accepted by the chain
	 */
	public void stateAccepted();
	
	/**
	 * Called when a new state is rejected by the chain
	 */
	public void stateRejected();
	
}
