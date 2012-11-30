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


package document;


import java.io.File;
import java.io.StringWriter;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Stack;

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

import newgui.gui.modelElements.ElementProvider;
import newgui.gui.modelElements.Configurator.InputConfigException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import component.LikelihoodComponent;

import parameter.AbstractParameter;
import parameter.CompoundParameter;

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

	//Holds a reference to all parameters that have been added
	List<Element> parameters = new ArrayList<Element>();
	

	//Holds a reference to all parameters that have been added
	List<Element> likelihoods = new ArrayList<Element>();
	
	
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
		
		//Scan node for parameters and likelihoods to add to list
		scoopParametersAndLikelihoods(node);
	}
	
	/**
	 * Traverse this node and all descendants looking for any element children that are
	 * parameters and add them
	 * @param node
	 */
	private void scoopParametersAndLikelihoods(Node node) {
		Stack<Element> stack = new Stack<Element>();
		if (node.getNodeType() == Node.ELEMENT_NODE)
			stack.push((Element)node);
		
		while( ! stack.isEmpty()) {
			Element el = stack.pop();
			if (isParameter(el))
				parameters.add(el);
			if (isLikelihood(el))
				likelihoods.add(el);
			//NodeList children = el.getChildNodes();
			for(Node child = el.getFirstChild(); child != null; child = child.getNextSibling()) {
				if (child.getNodeType() == Node.ELEMENT_NODE)
					stack.push((Element)child);
			}
		}
	}
	
	/**
	 * True if the given element has a class attribute from which an AbstractParameter is assignable
	 * @param el
	 * @return
	 */
	private static boolean isParameter(Element el) {
		String className = el.getAttribute(XMLLoader.CLASS_NAME_ATTR);
		if (className == null || className.length()==0 || className.equals(XMLLoader.LIST_ATTR))
			return false;
		
		try {
			Class<?> clazz = ClassLoader.getSystemClassLoader().loadClass(className);
			if (AbstractParameter.class.isAssignableFrom(clazz)) {
				//Don't load CompoundParameters by default
//				if (CompoundParameter.class.isAssignableFrom(clazz))
//					return false;
				
				//System.out.println("Class " + clazz + " is assignable from AbstractParam");
				return true;
			}
		} catch (ClassNotFoundException e) {
			System.err.println("Could not load class \'" + className + "\': " + e);
			return false;
		}
		return false;
	}

	/**
	 * Returns true if the given element has a class name attribute from which a LikelihoodComponent is assignable
	 * @param el
	 * @return
	 */
	private static boolean isLikelihood(Element el) {
		String className = el.getAttribute(XMLLoader.CLASS_NAME_ATTR);
		if (className == null || className.length()==0 || className.equals(XMLLoader.LIST_ATTR))
			return false;
		
		try {
			Class clazz = ClassLoader.getSystemClassLoader().loadClass(className);
			//System.out.println("Loaded class : " + clazz);
			if (LikelihoodComponent.class.isAssignableFrom(clazz)) {
				return true;
			}
		} catch (ClassNotFoundException e) {
			System.err.println("Could not load class \'" + className + "\': " + e);
			return false;
		}
		return false;
	}
	
	public void appendEmptyNode() {
		Node node = domDoc.createTextNode("\n");
		appendNode(node);
	}
	
	/**
	 * Get all parameter elements (Elements with a class that is assignable from AbstractParameter) that have
	 * been added to this builder 
	 * @return
	 */
	public List<Element> getParameters() {
		return parameters;
	}
	
	
	/**
	 * Get all likelihoods elements (Elements with a class that is assignable from LikelihoodComponent) that have
	 * been added to this builder 
	 * @return
	 */
	public List<Element> getLikelihoods() {
		return likelihoods;
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
