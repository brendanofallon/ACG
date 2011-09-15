package gui.inputPanels.loggerConfigs;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.xml.parsers.ParserConfigurationException;

import logging.BreakpointDensity;
import logging.BreakpointLocation;
import logging.StateLogger;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import xml.XMLLoader;

public class BPLocationConfig extends LoggerConfigurator {

	
	
	public BPLocationConfig() {
		this.setOpaque(false);
		this.setLayout(new FlowLayout(FlowLayout.LEFT));
		

		JLabel nameLabel = new JLabel("Breakpoint location:");
		this.add(nameLabel);
		
		this.add(new JLabel("Burnin:"));
		SpinnerNumberModel burninModel = new SpinnerNumberModel(5000000, 0, 200000000, 10000);
		bpBurninSpinner = new JSpinner(burninModel);
		bpBurninSpinner.setToolTipText("Number of MC states before data is collected");
		bpBurninSpinner.setPreferredSize(new Dimension(120, 26));
		this.add(bpBurninSpinner);
		
		this.add(new JLabel("Frequency:"));
		SpinnerNumberModel freqModel = new SpinnerNumberModel(10000, 0, 200000000, 1000);
		freqSpinner = new JSpinner(freqModel);
		freqSpinner.setPreferredSize(new Dimension(100, 26));
		this.add(freqSpinner);
		
		
		this.add(new JLabel("File name:"));
		bpFilenameField = new JTextField("breakpoint_location.txt");
		bpFilenameField.setPreferredSize(new Dimension(180, 26));
		this.add(bpFilenameField);
		
		this.setAlignmentX(Component.LEFT_ALIGNMENT);
	}
	

	
	@Override
	public Element[] getRootXMLNodes(Document doc)
			throws ParserConfigurationException, InputConfigException {
		
		if (ARGref == null) {
			throw new InputConfigException("ARG not specified for BP location element");
		}
		
		Element el = doc.createElement("BreakpointLocation");
		el.setAttribute(XMLLoader.CLASS_NAME_ATTR, BreakpointLocation.class.getCanonicalName());
		el.setAttribute(StateLogger.XML_FILENAME, bpFilenameField.getText());
		el.setAttribute(StateLogger.XML_FREQUENCY, "" + freqSpinner.getValue().toString());
		
		Element argEl = doc.createElement(ARGref);
		el.appendChild(argEl);
		
		return new Element[]{el};
	}

	@Override
	public String getName() {
		return "Breakpoint location";
	}

	@Override
	public String getDescription() {
		return "Locations of breakpoints along sequence and in time	";
	}
	

	JSpinner freqSpinner;
	JSpinner bpBurninSpinner;
	JTextField bpFilenameField;

}
