package logging;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
	ARG arg;
	int site;
	int logFrequency = 20000;
	int burnin = 5000000;
	BufferedWriter writer;
	
	
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
		
		String filename = attrs.get("filename");
		if (filename != null) {
			try {
				File file = new File(filename);
				writer = new BufferedWriter( new FileWriter(file));
			}
			catch (IOException ex) {
				System.err.println("Could not create output stream for file : " + filename);
				System.exit(1);
			}
		}
		else {
			throw new IllegalArgumentException("You must specify a file name ('filename=X') to construct a marginal tree logger");
		}
				
		String freqStr = attrs.get("frequency");
		if (freqStr != null) {
			Integer freq = Integer.parseInt(freqStr);
			if (freq != null)
				logFrequency = freq;
		}
		
	}

	
	public MarginalTreeLogger(ARG arg, int site, String filename) {
		this.arg = arg;
		this.site = site;
		File file = new File(filename);
		try {
			writer = new BufferedWriter( new FileWriter(file));
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
		try {
			writer.write(" [ state= " + state + "] \t " + newick + "\n");
			writer.flush();
		} catch (IOException e) {
			System.err.println("Error writing marginal newick tree: " + e);
		}
		
	}

	@Override
	public void chainIsFinished() {
		try {
			writer.close();
		} catch (IOException e) {
			//Don't care
		}
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
