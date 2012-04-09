package newgui.datafile.resultsfile;

import java.util.ArrayList;
import java.util.List;

import gui.figure.series.XYSeries;

import newgui.datafile.XMLConversionError;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import logging.MemoryStateLogger;

/**
 * A class to read and write StateLogger-type information (parameter values and likelihoods)
 * from an xml element
 * @author brendan
 *
 */

public class StateElementConverter {

	public static final String XML_STATELOGGER = "statelogger.element";
	public static final String XML_VALUES = "values";
	public static final String XML_CLASS = "class";
	public static final String XML_LABEL = "label";
	
	
	public static Element createElement(MemoryStateLogger logger, Document doc) {
		Element el = doc.createElement(XML_STATELOGGER);
		for(String seriesName : logger.getSeriesNames()) {
			XYSeries series = logger.getSeries(seriesName);
			Element seriesEl = XYSeriesElementReader.createElement(doc, series);
			
			el.appendChild(seriesEl);
		}
		return el;
	}
	
	public static List<String> getSeriesNames(Element el) {
		if (! el.getNodeName().equals(XML_STATELOGGER)) {
			throw new IllegalArgumentException("Element is not a state logger element");
		}
		
		List<String> names = new ArrayList<String>();
		NodeList children = el.getChildNodes();
		for(int i=0; i<children.getLength(); i++) {
			Node child = children.item(i);
			if (child.getNodeType() == Node.ELEMENT_NODE && child.getNodeName() == XYSeriesElementReader.XML_SERIES) {
				Element seriesEl = (Element)child;
				String name = seriesEl.getAttribute(XYSeriesElementReader.XML_LABEL);
				names.add(name);
			}
		}
		return names;
	}
	
	
	public static XYSeriesInfo getSeriesForName(Element el, String seriesName) throws XMLConversionError {
		if (! el.getNodeName().equals(XML_STATELOGGER)) {
			throw new IllegalArgumentException("Element is not a state logger element");
		}
		
		List<String> names = new ArrayList<String>();
		NodeList children = el.getChildNodes();
		for(int i=0; i<children.getLength(); i++) {
			Node child = children.item(i);
			if (child.getNodeType() == Node.ELEMENT_NODE && child.getNodeName() == XYSeriesElementReader.XML_SERIES) {
				Element seriesEl = (Element)child;
				String name = seriesEl.getAttribute(XYSeriesElementReader.XML_LABEL);
				if (name.equals(seriesName)) {
					XYSeriesInfo series = XYSeriesElementReader.readFromElement(seriesEl);
					return series;
				}
				
			}
		}
		return null;
	}
	
}
