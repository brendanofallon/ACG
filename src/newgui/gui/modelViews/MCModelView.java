package newgui.gui.modelViews;

import gui.ErrorWindow;
import gui.modelElements.MCMCModelElement;
import gui.modelElements.Configurator.InputConfigException;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class MCModelView extends JPanel {

	private MCMCModelElement model;
	JSpinner lengthSpinner;
	
	JCheckBox useHeatingBox;
	JSpinner chainsSpinner;
	JSpinner threadsSpinner;
	JTextField lambdaField;
	JCheckBox adaptiveHeatingBox;
	
	public MCModelView(MCMCModelElement model) {
		this.model = model;
		initComponents();
		updateView();
	}
	
	private void initComponents() {
		this.setOpaque( false );
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));

		SpinnerNumberModel lengthModel = new SpinnerNumberModel(model.getRunLength(), 1, Integer.MAX_VALUE, 10000);
		lengthSpinner = new JSpinner(lengthModel);
		lengthSpinner.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				setRunLength((Integer)lengthSpinner.getValue() );
				
			}
		});
		add(new JLabel("Markov chain properties"));
		
		addComp("Run length :", lengthSpinner);
		
		JSeparator sep = new JSeparator( JSeparator.HORIZONTAL);
		add(sep);
		
		useHeatingBox = new JCheckBox("Use Metropolis coupling");
		useHeatingBox.setToolTipText("Use multiple chains with heating to improve mixing");
		useHeatingBox.setSelected( model.isUseMC3() );
		useHeatingBox.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				heatingBoxChanged();
			}	
		});
		add(useHeatingBox);
		
		chainsSpinner = new JSpinner(new SpinnerNumberModel( model.getChains(), 2, 128, 1));
		chainsSpinner.setToolTipText("Total number of Markov chains, including cold chain");
		chainsSpinner.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				setChainNumber((Integer)chainsSpinner.getValue() );
				
			}
		});
		addComp("Chains : ", chainsSpinner);
		chainsSpinner.setEnabled(false);
		
		threadsSpinner = new JSpinner(new SpinnerNumberModel( model.getThreads(), 1, 64, 1));
		threadsSpinner.setToolTipText("Number of processor threads to use");
		threadsSpinner.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				setThreadCount( (Integer)threadsSpinner.getValue() );
			}
		});
		addComp("Threads : ", threadsSpinner);
		threadsSpinner.setEnabled(false);		
		
		adaptiveHeatingBox = new JCheckBox("Use adaptive heating");
		adaptiveHeatingBox.setToolTipText("Heating amount will be adjusted autmatically (recommended)");
		adaptiveHeatingBox.setSelected(true);
		adaptiveHeatingBox.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				adaptiveHeatingBoxChanged();
			}
		});
		add(adaptiveHeatingBox);
		
		lambdaField = new JTextField( model.getLambda().getValue() + "" );
		lambdaField.setMinimumSize(new Dimension(100, 36));
		lambdaField.setMaximumSize(new Dimension(100, 36));
		lambdaField.setEnabled(false);
		addComp("Heating value (lambda): ", lambdaField);
	}

	/**
	 * Set number of threads in MC3 run 
	 * @param threadCount
	 */
	protected void setThreadCount(int threadCount) {
		model.setThreads( (Integer)threadsSpinner.getValue() );
	}

	/**
	 * Set the number of chains for use in an MC3 run
	 * @param chainCount
	 */
	protected void setChainNumber(Integer chainCount) {
		model.setChains( chainCount );
	}

	/**
	 * Set the total run length of the chain in the model. Called when the 
	 * run length spinner is changed
	 * @param length
	 */
	protected void setRunLength(Integer length) {
		model.setRunLength(  length );
	}

	/**
	 * Update widgets to reflect changes in model settings
	 */
	public void updateView() {
		lengthSpinner.setValue( model.getRunLength() );
		useHeatingBox.setSelected( model.isUseMC3());
		if (model.isUseMC3()) {
			chainsSpinner.setEnabled(true);
			threadsSpinner.setEnabled(true);
		}
		else {
			chainsSpinner.setEnabled(false);
			threadsSpinner.setEnabled(false);
		}
		
		adaptiveHeatingBox.setSelected(model.isUseAdaptiveMC3());
		lambdaField.setText("" + model.getLambda().getValue());
		if ( model.isUseAdaptiveMC3() )
			lambdaField.setEnabled(false);
		else
			lambdaField.setEnabled(true);
			
		chainsSpinner.setValue( model.getChains() );
		threadsSpinner.setValue( model.getThreads() );
		revalidate();
		repaint();
	}
	
	/**
	 * Update all values in model to reflect selections in view widgets. This should be called before
	 * the beginning of any MCMC run  (or file save) to make sure changes are reflected in model. 
	 */
	public void updateModel() throws InputConfigException {
		System.out.println("Updating model from view! Run length is : " + lengthSpinner.getValue() );
		adaptiveHeatingBoxChanged();
		heatingBoxChanged();
		
		Integer length = (Integer)lengthSpinner.getValue();
		model.setRunLength(  length );
		
		Integer chains = (Integer)chainsSpinner.getValue();
		model.setChains(chains);
		
		Integer threads = (Integer)threadsSpinner.getValue();
		model.setThreads(threads);
		
		if (useHeatingBox.isSelected() && (!adaptiveHeatingBox.isSelected())) {
			String lambdaText = lambdaField.getText();
			try {
				Double lambda = Double.parseDouble(lambdaText);
				model.getLambda().setValue(lambda);
			}
			catch (NumberFormatException nfe) {
				throw new InputConfigException("Please enter an value into the lambda field");
			}
		}
		
	}
	
	protected void adaptiveHeatingBoxChanged() {
		if (adaptiveHeatingBox.isSelected()) {
			lambdaField.setEnabled(false);
			model.setUseAdaptiveMC3(true);
		}
		else {
			if (useHeatingBox.isSelected())
				lambdaField.setEnabled(true);

			model.setUseAdaptiveMC3(false);
		}
	}
	
	protected void heatingBoxChanged() {
		if (useHeatingBox.isSelected()) {
			threadsSpinner.setEnabled(true);
			chainsSpinner.setEnabled(true);
			adaptiveHeatingBox.setEnabled(true);
			if (adaptiveHeatingBox.isSelected()) {
				lambdaField.setEnabled(false);
			}
			else {
				lambdaField.setEnabled(true);	
			}
			model.setUseMC3(true);
		}
		else {
			threadsSpinner.setEnabled(false);
			adaptiveHeatingBox.setEnabled(false);
			chainsSpinner.setEnabled(false);
			lambdaField.setEnabled(false);
			model.setUseMC3(false);
		}
	}

	private void addComp(String label, JComponent comp) {
		JPanel panel = new JPanel();
		panel.setOpaque( false );
		panel.setLayout(new FlowLayout(FlowLayout.LEFT));
		panel.setAlignmentX(Component.LEFT_ALIGNMENT);
		panel.add(new JLabel(label));
		panel.add(comp);
		this.add(panel);
	}

	public void setModel(MCMCModelElement mcModelElement) {
		this.model = mcModelElement;
		updateView();
	}
}

