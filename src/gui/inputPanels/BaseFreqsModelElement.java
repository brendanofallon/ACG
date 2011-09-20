package gui.inputPanels;

import modifier.AbstractModifier;
import modifier.DirichletModifier;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import dlCalculation.substitutionModels.BaseFrequencies;
import dlCalculation.substitutionModels.TN93Matrix;
import gui.document.ACGDocument;
import gui.inputPanels.Configurator.InputConfigException;
import parameter.DoubleParameter;
import sequence.DNAUtils;
import xml.XMLLoader;
import xml.XMLUtils;

public class BaseFreqsModelElement {

	private boolean estimate = true;
	private String modNodeLabel = null;
	private double initA = 0.25;
	private double initC = 0.25;
	private double initG = 0.25;
	private double initT = 0.25;
	
	private String defaultLabel = "BaseFrequencies";
	private String label = defaultLabel;
	
	public void setLabel(String label) {
		this.label = label;
	}
	
	public static boolean isAcceptable(Element el) {
		String childClass = el.getAttribute(XMLLoader.CLASS_NAME_ATTR);
		if (childClass == null)
			return false;
		if (childClass.equals(BaseFrequencies.class.getCanonicalName()))
			return true;
		
		return false;
	}
	
	
	public void readSettings(Element el) throws InputConfigException {
		String className = el.getAttribute(XMLLoader.CLASS_NAME_ATTR);
		if (className == null || (!className.equals(BaseFrequencies.class.getCanonicalName()))) {
			throw new InputConfigException("Element is not of class DoubleParameter");
		}
		
		setLabel(el.getNodeName());
		
		String statStr = el.getAttribute(TN93Matrix.XML_STATIONARIES);
		if (statStr != null) {
			String[] stats = statStr.split(" ");
			if (stats.length != 4) {
				throw new InputConfigException("Could not parse stationaries from argument list, got : " + statStr);
			}

			try {
				initA = Double.parseDouble(stats[DNAUtils.A]);
				initC = Double.parseDouble(stats[DNAUtils.C]);
				initG = Double.parseDouble(stats[DNAUtils.G]);
				initT = Double.parseDouble(stats[DNAUtils.T]);
			}
			catch (NumberFormatException nfe) {
				throw new InputConfigException("Could not parse a number from the stationaries list : " + statStr);
			}	

		}
		else {
			initA = 0.25;
			initC = 0.25;
			initG = 0.25;
			initT = 0.25;
		}
		
		
		boolean foundMod = false;
		NodeList children = el.getChildNodes();
		for(int i=0; i<children.getLength(); i++) {
			Node node = children.item(i);
			if (node instanceof Element) {
				Element child = (Element)node;
				String childClass = child.getAttribute(XMLLoader.CLASS_NAME_ATTR);
				if (childClass != null && childClass.equals(DirichletModifier.class.getCanonicalName())) {
					foundMod = true;
					estimate = true;
					modNodeLabel = child.getNodeName();
				}
			}
		}
		
		//No modifiers found, so we're not estimating base frequencies here
		if (!foundMod) {
			estimate = false;
		}
		
		
	}
	
	
	public void setStationaries(double a, double c, double g, double t) {
		this.initA = a;
		this.initC = c;
		this.initG = g;
		this.initT = t;
	}
	
	public void setEstimate(boolean estimate) {
		this.estimate = estimate;
	}
	
	public Element getElement(ACGDocument doc) {
		Element el = doc.createElement(label);
		el.setAttribute(XMLLoader.CLASS_NAME_ATTR, BaseFrequencies.class.getCanonicalName());
	
		String statStr= initA + "  " + initC + "  " + initG + "  " + initG;
		el.setAttribute(TN93Matrix.XML_STATIONARIES, statStr);
		
		if (estimate) {
			Element dirichletMod;
			if (modNodeLabel != null)
				dirichletMod = doc.createElement(modNodeLabel);
			else
				dirichletMod = doc.createElement(label + "Mod");

			dirichletMod.setAttribute(XMLLoader.CLASS_NAME_ATTR, DirichletModifier.class.getCanonicalName());
			dirichletMod.setAttribute(AbstractModifier.XML_FREQUENCY, "0.1");
			el.appendChild(dirichletMod);
		}
		
		return el;
	}
}

