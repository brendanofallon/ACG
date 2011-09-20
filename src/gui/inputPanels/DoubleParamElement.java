package gui.inputPanels;

import gui.document.ACGDocument;

import modifier.AbstractModifier;
import modifier.ScaleModifier;
import modifier.SimpleModifier;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sun.org.apache.xalan.internal.xsltc.compiler.util.NodeType;
import com.sun.tools.javac.util.Name;

import parameter.DoubleParameter;

import xml.XMLLoader;

import gui.inputPanels.Configurator.InputConfigException;
import gui.inputPanels.DoubleModifierElement.ModType;

/**
 * Reads / writes XML from DoubleParameter elements, used by many element configurators
 * @author brendano
 *
 */
public class DoubleParamElement {

	ModType modType = null;
	String modifierLabel = null;
	Double modifierFrequency = 1.0;
	
	double value = 1.0; 
	double upperBound = Double.NEGATIVE_INFINITY;
	double lowerBound = Double.POSITIVE_INFINITY;
	String elementName = null;
	
	public DoubleParamElement() {
		//No op constructor
	}
	
	public DoubleParamElement(Element el) throws InputConfigException {
		this.readSettings(el);
	}
	
	/**
	 * Set the type of modifier for this double parameter. Null indicates no modifier
	 * @param modType
	 */
	public void setModifierType(ModType modType) {
		this.modType = modType;
	}
	
	public void setModifierFrequency(double modFreq) {
		this.modifierFrequency = modFreq;
	}
	
	/**
	 * Returns true if the given Element has class DoubleParameter
	 * @param el
	 * @return
	 */
	public static boolean isAcceptable(Element el) {
		String childClass = el.getAttribute(XMLLoader.CLASS_NAME_ATTR);
		if (childClass == null)
			return false;
		if (childClass.equals(DoubleParameter.class.getCanonicalName()))
			return true;
		
		return false;
	}
	
	public ModType getModType() {
		return modType;
	}

	public Double getModifierFrequency() {
		return modifierFrequency;
	}

	public double getValue() {
		return value;
	}

	public double getUpperBound() {
		return upperBound;
	}

	public double getLowerBound() {
		return lowerBound;
	}

	public String getElementName() {
		return elementName;
	}
	
	public void setValue(double val) {
		this.value = val;
	}
	
	public void setLowerBound(double lower) {
		this.lowerBound = lower;
	}
	
	public void setUpperBound(double upper) {
		this.upperBound = upper;
	}
	
	public void setLabel(String label) {
		this.elementName = label;
	}
	
	public void setModifierLabel(String label) {
		this.modifierLabel = label;
	}
	
	/**
	 * Sets the settings of this object to be those given by the element provided
	 * @param el
	 * @throws InputConfigException
	 */
	public void readSettings(Element el) throws InputConfigException {
		String className = el.getAttribute(XMLLoader.CLASS_NAME_ATTR);
		if (className == null || (!className.equals(DoubleParameter.class.getCanonicalName()))) {
			throw new InputConfigException("Element is not of class DoubleParameter");
		}
		
		String label = el.getNodeName();
		setLabel(label);
		
		String valStr = el.getAttribute(DoubleParameter.XML_VALUE);
		if (valStr == null)
			throw new InputConfigException("Could not find value for DoubleParameter with label " + elementName);
		try {
			Double val = Double.parseDouble(valStr);
			setValue(val);
		}
		catch (NumberFormatException ex) {
			throw new InputConfigException("Could not parse value for DoubleParameter with label " + elementName + ", got : " + valStr);
		}
		
		String lowerStr = el.getAttribute(DoubleParameter.XML_LOWERBOUND);
		if (lowerStr != null) {
			try {
				Double val = Double.parseDouble(lowerStr);
				setLowerBound(val);
			}
			catch (NumberFormatException ex) {
				throw new InputConfigException("Could not parse lower bound for DoubleParameter with label " + elementName + ", got : " + valStr);
			}
		}
		
		String upperStr = el.getAttribute(DoubleParameter.XML_UPPERBOUND);
		if (upperStr != null) {
			try {
				Double val = Double.parseDouble(upperStr);
				setUpperBound(val);
			}
			catch (NumberFormatException ex) {
				throw new InputConfigException("Could not parse upper bound for DoubleParameter with label " + elementName + ", got : " + valStr);
			}
		}
		
		NodeList childNodes = el.getChildNodes();
		boolean found = false;
		for(int i=0; i<childNodes.getLength(); i++) {
			Node node = childNodes.item(i);
			if ( node.getNodeType() == Node.ELEMENT_NODE ) {
				Element childEl = (Element)node;
				String childClass = childEl.getAttribute(XMLLoader.CLASS_NAME_ATTR);
				if (childClass != null) {
					if (childClass.equals( SimpleModifier.class.getCanonicalName())) {
						this.setModifierType(ModType.Simple);
						this.setModifierLabel(childEl.getNodeName());
						found = true;
					}
					if (childClass.equals( ScaleModifier.class.getCanonicalName())) {
						this.setModifierType(ModType.Scale);
						this.setModifierLabel(childEl.getNodeName());
						found = true;
					}
					
					String freqStr = childEl.getAttribute(AbstractModifier.XML_FREQUENCY);
					if (freqStr != null && freqStr.length()>0) {
						try {
						Double freq = Double.parseDouble(freqStr);
						if (freq != null)
							setModifierFrequency(freq);
						}
						catch (NumberFormatException nfe) {
							throw new InputConfigException("Could not parse frequency for modifier with label: " + childEl.getNodeName() + ", found : " + freqStr );
						}
					}
					
					
					if (!found)
						setModifierType(null);
				}
				
			}
		}
		
	}
	
	public Element getElement(ACGDocument doc) throws InputConfigException {
		if (elementName == null)
			throw new InputConfigException("Element label required for double parameter");
	
		Element el = doc.createElement(elementName);
		el.setAttribute(XMLLoader.CLASS_NAME_ATTR, DoubleParameter.class.getCanonicalName());
		el.setAttribute(DoubleParameter.XML_VALUE, "" + value);
		el.setAttribute(DoubleParameter.XML_LOWERBOUND, "" + lowerBound);
		el.setAttribute(DoubleParameter.XML_UPPERBOUND, "" + upperBound);
		
		if (modType != null) {
			DoubleModifierElement modEl = new DoubleModifierElement();
			modEl.setType( modType );
			modEl.setLabel(modifierLabel);
			modEl.setFrequency(modifierFrequency);
			el.appendChild( modEl.getElement(doc));
		}
		
		return el;
	}
}
