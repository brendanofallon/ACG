package gui.inputPanels.loggerConfigs;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import gui.inputPanels.Configurator.InputConfigException;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Base class for individual logger views that appear in the loggers panel. These graphically
 * represent the information in a LoggerModel
 * @author brendano
 *
 */
public abstract class AbstractLoggerView extends JPanel {
	
	LoggerModel model;
	JTextField filenameField;
	JSpinner burninSpinner;
	JSpinner freqSpinner;
	
	static final Color lightColor = new Color(0.99f, 0.99f, 0.99f, 0.8f);
	static final Color darkColor = new Color(0.45f, 0.45f, 0.45f, 0.7f);
	
	public AbstractLoggerView(final LoggerModel model) {
		this.model = model;
		initializeComponents();
		this.setBorder(BorderFactory.createEmptyBorder(2, 3, 2, 3));
	}
	
	/**
	 * Default component layout
	 */
	protected void initializeComponents() {
		setLayout(new FlowLayout(FlowLayout.LEFT));
		setOpaque(false);
		
		add(new JLabel(model.getDefaultLabel()));
		
		filenameField = new JTextField( model.getDefaultLabel() + ".log");
		filenameField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					updateFields();
				} catch (InputConfigException e1) {
					//Exception is handle in model.getLoggerNodes(...) 
				}
			}
		});
		add(new JLabel("File name:"));
		add(filenameField);
		
		SpinnerNumberModel burninModel = new SpinnerNumberModel(1000000, 0, Integer.MAX_VALUE, 10000);
		burninSpinner = new JSpinner(burninModel);
		burninSpinner.setPreferredSize(new Dimension(130, 30));
		burninSpinner.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				model.setBurnin( (Integer)burninSpinner.getValue());
			}
		});
		add(new JLabel("Burn-in:"));
		add(burninSpinner);
		
		SpinnerNumberModel freqModel = new SpinnerNumberModel(10000, 0, Integer.MAX_VALUE, 1000);
		freqSpinner = new JSpinner(freqModel);
		freqSpinner.setPreferredSize(new Dimension(100, 30));
		freqSpinner.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				model.setLogFrequency( (Integer)freqSpinner.getValue());
			}
		});
		add(new JLabel("Frequency:"));
		add(freqSpinner);
	}
	
	/**
	 * Default preferred size
	 * @return
	 */
	public Dimension getPreferredDimensions() {
		return new Dimension(1000, 44);
	}
	
	public LoggerModel getModel() {
		return model;
	}
	
	
	protected abstract void updateModelFromView() throws InputConfigException;
	
	
	public void updateFields() throws InputConfigException {
		String filename = filenameField.getText();
		filename.replaceAll(" ", "_");
		model.setOutputFilename(filename);
		model.setBurnin( (Integer)burninSpinner.getValue());
		model.setLogFrequency( (Integer)freqSpinner.getValue() );
		updateModelFromView();
	}
	
	/**
	 * Updates widgets with info from model
	 */
	public void updateView() {
		filenameField.setText( model.getOutputFilename() );
		burninSpinner.setValue( model.getBurnin() );
		freqSpinner.setValue( model.getLogFrequency() );
		revalidate();
		repaint();
	}
	
	
	/**
	 * Return the name of the logger (eg "State Logger" or "TMRCA logger")
	 */
	public abstract String getName();
	
	/**
	 * A brief description of the logger, suitable for use in a tool-tip  description
	 */
	public abstract String getDescription();
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		Graphics2D g2d = (Graphics2D)g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
		
		g2d.setColor(lightColor);
		g2d.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 14, 14);
		
		g2d.setColor(darkColor);
		g2d.drawRoundRect(0, 0, getWidth()-2, getHeight()-2, 14, 14);
	}
	
}