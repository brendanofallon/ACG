package newgui.datafile;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import newgui.datafile.FileNote.XMLConverter;
import newgui.gui.display.Display;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A data file that stores its info as XML. Right now this is probably the best base for all types of data fiels.
 * This also implements a lot of the basic data file functions 
 * @author brendan
 *
 */
public class XMLDataFile extends DataFile {

	public static final String ROOT_NAME = "ACGDataFile";
	public static final String DATA = "data";
	
	protected Document doc;
	
	/**
	 * Create a new XMLDataFile with a Document containing only the a root node with node name
	 * equal to ROOT_NAME
	 */
	public XMLDataFile() {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder;
		try {
			builder = factory.newDocumentBuilder();
			doc = builder.newDocument();
			doc.appendChild( doc.createElement(ROOT_NAME));
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			throw new IllegalStateException("Could not create document builder: " + e.getMessage());
		}
	}
	
	public XMLDataFile(Document doc) {
		this.doc = doc;
		this.source = null;
		initialize();
	}
	
	public XMLDataFile(File file) {
		source = file;
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder;
		try {
			builder = factory.newDocumentBuilder();
			doc = builder.parse(file);
			initialize();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public Display getDisplay() {
		throw new IllegalArgumentException("Not sure how to do this yet");
	}
	
	/**
	 * Append a new element to this document at root level
	 * @param el
	 */
	protected void appendElement(Element el) {
		doc.appendChild(el);
	}
	
	private void initialize() {
		//TODO load other file info here?
	}
	
	@Override
	public void appendNote(FileNote note) {
		Element notesEl = getTopLevelElement(FileNote.NOTES);
		Element newNoteEl = FileNote.getConverter().writeToXML(doc, note);
		notesEl.appendChild(newNoteEl);
	}

	@Override
	public void removeNote(FileNote note) {
		Element notesEl = getTopLevelElement(FileNote.NOTES);
		NodeList childs = notesEl.getChildNodes();
		for(int i=0; i<childs.getLength(); i++) {
			Node child = childs.item(i);
			if (child.getNodeType() == Node.ELEMENT_NODE && child.getNodeName().equals(FileNote.NOTES)) {
				Element cEl = (Element)child;
				String id = cEl.getAttribute(FileNote.XMLConverter.ID);
				if (id.equals(note.id)) {
					notesEl.removeChild(child);
					break;
				}
			}
		}
	}

	@Override
	public List<FileNote> getNotes() {
		List<FileNote> notes = new ArrayList<FileNote>(); 
		Element noteElement = getTopLevelElement(FileNote.NOTES);
		if (noteElement != null) {
			NodeList childs = noteElement.getChildNodes();
			for(int i=0; i<childs.getLength(); i++) {
				Node child = childs.item(i);
				if (child.getNodeType() == Node.ELEMENT_NODE && child.getNodeName().equals(FileNote.XMLConverter.NOTE)) {
					XMLConverter converter = FileNote.getConverter();
					try {
						FileNote note = converter.readFromXML((Element)child);
						notes.add(note);
					} catch (XMLConversionError e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				}
			}
		}
		
		return notes;
	}

	@Override
	public void save() throws IOException {
		if (source == null)
			throw new IOException("Source file has not been specified");
		else {
			String xmlStr;
			try {
				xmlStr = getXMLString();
				BufferedWriter writer = new BufferedWriter(new FileWriter(source));
				writer.write(xmlStr);
				writer.close();
			} catch (TransformerException e) {
				throw new IOException(e.getMessage());
			}

		}
	}

	/**
	 * Obtain an xml-string representing all of the data in this data file
	 * @return
	 * @throws TransformerException
	 */
	protected String getXMLString() throws TransformerException {
		return getXMLString(doc);
	}
	
	/**
	 * Obtain an xml-string with data from the subtree rooted at the given node
	 * @param root
	 * @return
	 * @throws TransformerException
	 */
	protected String getXMLString(Node root) throws TransformerException {
		TransformerFactory transfac = TransformerFactory.newInstance();
		Transformer trans = transfac.newTransformer();
		trans.setOutputProperty(OutputKeys.INDENT, "yes");
		//create string from xml tree
		StringWriter sw = new StringWriter();
		StreamResult result = new StreamResult(sw);
		DOMSource source = new DOMSource(root);
		trans.transform(source, result);
		String xmlString = sw.toString();
		return xmlString;
	}
	
	/**
	 * Obtain the element that is an immediate descendant of the root element 
	 * and has a node name equal to the given name
	 * @param nodeName
	 * @return
	 */
	protected Element getTopLevelElement(String nodeName) {
		NodeList childs = doc.getDocumentElement().getChildNodes();
		for(int i=0; i<childs.getLength(); i++) {
			Node child = childs.item(i);
			if (child.getNodeType() == Node.ELEMENT_NODE && child.getNodeName().equals(nodeName)) {
				return (Element)child;
			}
		}
		return null;
	}

	/**
	 * Tests whether this data file contains a node with the given name
	 * @param name
	 * @return
	 */
	public boolean containsElementByName(String name) {
		return getTopLevelElement(name) != null;
	}
	
	/**
	 * Obtain the element that is an immediate descendant of this element
	 * and has the given name
	 * @param nodeName
	 * @return
	 */
	public static Element getChildByName(Element el, String nodeName) {
		NodeList childs = el.getChildNodes();
		for(int i=0; i<childs.getLength(); i++) {
			Node child = childs.item(i);
			if (child.getNodeType() == Node.ELEMENT_NODE && child.getNodeName().equals(nodeName)) {
				return (Element)child;
			}
		}
		return null;
	}


}
