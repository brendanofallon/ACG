package gui.inputPanels;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public interface Configurator {
	
	public Element[] getXMLNodes(Document doc) throws ParserConfigurationException, InputConfigException;
	
	class InputConfigException extends Exception {
		
		public InputConfigException(String message) {
			super(message);
		}
	}
}
