/********************************************************************
*
* 	Copyright 2011 Brendan O'Fallon
*
*   Licensed under the Apache License, Version 2.0 (the "License");
*   you may not use this file except in compliance with the License.
*   You may obtain a copy of the License at
*
*       http://www.apache.org/licenses/LICENSE-2.0
*
*   Unless required by applicable law or agreed to in writing, software
*   distributed under the License is distributed on an "AS IS" BASIS,
*   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*   See the License for the specific language governing permissions and
*   limitations under the License.
*
***********************************************************************/


package newgui.gui.modelElements;


import modifier.AbstractModifier;
import modifier.ScaleModifier;
import modifier.SimpleModifier;
import newgui.gui.modelElements.Configurator.InputConfigException;

import org.w3c.dom.Element;

import document.ACGDocument;

import parameter.DoubleParameter;

import xml.XMLLoader;


/**
 * Reads / writes XML for the two classes SimpleModifier and ScaleModifier
 * @author brendano
 *
 */
public class DoubleModifierElement {

	public enum ModType {Simple, Scale};
	
	private ModType type = null;
	private double frequency = 1.0;
	
	private String label = null;
	
	public DoubleModifierElement() {
		
	}
	
	public DoubleModifierElement(Element el) throws InputConfigException {
		this.readElement(el);
	}
	
	/**
	 * Returns true if we can turn the given element into a SimpleModifier or ScaleModifier
	 * @param el
	 * @return
	 */
	public static boolean isAssignable(Element el) {
		String className = el.getAttribute(XMLLoader.CLASS_NAME_ATTR);
		if (className == null) {
			return false;
		}
	
		if ( (!className.equals(SimpleModifier.class.getCanonicalName())) && (!className.equals(ScaleModifier.class.getCanonicalName())) ) {
			return false;
		}
		
		return true;
	}
	
	public void setLabel(String label) {
		this.label = label;
	}
	
	public void setType(ModType type) {
		this.type = type;
	}
	
	public void setFrequency(double freq) {
		this.frequency = freq;
	}
	
	public String getLabel() {
		return label;
	}
	
	public ModType getType() {
		return type;
	}
	
	public double getFrequency() {
		return frequency;
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
		if (freqStr != null && freqStr.length()>0) {
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
