package gui.inputPanels;

import gui.document.ACGDocument;

import modifier.AbstractModifier;
import modifier.ScaleModifier;
import modifier.SimpleModifier;

import org.w3c.dom.Element;

import parameter.DoubleParameter;

import xml.XMLLoader;

import gui.inputPanels.Configurator.InputConfigException;

/**
 * Reads / writes XML for the two classes SimpleModifier and ScaleModifier
 * @author brendano
 *
 */
public class DoubleModifierElement {

	enum ModType {Simple, Scale};
	
	private ModType type = null;
	private double frequency = 1.0;
	
	private String label = null;
	
	public void setLabel(String label) {
		this.label = label;
	}
	
	public void setType(ModType type) {
		this.type = type;
	}
	
	public void setFrequency(double freq) {
		this.frequency = freq;
	}
	
	public void readElement(Element el) throws InputConfigException {
		String className = el.getAttribute(XMLLoader.CLASS_NAME_ATTR);
		if (className == null) {
			throw new InputConfigException("No class found for element with label : " + el.getNodeName());
		}
	
		if ( (!className.equals(SimpleModifier.class.getCanonicalName())) && (!className.equals(ScaleModifier.class.getCanonicalName())) ) {
			throw new InputConfigException("Element is not of class Simple or Scale Modifier");
		}
		
		setLabel(el.getNodeName());
		if (className.equals(SimpleModifier.class.getCanonicalName())) 
			setType(ModType.Simple);
		if (className.equals(ScaleModifier.class.getCanonicalName())) 
			setType(ModType.Scale);
		
		String freqStr = el.getAttribute(AbstractModifier.XML_FREQUENCY);
		if (freqStr != null) {
			try {
				Double freq = Double.parseDouble(freqStr);
				setFrequency(freq);
			}
			catch (NumberFormatException nfe) {
				throw new InputConfigException("Could not parse frequency from element : " + el.getNodeName());
			}
		}
		
	}
	
	public Element getElement(ACGDocument doc) throws InputConfigException {
		if (label == null)
			throw new InputConfigException("No label set for DoubleModifierElement");
		if (type == null)
			throw new InputConfigException("No type set of DoubleModifier element with label : " + label);
		
		Element el = doc.createElement(label);
		
		if (type == ModType.Simple) {
			el.setAttribute(XMLLoader.CLASS_NAME_ATTR, SimpleModifier.class.getCanonicalName());
		}
		if (type == ModType.Scale) {
			el.setAttribute(XMLLoader.CLASS_NAME_ATTR, ScaleModifier.class.getCanonicalName());
		}
		
		el.setAttribute(AbstractModifier.XML_FREQUENCY, "" + frequency);
		
		return el;
	}
}
