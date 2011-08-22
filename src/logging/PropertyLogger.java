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
public abstract class PropertyLogger implements MCMCListener {
	
	public static final String FREQUENCY = "frequency";
	public static final String BURNIN = "burnin";
	public static final String FILENAME = "filename";
	public static final String WRITE_TEMP_DATA = "write.temp.data";
	
	MCMC chain = null;
	int collectionFrequency;
	int calls = 0;
	//Don't collect info before this number of states
	int burnin = 500000;
	
	protected boolean writeTempData = false;
	protected int writeTempFrequency = 100000; //Frequency to write in MCMC gens
	
	//We periodically write what we know to a file so that if a long
	//run is interrupted some info can be recovered
	protected String tempFileName;
	
	protected PrintStream outputStream = System.out;
	
	SiteMap siteMap = null; //Used for mapping output to other coordinates

	public PropertyLogger(Map<String, String> attrs) {
		Integer burn = XMLUtils.getOptionalInteger(BURNIN, attrs);
		if (burn == null)
			this.burnin = 1000000;
		else
			this.burnin = burn;
		
		Integer collect = XMLUtils.getOptionalInteger(FREQUENCY, attrs);
		if (collect == null)
			this.collectionFrequency = 10000;
		else
			this.collectionFrequency = collect;
		
		String filename = XMLUtils.getStringOrFail(FILENAME, attrs);
		File file = new File(filename);
		try {
			setOutputFile(file);
		} catch (FileNotFoundException e) {
			System.out.println("Could not open output file for logging : " + filename);
			//Shouldn't happen, right?
			e.printStackTrace();
		}
		
		Boolean writeTempData = XMLUtils.getOptionalBoolean(WRITE_TEMP_DATA, attrs);
		if (writeTempData != null) {
			this.writeTempData = writeTempData;
			tempFileName = filename;
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
	
	public void setOutputFile(File outputfile) throws FileNotFoundException {
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
		outputStream.println(str);
		outputStream.close();
	}
}
