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


package dlCalculation.substitutionModels;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import cern.jet.random.Gamma;

import logging.StringUtils;
import math.RandomSource;
import modifier.DirichletModifier;
import modifier.IllegalModificationException;
import modifier.ModificationImpossibleException;
import modifier.Modifier;

import parameter.AbstractParameter;
import parameter.InvalidParameterValueException;
import sequence.DNAUtils;
import xml.XMLUtils;

/**
 * A class to encapsulate the stationary frequencies for the evolutionary model
 * These typically get modified by a Dirichlet modifier. 
 * @author brendan
 *
 */
public class BaseFrequencies extends AbstractParameter<double[]> {
	
	static final int T = DNAUtils.T;
	static final int C = DNAUtils.C;
	static final int A = DNAUtils.A;
	static final int G = DNAUtils.G;
	
	String[] logKeys = new String[]{"a.freq", "c.freq", "g.freq", "t.freq"};
			
	public BaseFrequencies(Map<String, String> attrs, Modifier<?> mod) {
		this(attrs);
		addModifier(mod);
	}
	
	public BaseFrequencies(Map<String, String> attrs) {
		super(attrs);
		currentValue = new double[4];
		
		String statStr = XMLUtils.getOptionalString(TN93Matrix.XML_STATIONARIES, attrs);
		if (statStr != null) {
			String[] stats = statStr.split("\\s+");
			if (stats.length < 4) {
				throw new IllegalArgumentException("Could not parse stationaries from argument list, got : " + statStr + " (" + stats.length + " elements)");
			}

			try {
				currentValue[A] = Double.parseDouble(stats[A]);
				currentValue[C] = Double.parseDouble(stats[C]);
				currentValue[T] = Double.parseDouble(stats[T]);
				currentValue[G] = Double.parseDouble(stats[G]);
			}
			catch (NumberFormatException nfe) {
				throw new IllegalArgumentException("Could not parse a number from the stationaries list : " + statStr);
			}	

		}
		else {
			currentValue[0] = 0.25;
			currentValue[1] = 0.25;
			currentValue[2] = 0.25;
			currentValue[3] = 0.25;
		}
		
		activeValue = currentValue;
	}
	
	
	public BaseFrequencies(double[] stationaries) {
		super(new HashMap<String, String>());
		currentValue = new double[4];
		System.arraycopy(stationaries, 0, currentValue, 0, 4);
		activeValue = currentValue;
	}
	
	
	public void addModifier(Modifier mod) {
		//TODO Make sure this modifier is OK for this parameter
		mod.setParameter(this);
		modifiers.add(mod);
		modFrequenciesKnown = false;
	}
	
	
	/**
	 * Returns the active stationary list
	 * @return
	 */
	public double[] getStationaries() {
		return getValue();
	}

	public int getKeyCount() {
		return 4;
	}
	
	/**
	 * Returns a list of keys associated with the loggable items of this param. 
	 * Default implementation is a single item whose key is 'getName()'
	 */
	public String[] getLogKeys() {
		return logKeys;
	}
	
	/**
	 * Return the current value of the log item associated with the given key.
	 * Default implementation is to always just return currentValue
	 */
	public Object getLogItem(String key) {
		double[] freqs = getStationaries();
		
		if (key.equals(logKeys[0]))
			return freqs[A];
		if (key.equals(logKeys[1]))
			return freqs[C];
		if (key.equals(logKeys[2]))
			return freqs[G];
		if (key.equals(logKeys[3]))
			return freqs[T];
		
		return null;
	}
	
	public String getLogString() {
		double[] freqs = getStationaries();
		return "" + freqs[A] + "\t" + freqs[C] + "\t" + freqs[G] + "\t" + freqs[T];		
	}
	

	
//	public static void main(String[] args) {
//		RandomSource.initialize();
//		double[] stats = new double[]{0.25, 0.25, 0.25, 0.25};
//
//		BaseFrequencies freqs = new BaseFrequencies(stats);
//		DirichletModifier mod = new DirichletModifier();
//		freqs.addModifier(mod);
//		
//		try {
//			BufferedWriter writer = new BufferedWriter(new FileWriter("freqData.log"));
//			writer.write("A \t C \t T \t G \n");
//			for(int i=0; i<100000; i++) {
//				try {
//					mod.modify();
//				} catch (InvalidParameterValueException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				} catch (IllegalModificationException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				} catch (ModificationImpossibleException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//				freqs.acceptValue();
//				stats = freqs.getValue();
//				if (i%50==0) {
//					System.out.println(stats[0] + "\t" + stats[1] + "\t" + stats[2] + "\t" + stats[3]);
//					writer.write(stats[0] + "\t" + stats[1] + "\t" + stats[2] + "\t" + stats[3] + "\n" );
//				}
//			}
//			writer.close();
//
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
}
