package newgui.datafile;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import newgui.gui.display.Display;
import newgui.gui.display.primaryDisplay.PrimaryDisplay;
import gui.ErrorWindow;
import gui.document.ACGDocument;
import gui.inputPanels.AnalysisModel;
import gui.inputPanels.Configurator.InputConfigException;

/**
 * An analysis data file basically wraps an ACGDocument and turns it into a DataFile. This may be
 * somewhat confusing since an ACGDoc has much of the same functionality - it has it's own DOM object, etc. 
 * 
 * @author brendano
 *
 */
public class AnalysisDataFile extends XMLDataFile {

	public static final String MODEL = "model"; 
	
	public AnalysisDataFile() {
		//Nothing to do
	}
	
	public AnalysisDataFile(File file) {
		super(file);
	}
	
	public Display getDisplay() {
		PrimaryDisplay display = new PrimaryDisplay();
		
		try {
			ACGDocument doc = getACGDocument();
			doc.loadAndVerifyClasses();
			AnalysisModel model = new AnalysisModel();
			model.readFromDocument( doc );
			display.showAnalysisDetails( model );
		} catch (InputConfigException e) {
			e.printStackTrace();
			ErrorWindow.showErrorWindow(e);
		} catch (Exception e) {
			e.printStackTrace();
			ErrorWindow.showErrorWindow(e);
		}
		return display;
	}
	
	public void setACGDocument(ACGDocument acgDoc) throws XMLConversionError {
		updateElement(acgDoc);
	}
	
	private void updateElement(ACGDocument acgDoc) throws XMLConversionError {
		Element analysisEl = getTopLevelElement(MODEL);
		if (analysisEl == null) {
			analysisEl = doc.createElement(MODEL);
			doc.getDocumentElement().appendChild(analysisEl);
		}
		
		//Clear all current nodes from analsysiElement and re-add new ones
		NodeList children = analysisEl.getChildNodes();
		for(int i=0; i<children.getLength(); i++) 
			analysisEl.removeChild( children.item(i));
		
		//Clone entire document tree into newNode
		Node newNode = doc.importNode( acgDoc.getDocument().getDocumentElement(), true);
		analysisEl.appendChild(newNode);
		try {
			String str = getXMLString(newNode);
			System.out.println("Creating analysis node with this string: \n" + str);
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	/**
	 * Create a new ACGDocument based on the settings in this data file
	 * @return
	 */
	public ACGDocument getACGDocument() {
		Element el = getTopLevelElement(MODEL);
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder;
		try {
			builder = factory.newDocumentBuilder();
			Document newDoc = builder.newDocument();
			NodeList children = el.getChildNodes();
			Element childToClone = null;
			for(int i=0; i<children.getLength(); i++) {
				Node child = children.item(i);
				if (child.getNodeType() == Node.ELEMENT_NODE) {
					childToClone = (Element)child;
					
				}
			}
			if (childToClone == null) {
				throw new XMLConversionError("Could not find analysis part of file", el);
			}
			Node clonedNode = newDoc.importNode(childToClone, true);
			if (clonedNode == null) {
				System.out.println("Cloned node is null!");
			}
			try {
				String str = getXMLString(clonedNode);
				System.out.println("Creating ACGDocument with this string: \n" + str);
			} catch (TransformerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			newDoc.appendChild(clonedNode);
			ACGDocument acgDoc = new ACGDocument(newDoc);
			return acgDoc;
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
		
		return null;
	}
}
