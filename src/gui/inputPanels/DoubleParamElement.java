package gui.inputPanels;

import gui.document.ACGDocument;

import org.w3c.dom.Element;

import parameter.DoubleParameter;

import xml.XMLLoader;

import gui.inputPanels.Configurator.InputConfigException;

/**
 * Reads / writes XML from DoubleParameter elements, used by many element configurators
 * @author brendano
 *
 */
public class DoubleParamElement {

	double value = 1.0; 
	double upperBound = Double.NEGATIVE_INFINITY;
	double lowerBound = Double.POSITIVE_INFINITY;
	String elementName = null;
	
	
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
		
		
		
	}
	
	public Element getElement(ACGDocument doc) throws InputConfigException {
		if (elementName == null)
			throw new InputConfigException("Element label required for double parameter");
	
		Element el = doc.createElement(elementName);
		el.setAttribute(XMLLoader.CLASS_NAME_ATTR, DoubleParameter.class.getCanonicalName());
		el.setAttribute(DoubleParameter.XML_VALUE, "" + value);
		el.setAttribute(DoubleParameter.XML_LOWERBOUND, "" + lowerBound);
		el.setAttribute(DoubleParameter.XML_UPPERBOUND, "" + upperBound);
		
		return el;
	}
}
