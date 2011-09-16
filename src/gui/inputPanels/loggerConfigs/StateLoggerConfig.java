package gui.inputPanels.loggerConfigs;

import gui.document.ACGDocument;
import gui.inputPanels.Configurator;
import gui.inputPanels.Configurator.InputConfigException;

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
public class StateLoggerConfig extends LoggerConfigurator {

	
	public StateLoggerConfig() {
		setOpaque(false);
		this.setLayout(new FlowLayout(FlowLayout.LEFT));
		
		JLabel nameLabel = new JLabel("State logger:");
		this.add(nameLabel);
				
		this.add( new JLabel("Frequency:"));
		SpinnerNumberModel freqModel = new SpinnerNumberModel(10000, 0, 200000000, 1000);
		freqSpinner = new JSpinner(freqModel);
		freqSpinner.setPreferredSize(new Dimension(100, 26));
		this.add(freqSpinner);
		
		this.add(new JLabel("File name:"));
		stateLoggerFilenameField = new JTextField("stateLogger.log");
		stateLoggerFilenameField.setPreferredSize(new Dimension(200, 26));
		this.add(stateLoggerFilenameField);
		
		this.setAlignmentX(Component.LEFT_ALIGNMENT);
	}
	
	@Override
	public String getName() {
		return "State logger";
	}
	
	@Override
	public String getDescription() {
		return "Logs parameter values to a .log file";
	}
	
	
	@Override
	public Element[] getRootXMLNodes(Document doc)
			throws ParserConfigurationException, InputConfigException {
		
		Element el = doc.createElement("StateLogger");
		el.setAttribute(XMLLoader.CLASS_NAME_ATTR, StateLogger.class.getCanonicalName());
		el.setAttribute(StateLogger.XML_FILENAME, stateLoggerFilenameField.getText());
		el.setAttribute(StateLogger.XML_FREQUENCY, "" + freqSpinner.getValue().toString());
		el.setAttribute(StateLogger.XML_ECHOTOSCREEN, "false");
		
		return new Element[]{el};
	}


	private JSpinner freqSpinner;
	private JTextField stateLoggerFilenameField;
	@Override
	public void readNodesFromDocument(ACGDocument doc)
			throws InputConfigException {
		// TODO Auto-generated method stub
		
	}
	
	

}
