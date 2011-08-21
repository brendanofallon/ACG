package gui.inputPanels;

import gui.widgets.RoundedPanel;

import java.awt.Dimension;

import javax.swing.JComboBox;
import javax.swing.JLabel;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import xml.XMLLoader;

public class SiteModelConfigurator extends RoundedPanel implements Configurator {
	
	public SiteModelConfigurator() {
		setMaximumSize(new Dimension(1000, 55));
		setPreferredSize(new Dimension(500, 55));
		add(new JLabel("Mutation model: "));
		mutBox = new JComboBox(new Object[]{"JC69", "K2P", "F84", "TN93"}); 
		add(mutBox);
		add(new JLabel("Rate model: "));
		rateBox = new JComboBox(new Object[]{"1 rate", "Gamma rates", "Custom rates"}); 
		add(rateBox);
	}
	
	@Override
	public Node[] getXMLNodes(Document doc) {
		Element mutNode = createMutNode(doc);
		Element siteNode = createSiteNode(doc);
		
		return new Node[]{mutNode};
	}
	
	private Element createSiteNode(Document doc) {
	
		return null;
	}

	private Element createMutNode(Document doc) {
		System.out.println("Selected model is : " + mutBox.getSelectedItem().toString() );
		if (mutBox.getSelectedItem().toString().equals("F84")) {
			Element el = doc.createElement("F84Model");
			el.setAttribute(XMLLoader.CLASS_NAME_ATTR, "" + dlCalculation.substitutionModels.F84Matrix.class);

			Element baseEl = doc.createElement("BaseFrequencies");
			baseEl.setAttribute(XMLLoader.CLASS_NAME_ATTR, "" + dlCalculation.substitutionModels.BaseFrequencies.class);
			Element baseModEl = doc.createElement("BaseFreqsMod");
			baseModEl.setAttribute(XMLLoader.CLASS_NAME_ATTR, "" + modifier.DirichletModifier.class);
			baseEl.appendChild(baseModEl);
			
			Element kappa = createDoubleParamElement(doc, "Kappa", 2.0, 0.5, 500);
			
			Element modEl = doc.createElement("KappaMod");
			modEl.setAttribute(XMLLoader.CLASS_NAME_ATTR, "" + modifier.SimpleModifier.class);
			
			kappa.appendChild(modEl);
			el.appendChild(baseEl);
			el.appendChild(kappa);
			return el;
		}
		
		if (mutBox.getSelectedItem().toString().equals("TN93")) {
			Element el = doc.createElement("TN93Model");
			el.setAttribute(XMLLoader.CLASS_NAME_ATTR, "" + dlCalculation.substitutionModels.TN93Matrix.class);
			
			Element baseEl = doc.createElement("BaseFrequencies");
			baseEl.setAttribute(XMLLoader.CLASS_NAME_ATTR, "" + dlCalculation.substitutionModels.BaseFrequencies.class);
			Element baseModEl = doc.createElement("BaseFreqsMod");
			baseModEl.setAttribute(XMLLoader.CLASS_NAME_ATTR, "" + modifier.DirichletModifier.class);
			baseEl.appendChild(baseModEl);
			
			Element kappaR = createDoubleParamElement(doc, "KappaR", 2.0, 0.5, 500);
			Element modREl = doc.createElement("KappaRMod");
			modREl.setAttribute(XMLLoader.CLASS_NAME_ATTR, "" + modifier.SimpleModifier.class);

			Element kappaY = createDoubleParamElement(doc, "KappaY", 2.0, 0.5, 500);
			Element modYEl = doc.createElement("KappaYMod");
			modYEl.setAttribute(XMLLoader.CLASS_NAME_ATTR, "" + modifier.SimpleModifier.class);
			
			kappaR.appendChild(modREl);
			kappaY.appendChild(modYEl);
			
			el.appendChild(baseEl);
			el.appendChild(kappaR);
			el.appendChild(kappaY);
			
			return el;
		}
		
		
		System.out.println("Returning NULL");
		return null;
	}
	
	private static Element createDoubleParamElement(Document doc, String label, double value, double lowerBound, double upperBound) {
		Element el = doc.createElement(label);
		el.setAttribute(XMLLoader.CLASS_NAME_ATTR, "" + parameter.DoubleParameter.class);
		el.setAttribute("value", "" + value);
		el.setAttribute("lowerBound", "" + lowerBound);
		el.setAttribute("upperBound", "" + upperBound);
		
		return el;
	}

	private JComboBox mutBox;
	private JComboBox rateBox;
}
