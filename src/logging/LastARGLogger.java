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
import java.io.IOException;
import java.util.Map;

import org.w3c.dom.ranges.RangeException;

import parameter.AbstractParameter;
import xml.XMLUtils;

import arg.ARG;
import arg.argIO.ARGParser;
import mcmc.MCMC;
import mcmc.MCMCListener;

/**
 * Periodically writes an arg to a file, over-writing the old one each time. This is so an ARG can 
 * easily be recovered to initiate new runs, if desired. 
 * @author brendan
 *
 */
public class LastARGLogger implements MCMCListener {

	ARG arg;
	String filename;
	ARGParser parser = new ARGParser();
	int frequency = 10000;
	
	public LastARGLogger(ARG arg, String fileName, int frequency) {
		this(arg, fileName);
		this.frequency = frequency;
	}
	
	
	public LastARGLogger(Map<String, String> attrs, ARG arg) {
		this.arg = arg;
		this.filename = XMLUtils.getStringOrFail("filename", attrs);
		if (!filename.endsWith(".xml")) 
			filename = filename + ".xml";
		
		Integer freq = XMLUtils.getOptionalInteger("frequency", attrs);
		if (freq != null)
			frequency = freq;
	}
	
	public LastARGLogger(ARG arg, String fileName) {
		this.arg = arg;
		this.filename = fileName;
		if (!filename.endsWith(".xml")) 
			filename = filename + ".xml";
	}
	
	

	@Override
	public void newState(int stateNumber) {
		if (stateNumber % frequency == 0) {
			File file = new File(filename);
			try {
				parser.writeARG(arg, file);
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
		File file = new File(filename);
		try {
			parser.writeARG(arg, file);
			System.out.println("Final arg written to file : " + filename);
		} catch (IOException e) {
			System.err.println("Could not write recovery arg to file, reason: " + e);
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
