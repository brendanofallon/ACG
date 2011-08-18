package logging;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Map;

import parameter.AbstractParameter;

import arg.ARG;
import arg.TreeUtils;
import mcmc.MCMC;
import mcmc.MCMCListener;

/**
 * Marginal tree loggers emit a newick-formatted tree corresponding to a given site to a file, one tree per line
 * @author brendano
 *
 */
public class MarginalTreeLogger implements MCMCListener {

	public static final String XML_SITE = "site";
	public static final String XML_FILENAME = "filename";
	public static final String XML_FREQUENCY = "frequency";
	public static final String XML_BURNIN = "burnin";
	
	ARG arg;
	int site;
	int logFrequency = 10000;
	int burnin = 1000000;
	PrintStream writer;
	
	public MarginalTreeLogger(ARG arg, int site, int burnin, int frequency, String filename) {
		this.arg = arg;
		this.site = site;
		this.burnin = burnin;
		this.logFrequency = frequency;
		if (filename == null)
			writer = System.out;
		else {
			try {
				writer = new PrintStream(new File(filename));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public MarginalTreeLogger(Map<String, String> attrs, ARG arg) {
		this.arg = arg;
		
		String siteStr = attrs.get(XML_SITE);
		if (siteStr != null) {
			try {
				site = Integer.parseInt(siteStr);
			}
			catch (NumberFormatException nfe) {
				System.err.println("Could not parse a site for marginal tree logger, got '" + siteStr + "' ");
				site = -1;
				System.exit(1);
			}
		}
		else {
			throw new IllegalArgumentException("You must specify a ('site=X') to construct a marginal tree logger");
		}
		
		String filename = attrs.get(XML_FILENAME);
		if (filename != null) {
			try {
				File file = new File(filename);
				writer = new PrintStream( file);
			}
			catch (IOException ex) {
				System.err.println("Could not create output stream for file : " + filename);
				System.exit(1);
			}
		}
		else {
			throw new IllegalArgumentException("You must specify a file name ('filename=X') to construct a marginal tree logger");
		}
				
		String freqStr = attrs.get(XML_FREQUENCY);
		if (freqStr != null) {
			Integer freq = Integer.parseInt(freqStr);
			if (freq != null)
				logFrequency = freq;
		}
		
		String burnStr = attrs.get(XML_BURNIN);
		if (burnStr != null) {
			Integer burnParsed = Integer.parseInt(freqStr);
			if (burnParsed != null)
				burnin = burnParsed;
		}
		
	}

	
	public MarginalTreeLogger(ARG arg, int site, String filename) {
		this.arg = arg;
		this.site = site;
		File file = new File(filename);
		try {
			writer = new PrintStream(file);
		} catch (IOException e) {
			System.err.println("Could not create marginal tree writer with file name: " + filename);
		}
	}
	
	
	@Override
	public void newState(int stateNumber) {
		if (stateNumber>burnin && stateNumber % logFrequency == 0) {
			logTree(stateNumber);
		}
		
	}

	private void logTree(int state) {
		String newick = TreeUtils.getMarginalNewickTree(arg, site);
		writer.println(" [ state= " + state + "] \t " + newick);
		writer.flush();
	}

	@Override
	public void chainIsFinished() {
		writer.close();
	}

	
	@Override
	public void setMCMC(MCMC chain) {
		ARG newARG = findARG(chain);
		if (newARG == null) {
			throw new IllegalArgumentException("Cannot listen to a chain without an arg  parameter");
		}
		this.arg = newARG;
	}
	
	private ARG findARG(MCMC mc) {
		for(AbstractParameter<?> par : mc.getParameters()) {
			if (par instanceof ARG)
				return (ARG)par;
		}
		return null;
	}
}
