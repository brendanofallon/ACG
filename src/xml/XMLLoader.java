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


package xml;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


/**
 * An xml loader expands the functionality of the PluginLoader by reading plugin information from an xml file.
 * The file is specified at instantiation time and cannot be changed later. The format of the file is fairly
 * general, nodes reference via an attribute of type className = (canonical class name), such as className="java.util.ArrayList"
 * Node names are used to distinguish different elements of the same class, so
 * <nodeA className="java.util.List"/> refers to a different list than does
 * <nodeB className="java.util.List"/>.
 * Internally, these node names are referred to as 'labels'
 * 
 * If nodes contain other nodes, we assume that the objects the child nodes refer to are arguments to the
 * constructor to the parent node. so
 * <nodeA class="util.ClassA">
 * 		<childA class="util.ClassB"/>
 * </nodeA>
 * 
 * will attempt to find a constructor for nodeA that takes an argument of type classB. 
 * 
 * The situation is actually slightly more complicated since we require a mechanism to pass attributes
 * to the created objects as well. For all nodes a map of the form key=value is created with all attributes given,
 * and these are passed as the first argument to the constructor. 
 * 
 * For nodes with text content, all text is passed into the map with the key "content"
 * In addition, the label of the instantiated object is passed into the may under the key "xml.label"
 * 
 * 
 * Finally, we allow for a special case node type, which is a variable-length list of arguments. These have the
 * form 
 * <list class="list>
 * 	<itemA class="Integer"/>
 *  <itemB class="Integer"/>
 * </list>
 * 
 * This creates an ArrayList<Object> containing items itemA and itemB, and passes that to the constructor. In this
 * manner we can handle passing in different numbers of arguments to the constructors.  
 * @author brendan
 *
 */
public class XMLLoader {
	
	//Static access to the loader. 
	static XMLLoader primaryLoader = null;

	public static final String CLASS_NAME_ATTR = "class";
	public static final String NODE_ID = "element.id";
	public static final String LIST_ATTR = "list";
	public static final String TEXT_CONTENT = "content";

	PluginLoader loader = new PluginLoader();
	Document doc = null;
	
	boolean verbose = false;

	//Maps from object labels to the XML Element that defines the object
	Map<String, Element> elementMap = new HashMap<String, Element>();
	
	//Maps from object labels to classes
	Map<String, Class> classMap = new HashMap<String, Class>();

	//Mapping from object labels to the actual objects created. This is empty until after a call to instantiateAll()
	Map<String, Object> objMap = new HashMap<String, Object>();

	//Maps from element labels to a ConstructorInfo obj that describes how to construct the object
	Map<String, ConstructorInfo> consMap = new HashMap<String, ConstructorInfo>();

	/**
	 * Create a new xml loader associated with the given file. 
	 * 
	 * @param xmlFile
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	public XMLLoader(File xmlFile) throws ParserConfigurationException, SAXException, IOException {
//		if (primaryLoader != null) {
//			System.out.println("Warning: Multiple XML loaders operating concurrently, problems may arise");
//		}
		primaryLoader = this;
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		doc = builder.parse(xmlFile);
	}

	
	public XMLLoader(Document doc) throws ParserConfigurationException, SAXException, IOException {
//		if (primaryLoader != null) {
//			System.out.println("Warning: Multiple XML loaders operating concurrently, problems may arise");
//		}
		primaryLoader = this;
		this.doc = doc;
	}
	
	/**
	 * Turn on/off emitting of things to System.out
	 * @param verb
	 */
	public void setVerbose(boolean verb) {
		this.verbose = verb;
	}
	
	/**
	 * Add a new path to the list of places to look for classes. Can be a jar file, class file, or directory. 
	 * If a directory, we will add all .class files and .jar files in that directory. 
	 * @param path
	 */
	public void addPath(File path) {
		loader.addPluginPath(path);
	}

	/**
	 * Forget all instantiated object references. Future calls to getObjectForLabel(label) will create NEW objects
	 */
	public void clearObjectMap() {
		objMap.clear();
	}

	/**
	 * Add the given item to the object map
	 */
	public void addToObjectMap(String label, Object obj) {
		objMap.put(label, obj);
	}
	
	/**
	 * Returns the Class corresponding to object with the given label 
	 * @param label
	 * @return
	 */
	public Class getClassForLabel(String label) {
		return classMap.get(label);
	}
	
	/**
	 * Returns a list of the labels of all child nodes descending from the node
	 * of the given label
	 * @param label
	 * @return
	 */
	public List<String> getChildLabelsForLabel(String label) {
		List<String> children = new ArrayList<String>();
		
		ConstructorInfo consInfo = consMap.get(label);
		
		if (consInfo == null)
			return null;
		else {
			if (consInfo.refLabels != null) {
				for(int i=0; i<consInfo.refLabels.length; i++) {
					children.add(consInfo.refLabels[i]);
				}
			}
			return children;
		}
	}
	
	/**
	 * Return a collection of all elements loaded so far via calls to loadConstructorInfo
	 * This is available before instantiation, but only after loadAllClasses() has been called 
	 * @return
	 */
	public Collection<Element> getElements() {
		return elementMap.values();
	}
	
	/**
	 * Returns the DOM element associated with the given label. This always returns the Element
	 * that contains the constructor info for the object - not a random element that just references
	 * the first one. 
	 * @param label
	 * @return
	 */
	public Element getElementForLabel(String label) {
		return elementMap.get(label);
	}
	
	/**
	 * Examines the children of a given node and gathers the information required to call a constructor for the 
	 * class associated with this node. Attributes (including class and node label) are put into a map, and lists of the
	 * labels and classes referenced by all children are put into arrays. This information is all encapsulated
	 * into a ConstructorItem, and put into a map that is referenced by this node's label.
	 * 
	 * If there is already an entry in the 'consMap' (the map of ConstructorItems), we do nothing. 
	 * 
	 * This method also builds the 'elementMap', which stores a mapping from label to DOM Element
	 * 
	 * @param node The node whose children will be examined
	 */
	private void loadConstructorInfo(Node node) {
		Element nodeEl = (Element)node;
		String nodeLabel = node.getNodeName();
		
		if (consMap.get(nodeLabel)!=null) {
			return;
		}
		
		elementMap.put(nodeLabel, nodeEl);

		//Find all attributes and put them into a Map which will be added to the constructorItems object
		Map<String, String> attrMap = new HashMap<String, String>();
		NamedNodeMap namedAttrMap = nodeEl.getAttributes();
		for(int i=0; i<namedAttrMap.getLength(); i++) {
			Node attrNode = namedAttrMap.item(i);
			attrMap.put(attrNode.getNodeName(), attrNode.getNodeValue());
		}

		//Also put the label of the node into the attr map, so objects can know what their label is
		attrMap.put(NODE_ID, nodeLabel);
		
		List<String> refList  = new ArrayList<String>();
		List<Class<?>> refClasses  = new ArrayList<Class<?>>();

		NodeList kids = node.getChildNodes();
		for(int i=0; i<kids.getLength(); i++) {
			try {
				Element kidEl = (Element)kids.item(i);
				String className = kidEl.getAttribute(CLASS_NAME_ATTR);
				String label = kidEl.getNodeName();
				Class<?> refClass = findClass(label, className); 
				refList.add(label);
				refClasses.add(refClass);
			}
			catch (ClassCastException ccex) {
				//	System.out.println("Could not cast node : " + kids.item(i) + " to an element");
			}

		}

		
		if (nodeEl.getAttribute(CLASS_NAME_ATTR).equals(LIST_ATTR)) {
			refClasses.clear();
			refClasses.add( Object[].class);
		}


		String[] labelList = new String[refList.size()];
		for(int i=0; i<refList.size(); i++)
			labelList[i] = refList.get(i);

		Class<?>[] classes = new Class<?>[refClasses.size()];
		for(int i=0; i<refClasses.size(); i++) {
			classes[i] = refClasses.get(i);
		}
		
		ConstructorInfo consItems = new ConstructorInfo(attrMap, labelList, classes);
		consMap.put(nodeLabel, consItems);
		
		if (nodeEl.getAttribute(CLASS_NAME_ATTR).equals(LIST_ATTR)) {
			consItems.isList = true;
		}
	}

	/**
	 * Attempt to find a Class with the given label in the map field classMap. If an entry for the given label
	 * exists in the classMap, we simply return the Class (after doing a bit of error checking).
	 * If no such class already exists, we attempt to load the class using the pluginLoader and the given className.
	 * If successful, we add a new entry to the map with key=label and value=Class. 
	 * 
	 * @param label A unique identifier for the object to be found / created
	 * @param className The fully qualified name of the class (i.e. package.Class)
	 * @return A class object 
	 */
	private Class<?> findClass(String label, String className) {		
		Class<?> clz = classMap.get(label);
		
		if (className.equals(LIST_ATTR)) {
			className = "java.util.List";
		}
		
		if (clz == null) {
			try {
				clz = loader.getClassForName(className);
			} catch (ClassNotFoundException e) {
				throw new InvalidInputFileException("Could not find class '" + className + "' for object with label: " + label);
			}

			if (clz == null) {
					throw new InvalidInputFileException("Error loading class: " + className + " for object: " + label);
				
			}
			classMap.put(label, clz);
		}
		else { //If the label was already in the map, check to make sure the associated class is the same as the one we specified in the argument
			String loadedClassName = clz.getCanonicalName();
			if (className != null && (!className.equals("")) && (!loadedClassName.equals(className))) {
				throw new IllegalArgumentException("An item with label " + label + " has already been loaded, but it is of type: " + loadedClassName + ", not |" + className +"|");
			}
		}
		return clz;
	}

	/**
	 * Returns a list of all object labels with a class that "isAssignableFrom" the given class
	 * @param clz
	 * @return
	 */
	public List<String> getObjLabelsForClass(Class<?> clz) {
		List<String> labels = new ArrayList<String>();
		for(String label : classMap.keySet()) {
			Class<?> c = classMap.get(label);
			if (clz.isAssignableFrom(c)) {
				labels.add(label);
			}
		}
		return labels;
	}
	
	/**
	 * Recursively examine all nodes starting from the document root, and load the associated classes using the pluginLoader
	 */
	public void loadAllClasses() {
		loadClasses(doc.getDocumentElement());
	}
	
	/**
	 * Recursive helper function for node examination and class loading. Loads all classes below the given XML node
	 */
	private void loadClasses(Node root) {
		NodeList nodeList = root.getChildNodes();

		for (int i = 0; i < nodeList.getLength(); i++) {

			Node node = nodeList.item(i);

			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element el = (Element) node;
				String className = el.getAttribute(CLASS_NAME_ATTR);
				String label = el.getNodeName();
				
				//System.out.println("Node name: " + el.getNodeName() + " className: " + className + " label: " + label);
				loadConstructorInfo(node);
				loadClasses(el);
				Class<?> loadedClass = findClass(label, className); //Puts label=className mapping into classMap
			}
			
		}	
		
		//Search for additional text elements to add in postorder
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			if (node.getNodeType() == Node.TEXT_NODE) {
				String label = root.getNodeName();
				String text = node.getNodeValue();
				if (text.trim().length()>0) {
					ConstructorInfo consInfo = consMap.get(label);
					if (consInfo == null) {
						System.err.println("Found a text node with text: " + node.getNodeValue() + " but there's no constructor info for label: " + label);
					}
					else {

						//Add text content to the attribute map
						if (text.trim().length()>0) {
							consInfo.attrMap.put(TEXT_CONTENT, text);
							
						}
					}
				}
			}
		}
	}

	/**
	 * Traverse all elements in the xml file and create all objects 
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	public void instantiateAll() throws InstantiationException, IllegalAccessException, InvocationTargetException {
		NodeList nodeLst = doc.getDocumentElement().getChildNodes();

		for (int s = 0; s < nodeLst.getLength(); s++) {

			Node node = nodeLst.item(s);

			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element el = (Element) node;
				String label = el.getNodeName();

				Object obj = getObjectForLabel(label);
			}

		}	

	}

	/**
	 * Attempts to locate a constructor that can accept an argument list of the classes given in the refClasses argument
	 * @param clazz Class to search for appropriate constructors
	 * @param refClasses Classes specifying the constructor arg list
	 * @return A constructor matching the argument list
	 */
	private Constructor findConstructor(Class clazz, Class[] refClasses) {
		Constructor cons;
		try {
			cons = clazz.getConstructor(refClasses);
			return cons;
		} catch (Exception ex) {
			//Expected the some exceptions will be thrown here, we don't care
		}
		
		
		Constructor[] consList = clazz.getConstructors();
		for(int i=0; i<consList.length; i++) {
			if (consList[i].getParameterTypes().length == refClasses.length) {
				Class[] conPars = consList[i].getParameterTypes();
				boolean works = true;
				for(int j=0; j<conPars.length; j++) {
					if (! conPars[j].isAssignableFrom(refClasses[j])) 
						works = false;
					
				}
				if (works)
					return consList[i];
			}
		}
		System.err.println("For class " + clazz + " Could not find constructor with parameter list : ");
		for(int i=0; i<refClasses.length; i++)
			System.out.println(refClasses[i]);
		System.exit(0);
		return null;
	}

	/**
	 * Get the primary XMLLoader. Currently this means whatever XMLLoader was most recently instantiated
	 * @return
	 */
	public static XMLLoader getPrimaryLoader() {
		return primaryLoader;
	}
	
	/**
	 * Add an entry to the attribute map that will be used to create the object with the given label. 
	 * This will result in a nullpointerexception if there is no object with the given objLabel. 
	 * @param objLabel
	 * @param key
	 * @param value
	 * @return The previous value associated with the key or null if no entry existed
	 */
	public String addAttribute(String objLabel, String key, String value) {
		ConstructorInfo consItems = consMap.get(objLabel);
		return consItems.attrMap.put(key, value);
	}
	
	/**
	 * Perform a reverse-lookup of an object label for a given object. This is only valid after
	 * a call to instantiateAll()
	 * @param obj
	 * @return
	 */
	public String getLabelForObject(Object obj) {
		for(String key : objMap.keySet()) {
			Object valueObj = objMap.get(key);
			if (valueObj == obj)
				return key;
		}
		return null;
	}
	
	/**
	 * Given a list of objects, look up their IDs in the given XMLLoader 
	 * and return them all in a map from id to object
	 * @param objs List of objects whose IDs will be looked up
	 * @return Map from node_id to object for all given objects
	 */
	public Map<String, Object> findObjectLabelMapping(List<Object> objs) {
		Map<String, Object> map = new HashMap<String, Object>();
		for(Object obj : objs) {
			String label = getLabelForObject(obj);
			if (label == null)
				throw new IllegalArgumentException("Object " + obj + " is not in the object map, cannot lookup id");
			map.put(label, obj);
		}
		return map;
	}
	
	/**
	 * Recursive function that attempts to instantiate the class with the given label. If an object with
	 * the given label has already been constructed, we simply return that (it should be in the objMap map).
	 * If not, we look up the class associated with the given label along with the labels of any sub-
	 * objects provided in the xml file. We then look for a constructor in the class referenced 
	 * whose argument list matches the classes of the provided references. If found, we use the given 
	 * constructor to instantiate the object and (after adding the object to objMap) return it. 
	 *   
	 * @param label The label of the object to be returned
	 * @return The object found / created
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException 
	 */
	public Object getObjectForLabel(String label) throws InstantiationException, IllegalAccessException, InvocationTargetException {
		Object obj = objMap.get(label);

		if (obj == null) {
			Class<?> clazz = classMap.get(label);
			if (clazz == null) {
				throw new IllegalArgumentException("No class or object exists for label : " + label);
			}

			ConstructorInfo consItems = consMap.get(label);
			
			if (consItems == null) {
				throw new IllegalArgumentException("No constructor info found for object with label: " + label);
			}
			consItems.attrMap.put(NODE_ID, label);


			try {
				//A bit of special case code : if we're dealing with an XMLList element, the argument list
				//is always {attributes, Object[]} with the elements in the Object array being the children 
				if (consItems.isList) {
					Object[] list = new Object[consItems.refLabels.length];

					//Potential for confusion : The first item is always the attribute map, we then add
					//all of the subsequent classes after that, for a total of refClasses.length+1 total items
					for(int j=0; j<consItems.refLabels.length; j++) {
						list[j] = getObjectForLabel( consItems.refLabels[j]);
					}

					obj = new ArrayList<Object>();
					for(int j=0; j<list.length; j++)
						((ArrayList<Object>)obj).add(list[j]);

				}
				else {
					//Potential for confusion : The first item is always the attribute map, we then add
					//all of the subsequent classes after that, for a total of refClasses.length+1 total items
					Constructor<?> cons = findConstructor(clazz, consItems.refClasses); 

					Object[] args = new Object[consItems.refClasses.length];
					args[0] = consItems.attrMap;
					for(int j=1; j<consItems.refClasses.length; j++) {
						args[j] = getObjectForLabel( consItems.refLabels[j-1]);
					}

					if (verbose)
						System.out.println("Creating object of class " + clazz + " with label " + label);
					obj = cons.newInstance(args);

				}


			} catch (SecurityException e) {
				System.out.println("\n Security exception when trying to get constructor for node with label " + label);
				System.exit(0);
			} catch (IllegalArgumentException e) {
				System.out.println("\n Illegal arg exception when trying to instantiate : " + label + "\n" + e);
				//System.exit(0);
			} 


			objMap.put(label, obj);
		}

		return obj;
	}

	/**
	 * A small class that contains information about a particular constructor, including the attributes that should
	 * be passed to it.
	 * @author brendan
	 *
	 */
	class ConstructorInfo {

		Map<String, String> attrMap;
		String[] refLabels;
		Class[] refClasses;

		boolean isList = false;
		
		public ConstructorInfo(Map<String, String> attrMap, String[] refLabels, Class[] classList) {
			this.attrMap = attrMap;
			this.refLabels = refLabels;
			this.refClasses = new Class[classList.length+1];
			this.refClasses[0] = Map.class;
			for(int i=0; i<classList.length; i++) {
				this.refClasses[i+1] = classList[i];
			}
		}
	}
	
	


	public static void main(String[] args) {
		String filename = "plugins.xml";
		if (args.length>0) {
			filename = args[0];
		}
		System.out.println("Loading elements from " + filename);
		File xmlFile = new File(filename);
		try {
			XMLLoader loader = new XMLLoader(xmlFile);
			
			//All arguments except the first are assumed to be additional jar or class files, or directories containing them
			for(int i=1; i<args.length; i++)
				loader.addPath(new File(args[i]));
			
			loader.loadAllClasses();
			loader.instantiateAll();
		} catch (Exception ex) {
			System.out.println("Error encoutered during class loading : \n" + ex + "\n");
			//ex.printStackTrace();
		}		


	}

}

