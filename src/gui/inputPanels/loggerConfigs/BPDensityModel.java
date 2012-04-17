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


package gui.inputPanels.loggerConfigs;

import logging.BreakpointDensity;
import logging.BreakpointLocation;
import logging.PropertyLogger;
import logging.StateLogger;
import gui.document.ACGDocument;
import gui.inputPanels.Configurator.InputConfigException;

import org.w3c.dom.Element;

public class BPDensityModel extends LoggerModel {

	private int bins = 500;
	
	public int getBins() {
		return bins;
	}

	public void setBins(int bins) {
		this.bins = bins;
	}

	@Override
	public String getDefaultLabel() {
		return "BreakpointDensity";
	}

	@Override
	public Class getLoggerClass() {
		return BreakpointDensity.class;
	}

	
	@Override
	protected Element getElement(ACGDocument doc) throws InputConfigException {
		if (argRef == null)
			throw new InputConfigException("ARG reference not set for BreakpointDensity");
		
		Element el = createElement(doc, getModelLabel(), logging.BreakpointDensity.class );
		el.setAttribute(PropertyLogger.FILENAME, getOutputFilename());
		el.setAttribute(PropertyLogger.BURNIN, "" + getBurnin());
		el.setAttribute(PropertyLogger.FREQUENCY, "" + getLogFrequency());
		el.setAttribute(PropertyLogger.LABEL, getModelLabel());
		el.setAttribute(BreakpointDensity.BINS, "" + bins);
		
		Element argEl = doc.createElement( argRef.getModelLabel() );
		el.appendChild(argEl);
		return el;
	}

	@Override
	public void readElements(ACGDocument doc) throws InputConfigException {
		Element el = getSingleElementForClass(doc, BreakpointDensity.class);
		readFilename(el);
		readBurnin(el);
		readFrequency(el);
		readLabel(el);
		String echoStr = el.getAttribute(BreakpointDensity.BINS);
		if (echoStr != null && echoStr.length()>0) {
			try {
				Integer bins = Integer.parseInt(echoStr);
				setBins(bins);
			}
			catch (NumberFormatException nfe) {
				throw new InputConfigException("Could not parse number of bins for element " + el.getNodeName());
			}
		}
	}

}
