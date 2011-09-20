package gui.inputPanels;

import gui.document.ACGDocument;
import gui.inputPanels.Configurator.InputConfigException;

import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * An interface for all things that can produce a list of DOM Nodes (not necessarily elements, I guess)
 * @author brendano
 *
 */
public interface ElementProvider {

	public List<Node> getElements(ACGDocument doc) throws InputConfigException ;
	
	
}
