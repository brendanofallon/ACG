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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Map;

import sequence.SiteMap;
import xml.XMLUtils;

import mcmc.MCMC;
import mcmc.MCMCListener;

/**
 * Convenient base class for loggers with some useful methods. Handles writing of temporary data, which
 * is summary-like data written to files as the MCMC runs, so if things end unexpectedly some
 * data will remain accessible. 
 * @author brendano
 *
 */
public abstract class PropertyLogger implements MCMCListener, Named {
	
	public static final String FREQUENCY = "frequency";
	public static final String BURNIN = "burnin";
	public static final String FILENAME = "filename";
	public static final String WRITE_TEMP_DATA = "write.temp.data";
	public static final String lineSep = System.getProperty("line.separator");
	
	protected MCMC chain = null;
	int collectionFrequency;
	int calls = 0;
	//Don't collect info before this number of states
	int burnin = 500000;
	
	protected boolean writeTempData = true;
	protected int writeTempFrequency = 100000; //Frequency to write in MCMC gens
	
	//We periodically write what we know to a file so that if a long
	//run is interrupted some info can be recovered
	protected String tempFileName;
	
	protected PrintStream outputStream = System.out;
	
	SiteMap siteMap = null; //Used for mapping output to other coordinates

	public PropertyLogger(Map<String, String> attrs) {
		Integer burn = XMLUtils.getOptionalInteger(BURNIN, attrs);
		if (burn == null) {
			this.burnin = 1000000;
			System.err.println("WARNING : Burnin not specified in attributes for logger : " + this.getClass().getCanonicalName() + ", defaulting to 1,000,000");
		}
		else
			this.burnin = burn;
		
		Integer collect = XMLUtils.getOptionalInteger(FREQUENCY, attrs);
		if (collect == null)
			this.collectionFrequency = 10000;
		else
			this.collectionFrequency = collect;
		
		String filename = XMLUtils.getStringOrFail(FILENAME, attrs);
		if (filename == null || filename.length() == 0) {
			writeTempData = false;
			outputStream = null;
			System.out.println("Filename is null, not writing property logger data to file.");
		}
		else {
			File file = new File(filename);
			try {
				setOutputFile(file);
			} catch (FileNotFoundException e) {
				System.err.println("Could not open output file for logging : " + filename);
				//Shouldn't happen, right?
				e.printStackTrace();
			} catch (IOException e) {
				System.err.println("Could not open output file for logging : " + filename);
				e.printStackTrace();
			}

			Boolean writeTempData = XMLUtils.getOptionalBoolean(WRITE_TEMP_DATA, attrs);
			if (writeTempData != null) {
				this.writeTempData = writeTempData;
				tempFileName = filename;
			}
			else {
				//Nothing provided, which means we ARE writing temp data, to a file of the same name as the final output file
				if (filename != null)
					tempFileName = filename;
			}
		}
	}
	
	public PropertyLogger(int burnin, int collectionFrequency) {
		this.burnin = burnin;
		this.collectionFrequency = collectionFrequency;
	}
	
	/**
	 * Set the "burn-in" value for this logger. No data will be collected when 
	 * the MCMC state is less than this value. 
	 * @param burnin
	 */
	public void setBurnin(int burnin) {
		this.burnin = burnin;
	}

	/**
	 * Returns true if the burnin period has been exceeded for this logger
	 * @return
	 */
	public boolean getBurninExceeded() {
		if (chain == null)
			return false;
		else
			return chain.getCurrentState() > burnin;
	}
	
	/**
	 * Optionally translate coordinates into different space defined by the given map. Right now this is used 
	 * to translate sites back to original coordinates when columns have been removed from input alignment
	 * @param map
	 */
	public void setSiteMap(SiteMap map) {
		this.siteMap = map;
	}
	
	/**
	 * Return a text representation of the property suitable for writing to output files. 
	 * @return
	 */
	public abstract String getSummaryString();
	
	
	/**
	 * Called when the mcmc reaches a state that's a multiple of collection frequency and is greater than burnin. 
	 * @param stateNumber
	 */
	public abstract void addValue(int stateNumber);
	
	
	public void newState(int stateNumber) {
		if (stateNumber > 0 && stateNumber % collectionFrequency == 0 && stateNumber>=burnin) {
			addValue(stateNumber);
		}
		
		if (writeTempData && stateNumber > 0 && stateNumber % writeTempFrequency == 0 && stateNumber>burnin) {
			writeTempData();
		}	
	}
	
	public void setOutputFile(File outputfile) throws IOException {
		System.out.println("Attempting to create file : " + outputfile.getAbsolutePath());
		if (outputfile == null || outputfile.isDirectory())  {
			System.out.println("Output file is directory, ignoring and not writing data to file.");
			outputStream = null;
			return;
		}
		if (! outputfile.exists()) {
			outputfile.createNewFile();
		}
		outputStream = new PrintStream(new FileOutputStream(outputfile));
	}
	
	public void setPrintStream(PrintStream stream) {
		outputStream = stream;
	}
	
	
	protected void writeTempData() {
		if (tempFileName == null)
			return;
		
		String str = getSummaryString();
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(tempFileName));
			writer.write(str);
			writer.close();
		} catch (IOException e) {
			
		}
	}
	
	/**
	 * If true write data periodically to the given file
	 * @param write
	 */
	public void setWriteTempData(boolean write, int frequency, String filename) {
		this.writeTempData = write;
		this.writeTempFrequency = frequency;
		this.tempFileName = filename;	
	}
	
	
	@Override
	public void setMCMC(MCMC chain) {
		this.chain = chain;
	}
	
	@Override
	public void chainIsFinished() {
		String str = getSummaryString();
		if (outputStream != null) {
			outputStream.println(str);
			outputStream.close();
		}
	}
}
