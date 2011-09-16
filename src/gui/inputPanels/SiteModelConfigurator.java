package gui.inputPanels;

import gui.document.ACGDocument;
import gui.widgets.RoundedPanel;

import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import parameter.DoubleParameter;

import dlCalculation.siteRateModels.ConstantSiteRates;
import dlCalculation.siteRateModels.GammaSiteRates;
import dlCalculation.substitutionModels.BaseFrequencies;
import dlCalculation.substitutionModels.TN93Matrix;

import xml.XMLLoader;

public class SiteModelConfigurator extends RoundedPanel implements Configurator {
	
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
	
	public SiteModelConfigurator() {
		setMaximumSize(new Dimension(1000, 60));
		setPreferredSize(new Dimension(500, 60));
		add(new JLabel("Mutation model: "));
		mutBox = new JComboBox(new Object[]{/*"JC69", "K2P", */ "F84", "TN93"}); 
		add(mutBox);
		add(new JLabel("Rate model: "));
		rateBox = new JComboBox(rateTypes);
		
		add(rateBox);
		
		rateConfigPanel = new JPanel();
		rateConfigPanel.setLayout(new CardLayout());
		rateConfigPanel.setOpaque(false);
		add(rateConfigPanel);
		
		oneRatePanel = new JPanel();
		oneRatePanel.add(new JLabel("Rate :"));
		oneRatePanel.setToolTipText("Pick the rate at which sites evolve in expected substitutions / time");
		rateTextField = new JTextField("1.0");
		rateTextField.setPreferredSize(new Dimension(60, 24));
		rateTextField.setHorizontalAlignment(JTextField.RIGHT);
		oneRatePanel.add(rateTextField);
		oneRatePanel.setOpaque(false);
		rateConfigPanel.add(oneRatePanel, rateTypes[0]);
		
		gammaPanel = new JPanel();
		categsSpinner = new JSpinner(new SpinnerNumberModel(4, 1, 100, 1));
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
		
		customPanel = new JPanel();
		customPanel.add(new JTextField("Custom rate stuff"));
		customPanel.setOpaque(false);
		rateConfigPanel.add(customPanel,rateTypes[2]);
		
		rateBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent evt) {
				CardLayout cl = (CardLayout)(rateConfigPanel.getLayout());
			    cl.show(rateConfigPanel, (String)evt.getItem());
			    rateConfigPanel.repaint();
			}
		});
	}


	protected void alphaBoxSwitched() {
		if (estAlphaBox.isSelected()) {
			alphaField.setEnabled(false);
		}
		else {
			alphaField.setEnabled(true);
		}
	}


	@Override
	public Element[] getRootXMLNodes(Document doc) throws InputConfigException  {
		params = new ArrayList<Element>();
		likelihoods = new ArrayList<Element>();
		
		Element mutNode = createMutNode(doc);
		Element siteNode = createSiteNode(doc);
		//Order important here!
		return new Element[]{mutNode, siteNode};
	}
	
	private Element createSiteNode(Document doc) throws InputConfigException {
		Element siteNode = doc.createElement("siteModel");
		if (rateBox.getSelectedItem().toString().equals(rateTypes[0])) {
			siteNode.setAttribute(XMLLoader.CLASS_NAME_ATTR,  ConstantSiteRates.class.getCanonicalName());
			Double rate = 1.0;
			try {
				rate = Double.parseDouble(rateTextField.getText());
			}
			catch (NumberFormatException ex) {
				throw new InputConfigException("Could not read value for rate parameter (found : " + rateTextField.getText() + ")");
			}
			siteNode.setAttribute(ConstantSiteRates.XML_RATE, "" + rate);
			return siteNode;
		}
		
		if (rateBox.getSelectedItem().toString().equals(rateTypes[1])) {
			siteNode.setAttribute(XMLLoader.CLASS_NAME_ATTR,  GammaSiteRates.class.getCanonicalName());
			Integer categs = (Integer)categsSpinner.getValue();
			siteNode.setAttribute(GammaSiteRates.XML_CATEGORIES, "" + categs);
			
			Element alpha = createDoubleParamElement(doc, "alpha", 1.0, 0.01, 50);
			if (estAlphaBox.isSelected()) {
				Element alphaMod = doc.createElement("alphaModifier");
				params.add(alpha);
				alphaMod.setAttribute(XMLLoader.CLASS_NAME_ATTR,  modifier.ScaleModifier.class.getCanonicalName());
				alpha.appendChild(alphaMod);
				
				//TODO Prior for alpha?
			}
			else {
				try {
					Double alphaValue = Double.parseDouble(alphaField.getText());
					alpha.setAttribute("value", "" + alphaValue);
				}
				catch (NumberFormatException nfe) {
					throw new InputConfigException("Could not parse a number for alpha value (found " + alphaField.getText() + ")");
				}
			}
			siteNode.appendChild(alpha);
			
			return siteNode;
		}
		
		if (rateBox.getSelectedItem().toString().equals(rateTypes[2])) {
			siteNode.setAttribute(XMLLoader.CLASS_NAME_ATTR, ConstantSiteRates.class.getCanonicalName());
			throw new InputConfigException("Not implemented yet");
			//return null;
		}
		
		System.out.println("Hmm, returning null for the site node");
		return null;
	}

	private Element createMutNode(Document doc) {
		if (mutBox.getSelectedItem().toString().equals("F84")) {
			Element el = doc.createElement("F84Model");
			el.setAttribute(XMLLoader.CLASS_NAME_ATTR,  dlCalculation.substitutionModels.F84Matrix.class.getCanonicalName());

			Element baseEl = doc.createElement("BaseFrequencies");
			baseEl.setAttribute(XMLLoader.CLASS_NAME_ATTR, dlCalculation.substitutionModels.BaseFrequencies.class.getCanonicalName());
			baseEl.setAttribute(BaseFrequencies.XML_PARAM_FREQUENCY, "0.1");
			baseEl.setAttribute(TN93Matrix.XML_STATIONARIES, "0.25 0.25 0.25 0.25");
			
			Element baseModEl = doc.createElement("BaseFreqsMod");
			baseModEl.setAttribute(XMLLoader.CLASS_NAME_ATTR, modifier.DirichletModifier.class.getCanonicalName());
			baseEl.appendChild(baseModEl);
			params.add(baseEl);
			
			Element kappa = createDoubleParamElement(doc, "Kappa", 2.0, 0.5, 500);
			params.add(kappa);
			
			Element modEl = doc.createElement("KappaMod");
			modEl.setAttribute(XMLLoader.CLASS_NAME_ATTR,  modifier.SimpleModifier.class.getCanonicalName());
			
			kappa.appendChild(modEl);
			el.appendChild(baseEl);
			el.appendChild(kappa);
			return el;
		}
		
		if (mutBox.getSelectedItem().toString().equals("TN93")) {
			Element el = doc.createElement("TN93Model");
			el.setAttribute(XMLLoader.CLASS_NAME_ATTR, dlCalculation.substitutionModels.TN93Matrix.class.getCanonicalName());
			
			Element baseEl = doc.createElement("BaseFrequencies");
			baseEl.setAttribute(XMLLoader.CLASS_NAME_ATTR, dlCalculation.substitutionModels.BaseFrequencies.class.getCanonicalName());
			baseEl.setAttribute(BaseFrequencies.XML_PARAM_FREQUENCY, "0.1");
			baseEl.setAttribute(TN93Matrix.XML_STATIONARIES, "0.25 0.25 0.25 0.25");
			
			Element baseModEl = doc.createElement("BaseFreqsMod");
			baseModEl.setAttribute(XMLLoader.CLASS_NAME_ATTR, modifier.DirichletModifier.class.getCanonicalName());
			baseEl.appendChild(baseModEl);
			params.add(baseEl);
			
			Element kappaR = createDoubleParamElement(doc, "KappaR", 2.0, 0.5, 500);
			Element modREl = doc.createElement("KappaRMod");
			params.add(kappaR);
			modREl.setAttribute(XMLLoader.CLASS_NAME_ATTR,  modifier.SimpleModifier.class.getCanonicalName());

			Element kappaY = createDoubleParamElement(doc, "KappaY", 2.0, 0.5, 500);
			params.add(kappaY);
			Element modYEl = doc.createElement("KappaYMod");
			modYEl.setAttribute(XMLLoader.CLASS_NAME_ATTR,  modifier.SimpleModifier.class.getCanonicalName());
			
			kappaR.appendChild(modREl);
			kappaY.appendChild(modYEl);
			
			el.appendChild(baseEl);
			el.appendChild(kappaR);
			el.appendChild(kappaY);
			
			return el;
		}
		
		return null;
	}
	
	private static Element createDoubleParamElement(Document doc, String label, double value, double lowerBound, double upperBound) {
		Element el = doc.createElement(label);
		el.setAttribute(XMLLoader.CLASS_NAME_ATTR,  parameter.DoubleParameter.class.getCanonicalName());
		el.setAttribute("value", "" + value);
		el.setAttribute("lowerBound", "" + lowerBound);
		el.setAttribute("upperBound", "" + upperBound);
		
		return el;
	}

	private JComboBox mutBox;
	private JComboBox rateBox;

	@Override
	public Element[] getParameters() {
		return params.toArray(new Element[]{});
	}


	@Override
	public Element[] getLikelihoods() {
		return likelihoods.toArray(new Element[]{});
	}


	@Override
	public void readNodesFromDocument(ACGDocument doc)
			throws InputConfigException {
		// TODO Auto-generated method stub
		
	}
}
