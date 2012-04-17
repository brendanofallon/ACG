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

import gui.document.ACGDocument;
import gui.inputPanels.Configurator.InputConfigException;
import logging.BreakpointDensity;
import logging.PropertyLogger;
import logging.StateLogger;

import org.w3c.dom.Element;

public class StateLoggerModel extends LoggerModel {

	private boolean echoToScreen = false;

	public StateLoggerModel() {
		setOutputFilename("StateLog.log");
	}
	
	public boolean getEchoToScreen() {
		return echoToScreen;
	}

	public void setEchoToScreen(boolean echoToScreen) {
		this.echoToScreen = echoToScreen;
	}

	@Override
	public Class getLoggerClass() {
		return StateLogger.class;
	}
	
	@Override
	public void readElements(ACGDocument doc) throws InputConfigException {
		Element el = getSingleElementForClass(doc, StateLogger.class);
		readFilename(el);
		readFrequency(el);
		readBurnin(el);
		readLabel(el);
		String echoStr = el.getAttribute(StateLogger.XML_ECHOTOSCREEN);
		if (echoStr != null && echoStr.length()>0) {
			Boolean echo = Boolean.parseBoolean(echoStr);
			if (echo != null)
				setEchoToScreen(echo);
		}
		
	}

	@Override
	protected Element getElement(ACGDocument doc) {
		Element el = createElement(doc, getModelLabel(), logging.StateLogger.class );
		el.setAttribute(StateLogger.XML_FILENAME, getOutputFilename());
		el.setAttribute(StateLogger.XML_FREQUENCY, "" + getLogFrequency());
		el.setAttribute(StateLogger.XML_ECHOTOSCREEN, "" + getEchoToScreen());
		el.setAttribute(PropertyLogger.LABEL, getModelLabel());
		return el;
	}

	@Override
	public String getDefaultLabel() {
		return "StateLogger";
	}

}
