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

import java.text.DecimalFormat;
import java.text.NumberFormat;

import arg.ARG;
import parameter.AbstractParameter;

/**
 * Collects root height only when arg has a given number of breakpoints. 
 * @author brendano
 *
 */
public class RootHeightCollector extends HistogramCollector {

	int bps; //Number of breakpoints 
	ARG arg; //Arg to collect root height of 

	NumberFormat formatter = new DecimalFormat("0.0####");
	
	public RootHeightCollector(ARG arg, int breakpoints,
			int collectionFrequency, double histMin, double histMax, int bins) {
		super(arg, "root.height", 1000000, collectionFrequency, histMin, histMax, bins);
		
		this.arg = arg;
		this.bps = breakpoints;
	}

	
	/**
	 * Add current value of parameter to the histogram
	 */
	protected void addValue() {
		if (arg.getRecombNodes().size()==bps) {
			Double rootHeight = arg.getMaxHeight();
			histo.addValue(rootHeight);
		}
	}
	
	public void chainIsFinished() {
		outputStream.append("\n Histogram of root height conditional on breakpoints =  " + bps + "\n");
		for(int i=0; i<histo.getBinCount(); i++) {
			int site = (int)Math.round(i*histo.getBinWidth());
//			if (siteMap != null)
//				site = siteMap.getOriginalSite(site);
			outputStream.append(site + "\t" + formatter.format( histo.getFreq(i)) + "\n");
		}
		outputStream.append("Count : " + histo.getCount() + "\n");
		outputStream.append("Mean value: "+  histo.getMean() + "\n");
		outputStream.flush();
	}
}
