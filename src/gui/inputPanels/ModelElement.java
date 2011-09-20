package gui.inputPanels;

import gui.document.ACGDocument;
import gui.inputPanels.Configurator.InputConfigException;

import java.util.List;

import org.w3c.dom.Element;

import parameter.DoubleParameter;
import xml.XMLLoader;

public abstract class ModelElement {
	
	
	/**
	 * Return a list of DOM elements representing the data in this ModelElement
	 * @param doc
	 * @return
	 */
	public abstract List<Element> getElements(ACGDocument doc) throws InputConfigException;
	
	/**
	 * Read settings from the given document and store the data. Subsequent calls to
	 * getElements should return the information that was read in. 
	 * @param doc
	 */
	public abstract void readElements(ACGDocument doc) throws InputConfigException;
	
	
		
	
}
