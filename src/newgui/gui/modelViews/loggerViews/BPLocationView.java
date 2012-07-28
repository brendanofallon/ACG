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


import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.xml.parsers.ParserConfigurationException;

import logging.BreakpointDensity;
import logging.BreakpointLocation;
import logging.StateLogger;


import org.w3c.dom.Document;
import org.w3c.dom.Element;

import document.ACGDocument;

import xml.XMLLoader;


import gui.loggerConfigs.BPLocationModel;
import gui.modelElements.Configurator.InputConfigException;

public class BPLocationView extends DefaultLoggerView {

	JSpinner seqBinsSpinner;
	JSpinner timeBinsSpinner;
	JTextField heightField;
	JCheckBox setHeightBox;
	BPLocationModel bpModel = null;
	
	public BPLocationView() {
		this(new BPLocationModel());
	}
	
	public BPLocationView(final BPLocationModel model) {
		super(model);
		this.bpModel = model;
		initComponents();
		updateView();
	}

	
	/** Default preferred size
	 * @return
	 */
	public Dimension getPreferredDimensionsLarge() {
		return new Dimension(400, 250);
	}

	/**
	 * Default component layout
	 */
	protected void initComponents() {
		SpinnerNumberModel binsModel = new SpinnerNumberModel(250, 1, 50000, 10);
		seqBinsSpinner = new JSpinner(binsModel);
		seqBinsSpinner.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				((BPLocationModel) model).setSeqBins( (Integer)seqBinsSpinner.getValue());
			}
		});
		centerPanel.add(new JLabel("Seq. Bins:"));
		centerPanel.add(seqBinsSpinner, "wrap");
		
		binsModel = new SpinnerNumberModel(250, 1, 50000, 10);
		timeBinsSpinner = new JSpinner(binsModel);
		timeBinsSpinner.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				((BPLocationModel) model).setTimeBins( (Integer)seqBinsSpinner.getValue());
			}
		});
		centerPanel.add(new JLabel("Time Bins:"));
		centerPanel.add(timeBinsSpinner, "wrap");
		
		setHeightBox = new JCheckBox("Set max. height");
		setHeightBox.setToolTipText("If checked, use the given value as the max. height of the tree to collect information.");
		setHeightBox.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				updateHeightBox();
			}
			
		});
		centerPanel.add(setHeightBox);
		
		//centerPanel.add(new JLabel("Max height:"));
		heightField = new JTextField("0.001");
		heightField.setToolTipText("Maximum depth at which to collect breakpoint locations");
		heightField.setPreferredSize(new Dimension(50, 30));
		heightField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					updateHeightField();
				} catch (InputConfigException e1) {
				}
			}
		});
		heightField.setEnabled(false);
		centerPanel.add(heightField, "wrap");
	} 
	
	protected void updateHeightField() throws InputConfigException {
		if (setHeightBox.isSelected()) {
			try {
				Double height = Double.parseDouble( heightField.getText() );
				bpModel.setMaxDepth(height);
			}
			catch (NumberFormatException nfe) {
				throw new InputConfigException("Please enter a single positive value for the maximum collection depth");
			}
		}
		else {
			bpModel.setMaxDepth(null);
		}
	}

	protected void updateHeightBox() {
		if (setHeightBox.isSelected()) {
			heightField.setEnabled(true);
		}
		else {
			heightField.setEnabled(false);
			bpModel.setMaxDepth(null);
		}
	}




	@Override
	public String getName() {
		return "Breakpoint location";
	}

	@Override
	public String getDescription() {
		return "Locations of breakpoints along sequence and in time	";
	}

	@Override
	protected void updateModelFromView() throws InputConfigException {	
		updateHeightField();
		bpModel.setSeqBins( (Integer)seqBinsSpinner.getValue() );
		bpModel.setTimeBins( (Integer)timeBinsSpinner.getValue() );
		model.setBurnin( (Integer)burninSpinner.getValue());
		model.setLogFrequency( (Integer)freqSpinner.getValue() );
	}
	
	/**
	 * Updates widgets with info from model
	 */
	public void updateView() {
		loggerLabelField.setText( model.getModelLabel() );
		loggerLabelField.repaint();
		burninSpinner.setValue( model.getBurnin() );
		burninSpinner.repaint();
		freqSpinner.setValue( model.getLogFrequency() );
		seqBinsSpinner.setValue( bpModel.getSeqBins());
		timeBinsSpinner.setValue( bpModel.getTimeBins());
		setHeightBox.setSelected( bpModel.getMaxDepth()!=null );
		if (bpModel.getMaxDepth()!= null) {
			heightField.setText( bpModel.getMaxDepth() + "");
		}
		revalidate();
		repaint();
	}


}
