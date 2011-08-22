package logging;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
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
	
	String filename;
	ARGParser parser = new ARGParser();
	DataLikelihood dataLikelihood = null;
	MCMC currentChain = null;
	double maxLnL = Double.NEGATIVE_INFINITY; //Tracks current max likelihood so we know if we reach a new max
	int burnin = 10000;
	int frequency = 10000;
	
	public MPEARG(DataLikelihood dl, String fileName, int frequency) {
		this(dl, fileName);
		this.frequency = frequency;
	}
	
	public MPEARG(DataLikelihood dl, String fileName) {
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
				double heat = currentChain.getTemperature();
				if (heat != 1.0) {
					System.out.println("Hmm chain heat is not 1.0, MPE arg may not be listening to the cold chain");
				}
				if (currentDL > maxLnL) {
					System.out.println("Found new MPE arg, likelihood : " + currentDL);
					maxLnL = currentDL;
					parser.writeARG(dataLikelihood.getTree(), file);
					
					//Debugging stuff here...
//					BufferedWriter veriWriter = new BufferedWriter(new FileWriter("verify_arg_recovery.trees"));
//					dataLikelihood.setChain(currentChain);
//					dataLikelihood.writeVerificationLogLine(dataLikelihood.getCurrentLogLikelihood(), veriWriter);
//					veriWriter.close();
//					
//					System.out.println("ARG has : " + dataLikelihood.getTree().getDLRecombNodes().size() + " dl recombs");
//					System.out.println("Wrote verification tree, press a key to continue");
//					System.in.read();
					
				}
			} catch (IOException e) {
				System.err.println("Could not write recovery arg to file, reason: " + e);
				e.printStackTrace();
			}
			catch(RuntimeException ex) {
				System.err.println("Uh oh, caught runtime exception while attempting to write MPE ARG");
				ex.printStackTrace();
			}
		}
		
		
	}

	@Override
	public void chainIsFinished() {
		//Nothing to do here
	}
	
	
	@Override
	public void setMCMC(MCMC chain) {
		currentChain = chain;
		dataLikelihood = findDL(chain);
		if (dataLikelihood == null) {
			throw new IllegalArgumentException("Cannot listen to a chain without a data likelihood component");
		}
	}
	
	private DataLikelihood findDL(MCMC mc) {
		for(LikelihoodComponent comp : mc.getComponents()) {
			if (comp instanceof DataLikelihood) {
				return (DataLikelihood)comp;
			}
		}
		return null;
	}

}
