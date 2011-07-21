package logging;

import java.io.File;
import java.io.IOException;

import arg.ARG;
import arg.argIO.ARGParser;
import arg.argIO.ARGWriter;
import mcmc.MCMC;
import mcmc.MCMCListener;

/**
 * Debugging class the writes args to a file after a certain number of MCMC states have passed
 * @author brendano
 *
 */
public class ErrorTreeWriter implements MCMCListener {

	ARG arg = null;
	ARGWriter argWriter = new ARGParser();
	
	public ErrorTreeWriter(ARG arg) {
		this.arg = arg;
	}
	

	@Override
	public void newState(int state) {
		if (state > 1204) 
			writeTree(state);
	}

	
	private void writeTree(int state) {
		try {
			argWriter.writeARG(arg, new File("errorTree_state" + state + ".xml"));
		} catch (IOException e) {
			System.err.println("Error writing error tree: " + e);
			e.printStackTrace();
		}
	}
	
	
	@Override
	public void chainIsFinished() {
		//Nothing to do here
	}

	@Override
	public void setMCMC(MCMC chain) {
		//Dont really care about this either
	}
}
