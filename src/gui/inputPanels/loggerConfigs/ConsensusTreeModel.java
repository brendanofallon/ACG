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

import logging.ConsensusTreeLogger;
import logging.PropertyLogger;
import gui.document.ACGDocument;
import gui.inputPanels.Configurator.InputConfigException;

import org.w3c.dom.Element;

public class ConsensusTreeModel extends LoggerModel {
	
	//Site at which to consense trees
	int site = 1;
	boolean useDefaultFilename = true;

	
	public boolean getUseDefaultFilename() {
		return useDefaultFilename;
	}
	
	public int getSite() {
		return site;
	}

	public void setSite(int site) {
		this.site = site;
	}

	@Override
	public Class getLoggerClass() {
		return ConsensusTreeLogger.class;
	}

	public void setOutputFilename(String outputFilename) {
		if (! outputFilename.equals( this.outputFilename)) {
			useDefaultFilename = false;
		}
		this.outputFilename = outputFilename;
	}
	
	@Override
	public String getDefaultLabel() {
		return "ConsensusTree";
	}

	@Override
	protected Element getElement(ACGDocument doc) throws InputConfigException {
		if (argRef == null)
			throw new InputConfigException("ARG reference not set for ConsensusTree logger");
		
		//Could consider doing something here to ensure that we dont end up specifying identical labels...
		String elementLabel = getModelLabel().replace(" ", "_");
		
		Element el = createElement(doc, elementLabel, logging.ConsensusTreeLogger.class );
		if (useDefaultFilename)
			el.setAttribute(PropertyLogger.FILENAME, getModelLabel() + getSite() + ".tre");
		else
			el.setAttribute(PropertyLogger.FILENAME, getOutputFilename());
		el.setAttribute(PropertyLogger.FREQUENCY, "" + getLogFrequency());
		el.setAttribute(PropertyLogger.BURNIN, "" + getBurnin());
		el.setAttribute(PropertyLogger.LABEL, getModelLabel());
		el.setAttribute(ConsensusTreeLogger.XML_SITE, "" + site);
		
		Element argEl = doc.createElement( argRef.getModelLabel() );
		el.appendChild(argEl);
		return el;
	}

	@Override
	public void readElements(ACGDocument doc) throws InputConfigException {
		Element el = getSingleElementForClass(doc, ConsensusTreeLogger.class);
		readFilename(el);
		readBurnin(el);
		readFrequency(el);
		readLabel(el);
		String echoStr = el.getAttribute(ConsensusTreeLogger.XML_SITE);
		if (echoStr != null && echoStr.length()>0) {
			try {
				Integer bins = Integer.parseInt(echoStr);
				setSite(bins);
			}
			catch (NumberFormatException nfe) {
				throw new InputConfigException("Could not parse site for element " + el.getNodeName());
			}
		}
	}

}
