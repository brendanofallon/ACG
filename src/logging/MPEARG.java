package logging;

import java.io.File;
import java.io.IOException;

import component.LikelihoodComponent;

import dlCalculation.DataLikelihood;
import parameter.AbstractParameter;
import arg.ARG;
import arg.argIO.ARGParser;
import mcmc.MCMC;
import mcmc.MCMCListener;

/**
 * A logger that writes the arg with the maximum data likelihood to a file. 
 * @author brendano
 *
 */
public class MPEARG implements MCMCListener {
	
	ARG arg;
	String filename;
	ARGParser parser = new ARGParser();
	DataLikelihood dataLikelihood = null;
	double maxLnL = Double.NEGATIVE_INFINITY; //Tracks current max likelihood so we know if we reach a new max
	int burnin = 1000000;
	int frequency = 10000;
	
	public MPEARG(DataLikelihood dl, String fileName, int frequency) {
		this(dl, fileName);
		this.frequency = frequency;
	}
	
	public MPEARG(DataLikelihood dl, String fileName) {
		this.arg = dl.getTree();
		this.dataLikelihood = dl;
		this.filename = fileName;
		if (!filename.endsWith(".xml")) 
			filename = filename + ".xml";
	}

	@Override
	public void newState(int stateNumber) {
		if (stateNumber > burnin && stateNumber % frequency == 0) {
			File file = new File(filename);
			try {
				Double currentDL = dataLikelihood.getCurrentLogLikelihood();
				if (currentDL > maxLnL) {
					System.out.println("Found new MPE arg, likelihood : " + currentDL);
					maxLnL = currentDL;
					parser.writeARG(arg, file);	
				}
			} catch (IOException e) {
				System.err.println("Could not write recovery arg to file, reason: " + e);
			}
			catch(RuntimeException ex) {
				System.err.println("Uh oh,  caught runtime exception while attempting to write LastARG!");
			}
		}
		
		
	}

	@Override
	public void chainIsFinished() {
		//Nothing to do here
	}
	
	
	@Override
	public void setMCMC(MCMC chain) {
		dataLikelihood = findARG(chain);
		if (dataLikelihood == null) {
			throw new IllegalArgumentException("Cannot listen to a chain without an data likelihood component");
		}
		this.arg = dataLikelihood.getTree();
	}
	
	private DataLikelihood findARG(MCMC mc) {
		for(LikelihoodComponent comp : mc.getComponents()) {
			if (comp instanceof DataLikelihood) {
				return (DataLikelihood)comp;
			}
		}
		return null;
	}

}
