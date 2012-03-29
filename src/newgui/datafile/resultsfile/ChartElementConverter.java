package newgui.datafile.resultsfile;

import newgui.datafile.XMLConversionError;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import logging.PropertyLogger;

public interface ChartElementConverter {

	/**
	 * Create an XML DOM element that contains all of the information in the given logger
	 * @param logger
	 * @param doc
	 * @return
	 */
	public Element convertLoggerToElement(PropertyLogger logger, Document doc) throws XMLConversionError;

	/**
	 * Convert the given XML DOM element into a PropertyLogger of the correct type with the correct data
	 * @param el
	 * @return
	 */
	public PropertyLogger convertElementToLogger(Element el) throws XMLConversionError;
	
}
