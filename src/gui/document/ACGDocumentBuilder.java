package gui.document;

import java.io.File;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import xml.XMLLoader;

/**
 * A class to aid in the creation of create an ACG document. Not really much functionality here...
 * we can just add nodes at the root level and then obtain the dom Document or a String
 * representation
 *  
 * @author brendan
 *
 */
public class ACGDocumentBuilder {

	Document doc  = null;
	Element rootElement = null;
	Element randomSource = null;
	
	public ACGDocumentBuilder() throws ParserConfigurationException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		doc = builder.newDocument();
		rootElement = doc.createElement("ACG");
		doc.appendChild(rootElement);
		
		addRandomSource();
		
	}
	
	public void addRandomSource() {
		Element ranEl = doc.createElement("RandomSource");
		ranEl.setAttribute(XMLLoader.CLASS_NAME_ATTR, math.RandomSource.class.getCanonicalName());
		rootElement.appendChild(ranEl);
	}

	/**
	 * Append this node to the root element of the document
	 * @param node
	 */
	public void appendNode(Node node) {
		rootElement.appendChild(node);
	}
	
//	public Node getNodeForLabel() {
//		
//	}
	
	/**
	 * Obtain the primary XML Document created by this DocumentBuilder
	 * @return
	 */
	public Document getDocument() {
		return doc;
	}
	
	public ACGDocument getACGDocument() {
		return new ACGDocument(getDocument());
	}
	
}
