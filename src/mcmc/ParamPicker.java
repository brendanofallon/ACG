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


package mcmc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import math.RandomSource;

import parameter.AbstractParameter;
import xml.XMLLoader;

/**
 * Assists in picking of parameters in proportion to their frequency. We do this a lot so its nice to
 * have something fairly efficient here. 
 * @author brendano
 *
 */
public class ParamPicker {

	//Holds a list of parameters and their probability of being picked on any given round
	ParFreq[] params;
	
	public ParamPicker(List<AbstractParameter<?>> paramList) {
		buildList(paramList);
	}
	
	/**
	 * Pick a parameter from the list with probability relative to its sampleFrequency 
	 * @return
	 */
	public AbstractParameter<?> pickParameter() {
		
		double r = RandomSource.getNextUniform();
		AbstractParameter<?> param;
		int count = 0;
		while(r > params[count].freq) {
			r -= params[count].freq;
			count++;
		}
		
		param = params[count].param;
		return param;
	}
	
	/**
	 * Created list of parameters and probabilities and sort by descending frequency so we often dont
	 * have to go past the first few entries
	 * @param paramList
	 */
	private void buildList(List<AbstractParameter<?>> paramList) {
		double frequencySum = 0;
		List<AbstractParameter<?>> paramsToModify = new ArrayList<AbstractParameter<?>>();
		for(AbstractParameter<?> param : paramList) {
			if (param.getModifierCount() > 0) {
				paramsToModify.add(param);
				frequencySum += param.getSampleFrequency();
			}
		}
		
		params = new ParFreq[ paramsToModify.size() ];
		for(int i=0; i<params.length; i++) {
			params[i] = new ParFreq();
			params[i].param = paramsToModify.get(i);
			params[i].freq = paramsToModify.get(i).getSampleFrequency() / frequencySum;
		}
		Arrays.sort( params, new FreqComparator());
	}
	
	
	public String toString() {
		StringBuilder strB = new StringBuilder();
		for(int i=0; i<params.length; i++) {
			strB.append( params[i].param.getAttribute(XMLLoader.NODE_ID) + "\t : " + params[i].freq + "\n");

		}
		return strB.toString();
	}
	
	class FreqComparator implements Comparator<ParFreq> {

		@Override
		public int compare(ParFreq p0, ParFreq p1) {
			if (p0.param.getSampleFrequency() < p1.param.getSampleFrequency())
				return 1;
			else
				return -1;
		}
		
	}
	
	
	class ParFreq {
		AbstractParameter<?> param = null;
		double freq = 1.0;
	}
}
