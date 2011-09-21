package gui.inputPanels;

import gui.document.ACGDocument;
import gui.inputPanels.Configurator.InputConfigException;

import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import coalescent.DemographicParameter;

import parameter.DoubleParameter;
import xml.XMLLoader;

public abstract class ModelElement implements ElementProvider {
	
	
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
		if (valStr == null)
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
}
