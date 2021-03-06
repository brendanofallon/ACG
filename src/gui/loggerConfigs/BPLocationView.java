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


import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
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

import net.miginfocom.swing.MigLayout;
import newgui.gui.modelElements.Configurator.InputConfigException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import document.ACGDocument;

import xml.XMLLoader;



public class BPLocationView extends AbstractLoggerView {

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
	}

	
	/** Default preferred size
	 * @return
	 */
	public Dimension getPreferredDimensions() {
		return new Dimension(1000, 80);
	}

	/**
	 * Default component layout
	 */
	protected void initializeComponents() {
		setLayout(new GridLayout(2, 1, 2, 2));
		
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));
		topPanel.setOpaque(false);
		add(topPanel);
		
		JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.X_AXIS));
		bottomPanel.setOpaque(false);
		add(bottomPanel);
		
		setOpaque(false);
		
		JLabel modelLabel = new JLabel("<html><i>" + model.getDefaultLabel() + "</i><html>");
		topPanel.add(modelLabel);
		topPanel.add(Box.createHorizontalStrut(8));
		filenameField = new JTextField( model.getDefaultLabel() + ".log");
		filenameField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					updateFields();
				} catch (InputConfigException e1) {
					//handled elsewhere
				}
			}
		});
		topPanel.add(new JLabel("File name:"));
		topPanel.add(filenameField);
		topPanel.add(Box.createHorizontalStrut(8));
		SpinnerNumberModel burninModel = new SpinnerNumberModel(1000000, 0, Integer.MAX_VALUE, 10000);
		burninSpinner = new JSpinner(burninModel);
		burninSpinner.setPreferredSize(new Dimension(130, 30));
		burninSpinner.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				model.setBurnin( (Integer)burninSpinner.getValue());
			}
		});
		topPanel.add(new JLabel("Burn-in:"));
		topPanel.add(burninSpinner);
		topPanel.add(Box.createHorizontalStrut(8));
		SpinnerNumberModel freqModel = new SpinnerNumberModel(10000, 0, Integer.MAX_VALUE, 1000);
		freqSpinner = new JSpinner(freqModel);
		freqSpinner.setPreferredSize(new Dimension(100, 30));
		freqSpinner.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				model.setLogFrequency( (Integer)freqSpinner.getValue());
			}
		});
		topPanel.add(new JLabel("Frequency:"));
		topPanel.add(freqSpinner);
		
		SpinnerNumberModel binsModel = new SpinnerNumberModel(250, 1, 50000, 10);
		seqBinsSpinner = new JSpinner(binsModel);
		seqBinsSpinner.setMaximumSize(new Dimension(100, 30));
		seqBinsSpinner.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				((BPLocationModel) model).setSeqBins( (Integer)seqBinsSpinner.getValue());
			}
		});
		bottomPanel.add(Box.createHorizontalStrut(20));
		bottomPanel.add(new JLabel("Seq. Bins:"));
		bottomPanel.add(seqBinsSpinner);
		bottomPanel.add(Box.createHorizontalStrut(15));
		
		binsModel = new SpinnerNumberModel(250, 1, 50000, 10);
		timeBinsSpinner = new JSpinner(binsModel);
		timeBinsSpinner.setMaximumSize(new Dimension(100, 30));
		timeBinsSpinner.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				((BPLocationModel) model).setTimeBins( (Integer)seqBinsSpinner.getValue());
			}
		});
		bottomPanel.add(new JLabel("Time Bins:"));
		bottomPanel.add(timeBinsSpinner);
		bottomPanel.add(Box.createHorizontalStrut(15));
		
		setHeightBox = new JCheckBox("Set max. height");
		setHeightBox.setToolTipText("If checked, use the given value as the max. height of the tree to collect information. \n If unchecked, the value will be the mean of the ARG height during burnin");
		setHeightBox.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				updateHeightBox();
			}
			
		});
		bottomPanel.add(setHeightBox);
		bottomPanel.add(Box.createHorizontalStrut(8));
		bottomPanel.add(new JLabel("Max height:"));
		heightField = new JTextField("0.01");
		heightField.setToolTipText("Maximum depth at which to collect breakpoint locations");
		heightField.setPreferredSize(new Dimension(50, 30));
		heightField.setMaximumSize(new Dimension(70, 30));
		heightField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					updateHeightField();
				} catch (InputConfigException e1) {
				}
			}
		});
		heightField.setEnabled(false);
		bottomPanel.add(heightField);
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
		filenameField.setText( model.getOutputFilename() );
		filenameField.repaint();
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
