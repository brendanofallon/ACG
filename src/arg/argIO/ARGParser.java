package arg.argIO;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

import math.RandomSource;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import parameter.InvalidParameterValueException;

import arg.ARG;
import arg.ARGNode;
import arg.BiRange;
import arg.CoalNode;
import arg.CoalRangeList;
import arg.RecombNode;
import arg.SiteRange;
import arg.SiteRangeList;
import arg.TipNode;
import arg.TreeUtils;



/**
 * Reads an ARG in the pseudo-graphml like format used by TreesimJ and SunFish. This
 * class is a general purpose reader and writer, and is used for most input and output
 * of full ARGs. 
 * @author brendan
 *
 */
public class ARGParser implements ARGReader, ARGWriter {

	public static final String XML_DOCUMENT_ROOT_NAME = "graphml";
	public static final String XML_GRAPH = "graph";
	public static final String XML_NODE = "node";
	public static final String XML_EDGE = "edge";
	public static final String XML_ID = "id";
	public static final String XML_HEIGHT = "height";
	public static final String XML_SOURCE = "source";
	public static final String XML_TARGET = "target";
	public static final String XML_RANGE = "range";
	public static final String XML_START = "start";
	public static final String XML_END = "end";
	public static final String XML_RANGEMIN = "rangemin";
	public static final String XML_RANGEMAX = "rangemax";
	public static final String XML_NODEANNOTATION = "annotation";
	public static final String XML_ANNOTATION_KEY = "key";

	private int edgeCount = 0; //Counter for number of edges while writing ARGs
	
	public void emitMarginalTrees(ARG arg, File file) throws IOException {
		Integer[] breakpoints = arg.collectBreakPoints();
		Arrays.sort(breakpoints);
		BufferedWriter writer = new BufferedWriter(new FileWriter(file));
		int beginBP = 0;
		for(Integer bp : breakpoints) {
			CoalNode treeRoot = TreeUtils.createMarginalTree(arg, bp);
			String newick = TreeUtils.getNewick(treeRoot);
			writer.write("[start=" +beginBP + " end=" + bp + "] \t" + newick + "\n");
			//System.out.println("[start=" +beginBP + " end=" + bp + "]  " + newick + "\n");
			beginBP = bp;
		}
		
		//Don't forget the last one
		CoalNode treeRoot = TreeUtils.createMarginalTree(arg, beginBP+1);
		String newick = TreeUtils.getNewick(treeRoot);
		writer.write("[start=" +beginBP + " end=" + arg.getSiteCount() + "] \t " + newick + "\n");
		//System.out.println("[start=" +beginBP + " end=" + arg.getSiteCount() + "]  " + newick + "\n");
	}
	
	/**
	 * Generate a unique integer from the two given nodes. Not trivial since
	 * node numbers may be very high, especially after a long mcmc run. One could
	 * also concatenate the the five or six least-significant digits  
	 * @param a
	 * @param b
	 * @return
	 */
	private int makeKey(ARGNode a, ARGNode b) {
		String str;
		if (a.getNumber() < b.getNumber()) {
			str = "" + a.getNumber() + b.getNumber();
		}
		else {
			str = "" + b.getNumber() + a.getNumber();
		}
		return str.hashCode();
	}
	
	
	/**
	 * Create a DOM Element that represents the edge from source to target. If source is recombination
	 * then range info is added. When forceHighRange is false we assume that if source.getParent(0)==target
	 * then the lower half of the range is used to write range info. If forceHighRange is true then we 
	 * always simply assume that the upper part of the range leads from source to target
	 * 
	 * @param doc
	 * @param source
	 * @param target
	 * @return
	 */
	private Element createEdgeElement(Document doc, ARGNode source, ARGNode target, boolean forceHighRange) {
		Element edgeElement = doc.createElement(XML_EDGE);
		edgeElement.setAttribute(XML_SOURCE, source.getLabel());
		edgeElement.setAttribute(XML_TARGET, target.getLabel());
		edgeElement.setAttribute(XML_ID, "edge" + edgeCount);
		//If the source node was a recombination there's an additional child
		//describing range info
		if (source instanceof RecombNode) {
			RecombNode rNode = (RecombNode)source;
			Element rangeElement = doc.createElement(XML_RANGE);
			Element startElement = doc.createElement(XML_START);
			Element endElement = doc.createElement(XML_END);
			BiRange range = rNode.getRecombRange();

			if (rNode.getParent(0)==target && (! forceHighRange)) {
				startElement.setTextContent("" + range.getMin());
				endElement.setTextContent("" +range.getBreakpoint());				
			}
			else {
				startElement.setTextContent("" + range.getBreakpoint());
				endElement.setTextContent("" + range.getMax());
			}

		
			rangeElement.appendChild(startElement);
			rangeElement.appendChild(endElement);
			edgeElement.appendChild(rangeElement);
		}
		
		edgeCount++;
		return edgeElement;
	}
	
	/**
	 * Emit the given arg in pseudo-graphml form to the file provided. We first create a DOM object
	 * that mimics the arg structure, then write the DOM to a string and then write the string to the file.  
	 * @param arg
	 * @param file
	 * @throws IOException
	 */
	public void writeARG(ARG arg, File file) throws IOException {
		String xmlString = argToXml(arg);
		
		FileWriter writer = new FileWriter(file);
		writer.write(xmlString);
		writer.close();
	}
	
	public String argToXml(ARG arg) {
		int siteMin = 0;
		int siteMax = arg.getSiteCount();
		edgeCount = 0;
		//Easy to miss an edge or write it twice, so we maintain a set of all edges that
		//have been written. Its indexed by a key generated from node numbers, using the
		// makeKey function
		Set<Integer> edgesVisited = new HashSet<Integer>();
		
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.newDocument();
			
			Element root = doc.createElement(XML_DOCUMENT_ROOT_NAME);
			
			doc.appendChild(root);
			
			Element graphElement = doc.createElement(XML_GRAPH);
			root.appendChild(graphElement);
			graphElement.setAttribute(XML_RANGEMIN, String.valueOf(siteMin));
			graphElement.setAttribute(XML_RANGEMAX, String.valueOf(siteMax));
			
			for(ARGNode argNode : arg.getAllNodes()) {
				Element argNodeElement = doc.createElement(XML_NODE);
				argNodeElement.setAttribute(XML_ID, argNode.getLabel());
				argNodeElement.setAttribute(XML_HEIGHT, String.valueOf( argNode.getHeight()));
				
				//Add coalescing site info as a node-annotation
				if (argNode instanceof CoalNode) {
					Element annotationElement = doc.createElement(XML_NODEANNOTATION);
					annotationElement.setAttribute(XML_ANNOTATION_KEY, "c_nodes");
					CoalRangeList ranges = ((CoalNode)argNode).getCoalescingSites();
					if (ranges != null) {
						StringBuilder strBuilder = new StringBuilder();
						for(int i=0; i<ranges.size(); i++) {
							strBuilder.append(ranges.getRefID(i) + ": " + ranges.getRangeBegin(i) + "-" + ranges.getRangeEnd(i) + "[" + ranges.getLChild(i) + "][" + ranges.getRChild(i) + "]\n");
						}
						if (ranges.size()==0)
							strBuilder.append("X");
						annotationElement.setTextContent(strBuilder.toString());
						argNodeElement.appendChild(annotationElement);
					}
				}
				
				//Following block adds all annotations as XML blocks
				Set<String> annotations = argNode.getAnnotationKeys();
				for(String key : annotations) {
					Element annotationElement = doc.createElement(XML_NODEANNOTATION);
					annotationElement.setAttribute(XML_ANNOTATION_KEY, key);
					annotationElement.setTextContent(argNode.getAnnotation(key));
					argNodeElement.appendChild(annotationElement);
				}
				
				graphElement.appendChild(argNodeElement);
			}
			
			
			for(ARGNode argNode : arg.getInternalNodes()) {
				//Traverse over all children and parents of the node in question, for each
				//we see if we've already created an edge for that node pair, and if not 
				//we add one
				for(int i=0; i<argNode.getNumOffspring(); i++) {
					ARGNode sourceNode = argNode.getOffspring(i);
					int key = makeKey(sourceNode, argNode);
					if (! edgesVisited.contains(key)) {
						edgesVisited.add(key);

						Element edgeElement = createEdgeElement(doc, sourceNode, argNode, false); 
						graphElement.appendChild(edgeElement);
						
						if (sourceNode instanceof RecombNode) {
							RecombNode rNode = (RecombNode)sourceNode;
							if (rNode.getParent(0)==argNode && rNode.getParent(1)==argNode) {
								Element edge2 = createEdgeElement(doc, sourceNode, argNode, true);
								graphElement.appendChild(edge2);
							}
						}
					}
				}
				
				for(int i=0; i<argNode.getNumParents(); i++) {
					ARGNode targetNode = argNode.getParent(i);
					if (targetNode==null) //We may be at root
						break;
					int key = makeKey(argNode, targetNode);
					if (! edgesVisited.contains(key)) {
						edgesVisited.add(key);

						Element edgeElement = createEdgeElement(doc, argNode, targetNode, false); 
						graphElement.appendChild(edgeElement);
						
						if (argNode instanceof RecombNode) {
							RecombNode rNode = (RecombNode)argNode;
							if (rNode.getParent(0)==argNode && rNode.getParent(1)==argNode) {
								Element edge2 = createEdgeElement(doc, argNode, targetNode, true);
								graphElement.appendChild(edge2);
							}
						}
					}					
				}
			}
	
			
			TransformerFactory transfac = TransformerFactory.newInstance();
			Transformer trans = transfac.newTransformer();
			trans.setOutputProperty(OutputKeys.INDENT, "yes");
			//create string from xml tree
			StringWriter sw = new StringWriter();
			StreamResult result = new StreamResult(sw);
			DOMSource source = new DOMSource(doc);
			trans.transform(source, result);
			String xmlString = sw.toString();
			return xmlString;
			//System.out.println(xmlString);
			
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		}
		return null; //Error must have occurred
	}
	
	public int getSiteCountFromXML(File file) throws IOException {
		org.w3c.dom.Document data = null;
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			data = builder.parse(file);
		} 
		catch (SAXException se) {

		}
		catch (ParserConfigurationException pe) {

		}
		
		Node documentRoot = data.getDocumentElement();
		Node topLevelChild = documentRoot.getFirstChild();
				
		//Search for (the first) phylogeny element
		while(topLevelChild != null) {
			if (topLevelChild.getNodeType() == Node.ELEMENT_NODE && topLevelChild.getNodeName().equalsIgnoreCase(XML_GRAPH))
				break;
			
			topLevelChild = topLevelChild.getNextSibling();
		}
		
		String siteMaxStr = getAttributeForNode(topLevelChild, XML_RANGEMAX);
		String siteMinStr = getAttributeForNode(topLevelChild, XML_RANGEMIN);
		int siteMax = Integer.parseInt(siteMaxStr);
		int siteMin = Integer.parseInt(siteMinStr);
		
		return siteMax - siteMin;
	}
	
	

	public List<ARGNode> readARGNodes(File file) throws IOException, ARGParseException {
		return readARGNodes(file, new ARG());
	}
	
	
	/**
	 * Attempt to read an ARG from the file provided. This does not jiggle node heights
	 * and so may return an arg with multiple nodes at the same height 
	 */
	public ARG readARG(File file) throws IOException, ARGParseException {
		ARG arg = new ARG();
		try {
			List<ARGNode> nodes = readARGNodes(file, arg);
			int siteMin = findSiteMin(file);
			int siteMax = findSiteMax(file);
			arg.initializeFromNodeList(nodes, siteMax-siteMin, false);
		}
		catch (ARGParseException ape) {
			System.err.println("Error reading arg: " + ape);
			ape.printStackTrace();
			System.exit(0);
			return null;
		}
		
		
		try {
			arg.verifyHeights();
			arg.verifyReferences();
		} catch (InvalidParameterValueException e) {
			System.err.println("ARG did not pass reference checks after assembly from file : " + e);
			e.printStackTrace();
			return null;
		}
		return arg;
	}
	
	/**
	 * Returns the attribute corresponding to the first site (aka rangeMin) from a graphML-formatted 
	 * file. 
	 * @param file
	 * @return
	 * @throws ARGParseException 
	 */
	private Integer findSiteMax(File file) throws ARGParseException {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			org.w3c.dom.Document data = builder.parse(file);
			
			Node documentRoot = data.getDocumentElement();
			Node topLevelChild = documentRoot.getFirstChild();
					
			//Search for (the first) phylogeny element
			while(topLevelChild != null) {
				if (topLevelChild.getNodeType() == Node.ELEMENT_NODE && topLevelChild.getNodeName().equalsIgnoreCase(XML_GRAPH))
					break;
				
				topLevelChild = topLevelChild.getNextSibling();
			}
			 
		
			//Child should now be 'pointing' at 'graph' element, if we're at the end it's an error
			if (topLevelChild == null) {
				throw new ARGParseException("No GRAPH tag found in XML, cannot find arg");
			}
			
			String rangeMaxStr = getAttributeForNode(topLevelChild, XML_RANGEMAX);
			Integer rangeEnd;
			try {
				rangeEnd = Integer.parseInt(rangeMaxStr);
				return rangeEnd;
			}
			catch (NumberFormatException nfe) {
				throw new ARGParseException("Graph XML element must specify rangeMin and rangeMax attributes.");
			}
			catch (NullPointerException npe) {
				throw new ARGParseException("Graph XML element must specify rangeMin and rangeMax attributes.");
			}
		} 
		catch (SAXException se) {

		}
		catch (ParserConfigurationException pe) {

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return -1;
	}
	
	/**
	 * Returns the attribute corresponding to the first site (aka rangeMin) from a graphML-formatted 
	 * file. 
	 * @param file
	 * @return
	 * @throws ARGParseException 
	 */
	private Integer findSiteMin(File file) throws ARGParseException {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			org.w3c.dom.Document data = builder.parse(file);
			
			Node documentRoot = data.getDocumentElement();
			Node topLevelChild = documentRoot.getFirstChild();
					
			//Search for (the first) phylogeny element
			while(topLevelChild != null) {
				if (topLevelChild.getNodeType() == Node.ELEMENT_NODE && topLevelChild.getNodeName().equalsIgnoreCase(XML_GRAPH))
					break;
				
				topLevelChild = topLevelChild.getNextSibling();
			}
			 
		
			//Child should now be 'pointing' at 'graph' element, if we're at the end it's an error
			if (topLevelChild == null) {
				throw new ARGParseException("No GRAPH tag found in XML, cannot find arg");
			}
			
			String rangeMinStr = getAttributeForNode(topLevelChild, XML_RANGEMIN);
			String rangeMaxStr = getAttributeForNode(topLevelChild, XML_RANGEMAX);
			Integer rangeStart;
			Integer rangeEnd;
			try {
				rangeStart = Integer.parseInt(rangeMinStr);
				return rangeStart;
			}
			catch (NumberFormatException nfe) {
				throw new ARGParseException("Graph XML element must specify rangeMin and rangeMax attributes.");
			}
			catch (NullPointerException npe) {
				throw new ARGParseException("Graph XML element must specify rangeMin and rangeMax attributes.");
			}
		} 
		catch (SAXException se) {

		}
		catch (ParserConfigurationException pe) {

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return -1;
	}
	
	/**
	 * Attempt to read an ARG from the file provided, assuming the file is in GraphML-like format. Returns a list of all
	 * of the nodes in the ARG, suitable for handing to the ARG constructor. 
	 */
	public List<ARGNode> readARGNodes(File file, ARG arg) throws IOException, ARGParseException {
		org.w3c.dom.Document data = null;
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			data = builder.parse(file);
		} 
		catch (SAXException se) {

		}
		catch (ParserConfigurationException pe) {

		}

		if (data == null) 
			return null;
		
		List<ARGNode> nodes = null;
		nodes = xmlToARG(data, arg);
 		
		//checkARGValidity(nodes); necessary?
		return nodes;
	}
	
	
	/**
	 * Primary class for converting the xml into a tree. We recurse through the xml nodes looking for <clade> elements, creating new
	 * kids whenever we see them. Nearly all other information is added as attributes to the tree nodes.
	 * 
	 * @param doc
	 * @return
	 * @throws ARGParseException 
	 * @throws FileParseException
	 */
	private List<ARGNode> xmlToARG(org.w3c.dom.Document doc, ARG arg) throws ARGParseException {
		Node documentRoot = doc.getDocumentElement();
		Node topLevelChild = documentRoot.getFirstChild();
				
		//Search for (the first) phylogeny element
		while(topLevelChild != null) {
			if (topLevelChild.getNodeType() == Node.ELEMENT_NODE && topLevelChild.getNodeName().equalsIgnoreCase(XML_GRAPH))
				break;
			
			topLevelChild = topLevelChild.getNextSibling();
		}
		 
	
		//Child should now be 'pointing' at 'graph' element, if we're at the end it's an error
		if (topLevelChild == null) {
			throw new ARGParseException("No GRAPH tag found in XML, cannot find arg");
		}
		
		String rangeMinStr = getAttributeForNode(topLevelChild, XML_RANGEMIN);
		String rangeMaxStr = getAttributeForNode(topLevelChild, XML_RANGEMAX);
		Integer rangeStart;
		Integer rangeEnd;
		try {
			rangeStart = Integer.parseInt(rangeMinStr);
			rangeEnd = Integer.parseInt(rangeMaxStr);
		}
		catch (NumberFormatException nfe) {
			throw new ARGParseException("Graph XML element must specify rangeMin and rangeMax attributes.");
		}
		catch (NullPointerException npe) {
			throw new ARGParseException("Graph XML element must specify rangeMin and rangeMax attributes.");
		}
		
		
		Node graphChild = topLevelChild.getFirstChild(); 
		
		List<ProtoNode> protoNodes = new ArrayList<ProtoNode>();
		
		//First we read in all nodes, and then all the edges
		while(graphChild != null) {
			addNode(graphChild, protoNodes);
			graphChild = graphChild.getNextSibling();
		}
		
		graphChild = topLevelChild.getFirstChild(); 
		while(graphChild != null) {
			connectNodes(graphChild, protoNodes, rangeStart, rangeEnd);
			graphChild = graphChild.getNextSibling();
		}
		
		removeSingleLinks(protoNodes);
		
		boolean found = true;
		int count = 0;
		while (found) {
			found = splitMultifurcations(protoNodes);
			count++;
		}
		count--;
//		if (count>0)
//			System.out.println("Resolved " + count + " multifurcations in tree");
		
		removeSingleLinks(protoNodes);
		
		List<ARGNode> argNodes = convertToARGNodes(protoNodes, arg);
		
		return argNodes;
	}

	/**
	 * Removed protonodes that have one parent and one offspring (and reassigns parents/offspring accordingly)
	 * @param protoNodes
	 */
	private void removeSingleLinks(List<ProtoNode> protoNodes) {
		List<ProtoNode> nodesToRemove = new ArrayList<ProtoNode>();
		
		for(ProtoNode node : protoNodes) {
			if (node.getNumParents()==1 && node.getNumOffspring()==1) {
				ProtoNode parent = getNodeForID(protoNodes, node.parentIDs.get(0));
				parent.addOffspringID(node.offspringIDs.get(0));
				parent.offspringIDs.remove(eqIndex(parent.offspringIDs, node.id));

				ProtoNode child = getNodeForID(protoNodes, node.offspringIDs.get(0));
				reassignParent(child, node.id, parent.id);
				nodesToRemove.add(node);
			}
		}
		
		for(ProtoNode node : nodesToRemove) {
			protoNodes.remove(node);
		}
		
		//System.out.println("Removing " + nodesToRemove.size() + " single links");
	}
	
	
	/**
	 * Examines the given list of proto-nodes to determine if there are any multifurcations. If so, 
	 * we attempt to resolve them by creating new protonodes and adjusting references as necessary
	 * @param protoNodes
	 * @throws ARGParseException If we encounter a case where there are too many offspring to parse
	 */
	private boolean splitMultifurcations(List<ProtoNode> protoNodes) throws ARGParseException {
		List<ProtoNode> oldNodes = new ArrayList<ProtoNode>();
		oldNodes.addAll(protoNodes);
		protoNodes.clear();
		
		boolean foundMultifurcation = false;
		
		for(ProtoNode node : oldNodes) {

			//Make a new node just below this node that is child of node, move two offspring to it
			if ((node.getNumParents()==0 || node.getNumParents()==1) && node.getNumOffspring()==3) {
				foundMultifurcation = true;
				if (node.getNumParents()==0)
					System.out.println("**Resolving 0/3 multifurcation at root : " + node.id);
				else
					System.out.println("Resolving 1/3 multifurcation at node : " + node.id);

				ProtoNode newNode = shearOffspring(node, oldNodes);
							
				protoNodes.add(newNode);
				break;
			}
			
			//You guessed it, two new nodes that contain all descendants, old node is parent of two new nodes
			if (node.getNumOffspring()>3) {
				foundMultifurcation = true;
				//System.out.println("Resolving 1/4 multifurcation at node : " + node.id);
				ProtoNode[] newNodes = splitOffspring(node, oldNodes);
				
				for(String id : newNodes[0].offspringIDs) {
					ProtoNode kid = getNodeForID(oldNodes, id);
					if (! kid.hasParent(newNodes[0].id)) {
						System.err.println("Grandchild does not have the right parent!");
					}
				}
				
				for(String id : newNodes[1].offspringIDs) {
					ProtoNode kid = getNodeForID(oldNodes, id);
					if (! kid.hasParent(newNodes[1].id)) {
						System.err.println("Grandchild does not have the right parent!");
					}
				}
								
				protoNodes.add(newNodes[0]);
				protoNodes.add(newNodes[1]);
				break;
			}
			
			//To resolve these we make a new coal node just below the existing node, which has
			//both a coalescence and a recombination. 
			if (node.getNumOffspring()==2 && node.getNumParents()==2) {
				foundMultifurcation = true;
				//System.out.println("Resolving 2/2 multifurcation at node with id : " + node.id);
				ProtoNode newNode = new ProtoNode(node.id+"recSplit", node.getHeight()*0.99999);
				newNode.addOffspringID(node.offspringIDs.get(0));
				newNode.addOffspringID(node.offspringIDs.get(1));
				newNode.addParentID(node.id);
				node.offspringIDs.clear();
				node.offspringIDs.add(newNode.id);
				
				//Change parent ids for both nodes we just altered
				reassignParent(newNode.offspringIDs.get(0), node.id, newNode.id, oldNodes);
				reassignParent(newNode.offspringIDs.get(1), node.id, newNode.id, oldNodes);
				
				protoNodes.add(newNode);
				break;
			}
			
			//Make a new node below this node and move two offspring to it. This leaves a 2/2 multifurcation
			if (node.getNumOffspring()==3 && node.getNumParents()==2) {
				foundMultifurcation = true;
				//System.out.println("Resolving 3/2 multifurcation at node with id : " + node.id);
				ProtoNode newNode = new ProtoNode(node.id+"_32Split", node.getHeight()*0.9998);
				newNode.addOffspringID(node.offspringIDs.get(0));
				newNode.addOffspringID(node.offspringIDs.get(1));
				node.offspringIDs.remove(0);
				node.offspringIDs.remove(0);
				newNode.addParentID(node.id);
				node.offspringIDs.add(newNode.id);
				
				//Change parent ids for both nodes we just altered
				reassignParent(newNode.offspringIDs.get(0), node.id, newNode.id, oldNodes);
				reassignParent(newNode.offspringIDs.get(1), node.id, newNode.id, oldNodes);
				
				
				//Now split node again to make another coal just below recomb..
				ProtoNode newNode2 = new ProtoNode(node.id+"_22Split", node.getHeight()*0.9999);
				newNode2.addOffspringID(node.offspringIDs.get(0));
				newNode2.addOffspringID(node.offspringIDs.get(1));
				newNode2.addParentID(node.id);
				node.offspringIDs.clear();
				node.offspringIDs.add(newNode2.id);
				
				//Change parent ids for both nodes we just altered
				reassignParent(newNode2.offspringIDs.get(0), node.id, newNode2.id, oldNodes);
				reassignParent(newNode, node.id, newNode2.id);
				
				protoNodes.add(newNode2);
				protoNodes.add(newNode);
				break;
			}
			
			
		}
		
		for(ProtoNode pNode : protoNodes) {
			if (pNode.getNumParents()==0) {
				throw new IllegalArgumentException("Trying to add a node with no parents!");
			}
		}
		
		protoNodes.addAll(oldNodes);
		return foundMultifurcation;
	}

	/**
	 * Creates a new ProtoNode that is inserted just below the given node, and moves two offspring from given 
	 * node to new node so that both will have exactly two offspring
	 * @param node
	 * @param extantNodes
	 * @return
	 */
	private ProtoNode shearOffspring(ProtoNode node, List<ProtoNode> extantNodes) {
		if (node.getNumOffspring()!=3) {
			throw new IllegalArgumentException("Can't shear offspring for node without exactly 3 offspring (this one has " + node.getNumOffspring() + ")");
		}
		ProtoNode newNode = new ProtoNode(node.id + "13split", node.height*0.9999);
		newNode.addParentID(node.id);
		
		Collections.sort(node.offspringIDs);
		
		//If there's a trivial recombination then make sure if goes to the lower (new) node
		if (node.offspringIDs.get(1)==node.offspringIDs.get(2)) {
			String id = node.offspringIDs.remove(0);
			node.offspringIDs.add(id); //Cycle first to last, now the two that are the same are in the first two spots
		}
		
		newNode.addOffspringID(node.offspringIDs.get(0));
		newNode.addOffspringID(node.offspringIDs.get(1));
		
		
		node.offspringIDs.remove(0);
		node.offspringIDs.remove(0);
		node.addOffspringID(newNode.id);
		
		reassignParent(newNode.offspringIDs.get(0), node.id, newNode.id, extantNodes);
		reassignParent(newNode.offspringIDs.get(1), node.id, newNode.id, extantNodes);
		return newNode;
	}
	
	/**
	 * 
	 * @param node
	 * @param extantNodes
	 * @return
	 * @throws ARGParseException 
	 */
	private ProtoNode[] splitOffspring(ProtoNode node, List<ProtoNode> extantNodes) throws ARGParseException {
		if (node.getNumOffspring()<4) {
			throw new IllegalArgumentException("Can't perform node split on node with less than 4 offspring");
		}
		
		int lab1 = (int)(Math.random()*10000);
		int lab2 = (int)(Math.random()*10000);
		ProtoNode newNode1 = new ProtoNode(node.id + "split1" + lab1, node.getHeight()*0.9999);
		ProtoNode newNode2 = new ProtoNode(node.id + "split2" + lab2, node.getHeight()*0.9999);
		
		newNode1.addParentID(node.id);
		newNode2.addParentID(node.id);
		
		//Keeps trivial recombs together
		Collections.sort(node.offspringIDs);
		
		int div = node.getNumOffspring()/2; //First guess for dividing spot
		while(div < node.getNumOffspring() && node.offspringIDs.get(div-1).equals( node.offspringIDs.get(div))) {
			div++;
		}
		
		if (div == node.getNumOffspring()) {
			throw new ARGParseException("Cannot split offspring for node " + node.id);
		}
		
		
		//System.out.println("Splitting " + node.getNumOffspring() + " offspring from node " + node.id + " " + div + " / " + (node.getNumOffspring()-div));
		
		int i;
		for(i=0; i<div; i++) {
			newNode1.addOffspringID(node.offspringIDs.get(i));
			reassignParent(node.offspringIDs.get(i), node.id, newNode1.id, extantNodes);
		}
		
		for(i=div; i<node.getNumOffspring(); i++) {
			newNode2.addOffspringID(node.offspringIDs.get(i));
			reassignParent(node.offspringIDs.get(i), node.id, newNode2.id, extantNodes);
		}
		
		node.offspringIDs.clear();
		node.addOffspringID(newNode1.id);
		node.addOffspringID(newNode2.id);
		
		return new ProtoNode[]{newNode1, newNode2};
	}
	
	
	/**
	 * Replace one parent ID with another for the given node
	 * @param child1
	 * @param oldParentID
	 * @param newParentID
	 */
	private static void reassignParent(ProtoNode child1, String oldParentID, String newParentID) {
		//Search for and change ALL IDS that match
		int indx = eqIndex(child1.parentIDs, oldParentID);
		while (indx > -1) {
			child1.parentIDs.set(indx, newParentID);
			indx = eqIndex(child1.parentIDs, oldParentID);
		}
		
		if (child1.rangeID0 != null && child1.rangeID0.equals(oldParentID))
			child1.rangeID0 = newParentID;
		if (child1.rangeID1 != null && child1.rangeID1.equals(oldParentID))
			child1.rangeID1 = newParentID;
		
	}
	
	/**
	 * Given an id for a proto-node, change any parent reference matching oldParentID to newParentID. 
	 * @param id ID of node to alter
	 * @param oldParentID Existing parent id, the one to change
	 * @param newParentID New ID that will overwrite old id
	 * @param nodeList List of node in which node to change exists
	 */
	private static void reassignParent(String id, String oldParentID, String newParentID, List<ProtoNode> nodeList) {
		ProtoNode child1 = getNodeForID(nodeList, id);
		if (child1 == null)
			throw new IllegalArgumentException("Can not find node with id: " + id);
		reassignParent(child1, oldParentID, newParentID);
	}
	
	private static int eqIndex(List<String> list, String key) {
		for(int i=0; i<list.size(); i++) {
			if (list.get(i).equals(key))
				return i;
		}
		return -1;
	}
	
	/**
	 * Traverses the list of proto-nodes, converting them into official ARGNodes of the right variety based on 
	 * how many offspring and parents each one has. 
	 * @param protoNodes
	 * @return
	 * @throws ARGParseException 
	 */
	private List<ARGNode> convertToARGNodes(List<ProtoNode> protoNodes, ARG arg) throws ARGParseException {
		List<ARGNode> argNodes = new ArrayList<ARGNode>();
		boolean foundRoot = false;
				
		//We do this in two passes : First, we instantiate all nodes using parent and offspring number to infer the node type
		//And second, we add offspring and parents as necessary
		for(ProtoNode pNode : protoNodes) {
			ARGNode aNode = null;

			if (pNode.getNumParents()==1 && pNode.getNumOffspring()==2) {
				aNode = new CoalNode(arg);
			}
			if (pNode.getNumParents()==1 && pNode.getNumOffspring()==0) {
				aNode = new TipNode(arg);
			}
			if (pNode.getNumParents()==2 && pNode.getNumOffspring()==1) {
				aNode = new RecombNode(arg);
			}
			if (pNode.getNumParents()==0) {
				if (foundRoot) {
					throw new ARGParseException("Found multiple nodes with zero parents (including " + pNode.id + ")");
				}
				
				if (pNode.getNumOffspring()==2) {
					aNode = new CoalNode(arg);
					//System.out.println("Found root node : " + pNode.id);
					foundRoot = true;
				}
				else {
					System.out.println("Found a node with no parent but not exactly two offspring (node id: " + pNode.id + ")");
					throw new ARGParseException("Found a node with no parent but not exactly two offspring (node id: " + pNode.id + ")");
				}
				
			}
			
			if (aNode == null) {
				throw new ARGParseException("Cannot construct ARGNode for node with id: " + pNode.id + " num parents: " + pNode.getNumParents() + " num offspring: " + pNode.getNumOffspring());
			}
			else {
				aNode.setLabel(pNode.id);
				aNode.proposeHeight(pNode.getHeight());
				argNodes.add(aNode);
			}
			
		}
		
		int siteMax = Integer.MAX_VALUE;
		
		//Phase 2: Connect all argNodes with the parent-offspring info in the pNodes...
		for(ProtoNode pNode : protoNodes) {
			ARGNode aNode = getARGNodeForID(argNodes, pNode.id);
			
			if (aNode == null) {
				throw new ARGParseException("Could not find ARG node with id : " + pNode.id);
			}
			//A bit of shenanigans to make sure that the range info is associated with the right parent node
			//HIGH PROBABILITY OF BUGS HERE!
			if (pNode.hasRangeInfo()) {
				if (pNode.getNumParents() != 2) {
					throw new ARGParseException("Nodes with recombination range info must have exactly two parents (node " + pNode.id + " had " + pNode.getNumParents() +")");
				}
				if (! pNode.hasValidRangeInfo()) {
					throw new ARGParseException("Could not accurately parse range info for node: " + pNode.id);
				}
				if (siteMax == Integer.MAX_VALUE) {
					siteMax = pNode.rangeEnd1;
				}
				else {
					if (siteMax != pNode.rangeEnd1)
						throw new ARGParseException("Not all ranges agreed on max site index (found " + siteMax + " and also " + pNode.rangeEnd1);
				}
				ARGNode parentZero = getARGNodeForID(argNodes, pNode.rangeID0);
				ARGNode parentOne = getARGNodeForID(argNodes, pNode.rangeID1);
				
				if (parentZero == null) {
					throw new ARGParseException("Couldn't find proto node for (recomb) id : " + pNode.rangeID0);
				}
				if (parentOne == null) {
					throw new ARGParseException("Couldn't find proto node for (recomb) id : " + pNode.rangeID1);
				}
				if (! pNode.hasParent(pNode.rangeID0)) {
					throw new ARGParseException("Recomb range and parent info are not consistent for node "  + pNode.id);
				}
				if (! pNode.hasParent(pNode.rangeID1)) {
					throw new ARGParseException("Recomb range and parent info are not consistent for node "  + pNode.id);
				}
				
				RecombNode rNode = (RecombNode)aNode;
				rNode.proposeParent(0, parentZero);
				rNode.proposeParent(1, parentOne);
				rNode.proposeRecombRange(new BiRange(0, pNode.rangeEnd0, siteMax));
			}
			else {
				for(int i=0; i<pNode.getNumParents(); i++) {
					ARGNode parent = getARGNodeForID(argNodes, pNode.parentIDs.get(i));
					if (parent == null) {
						throw new ARGParseException("Could not find parent for referenced node " + pNode.parentIDs.get(i));
					}
					aNode.proposeParent(i, parent);
				}
			}
			
			for(int i=0; i<pNode.getNumOffspring(); i++) {
				aNode.proposeOffspring(i, getARGNodeForID(argNodes, pNode.offspringIDs.get(i)));
			}
			
			
		}
		
		return argNodes;
	}

	/**
	 * Creates a 'protoNode' with the same id and height as the given XML node, and adds it to the list of protonodes.
	 * Used by xmlToARG for arg reading. 
	 * @param child
	 * @param protoNodes
	 */
	private void addNode(Node child, List<ProtoNode> protoNodes) {
		if (child.getNodeName().equalsIgnoreCase(XML_NODE)) {
			String id = getAttributeForNode(child, XML_ID);
			String heightStr = getAttributeForNode(child, XML_HEIGHT);
			double height = Double.parseDouble(heightStr);
			ProtoNode pNode = new ProtoNode(id, height);
			//System.out.println("Adding node with id: " + id + " and height: " + height);
			protoNodes.add(pNode);
		}
	}
	

	private void connectNodes(Node child, List<ProtoNode> protoNodes, int rangeMin, int rangeMax) throws ARGParseException {
		if (child.getNodeName().equalsIgnoreCase(XML_EDGE)) {
			String sourceID = getAttributeForNode(child, XML_SOURCE);
			String targetID = getAttributeForNode(child, XML_TARGET);
			String edgeID = getAttributeForNode(child, XML_ID);
			ProtoNode source = getNodeForID(protoNodes, sourceID);
			ProtoNode target = getNodeForID(protoNodes, targetID);

			if (source == null) {
				throw new ARGParseException("Could not find node with id " + sourceID + ", referred to by edge");
			}
			if (target == null) {
				throw new ARGParseException("Could not find node with id " + targetID + ", referred to by edge");
			}

			if (source.getHeight() > target.getHeight()) {
				throw new ARGParseException("For edge " + XML_ID + " source node height cannot be greater than target node height");
			}

			source.addParentID( target.id);
			target.addOffspringID( source.id);
				
			Node rangeNode = getChildForNodeName(child, XML_RANGE);
			if (rangeNode != null) {
				String startStr = getNodeTextContent(rangeNode, XML_START);
				String endStr = getNodeTextContent(rangeNode, XML_END);
				if (startStr==null || endStr==null) {
					throw new ARGParseException("Could not read range start / end information for edge " + XML_ID);
				}
				try {
					Integer start = Integer.parseInt(startStr.trim());
					Integer end = Integer.parseInt(endStr.trim());
					if (start == 0) {
						source.setRangeInfoZero(target.id, start, end);
					}
					else {
						source.setRangeInfoOne(target.id, start, end);
					}
				}
				catch (NumberFormatException nfe) {
					throw new ARGParseException("Could not read integer value for start/end information for edge " + XML_ID);
				}


			}

			//System.out.println("Adding edge with id " + edgeID + " connecting  source " + sourceID + " to " + targetID);
		}
	}
	
	
	private ARGNode getARGNodeForID(List<ARGNode> argNodes, String id) {
		for(ARGNode node : argNodes) {
			if (node.getLabel().equals(id)) 
				return node;
		}
		return null;
	}
	
	/**
	 * Find and return the argNode whose ID matches the given id, or null if no node is found 
	 * @param argNodes
	 * @param sourceID
	 * @return
	 */
	private static ProtoNode getNodeForID(List<ProtoNode> protoNodes, String id) {
		for(ProtoNode node : protoNodes) {
			if (node.id.equals(id)) 
				return node;
		}
		return null;
	}

	/**
	 * Examine the node for an attribute with the given key, and if found return the key's value,
	 * otherwise null.
	 * @param xmlNode
	 * @param key Attribute key to search for
	 * @return The value associated with the given key
	 */
	private String getAttributeForNode(Node xmlNode, String key) {
		NamedNodeMap nodeMap = xmlNode.getAttributes();
		Node attrValue = nodeMap.getNamedItem(key);
		return attrValue.getNodeValue();
	}

	/**
	 * Examines all immediate descendants of the parent xmlnode to see if they name a nodeName equal
	 * to the name provided. If so this returns that node, or null if no child has the name.
	 * 
	 * @param parent Node whose children we examine
	 * @param name Name to search for
	 * @return A xml node with the specified name, if found
	 */
	private Node getChildForNodeName(Node parent, String name) {
		Node child = parent.getFirstChild();
		while(child != null) {
			if (child.getNodeName().equalsIgnoreCase(name)) {
				return child;
			}
			child = child.getNextSibling();
		}
		
		return null;
	}
	
	/**
	 * Attempts to find an immediate child element node with name 'nodeName', then attempts to find the text content associated with that
	 * node, then returns it. Hence, in the following xml
	 * 
	 * <taxonomy>
	 * 		<scientific_name>
	 * 			Homo sapiens
	 * 		</scientific_name>
	 * </taxonomy>
	 * 
	 * If parent is at 'taxonomy', we might call getNodeTextContent(parent, "scientific_name") to get the String "Homo sapiens"
	 * @param parent An xml node
	 * @param nodeName Node name of child whose text content we want
	 * @return Text content of child with the given name
	 */
	private String getNodeTextContent(Node parent, String nodeName) {
		Node node = getChildForNodeName(parent, nodeName);
		if (node == null)
			return null;
		
		Node child = node.getFirstChild();
		while (child != null) {
			if (child.getNodeName().equalsIgnoreCase("#text")) {
				return child.getNodeValue();
			}
			
			child = child.getNextSibling();
		}
		
		return null;
	}
	
	//We're not sure what type of node a node is until we finally parse all the edges, but it's convenient to create 
	//the nodes before we're done scanning all edges. This class is something we can instantiate, add other offspring,
	//parents, and range info to, and then later convert into an official ARGNode of the right class after seeing 
	//exactly how many connections there are
	class ProtoNode {
		
		String id; //The id of the node in question, defined by the XML document we're reading
		List<String> parentIDs = new ArrayList<String>();
		List<String> offspringIDs = new ArrayList<String>();
		double height = Double.NaN;
		//Info for nodes with range info, this stores the info for the parent for sites 0..breakpoint
		int rangeStart0 = -1;
		int rangeEnd0 = -1;
		String rangeID0 = null;
		//Similar to above, but for range of sites breakpoint..end
		int rangeStart1 = -1;
		int rangeEnd1 = -1;
		String rangeID1 = null;
		
		public ProtoNode(String id, double height) {
			this.id = id;
			this.height = height;
		}
		
		
		public double getHeight() {
			return height;
		}
		
		public void addParentID(String id) {
			parentIDs.add(id);
		}
		
		public void addOffspringID(String id) {
			offspringIDs.add(id);
		}
		
		public void setRangeInfoZero(String receivingParentId, int start, int end) {
			rangeStart0 = start;
			rangeEnd0 = end;
			rangeID0 = receivingParentId;
		}
		
		public void setRangeInfoOne(String receivingParentId, int start, int end) {
			rangeStart1 = start;
			rangeEnd1 = end;
			rangeID1 = receivingParentId;
		}
		
		public int getNumParents() {
			return parentIDs.size();
		}
		
		public int getNumOffspring() {
			return offspringIDs.size();
		}
		
		public boolean hasRangeInfo() {
			return rangeStart0 > -1;
		}
		
		/**
		 * Returns true if this protonode has the given id as a parent id
		 * @param id
		 * @return
		 */
		public boolean hasParent(String id) {
			for(String parID : parentIDs) {
				if (id.equals(parID)) {
					return true;
				}
			}
			return false;
		}
		
		public boolean hasValidRangeInfo() {
			boolean hasRange0 = rangeStart0>=0;
			boolean hasRange1 = rangeStart1>=0;
			//We have one range set but not the other, this is not OK
			if ((hasRange0 && (!hasRange1) ) || (hasRange1 && (!hasRange0))) {
				System.out.println("Range info has been set for one range but not the other");
				return false;
			}
			//We don't have either range, this is fine. 
			if ((!hasRange0) && (!hasRange1)) {
				return true;
			}
			if (rangeEnd0 != rangeStart1) {
				System.out.println("First and Second range breakpoints do not match : " + rangeEnd0 + " != " + rangeStart1);
				return false;
			}
			return true;
			
		}
		
	}
	

}
