package mcmc;

/**
 * Small debugging class to help with debugging. 
 * @author brendan
 *
 */
public class MCDebug implements MCMCListener {

	MCMC chain = null;
	
	@Override
	public void setMCMC(MCMC chain) {
		this.chain = chain;
	}

	@Override
	public void newState(int stateNumber) {
//		if (stateNumber == 8110) {
//			chain.verbose = true;
//		}
	}

	@Override
	public void chainIsFinished() {	}

	
}
