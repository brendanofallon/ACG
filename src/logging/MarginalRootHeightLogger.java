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

import java.util.Map;

import math.Histogram;
import math.LazyHistogram;
import arg.ARG;
import arg.TreeUtils;
import parameter.AbstractParameter;
import xml.XMLUtils;

/**
 * Simple collector that just collects the distribution of root heights at a single marginal site 
 * @author brendano
 *
 */
public class MarginalRootHeightLogger extends PropertyLogger {

	Histogram histo = new Histogram(0, 10.0, 100);
	ARG arg;
	int site;
	
	public MarginalRootHeightLogger(ARG arg, int burnin, int collectionFrequency, int site) {
		super(burnin, collectionFrequency);
		this.arg = arg;
		this.site = site;
	}

	public MarginalRootHeightLogger(Map<String, String> attrs, ARG arg) {
		super(attrs); //Burnin, collection frequency, and file name are set in base class
		this.arg = arg;
	}

	@Override
	public String getSummaryString() {
		String str = "Distribution of marginal root heights at site " + site + " " + lineSep;
		str += histo.toString();
		return str;
	}

	@Override
	public void addValue(int stateNumber) {
		histo.addValue( TreeUtils.createMarginalTree(arg, site).getHeight());
	}


}
