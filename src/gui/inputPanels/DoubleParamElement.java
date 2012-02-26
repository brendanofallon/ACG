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


package gui.inputPanels;

import java.util.List;

import gui.document.ACGDocument;

import modifier.AbstractModifier;
import modifier.ScaleModifier;
import modifier.SimpleModifier;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import parameter.DoubleParameter;

import xml.XMLLoader;

import gui.inputPanels.Configurator.InputConfigException;
import gui.inputPanels.DoubleModifierElement.ModType;
import gui.inputPanels.DoublePriorModel.PriorType;

/**
 * Reads / writes XML from DoubleParameter elements, used by many element configurators
 * @author brendano
 *
 */
public class DoubleParamElement {

	ModType modType = null;
	String modifierLabel = null;
	Double modifierFrequency = 1.0;
	
	double frequency = 1.0;
	

	double value = 1.0; 
	double upperBound = Double.POSITIVE_INFINITY;
	double lowerBound = Double.NEGATIVE_INFINITY;
	String elementName = null;
	
	private DoublePriorModel priorModel = new DoublePriorModel(this); 
	
	public DoubleParamElement() {
		priorModel.setType(PriorType.Uniform);
	}
	
	public DoubleParamElement(ACGDocument doc, Element el) throws InputConfigException {
		this.readSettings(doc, el);
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
	
	public double getFrequency() {
		return frequency;
	}

	public void setFrequency(double frequency) {
		this.frequency = frequency;
	}
	
	public ModType getModType() {
		return modType;
	}

	public String getModLabel() {
		return modifierLabel;
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

	public String getLabel() {
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
	 * The model describing prior information for this parameter, may be null
	 * if nothing has been specified
	 * @return
	 */
	public DoublePriorModel getPriorModel() {
		return priorModel;
	}
	
	/**
	 * Sets the settings of this object to be those given by the element provided
	 * @param el
	 * @throws InputConfigException
	 */
	public void readSettings(ACGDocument doc, Element el) throws InputConfigException {
		String className = el.getAttribute(XMLLoader.CLASS_NAME_ATTR);
		
		//The following sanity check actually breaks if we pass in an element that is a subclass of 
		//DoubleParameter (for instance, ConstantPopSize)... so it's turned off for now. If we do happen
		//to pass in something totally bogus, the later sanity checks in this method will catch it 
//		if (className == null || (!className.equals(DoubleParameter.class.getCanonicalName()))) {
//			throw new InputConfigException("Element is not of class DoubleParameter");
//		}
		
		String label = el.getNodeName();
		setLabel(label);
		
		String valStr = el.getAttribute(DoubleParameter.XML_VALUE);
		if (valStr == null || valStr.length()==0)
			throw new InputConfigException("Could not find value for DoubleParameter with label " + elementName);
		try {
			Double val = Double.parseDouble(valStr);
			setValue(val);
		}
		catch (NumberFormatException ex) {
			throw new InputConfigException("Could not parse value for DoubleParameter with label " + elementName + ", got : " + valStr);
		}
		
		
		valStr = el.getAttribute(DoubleParameter.XML_PARAM_FREQUENCY);
		if (valStr != null && valStr.length()>0) {
			try {
				Double val = Double.parseDouble(valStr);
				setFrequency(val);
			}
			catch (NumberFormatException ex) {
				throw new InputConfigException("Could not parse frequency for DoubleParameter with label " + elementName + ", got : " + valStr);
			}
		}
		
		
		String lowerStr = el.getAttribute(DoubleParameter.XML_LOWERBOUND);
		if (lowerStr != null && lowerStr.length()>0) {
			try {
				Double val = Double.parseDouble(lowerStr);
				setLowerBound(val);
			}
			catch (NumberFormatException ex) {
				throw new InputConfigException("Could not parse lower bound for DoubleParameter with label " + elementName + ", got : " + lowerStr);
			}
		}
		
		String upperStr = el.getAttribute(DoubleParameter.XML_UPPERBOUND);
		if (upperStr != null && upperStr.length()>0) {
			try {
				Double val = Double.parseDouble(upperStr);
				setUpperBound(val);
			}
			catch (NumberFormatException ex) {
				throw new InputConfigException("Could not parse upper bound for DoubleParameter with label " + elementName + ", got : " + upperStr);
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
		
		Element prior = ModelElement.getPriorForParam(doc, getLabel());
		if (prior != null) {
			getPriorModel().readSettings(prior);
		}
		else {
			System.out.println("WARNING: No prior found for parameter : " + getLabel() + " defaulting to uniform prior");
			getPriorModel().setType(PriorType.Uniform);
		}
		
	}
	
	public Element getElement(ACGDocument doc) throws InputConfigException {		
		if (elementName == null)
			throw new InputConfigException("Element label required for double parameter");
		
		Element el = doc.createElement(elementName);
		el.setAttribute(XMLLoader.CLASS_NAME_ATTR, DoubleParameter.class.getCanonicalName());
		el.setAttribute(DoubleParameter.XML_VALUE, "" + value);
		el.setAttribute(DoubleParameter.XML_PARAM_FREQUENCY, "" + frequency);
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
