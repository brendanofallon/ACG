package gui.inputPanels;

import java.awt.Dimension;
import java.awt.FlowLayout;

import gui.widgets.RoundedPanel;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class LoggerConfigurator extends RoundedPanel implements Configurator {

	
	public LoggerConfigurator() {
		setMaximumSize(new Dimension(1000, 50));
		setPreferredSize(new Dimension(500, 50));
		
		this.getMainPanel().setLayout(new BoxLayout(getMainPanel(), BoxLayout.Y_AXIS));
		
		JPanel stateLoggerPanel = new JPanel();
		stateLoggerPanel.setLayout(new FlowLayout());
		
		stateLoggerBox = new JCheckBox("State logger:");
		stateLoggerBox.setSelected(true);
		stateLoggerPanel.add(stateLoggerBox);
		stateLoggerPanel.add(new JLabel("Burnin:"));
		SpinnerNumberModel burninModel = new SpinnerNumberModel(5000000, 0, 200000000, 10000);
		stateLoggerBurninSpinner = new JSpinner(burninModel);
		stateLoggerPanel.add(stateLoggerBurninSpinner);
		
		stateLoggerPanel.add(new JLabel("File name:"));
		stateLoggerFilenameField = new JTextField("");
		stateLoggerPanel.add(stateLoggerFilenameField);
		
		add(stateLoggerPanel); 
		
		
		
	}
	
	
	
	
	@Override
	public Element[] getRootXMLNodes(Document doc)
			throws ParserConfigurationException, InputConfigException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Element[] getParameters() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Element[] getLikelihoods() {
		// TODO Auto-generated method stub
		return null;
	}

	
	JCheckBox stateLoggerBox;
	JSpinner stateLoggerBurninSpinner;
	JTextField stateLoggerFilenameField;
	
	

}
