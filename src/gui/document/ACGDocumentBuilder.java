package gui.document;

import gui.inputPanels.Configurator.InputConfigException;
import gui.inputPanels.ElementProvider;

import java.io.File;
import java.io.StringWriter;
import java.sql.Time;
import java.util.Date;
import java.util.List;

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

	ACGDocument acgDoc  = null;
	Document domDoc = null;
	Element rootElement = null;
	Element randomSource = null;
	
	private static String documentHeader = "ACG input document created by ACG Document Builder.\n To run this file, open it with ACG or type java -jar acg.jar [this file name] at the command line";

	
	public ACGDocumentBuilder() throws ParserConfigurationException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		domDoc = builder.newDocument();
		acgDoc = new ACGDocument( domDoc );
		rootElement = domDoc.createElement("ACG");
		domDoc.appendChild(rootElement);
	}
	
	/**
	 * Append a generic header to the document
	 */
	public void appendHeader() {
		Node topComment = domDoc.createComment(documentHeader);
		domDoc.appendChild(topComment);
	}
	
	public void appendTimeAndDateComment() {
		Date today = new Date();
		String str = "Created by ACG document builder on " + today;
		Node dateComment = domDoc.createComment(str);
		domDoc.appendChild(dateComment);		
	}
	
	public void addRandomSource() {
		Element ranEl = domDoc.createElement("RandomSource");
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
	
	public void appendEmptyNode() {
		Node node = domDoc.createTextNode(" ");
		appendNode(node);
	}
	
	/**
	 * Append all nodes obtained from provider.getElements() to the ACG document
	 * @param provider
	 * @throws InputConfigException 
	 */
	public void appendNodes(ElementProvider provider) throws InputConfigException {
		List<Node> elements = provider.getElements(acgDoc);
		for(Node node : elements) {
			appendNode(node);
		}
	}
	
	/**
	 * Obtain the primary XML Document created by this DocumentBuilder
	 * @return
	 */
	public Document getDocument() {
		return domDoc;
	}
	
	public ACGDocument getACGDocument() {
		return acgDoc;
	}
	
}
