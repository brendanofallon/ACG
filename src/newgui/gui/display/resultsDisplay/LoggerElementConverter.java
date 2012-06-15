package newgui.gui.display.resultsDisplay;

import logging.PropertyLogger;

import newgui.datafile.XMLConversionError;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Interface for objects that can write logger data to xml AND
 * that can produce a LoggerFigure from an xml element
 * @author brendan
 *
 */
public interface LoggerElementConverter {

	public LoggerResultDisplay getLoggerFigure(Element el) throws XMLConversionError;
	
	/**
	 * Create an Element that represents the data in the given logger
	 * @param logger
	 * @return
	 */
	public Element createElement(Document doc, PropertyLogger logger, String label);
	

}
