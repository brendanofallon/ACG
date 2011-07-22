package gui;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.SwingWorker;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import mcmc.MCMC;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import component.LikelihoodComponent;

import parameter.AbstractParameter;
import xml.XMLLoader;

/**
 * Basically wraps an XML input file and provides some convenience methods like validity checking and 'getParameters...', etc
 * @author brendano
 *
 */
public class RunnableInputFile {

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
	
	public RunnableInputFile(File file) {
		sourceFile = file;
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder;
		try {
			builder = factory.newDocumentBuilder();
			doc = builder.parse(file);
			loader = new XMLLoader(doc);
			loader.loadAllClasses(); //Attempt to load all of the classes referenced by the document
			
			turnOffMCMC(); //Find all mcmc objects and make sure they're not set to run right away
			loader.instantiateAll();
			
		} catch (Exception e) {
			doc = null;
			throw new InvalidInputFileException(e.getMessage());
		}
	}
	
	public ExecutingChain runMCMC() {
		List<String> mcLabels = getMCMCLabels();
		if (mcLabels.size()==0) {
			throw new InvalidInputFileException("Could not find any MCMC objects");
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
			}
			
		}
		return null;
	}
	
	/**
	 * Adds "run=false" as an attribute to all MCMC objects so they won't run right away when we create them
	 */
	private void turnOffMCMC() {
		List<String> mcLabels = getMCMCLabels();
		if (mcLabels.size()==0) {
			throw new InvalidInputFileException("Could not find any MCMC objects");
		}
		for(String label : mcLabels) {
			loader.addAttribute(label, "run", "false");
		}
	}

	public void createAllObjects() {
		loader.instantiateAll();
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
	 */
	public Object getObjectForLabel(String label) throws InstantiationException, IllegalAccessException {
		return loader.getObjectForLabel(label);
	}
	
	/**
	 * Return a list of all of the labels of the objects that are LikelihoodComponents
	 * @return
	 */
	public List<String> getLikelihoodLabels() {
		return loader.getObjLabelsForClass(LikelihoodComponent.class);
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
	
	
	
	public void checkValidity() throws InvalidInputFileException {
		//Hmm... what should we do here?
	}
	
	
	
	public void readAllNodes() {
		readNodes(doc.getDocumentElement());
	}
	
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
	 * Summarizes some information about an object that will be created from the XML input file
	 * @author brendano
	 *
	 */
	class XMLObject {
		String label;
		String className;
		Map<String, String> attrs = new HashMap<String, String>();
		// ???? Refs to other objects??
	}
	
	/**
	 * These get thrown when theres a problem with an input file
	 * @author brendano
	 *
	 */
	class InvalidInputFileException extends RuntimeException {
		
		public InvalidInputFileException(String message) {
			super(message);
		}
	}
	
}
