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


package newgui.gui.modelViews.loggerViews;

import gui.document.ACGDocument;
import gui.inputPanels.Configurator;
import gui.inputPanels.Configurator.InputConfigException;
import gui.inputPanels.loggerConfigs.StateLoggerModel;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.xml.parsers.ParserConfigurationException;

import logging.StateLogger;


import org.w3c.dom.Document;
import org.w3c.dom.Element;

import xml.XMLLoader;
/**
 * A panel that allows the user to configure a StateLogger
 * @author brendano
 *
 */
public class StateLoggerView extends DefaultLoggerView {

	public StateLoggerView() {
		this(new StateLoggerModel() );
	}
	
	public StateLoggerView(StateLoggerModel model) {
		super(model);
	}

	@Override
	public String getName() {
		return "State logger";
	}

	@Override
	public String getDescription() {
		return "Writes parameter values to a log file";
	}

	@Override
	protected void updateModelFromView() throws InputConfigException {
		//No-op on purpose, nothing to do here
	}
	

}
