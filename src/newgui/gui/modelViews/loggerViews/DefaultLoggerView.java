package newgui.gui.modelViews.loggerViews;

import gui.ACGFrame;
import gui.inputPanels.Configurator.InputConfigException;
import gui.inputPanels.loggerConfigs.LoggerModel;
import gui.widgets.SpinArrow;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.miginfocom.swing.MigLayout;
import newgui.UIConstants;
import newgui.gui.modelViews.LoggersView;
import newgui.gui.widgets.BorderlessButton;

/**
 * Basic view for a single logger model - this works for a few loggers, but the rest subclass
 * this guy to add in the extra fields they need. 
 * This is the newgui version of AbstractLoggerView
 * @author brendan
 *
 */
public abstract class DefaultLoggerView extends JPanel implements PropertyChangeListener {

	//Whether or not we're showing the centerPanel (if so we're open)
	public enum State {OPEN, CLOSED};
	
	protected LoggerModel model;
	protected JTextField filenameField;
	protected JSpinner burninSpinner;
	protected JSpinner freqSpinner;
	protected JPanel centerPanel;
	protected LoggersView loggerPanelParent = null;
	
	static final ImageIcon removeIcon = UIConstants.closeButton;
	
	static final Color lightColor = new Color(0.99f, 0.99f, 0.99f, 0.8f);
	static final Color darkColor = new Color(0.55f, 0.55f, 0.55f, 0.7f);
	private State currentState = State.CLOSED;
	
	
	
	public DefaultLoggerView(LoggerModel model) {
		centerPanel = new JPanel();
		this.model = model;
		initializeComponents();
		this.setBorder(BorderFactory.createEmptyBorder(2, 3, 2, 3));
		setPreferredSize( getPreferredDimensionsSmall() );
	}
	
	/**
	 * Associate the given loggersview  with this view, mostly so it can listen for
	 * logger removal requests 
	 * @param parent
	 */
	public void setLoggerParent(LoggersView parent) {
		this.loggerPanelParent = parent;
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName() == SpinArrow.SPIN_ARROW_PROPERTY) {
			if (evt.getNewValue() == Boolean.TRUE) {
				showDetailsPanel();
			}
			else {
				hideDetailsPanel();
			}
		}
		if (loggerPanelParent != null)
			loggerPanelParent.layoutLoggers();
	}
	
	/**
	 * If OPEN, this view is currently showing the details information
	 * @return
	 */
	public State getState() {
		return currentState;
	}
	
	private void hideDetailsPanel() {
		remove(centerPanel);
		this.setPreferredSize( getPreferredDimensionsSmall() );
		currentState = State.CLOSED;
		revalidate();
		repaint();		
	}

	private void showDetailsPanel() {
		remove(centerPanel);
		add(centerPanel, BorderLayout.CENTER);
		this.setPreferredSize( getPreferredDimensionsLarge() );
		currentState = State.OPEN;
		revalidate();
		repaint();
	}
	
	public Dimension getPreferredDimensionsSmall() {
		return new Dimension(400, 30);
	}
	
	/**
	 * Default preferred size
	 * @return
	 */
	public Dimension getPreferredDimensionsLarge() {
		return new Dimension(400, 150);
	}

	/**
	 * Default component layout
	 */
	protected void initializeComponents() {
		setLayout(new BorderLayout());
		setOpaque(false);
		
		JPanel topPanel = new JPanel();
		topPanel.setOpaque(false);
		topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));
		JLabel modelLabel = new JLabel("<html> <b> " + getName() + " </b> </html>");
		modelLabel.setHorizontalTextPosition(SwingConstants.LEFT);
		SpinArrow arrow = new SpinArrow();
		arrow.addPropertyChangeListener(SpinArrow.SPIN_ARROW_PROPERTY, this);
		topPanel.add(arrow);
		topPanel.add(Box.createHorizontalStrut(10));
		topPanel.add(modelLabel);

		BorderlessButton remove = new BorderlessButton(removeIcon);
		//remove.setAlignmentY(TOP_ALIGNMENT);
		remove.setToolTipText("Remove " + getModel().getModelLabel() );
		remove.setXDif(-4);
		remove.setYDif(-4);
		remove.setMinimumSize(new Dimension(24, 28));
		remove.setPreferredSize(new Dimension(24, 28));
		remove.setMaximumSize(new Dimension(24, 28));
		
		remove.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				removeThisView();

			}
		});
		topPanel.add(remove);
		add(topPanel, BorderLayout.NORTH);
		
	
		centerPanel.setOpaque(false);
		centerPanel.setLayout(new MigLayout());
		centerPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
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
	}
	
	
	protected void removeThisView() {
		if (loggerPanelParent != null)
			loggerPanelParent.removeLogger(this);
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
