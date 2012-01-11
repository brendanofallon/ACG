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


package gui.document;


import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.SwingWorker;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import jobqueue.ExecutingChain;

import mcmc.MCMC;
import mcmc.mc3.MC3;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;

import component.LikelihoodComponent;

import parameter.AbstractParameter;
import parameter.CompoundParameter;
import xml.InvalidInputFileException;
import xml.XMLLoader;

/**
 * Basically wraps an XML input file and provides some convenience methods like validity checking and 'getParameters...', etc
 * @author brendano
 *
 */
public class ACGDocument {

	public static final String CLASS_NAME_ATTR = "class";
	public static final String LIST_ATTR = "list";
	
	//Reference to original source file
	File sourceFile = null;
	
	//Parsed XML document
	Document doc = null;
	
	//XML class loading object which finds and loads the classes the document references
	XMLLoader loader = null;
	
	Map<String, XMLObject> objMap = new HashMap<String, XMLObject>();
	
	List<AbstractParameter<?>> params = new ArrayList<AbstractParameter<?>>();
	List<LikelihoodComponent> likelihoods = new ArrayList<LikelihoodComponent>();
	
	ValidityChecker validityChecker = new ACGValidityChecker();
	
	private boolean objectsCreated = false;
	
	public ACGDocument(File file) {
		sourceFile = file;
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder;
		try {
			builder = factory.newDocumentBuilder();
			doc = builder.parse(file);
			loader = new XMLLoader(doc);
			loader.loadAllClasses(); //Attempt to load all of the classes referenced by the document
			checkValidity(); //Must come after class loading
			turnOffMCMC(); //Find all mcmc objects and make sure they're not set to run right away
			
		} catch (InvocationTargetException ex) {
			try {
				System.out.println("Input file is : " + getXMLString());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			System.out.println(ex.getMessage());
			ex.printStackTrace();
			throw new InvalidInputFileException(ex.getTargetException().getMessage());
		} catch (Exception e) {
			try {
				System.out.println("Input file is : " + getXMLString());
			} catch (Exception ex) {
				// TODO Auto-generated catch block
				ex.printStackTrace();
			}
			System.out.println(e.getMessage());
			e.printStackTrace();
			throw new InvalidInputFileException(e.getMessage());
		}
	}
	
	
	/**
	 * Construct an ACG document from the given DOM object
	 * @param doc
	 */
	public ACGDocument(Document doc) {
		sourceFile = null;
		this.doc = doc;
		try {
			loader = new XMLLoader(doc);
		} 
		catch (Exception e) {
			try {
				System.out.println("Input file is : \n" + getXMLString());
			} catch (Exception ex) {
				// TODO Auto-generated catch block
				ex.printStackTrace();
			} 
			throw new InvalidInputFileException(e.getMessage());
		}
	}
	
	/**
	 * Return a new Element generated by the DOM document associated with this objects
	 * @param name
	 * @return
	 */
	public Element createElement(String name) {
		return doc.createElement(name);
	}
	
	/**
	 * Create a new comment node usingthe underlying DOM document
	 * @param comment
	 * @return
	 */
	public Node createComment(String comment) {
		return doc.createComment(comment);
	}
	
	
	/**
	 * Create a new text node using the underlying DOM document
	 * @param comment
	 * @return
	 */
	public Node createTextNode(String name) {
		return doc.createTextNode(name);
	}
	
	/**
	 * Set the 'source' file of this document, which is typically used to store a reference to the file from
	 * which these settings were most recently read from / written to. It may be null.
	 * @param file
	 */
	public void setSourceFile(File file) {
		this.sourceFile = file;
	}
	
	/**
	 * Get the 'source' file of this document, which is typically used to store a reference to the file from
	 * which these settings were most recently read from / written to. It may be null.
	 * @param file
	 */
	public File getSourceFile() {
		return this.sourceFile;
	}
	
	/**
	 * Load all classes referenced by this document and perform some simple validity checking
	 * @throws Exception
	 */
	public void loadAndVerifyClasses() throws Exception {
		loader.loadAllClasses(); //Attempt to load all of the classes referenced by the document
		checkValidity(); //Must come after class loading
	}
	/**
	 * Instantiate all of the objects described by the document. All exceptions are caught and
	 * wrapped into an InvalidInputFileException  
	 * @throws InvalidInputFileException
	 */
	public void instantiateAll()  {
		if (objectsCreated) {
			throw new IllegalStateException("Objects have already been created for document");
		}
		
		try {
			loader.instantiateAll();
			this.objectsCreated = true;
		} catch (InvocationTargetException e) {
			e.printStackTrace();
			throw new InvalidInputFileException(e.getTargetException().getMessage());
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new InvalidInputFileException(ex.getMessage());
		}
	}
	
	/**
	 * Creates a new ExecutingChain object that wraps an MCMC or MC3 object, begins
	 * execution of the chain, and returns the ExecutingChain object
	 * @return
	 */
	public ExecutingChain runMCMC() {
		List<String> mcLabels = getMCMCLabels();
		if (mcLabels.size()==0) {
			throw new InvalidInputFileException("Could not find any MCMC objects");
		}
		
		List<String> mcmcmcLabels = getLabelForClass(MC3.class);
		if (mcmcmcLabels.size() > 0) {
			try {
				MC3 mc3 = (MC3)loader.getObjectForLabel(mcmcmcLabels.get(0));
				ExecutingChain runner = new ExecutingChain(mc3);
				runner.execute();
				return runner;
				
			} catch (InstantiationException e) {
				throw new InvalidInputFileException("Could not create mc3 object : " + e.getMessage());
			} catch (IllegalAccessException e) {
				throw new InvalidInputFileException("Could not create mc3 object : " + e.getMessage());
			} catch (InvocationTargetException e) {
				throw new InvalidInputFileException("Could not create mc3 object : " + e.getMessage());
			}
			
		}
		
		if (mcLabels.size()==1) {
			try {
				MCMC mcmc = (MCMC)loader.getObjectForLabel(mcLabels.get(0));
				ExecutingChain runner = new ExecutingChain(mcmc);
				runner.execute();
				return runner;
			} catch (InstantiationException e) {
				throw new InvalidInputFileException("Could not create mcmc object : " + e.getMessage());
			} catch (IllegalAccessException e) {
				throw new InvalidInputFileException("Could not create mcmc object : " + e.getMessage());
			} catch (InvocationTargetException e) {
				throw new InvalidInputFileException("Could not create mcmc object : " + e.getMessage());
			}
			
		}
		
		
		return null;
	}
	
	/**
	 * Returns true if this document contains an MC3 object
	 * @return
	 */
	public boolean hasMC3() {
		return getLabelForClass(MC3.class).size() > 0;
	}
	
	/**
	 * Adds "run=false" as an attribute to all MCMC objects so they won't run right away when we create them
	 */
	public void turnOffMCMC() {
		List<String> mcLabels = getMCMCLabels();
		if (mcLabels.size()==0) {
			throw new InvalidInputFileException("Could not find any MCMC objects");
		}
		for(String label : mcLabels) {
			loader.addAttribute(label, MCMC.XML_RUNNOW, "false");
		}
		
		//Also turn off instant run for all MC3 objects, if any
		mcLabels = getLabelForClass(MC3.class);
		for(String label : mcLabels) {
			loader.addAttribute(label, MCMC.XML_RUNNOW, "false");
		}

	}

	/**
	 * Instantiate all objects in this document
	 */
//	public void createAllObjects() {
//		loader.instantiateAll();
//	}
	
	/**
	 * Put the key=value pair into the attribute list for the object with the given label. This will
	 * have no effect if the objects have already been created. 
	 * @param label
	 * @param key
	 * @param value
	 */
	public void addAttribute(String label, String key, String value) {
		loader.addAttribute(label, key, value);
	}
	
	
	/**
	 * Return a list of all of the labels of the objects that are MCMCs
	 * @return
	 */
	public List<String> getMCMCLabels() {
		return loader.getObjLabelsForClass(MCMC.class);
	}
	
	/**
	 * Return a list of all of the labels of the objects that are AbstractParameters
	 * @return
	 */
	public List<String> getParameterLabels() {
		return loader.getObjLabelsForClass(AbstractParameter.class);
	}
	

	/**
	 * Returns the object with the given label, instantiating if necessary
	 * @param label
	 * @return
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws InvocationTargetException 
	 */
	public Object getObjectForLabel(String label) throws InstantiationException, IllegalAccessException, InvocationTargetException {
		return loader.getObjectForLabel(label);
	}
	
	/**
	 * Return a list of all of the labels of the objects that are LikelihoodComponents
	 * @return
	 */
	public List<String> getLikelihoodLabels() {
		return loader.getObjLabelsForClass(LikelihoodComponent.class);
	}
	
	/**
	 * Return all object labels with a class that "isAssignableFrom" the given class
	 * @param clazz
	 * @return
	 */
	public List<String> getLabelForClass(Class clazz) {
		return loader.getObjLabelsForClass(clazz);
	}
	
	
	/**
	 * Return all object labels that are immediate descendants of the node with the 
	 * given label. Returns null if there is no node with the given label
	 * @param clazz
	 * @return
	 */
	public List<String> getChildrenForLabel(String label) {
		return loader.getChildLabelsForLabel(label);
	}
	
	public String toString() {
		StringBuilder str = new StringBuilder();
		str.append("Found " + objMap.size() + " total objects.. \n");
		for(String label : objMap.keySet()) {
			XMLObject obj = objMap.get(label);
			str.append(label + " : " + obj.className+ "\n");
		}
		
		return str.toString();
	}
	
	
	/**
	 * Check the validity of this document using the default validity checker
	 * @throws Exception
	 */
	public void checkValidity() throws Exception {
		validityChecker.checkValidity(this);
	}
	
	/**
	 * Returns the XML Element encountered in a postorder traversal of the nodes in the
	 * tree that has the given label. This Element is the one used to construct the corresponding
	 * object
	 * 
	 * @param label
	 * @return
	 */
	public Element getElementForLabel(String label) {
		return loader.getElementForLabel(label);
	}	
	
	/**
	 * Returns all Elements read by the loader
	 * @return
	 */
	public Collection<Element> getElements() {
		return loader.getElements();
	}
	
	/**
	 * Returns the class associated with the element with the given label
	 * @param label
	 * @return
	 */
	public Class<?> getClassForLabel(String label) {
		return loader.getClassForLabel(label);
	}
	
	/**
	 * Returns true if the element provided has any child element with the given label
	 * as the name of a node
	 * @param el
	 * @param label
	 * @return
	 */
	public boolean getElementRefersToLabel(Node root, String label) {
		NodeList nodeList = root.getChildNodes();
		boolean hasRef = false;
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);

			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element el = (Element) node;
				String nodeName = el.getNodeName();
				if (nodeName.equals(label)) {
					hasRef = true;
				}
				
				 if (! hasRef)
					 hasRef = getElementRefersToLabel(el, label);
			}
			
		}
		return hasRef;
	}
	
	
	
//	public void readAllNodes() {
//		readNodes(doc.getDocumentElement());
//	}
	
	/**
	 * Recursive helper function for node examination and class loading. Loads all classes below the given XML node
	 */
	private void readNodes(Node root) {
		NodeList nodeList = root.getChildNodes();

		for (int i = 0; i < nodeList.getLength(); i++) {

			Node node = nodeList.item(i);

			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element el = (Element) node;
				String className = el.getAttribute(CLASS_NAME_ATTR);
				String label = el.getNodeName();
				
				if (objMap.containsKey(label)) {
					String foundClass = objMap.get(label).className;
					if (className != null && (className.trim().length()>0) && (!foundClass.equals(className)))
						throw new InvalidInputFileException("Found conflicting classes for object with label : " + label );
				}
				else {
					//Object map didn't contain this label, so this is a new object
					if (className == null)
						throw new InvalidInputFileException("Could not find class attribute for object : " + label);
					
					XMLObject newObj = new XMLObject();
					newObj.label = label;
					newObj.className = className;
					objMap.put(label, newObj);
				}
				
				readNodes(el);
			}
			
		}	
		
		//Search for additional text elements to add in postorder
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			if (node.getNodeType() == Node.TEXT_NODE) {
				String label = root.getNodeName();
				String text = node.getNodeValue();
				if (text.trim().length()>0) {
					XMLObject xmlObj = objMap.get(label);
					if (xmlObj == null) {
						System.err.println("Found a text node with text: " + node.getNodeValue() + " but there's no constructor info for label: " + label);
					}
					else {
						//Add text content to the attribute map
						if (text.trim().length()>0) {
							xmlObj.attrs.put("content", text);
							
						}
					}
				}
			}
		}
	}
	
	
	/**
	 * Obtain an xml-style String representation of the document 
	 * @return
	 * @throws TransformerException
	 * @throws IOException 
	 */
	public String getXMLString() throws TransformerException, IOException {
		
		OutputFormat format = new OutputFormat(doc);
        format.setLineWidth(80);
        format.setIndenting(true);
        format.setIndent(4);
        Writer out = new StringWriter();
        XMLSerializer serializer = new XMLSerializer(out, format);
        serializer.serialize(doc);

        return out.toString();
		
//		TransformerFactory transfac = TransformerFactory.newInstance();
//		Transformer trans = transfac.newTransformer();
//		trans.setOutputProperty(OutputKeys.INDENT, "yes");
//		//create string from xml tree
//		StringWriter sw = new StringWriter();
//		StreamResult result = new StreamResult(sw);
//		DOMSource source = new DOMSource(doc);
//		trans.transform(source, result);
//		String xmlString = sw.toString();
//		return xmlString;
	}
	
	/**
	 * Summarizes some information about an object that will be created from the XML input file
	 * @author brendano
	 *
	 */
	class XMLObject {
		String label;
		String className;
		Map<String, String> attrs = new HashMap<String, String>();
	}

	
	
	
}
