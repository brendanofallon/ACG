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

import logging.ConsensusTreeLogger;
import logging.MPEARG;
import logging.PropertyLogger;

import newgui.gui.modelElements.Configurator.InputConfigException;

import org.w3c.dom.Element;

import document.ACGDocument;

import sun.util.logging.resources.logging;

public class MPEARGModel extends LoggerModel {

	@Override
	public Class getLoggerClass() {
		return MPEARG.class;
	}

	@Override
	public String getDefaultLabel() {
		return "MPE_ARG";
	}

	@Override
	protected Element getElement(ACGDocument doc) throws InputConfigException {
		if (argRef == null)
			throw new InputConfigException("ARG reference not set for MPE ARG logger");
		
		Element el = createElement(doc, getModelLabel(), MPEARG.class );
		el.setAttribute(PropertyLogger.FILENAME, getOutputFilename());
		el.setAttribute(PropertyLogger.FREQUENCY, "" + getLogFrequency());
		el.setAttribute(PropertyLogger.BURNIN, "" + getBurnin());
		el.setAttribute(PropertyLogger.LABEL, getModelLabel());
		Element argEl = doc.createElement( argRef.getModelLabel() );
		el.appendChild(argEl);
		return el;
	}

	@Override
	public void readElements(ACGDocument doc) throws InputConfigException {
		Element el = getSingleElementForClass(doc, MPEARG.class);
		readFilename(el);
		readBurnin(el);
		readFrequency(el);
		readLabel(el);
	}

}
