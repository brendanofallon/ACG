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
public class StateLoggerView extends AbstractLoggerView {

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
	

}
