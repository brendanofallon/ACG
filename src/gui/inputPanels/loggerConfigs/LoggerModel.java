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

import java.util.ArrayList;
import java.util.List;

import logging.PropertyLogger;
import logging.StateLogger;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import gui.document.ACGDocument;
import gui.inputPanels.ARGModelElement;
import gui.inputPanels.DoubleParamElement;
import gui.inputPanels.ModelElement;
import gui.inputPanels.Configurator.InputConfigException;

/**
 * Abstract base class for objects which can read & write XML from loggers 
 * @author brendano
 *
 */
public abstract class LoggerModel extends ModelElement {

	protected String modelLabel = null;
	protected String outputFilename = null;
	protected int logFrequency = 10000;
	protected int burnin = 1000000;	
	protected ARGModelElement argRef = null; //Many loggers need a reference to the ARG
	
	//We maintain a link to a view object so we can be sure all fields have been "updated" before we 
	//create any XML nodes
	private AbstractLoggerView view = null;
	
	public LoggerModel() {
		modelLabel = getDefaultLabel();
	}
	
	/**
	 * Set the "view" associated with this model. 
	 * @param view
	 */
	public void setView(AbstractLoggerView view) {
		this.view = view;
	}
	
	public abstract Class getLoggerClass();
	
	/**
	 * A reference to the ARGModelElement associated with this logger. May be null if no reference is required. 
	 * @return
	 */
	public ARGModelElement getArgRef() {
		return argRef;
	}

	public void setArgRef(ARGModelElement argRef) {
		this.argRef = argRef;
	}
	
	protected String readFilename(Element el) {
		String filename = el.getAttribute(StateLogger.XML_FILENAME);
		if (filename != null && filename.length()>0) {
			System.out.println("Logger " + this.getDefaultLabel() + " setting output filename to : " + filename);
			setOutputFilename(filename);
		}
		return filename;
	}
	
	protected Integer readBurnin(Element el) {
		String burnStr = el.getAttribute(PropertyLogger.BURNIN);
		if (burnStr != null && burnStr.length()>0) {
			Integer burnin = Integer.parseInt(burnStr);
			setBurnin(burnin);
			return burnin;
		}
		return null;
	}
	
	protected Integer readFrequency(Element el) {
		String freqStr = el.getAttribute(PropertyLogger.FREQUENCY);
		if (freqStr != null && freqStr.length()>0) {
			Integer freq = Integer.parseInt(freqStr);
			setBurnin(freq);
			return freq;
		}
		return null;
	}
	
	/**
	 * A generic default label for this logger (e.g. "StateLogger", etc)
	 * @return
	 */
	public abstract String getDefaultLabel();
	
	public int getLogFrequency() {
		return logFrequency;
	}
	
	public void setLogFrequency(int freq) {
		this.logFrequency = freq;
	}
	
	public String getOutputFilename() {
		return outputFilename;
	}

	public void setOutputFilename(String outputFilename) {
		this.outputFilename = outputFilename;
	}

	public int getBurnin() {
		return burnin;
	}

	public void setBurnin(int burnin) {
		this.burnin = burnin;
	}
	
	public String getModelLabel() {
		return modelLabel;
	}

	public void setModelLabel(String modelLabel) {
		this.modelLabel = modelLabel;
	}

	/**
	 * Convenience method to just return a single node so we dont have to make new arraylists every time
	 * @param doc
	 * @return
	 * @throws InputConfigException 
	 */
	protected abstract Element getElement(ACGDocument doc) throws InputConfigException;
	
	@Override
	public List<Node> getElements(ACGDocument doc) throws InputConfigException {
		List<Node> nodes = new ArrayList<Node>();
		if (view != null)
			view.updateFields();
		nodes.add( getElement(doc) );
		return nodes;
	}

	@Override
	/**
	 * Loggers dont provide parameters, so this is a no-op here
	 */
	public List<DoubleParamElement> getDoubleParameters() {
		return null;
	}
}
