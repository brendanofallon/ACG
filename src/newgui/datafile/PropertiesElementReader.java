package newgui.datafile;

import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Generic DOM parser/ converter for maps of key-value pairs, useful for storing and reading of arbitrary maps
 * in XML elements
 * @author brendano
 *
 */
public class PropertiesElementReader {

	public static final String XML_PROPERTIES = "property.map";
	public static final String XML_ENTRY = "entry";
	public static final String XML_KEY = "key";
	
	
	public static Map<String, String> readProperties(Element el) throws XMLConversionError {
		if (! el.getNodeName().equals(XML_PROPERTIES)) {
			throw new XMLConversionError("Element is not a property map", el);
		}
		
		Map<String, String> map = new HashMap<String, String>();
		NodeList children = el.getChildNodes();
		for(int i=0; i<children.getLength(); i++) {
			Node child = children.item(i);
			if (child.getNodeType() == Node.ELEMENT_NODE && child.getNodeName().equals(XML_ENTRY)) {
				Element childEl = (Element)child;
				String key = childEl.getAttribute(XML_KEY);
				String value = getTextContent(childEl);
				if (key == null)
					throw new XMLConversionError("No key found for element", childEl);
				if (value == null)
					throw new XMLConversionError("No value (text content) found for element ", childEl);
				System.out.println("Found " + key + " : " + value);
				map.put(key, value);
			}
		}
		return map;
	}
	
	public static Element createElement(Document doc, Map<String, String> map) {
		Element propEl = doc.createElement(XML_PROPERTIES);
		assignMap(doc, propEl, map);
		return propEl;
	}
	
	/**
	 * Clear current children of el and assign 
	 * @param doc
	 * @param el
	 * @param map
	 */
	public static void assignMap(Document doc, Element el, Map<String, String> map) {
		//Remove all children from element
		
		NodeList children = el.getChildNodes();
		for(int i=0; i<children.getLength(); i++)
			el.removeChild(children.item(i));
		
		for(String key : map.keySet()) {
			String value = map.get(key);
			Element propEl = createEntryElement(doc, key, value);
			el.appendChild(propEl);
		}
	}
	
	
	public static Element createEntryElement(Document doc, String key, String value) {
		Element el = doc.createElement(XML_ENTRY);
		el.setAttribute(XML_KEY, key);
		Node valueNode = doc.createTextNode(value);
		el.appendChild(valueNode);
		return el;
	}
	
	/**
	 * Return the value of the first text node child of the given element
	 * @param el
	 */
	public static String getTextContent(Element el) {
		NodeList children = el.getChildNodes();
		for(int i=0; i<children.getLength(); i++) {
			Node child = children.item(i);
			if (child.getNodeType() == Node.TEXT_NODE) {
				return child.getNodeValue();
			}
		}
		return null;
	}
	
}
