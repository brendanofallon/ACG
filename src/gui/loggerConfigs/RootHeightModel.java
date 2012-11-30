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


package gui.loggerConfigs;

import logging.BreakpointDensity;
import logging.PropertyLogger;
import logging.RootHeightDensity;

import newgui.gui.modelElements.Configurator;
import newgui.gui.modelElements.Configurator.InputConfigException;

import org.w3c.dom.Element;

import document.ACGDocument;



public class RootHeightModel extends LoggerModel {

	private int bins = 250;
	
	public int getBins() {
		return bins;
	}

	public void setBins(int bins) {
		this.bins = bins;
	}
	
	@Override
	public Class getLoggerClass() {
		return RootHeightDensity.class;
	}

	@Override
	public String getDefaultLabel() {
		return "MarginalTMRCA";
	}

	@Override
	protected Element getElement(ACGDocument doc) throws InputConfigException {
		if (argRef == null)
			throw new InputConfigException("ARG reference not set for BreakpointDensity");
		
		Element el = createElement(doc, getModelLabel(), logging.RootHeightDensity.class );
		el.setAttribute(PropertyLogger.FILENAME, getOutputFilename());
		el.setAttribute(PropertyLogger.FREQUENCY, "" + getLogFrequency());
		el.setAttribute(PropertyLogger.BURNIN, "" + getBurnin());
		el.setAttribute(BreakpointDensity.BINS, "" + bins);
		el.setAttribute(PropertyLogger.LABEL, getModelLabel());
		Element argEl = doc.createElement( argRef.getModelLabel() );
		el.appendChild(argEl);
		return el;
	}

	@Override
	public void readElements(ACGDocument doc) throws InputConfigException {
		Element el = getSingleElementForClass(doc, RootHeightDensity.class);
		readFilename(el);
		readFrequency(el);
		readBurnin(el);
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
