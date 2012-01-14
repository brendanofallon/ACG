package newgui.gui.modelViews;

import gui.inputPanels.Configurator.InputConfigException;
import gui.inputPanels.loggerConfigs.LoggerModel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.miginfocom.swing.MigLayout;

/**
 * Basic view for a single logger model - this works for a few loggers, but the rest subclass
 * this guy to add in the extra fields they need. 
 * This is the newgui version of AbstractLoggerView
 * @author brendan
 *
 */
public abstract class DefaultLoggerView extends JPanel {

	protected LoggerModel model;
	protected JTextField filenameField;
	protected JSpinner burninSpinner;
	protected JSpinner freqSpinner;
	
	static final Color lightColor = new Color(0.99f, 0.99f, 0.99f, 0.8f);
	static final Color darkColor = new Color(0.55f, 0.55f, 0.55f, 0.7f);
	
	public DefaultLoggerView(LoggerModel model) {
		this.model = model;
		initializeComponents();
		this.setBorder(BorderFactory.createEmptyBorder(2, 3, 2, 3));
		//this.setBorder(BorderFactory.createLineBorder(Color.BLUE));
	}
	
	/**
	 * Default component layout
	 */
	protected void initializeComponents() {
		setLayout(new BorderLayout());
		setOpaque(false);
		
		JLabel modelLabel = new JLabel("<html> <b> " + model.getDefaultLabel() + " </b> </html>");
		modelLabel.setHorizontalTextPosition(SwingConstants.LEFT);
		add(modelLabel, BorderLayout.NORTH);
		
		JPanel centerPanel = new JPanel();
		centerPanel.setOpaque(false);
		centerPanel.setLayout(new MigLayout());
		
		filenameField = new JTextField( model.getDefaultLabel() + ".log");
		filenameField.setFont(getFont());
		Dimension fieldSize = new Dimension(160, 30);
		filenameField.setMinimumSize( fieldSize );
		filenameField.setPreferredSize( fieldSize );
		filenameField.setMaximumSize( fieldSize );
		filenameField.setHorizontalAlignment(JTextField.RIGHT);
		filenameField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					updateFields();
				} catch (InputConfigException e1) {
					//Exception is handle in model.getLoggerNodes(...) 
				}
			}
		});
		
		JLabel filenameLabel = new JLabel("File name:");
		centerPanel.add(filenameLabel);
		centerPanel.add(filenameField, "wrap");
		
		SpinnerNumberModel burninModel = new SpinnerNumberModel(1000000, 0, Integer.MAX_VALUE, 1000);
		burninSpinner = new JSpinner(burninModel);
		burninSpinner.setPreferredSize(new Dimension(130, 30));
		burninSpinner.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				model.setBurnin( (Integer)burninSpinner.getValue());
			}
		});
		centerPanel.add(new JLabel("Burn-in:"));
		centerPanel.add(burninSpinner, "wrap");
		
		SpinnerNumberModel freqModel = new SpinnerNumberModel(10000, 0, Integer.MAX_VALUE, 1000);
		freqSpinner = new JSpinner(freqModel);
		freqSpinner.setPreferredSize(new Dimension(100, 30));
		freqSpinner.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				model.setLogFrequency( (Integer)freqSpinner.getValue());
			}
		});
		centerPanel.add(new JLabel("Frequency:"));
		centerPanel.add(freqSpinner, "wrap");
		this.add(centerPanel, BorderLayout.CENTER);
	}
	
	/**
	 * Default preferred size
	 * @return
	 */
	public Dimension getPreferredDimensions() {
		return new Dimension(700, 100);
	}
	
	public LoggerModel getModel() {
		return model;
	}
	
	public void setModel(LoggerModel model) {
		this.model = model;
		updateView();
	}
	
	/**
	 * Push the state of the components in this field to the underlying model
	 * @throws InputConfigException
	 */
	protected abstract void updateModelFromView() throws InputConfigException;
	

	/**
	 * Return the name of the logger (eg "State Logger" or "TMRCA logger")
	 */
	public abstract String getName();
	
	/**
	 * A brief description of the logger, suitable for use in a tool-tip  description
	 */
	public abstract String getDescription();
	
	
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
		filenameField.repaint();
		burninSpinner.setValue( model.getBurnin() );
		burninSpinner.repaint();
		freqSpinner.setValue( model.getLogFrequency() );
		revalidate();
		repaint();
	}
	


}
