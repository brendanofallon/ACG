package gui.inputPanels;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

public interface Configurator {
	
	
	public Node[] getXMLNodes(Document doc) throws ParserConfigurationException;
	
}
