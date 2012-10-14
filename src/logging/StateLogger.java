/********************************************************************
*
* 	Copyright 2011 Brendan O'Fallon
*
*   Licensed under the Apache License, Version 2.0 (the "License");
*   you may not use this file except in compliance with the License.
*   You may obtain a copy of the License at
*
*       http://www.apache.org/licenses/LICENSE-2.0
*
*   Unless required by applicable law or agreed to in writing, software
*   distributed under the License is distributed on an "AS IS" BASIS,
*   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*   See the License for the specific language governing permissions and
*   limitations under the License.
*
***********************************************************************/


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
import xml.XMLUtils;

import component.LikelihoodComponent;

import mcmc.MCMC;
import mcmc.MCMCListener;

/**
 * A logger that writes a parameter values and likelihoods to one or more print streams, 
 * including system.out. Use addStream(newStream) to add more print streams to print to. 
 * @author brendan
 *
 */
public class StateLogger implements MCMCListener, Named {

	public static final String XML_FILENAME = "filename";
	public static final String XML_FREQUENCY = "frequency";
	public static final String XML_ECHOTOSCREEN = "echoToScreen";
	
	List<PrintStream> streams = new ArrayList<PrintStream>();
	
	//The chain we listen to. This will change over time in MC3 runs.
	MCMC chain;
	boolean first = true;
	
	long lastTime = 0;
	int lastState = 0;
	
	int logFrequency = 1000;
	
	DecimalFormat bigFormatter = new DecimalFormat("###0.0###");
	DecimalFormat smallFormatter = new DecimalFormat("###0.0#####");
	
	public StateLogger() {
		lastTime = System.currentTimeMillis();
	}
	
	public StateLogger(Map<String, String> attrs) {
		lastTime = System.currentTimeMillis();
		String filename = XMLUtils.getOptionalString(XML_FILENAME, attrs);
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
		
		
		String writeStdout = attrs.get(XML_ECHOTOSCREEN);
		if (writeStdout != null) {
			boolean write = Boolean.parseBoolean(writeStdout);
			if (write) {
				addStream( System.out);
			}
		}
		
		String freqStr = attrs.get(XML_FREQUENCY);
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

	/**
	 * Set the number of steps between emission of state (default is 1000)
	 * @param steps
	 */
	public void setFrequency(int steps) {
		this.logFrequency = steps;
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
		str.append(PropertyLogger.lineSep);
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

	@Override
	public String getName() {
		return "statelogger";
	}


}

