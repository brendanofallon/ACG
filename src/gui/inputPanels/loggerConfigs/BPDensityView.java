package gui.inputPanels.loggerConfigs;

import gui.document.ACGDocument;
import gui.inputPanels.Configurator.InputConfigException;

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

import xml.XMLLoader;

public class BPDensityView extends AbstractLoggerView {

	JSpinner binsSpinner;
	BPDensityModel bpModel;
	
	public BPDensityView(final BPDensityModel model) {
		super(model);
		this.bpModel = model;
		SpinnerNumberModel binsModel = new SpinnerNumberModel(500, 1, 50000, 10);
		binsSpinner = new JSpinner(binsModel);
		binsSpinner.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				model.setBins( (Integer)binsSpinner.getValue());
			}
		});
		add(new JLabel("Bins:"));
		add(binsSpinner);
	}
	
	@Override
	public String getName() {
		return "Breakpoint density";
	}

	@Override
	public String getDescription() {
		return "Locations of recombination breakpoints along sequence";
	}
	
	
	JSpinner freqSpinner;
	JSpinner bpBurninSpinner;
	JTextField bpFilenameField;


}
