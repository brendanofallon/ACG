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


import java.util.List;

import newgui.gui.modelElements.Configurator.InputConfigException;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import document.ACGDocument;

import xml.XMLLoader;
import priors.AbstractPrior;

/**
 * Base class of many objects that can read data from XML and can generate XML nodes to reflect the 
 * data that was read or provided through other means (for instance, from a View component)
 * @author brendano
 *
 */
public abstract class ModelElement implements ElementProvider {
	
	/**
	 * Convenient access to the doubleparameters created by this element
	 * @return
	 */
	public abstract List<DoubleParamElement> getDoubleParameters();
	
	/**
	 * Return a list of DOM elements representing the data in this ModelElement
	 * @param doc
	 * @return
	 */
	public abstract List<Node> getElements(ACGDocument doc) throws InputConfigException;
	
	/**
	 * Read settings from the given document and store the data. Subsequent calls to
	 * getElements should return the information that was read in. 
	 * @param doc
	 */
	public abstract void readElements(ACGDocument doc) throws InputConfigException;
	
	/**
	 * Search the document for a single element of the given class. Exceptions are thrown if there
	 * are either zero or more than one element of the class
	 * @param doc
	 * @param classType
	 * @return
	 * @throws InputConfigException
	 */
	protected static Element getSingleElementForClass(ACGDocument doc, Class classType) throws InputConfigException {
		List<String> labels = doc.getLabelForClass(classType);
		
		if (labels.size()==0) 
			throw new InputConfigException("Could not find any elements with class " + classType);
		if (labels.size()>1) 
			throw new InputConfigException("Found multiple elements of class " + classType + ", right now we can only handle 1.");
		
		return doc.getElementForLabel(labels.get(0));
	}
	
	/**
	 * Returns the first element encountered that whose class isAssignableFrom AbstractPrior and 
	 * which has a reference to a node with the given label. This is pretty likely to be the prior
	 * for the given parameter
	 * @param doc
	 * @param paramLabel
	 * @return
	 */
	protected static Element getPriorForParam(ACGDocument doc, String paramLabel) {
		for(Element el : doc.getElements()) {
			Class<?> elementClass = doc.getClassForLabel(el.getNodeName());
			if (AbstractPrior.class.isAssignableFrom( elementClass )) {
				if (doc.getElementRefersToLabel(el, paramLabel)) {
					return el;
				}
			}
			
		}
		
		return null;
	}
	
	/**
	 * Search the document for an element of the given class. An exception is thrown if there
	 * is more than one element of the class
	 * @param doc
	 * @param classType
	 * @return
	 * @throws InputConfigException
	 */
	protected static Element getOptionalElementForClass(ACGDocument doc, Class classType) throws InputConfigException {
		List<String> labels = doc.getLabelForClass(classType);
		
		if (labels.size()>1) 
			throw new InputConfigException("Found multiple elements of class " + classType + ", right now we can only handle 1.");
		
		return doc.getElementForLabel(labels.get(0));
	}
	
	/**
	 * Return the number of immediate element descendants from the given element
	 * @param doc
	 * @param el
	 * @return
	 */
	protected static int getChildCount(Element el) {
		int count = 0;
		NodeList nodes = el.getChildNodes();
		for(int i=0; i<nodes.getLength(); i++) {
			if (nodes.item(i).getNodeType()==Node.ELEMENT_NODE)
				count++;
		}
		return count;
	}
	
	protected static int getChildCount(ACGDocument doc, Element el) {
		return doc.getChildrenForLabel(el.getNodeName()).size();
	}
	
	/**
	 * Return the number of immediate descendant elements from the element with the given label
	 * @param doc
	 * @param label
	 * @return
	 */
	protected static int getChildCount(ACGDocument doc, String label) {
		return doc.getChildrenForLabel(label).size();
	}
	
	/**
	 * Get the child Element at the given index from the element provided
	 * @param doc
	 * @param el
	 * @param which
	 * @return
	 */
	protected static Element getChild(ACGDocument doc, Element el, int which) {
		List<String> childRefs = doc.getChildrenForLabel(el.getNodeName());
		if (childRefs.size() <= which)
			return null;
		else
			return doc.getElementForLabel( childRefs.get(which));
	}
	
	/**
	 * Attempt to find an attribute with the given key and parse a Double from its value. 
	 * If no value if found, null is returned. If a value is found but we can't parse a double from it,
	 * an InputConfigException is thrown
	 * @param el
	 * @param key
	 * @return
	 * @throws InputConfigException
	 */
	protected static Double getOptionalDoubleAttribute(Element el, String key) throws InputConfigException {
		String valStr = el.getAttribute(key);
		if (valStr == null || valStr.length()==0)
			return null;
		try {
			Double val = Double.parseDouble(valStr);
			return val;
		}
		catch (NumberFormatException nfe) {
			throw new InputConfigException("Could not parse a value for attribute : " + key + " from element with label: " + el.getNodeName());
		}
	}
	
	
	/**
	 * Attempt to find an attribute with the given key and parse a Double from its value. 
	 * If no value if found or if a double cannot be parsed from the value an 
	 * InputConfigException is thrown
	 * @param el
	 * @param key
	 * @return
	 * @throws InputConfigException
	 */
	protected Double getDoubleAttribute(Element el, String key) throws InputConfigException {
		String valStr = el.getAttribute(key);
		if (valStr == null || valStr.length()==0)
			throw new InputConfigException("Could not find required attribute : " + key + " from element with label: " + el.getNodeName());
		
		try {
			Double val = Double.parseDouble(valStr);
			return val;
		}
		catch (NumberFormatException nfe) {
			throw new InputConfigException("Could not parse a value for attribute : " + key + " from element with label: " + el.getNodeName());
		}
	}
	
	
	/**
	 * Attempt to find an attribute with the given key and parse a Integer from its value. 
	 * If no value if found, null is returned. If a value is found but we can't parse a double from it,
	 * an InputConfigException is thrown
	 * @param el
	 * @param key
	 * @return
	 * @throws InputConfigException
	 */
	protected static Integer getOptionalIntegerAttribute(Element el, String key) throws InputConfigException {
		String valStr = el.getAttribute(key);
		if (valStr == null || valStr.length()==0)
			return null;
		try {
			Integer val = Integer.parseInt(valStr);
			return val;
		}
		catch (NumberFormatException nfe) {
			throw new InputConfigException("Could not parse a value for attribute : " + key + " from element with label: " + el.getNodeName());
		}
	}
	
	/**
	 * Attempt to find an attribute with the given key and parse a Integer from its value. 
	 * If no value if found, null is returned. If a value is found but we can't parse a double from it,
	 * an InputConfigException is thrown
	 * @param el
	 * @param key
	 * @return
	 * @throws InputConfigException
	 */
	protected static Integer getIntegerAttribute(Element el, String key) throws InputConfigException {
		String valStr = el.getAttribute(key);
		if (valStr == null || valStr.length()==0)
			throw new InputConfigException("Could not find required attribute : " + key + " from element with label: " + el.getNodeName());;
		try {
			Integer val = Integer.parseInt(valStr);
			return val;
		}
		catch (NumberFormatException nfe) {
			throw new InputConfigException("Could not parse a value for attribute : " + key + " from element with label: " + el.getNodeName());
		}
	}
	
	/**
	 * Create an element with the given label and of the class provided
	 * @param doc
	 * @param label
	 * @param clazz
	 */
	protected static Element createElement(ACGDocument doc, String label, Class clazz) {
		Element el  = doc.createElement(label);
		el.setAttribute(XMLLoader.CLASS_NAME_ATTR, clazz.getCanonicalName());
		return el;
	}
	
	/**
	 * Create and return a List element
	 * @param doc
	 * @param label
	 * @return
	 */
	protected static Element createList(ACGDocument doc, String label) {
		Element el  = doc.createElement(label);
		el.setAttribute(XMLLoader.CLASS_NAME_ATTR, XMLLoader.LIST_ATTR);
		return el;
	}
}
