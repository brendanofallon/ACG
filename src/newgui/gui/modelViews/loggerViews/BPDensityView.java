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

import gui.inputPanels.loggerConfigs.BPDensityModel;
import gui.modelElements.Configurator.InputConfigException;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.xml.parsers.ParserConfigurationException;

import logging.BreakpointDensity;
import logging.StateLogger;


import org.w3c.dom.Document;
import org.w3c.dom.Element;

import document.ACGDocument;

import xml.XMLLoader;

public class BPDensityView extends DefaultLoggerView {

	JSpinner binsSpinner;
	BPDensityModel bpModel;
	
	
	public BPDensityView() {
		this(new BPDensityModel());
	}
	
	public BPDensityView(BPDensityModel model) {
		super(model);
		this.bpModel = model;
		SpinnerNumberModel binsModel = new SpinnerNumberModel(500, 1, 50000, 10);
		binsSpinner = new JSpinner(binsModel);
		binsSpinner.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				try {
					updateModelFromView();
				} catch (InputConfigException e1) {
					//Would be really weird if this happened somehow
					e1.printStackTrace();
				}
			}
		});
		centerPanel.add(new JLabel("Bins:"));
		centerPanel.add(binsSpinner, "wrap");
		updateView();
	}
	
	
	
	@Override
	public String getName() {
		return "Breakpoint density";
	}

	@Override
	public String getDescription() {
		return "Locations of recombination breakpoints along sequence";
	}

	@Override
	protected void updateModelFromView() throws InputConfigException {
		bpModel.setBins( (Integer)binsSpinner.getValue() );
	}

	public Dimension getPreferredDimensions() {
		return new Dimension(400, 180);
	}
	
	/**
	 * Updates widgets with info from model
	 */
	public void updateView() {
		if (model.getOutputFilename() == null || model.getOutputFilename().length()==0) {
			model.setOutputFilename("breakpoint_density.txt");
		}
		loggerLabelField.setText( model.getModelLabel() );
		loggerLabelField.repaint();
		burninSpinner.setValue( model.getBurnin() );
		burninSpinner.repaint();
		freqSpinner.setValue( model.getLogFrequency() );
		binsSpinner.setValue( bpModel.getBins());
		revalidate();
		repaint();
	}
}
