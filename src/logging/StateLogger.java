package logging;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import parameter.Parameter;

import component.LikelihoodComponent;

import mcmc.MCMC;
import mcmc.MCMCListener;

/**
 * A logger that writes a parameter values and likelihoods to a one or more print streams, including system.out . Use addStream(newStream)
 * to add more print streams to print to. 
 * @author brendan
 *
 */
public class StateLogger implements MCMCListener {

	List<PrintStream> streams = new ArrayList<PrintStream>();
	
	MCMC chain;
	boolean first = true;
	
	long lastTime = 0;
	int lastState = 0;
	
	int logFrequency = 100;
	
	DecimalFormat bigFormatter = new DecimalFormat("###0.0###");
	DecimalFormat smallFormatter = new DecimalFormat("###0.0#####");
	
	public StateLogger() {
		lastTime = System.currentTimeMillis();
	}
	
	public StateLogger(Map<String, String> attrs) {
		lastTime = System.currentTimeMillis();
		String filename = attrs.get("filename");
		if (filename != null) {
			try {
				File file = new File(filename);
				addStream( new PrintStream(new FileOutputStream(file)));
				System.out.println("Writing state log file to " + filename);
			}
			catch (IOException ex) {
				System.err.println("Could not create output stream for file : " + filename);
			}
		}
		
		String writeStdout = attrs.get("echoToScreen");
		if (writeStdout != null) {
			boolean write = Boolean.parseBoolean(writeStdout);
			if (write) {
				addStream( System.out);
			}
		}
		
		String freqStr = attrs.get("frequency");
		if (freqStr != null) {
			Integer freq = Integer.parseInt(freqStr);
			if (freq != null)
				logFrequency = freq;
		}
		
	}
	
	public StateLogger(MCMC chain) {
		this.chain = chain;
		lastTime = System.currentTimeMillis();
	}
	
	public StateLogger(String fileName, MCMC chain) {
		this.chain = chain;
		File file = new File(fileName);
		try {
			streams.add( new PrintStream(new FileOutputStream(file)));	
		}
		catch (Exception ex) {
			
		}
	}
	
	
	public StateLogger(PrintStream ps) {
		streams.add(ps);
	}
	
	/**
	 * Add a new stream to the list of streams to manage
	 * @param ps
	 */
	public void addStream(PrintStream ps) {
		streams.add(ps);
	}
	
	/**
	 * Remove the given streams from the list of streams to write output to 
	 * @param ps
	 */
	public void removeStream(PrintStream ps) {
		streams.remove(ps);
	}
	
	/**
	 * Write the given string to all of the streams
	 * @param str
	 */
	public void logLine(String str) {
		for(PrintStream ps : streams) {
			ps.print(str);
		}
	}

	
	public String getLogHeader() {
		StringBuilder str = new StringBuilder();

		str.append("State \t");
		for(LikelihoodComponent comp : chain.getComponents()) {
			str.append(comp.getLogHeader() + "\t");
		}
		
		for(Parameter<?> p : chain.getParameters()) {
			str.append(p.getLogHeader() + "\t");
		}
		str.append(" mc.speed \n");
		return str.toString();
	}
	
	public String getLogString() {
		StringBuilder str = new StringBuilder();
		int state = chain.getStatesProposed();
		str.append(state + "\t");
		for(LikelihoodComponent comp : chain.getComponents()) {
			str.append(comp.getLogString() + "\t");
		}
		
		for(Parameter<?> p : chain.getParameters()) {
			str.append(p.getLogString() + "\t");
		}
		
		long thisTime = System.currentTimeMillis();
		long elapsedMS = thisTime - lastTime;
		if (elapsedMS>0) {
			int currentState = chain.getStatesProposed();
			double secsPerState = elapsedMS/(double)(currentState - lastState)/1000.0;
			double statesPerSec = 1.0/(secsPerState);
			lastState = currentState;
			lastTime = thisTime;
			str.append( StringUtils.format(statesPerSec,2) );
		}
		else {
			str.append("0");
		}
		str.append("\n");
		return str.toString();
	}
	

	@Override
	public void newState(int stateNumber) {
		logNewLine();
	}
	
	private void logNewLine() {
		if (first) {
			logLine( getLogHeader());
			first = false;
		}
		
		if (chain.getStatesProposed() % logFrequency == 0) {
			logLine( getLogString());
		}
	}
	
	public void chainIsFinished() {
		//We don't care about this now. 
	}

	@Override
	public void setMCMC(MCMC chain) {
		this.chain = chain;
	}


}

