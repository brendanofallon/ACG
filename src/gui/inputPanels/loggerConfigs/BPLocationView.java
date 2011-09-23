package gui.inputPanels.loggerConfigs;

import gui.document.ACGDocument;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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

import xml.XMLLoader;

public class BPLocationView extends AbstractLoggerView {

	JSpinner seqBinsSpinner;
	JSpinner timeBinsSpinner;
	JTextField heightField;
	JCheckBox setHeightBox;
	BPLocationModel bpModel = null;
	
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
		setLayout(new GridLayout(2, 1));
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		topPanel.setOpaque(false);
		add(topPanel);
		
		JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		bottomPanel.setOpaque(false);
		add(bottomPanel);
		
		setOpaque(false);
		
		topPanel.add(new JLabel(model.getDefaultLabel()));
		
		filenameField = new JTextField( model.getDefaultLabel() + ".log");
		filenameField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateFields();
			}
		});
		topPanel.add(new JLabel("File name:"));
		topPanel.add(filenameField);
		
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
		
		
		SpinnerNumberModel binsModel = new SpinnerNumberModel(500, 1, 50000, 10);
		seqBinsSpinner = new JSpinner(binsModel);
		seqBinsSpinner.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				((BPLocationModel) model).setSeqBins( (Integer)seqBinsSpinner.getValue());
			}
		});
		bottomPanel.add(new JLabel("Seq. Bins:"));
		bottomPanel.add(seqBinsSpinner);
		
		binsModel = new SpinnerNumberModel(500, 1, 50000, 10);
		timeBinsSpinner = new JSpinner(binsModel);
		timeBinsSpinner.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				((BPLocationModel) model).setTimeBins( (Integer)seqBinsSpinner.getValue());
			}
		});
		bottomPanel.add(new JLabel("Time Bins:"));
		bottomPanel.add(timeBinsSpinner);
		
		setHeightBox = new JCheckBox("Set max. depth");
		setHeightBox.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				updateHeightBox();
			}
			
		});
		bottomPanel.add(setHeightBox);
		
		bottomPanel.add(new JLabel("Max height:"));
		heightField = new JTextField("0.01");
		heightField.setPreferredSize(new Dimension(50, 30));
		heightField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateHeightField();
			}
		});
		bottomPanel.add(heightField);
	} 
	
	protected void updateHeightField() {
		try {
			Double height = Double.parseDouble( heightField.getText() );
			bpModel.setMaxDepth(height);
		}
		catch (NumberFormatException nfe) {
			JOptionPane.showMessageDialog(this, "Please enter a valid number");
		}
	}

	protected void updateHeightBox() {
		if (setHeightBox.isSelected()) {
			heightField.setEnabled(true);
		}
		else {
			heightField.setEnabled(false);
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
	


}
