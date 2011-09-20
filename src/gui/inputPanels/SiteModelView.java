package gui.inputPanels;

import gui.document.ACGDocument;
import gui.inputPanels.Configurator.InputConfigException;
import gui.inputPanels.SiteModelElement.MutModelType;
import gui.inputPanels.SiteModelElement.RateModelType;

import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.w3c.dom.Element;

/**
 * The 'view' portion for site / rate models, allowing user to see and configure some options
 * @author brendano
 *
 */
public class SiteModelView extends JPanel {

	//The 'model' portion that stores the data
	private SiteModelElement siteModel = new SiteModelElement();
	
	private final JPanel rateConfigPanel;
	
	private final JPanel gammaPanel;
	private final JPanel customPanel;
	private final JPanel oneRatePanel;

	private JTextField rateTextField;
	private JSpinner categsSpinner; 
	private JCheckBox estAlphaBox;
	private JTextField alphaField;
	
	List<Element> params = new ArrayList<Element>();
	List<Element> likelihoods = new ArrayList<Element>();;
	
	private final String[] rateTypes = new String[]{"One rate", "Gamma rates", "Custom rates"};
	

	
	public SiteModelView() {
		oneRatePanel = new JPanel();
		rateConfigPanel = new JPanel();
		gammaPanel = new JPanel();
		customPanel = new JPanel();
		initComponents();
	}

	
	
	
	/**
	 * Get the site model that holds the model data 
	 * @return
	 */
	public SiteModelElement getSiteModel() {
		return siteModel;
	}
	
	/**
	 * Refresh the values of the UI elements based on the state of siteModel. If new settings are loaded from a 
	 * file, for instance, call this method to make the right values appear in the widgets
	 */
	public void updateView() {
		if (siteModel.getModelType().equals( MutModelType.F84)) 
			mutBox.setSelectedIndex(0);
		
		if (siteModel.getModelType().equals( MutModelType.TN93))
			mutBox.setSelectedIndex(1);
		
		if (siteModel.getRateModelType().equals( RateModelType.Constant)) {
			rateBox.setSelectedIndex(0);
		}
		if (siteModel.getRateModelType().equals( RateModelType.Gamma)) {
			rateBox.setSelectedIndex(1);
			DoubleParamElement alphaPar = siteModel.getAlphaParamElement();
			if (alphaPar.getModType() == null) {
				estAlphaBox.setSelected(false);
				alphaField.setEnabled(true);
				alphaField.setText("" + alphaPar.getValue());
			}
			else {
				estAlphaBox.setSelected(true);
				alphaField.setEnabled(false);
			}
			
			Integer categs = siteModel.getRatCatgeoryCount();
			categsSpinner.setValue( categs );
		}
		if (siteModel.getRateModelType().equals( RateModelType.Custom)) {
			rateBox.setSelectedIndex(2);
		}

		
	}
	
	protected void alphaBoxSwitched() {
		siteModel.setEstimateAlpha(estAlphaBox.isSelected());
		if (estAlphaBox.isSelected()) {
			alphaField.setEnabled(false);
		}
		else {
			alphaField.setEnabled(true);
		}
	}


	public Element[] getRootXMLNodes(ACGDocument doc) throws InputConfigException  {
		List<Element> nodes = siteModel.getElements(doc);
		return nodes.toArray(new Element[]{});	
	}
	
	public Element[] getParameters() {
		return siteModel.getParameters().toArray(new Element[]{});
	}


	public Element[] getLikelihoods() {
		return siteModel.getLikelihoods().toArray(new Element[]{});
	}


	public void readNodesFromDocument(ACGDocument doc)
			throws InputConfigException {
		siteModel.readElements(doc);
	}
	
	
	private void initComponents() {
		setOpaque(false);
		setMaximumSize(new Dimension(1000, 60));
		setPreferredSize(new Dimension(500, 60));
		add(new JLabel("Mutation model: "));
		mutBox = new JComboBox(new Object[]{/*"JC69", "K2P", */ "F84", "TN93"}); 
		mutBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent evt) {
				if (mutBox.getSelectedIndex()==0)
			    	siteModel.setMutModelType( SiteModelElement.MutModelType.F84);
				if (mutBox.getSelectedIndex()==1)
			    	siteModel.setMutModelType( SiteModelElement.MutModelType.TN93);
				
			    
			    rateConfigPanel.repaint();
			}
		});
		add(mutBox);
		
		add(new JLabel("Rate model: "));
		rateBox = new JComboBox(rateTypes);
		rateBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent evt) {
				CardLayout cl = (CardLayout)(rateConfigPanel.getLayout());
			    cl.show(rateConfigPanel, (String)evt.getItem());
			    if (rateBox.getSelectedIndex()==0)
			    	siteModel.setRateModelType( SiteModelElement.RateModelType.Constant);
			    if (rateBox.getSelectedIndex()==1)
			    	siteModel.setRateModelType( SiteModelElement.RateModelType.Gamma);
			    if (rateBox.getSelectedIndex()==0)
			    	siteModel.setRateModelType( SiteModelElement.RateModelType.Custom);

			    rateConfigPanel.repaint();
			}
		});
		add(rateBox);
		

		rateConfigPanel.setLayout(new CardLayout());
		rateConfigPanel.setOpaque(false);
		add(rateConfigPanel);
		
		oneRatePanel.add(new JLabel("Rate :"));
		oneRatePanel.setToolTipText("Pick the rate at which sites evolve in expected substitutions / time");
		rateTextField = new JTextField("1.0");
		rateTextField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				siteModel.setConstantRate( Double.parseDouble(rateTextField.getText()));
				System.out.println("Setting constant rate to L : " + Double.parseDouble(rateTextField.getText()));
			}
		});
		rateTextField.setPreferredSize(new Dimension(60, 24));
		rateTextField.setHorizontalAlignment(JTextField.RIGHT);
		oneRatePanel.add(rateTextField);
		oneRatePanel.setOpaque(false);
		rateConfigPanel.add(oneRatePanel, rateTypes[0]);


		categsSpinner = new JSpinner(new SpinnerNumberModel(4, 1, 100, 1));
		categsSpinner.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent evt) {
				siteModel.setRateCategories( (Integer)categsSpinner.getValue());
			}
		});
		categsSpinner.setToolTipText("Number of categories in discrete gamma rates model");
		gammaPanel.add(new JLabel("Categories :"));
		gammaPanel.add(categsSpinner);
		estAlphaBox = new JCheckBox("Estimate alpha");
		estAlphaBox.setToolTipText("Estimate the shape of the gamma distribution from the data");
		estAlphaBox.setSelected(true);
		estAlphaBox.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				alphaBoxSwitched();
			}
		});
		
		gammaPanel.add(estAlphaBox);
		alphaField = new JTextField("1.0");
		alphaField.setEnabled(false);
		gammaPanel.add(alphaField);
		
		gammaPanel.setOpaque(false);
		rateConfigPanel.add(gammaPanel, rateTypes[1]);
		
		customPanel.add(new JTextField("Custom rate stuff"));
		customPanel.setOpaque(false);
		rateConfigPanel.add(customPanel,rateTypes[2]);
	}

	private JComboBox mutBox;
	private JComboBox rateBox;


	
}
