package newgui.datafile.resultsfile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import gui.figure.series.XYSeries;
import logging.PropertyLogger;
import math.Histogram;
import newgui.datafile.XMLConversionError;
import newgui.datafile.XMLDataFile;


import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Houses a few static utilities for reading and writing Histograms to/from XML
 * @author brendano
 *
 */
public abstract class HistogramElementReader implements ChartElementConverter {
	
	//Signifies element containing main data series
	public static final String PRIMARY_DATA = "primary.data";
	
	public static final String HISTOGRAM = "histogram";
	//public static final String HISTO_MEAN = "histogram.mean";
	public static final String HISTO_MORETHANMAX = "histogram.morethanmax";
	public static final String HISTO_LESSTHANMIN = "histogram.lessthanmin";
	public static final String HISTO_BINWIDTH = "histogram.binwidth";
	public static final String HISTO_MIN = "histogram.min";
	public static final String HISTO_COUNT = "histogram.count";
	public static final String HISTO_SUM = "histogram.sum";
	public static final String HISTO_DATA = "histogram.data";
	
	public static Histogram readHistogramFromElement(Element el) throws XMLConversionError {
		Double moreThanMax = parseDoubleFromChild(el, HISTO_MORETHANMAX);
		Double lessThanMin = parseDoubleFromChild(el, HISTO_LESSTHANMIN);
		Double binWidth = parseDoubleFromChild(el, HISTO_BINWIDTH);
		Double histoMin = parseDoubleFromChild(el, HISTO_MIN);
		Double count = parseDoubleFromChild(el, HISTO_COUNT);
		Double sum = parseDoubleFromChild(el, HISTO_SUM);
		
		double[] counts = readDoubleArray(el, HISTO_DATA);
		Histogram hist = new Histogram(counts, binWidth, histoMin, sum, count, moreThanMax, lessThanMin);
		return hist;
	}
	
	/**
	 * Write all attributes of the given node into a new Map and return it
	 * @param el
	 * @return
	 */
	protected static Map<String, String> readPropertyLoggerAttrs(Element el) {
		Map<String, String> attrs = new HashMap<String, String>();
		NamedNodeMap nodeMap = el.getAttributes();
		for(int i=0; i<nodeMap.getLength(); i++) {
			Node node = nodeMap.item(i);
			String key = node.getNodeName();
			String value = node.getNodeValue();
			attrs.put(key, value);
		}
		return attrs;
	}
	
	public static Element createHistogramElement(Document doc, Histogram histo) {
		Element el = doc.createElement(HISTOGRAM);
		el.appendChild( createTextNode(doc, HISTO_MORETHANMAX, "" + histo.getMoreThanMax()));
		el.appendChild( createTextNode(doc, HISTO_LESSTHANMIN, "" + histo.getLessThanMin())); 
		el.appendChild( createTextNode(doc, HISTO_SUM, "" + histo.getSum()));
		el.appendChild( createTextNode(doc, HISTO_BINWIDTH, "" + histo.getBinWidth())); 
		el.appendChild( createTextNode(doc, HISTO_COUNT, "" + histo.getCount())); 
		el.appendChild( createTextNode(doc, HISTO_MIN, "" + histo.getMin())); 
		el.appendChild( createDoubleArrayChild(doc, HISTO_DATA, histo.getData()));
		return el;
	}
	
	/**
	 * Create a new element and add the given text to it, and return the element
	 * @param doc
	 * @param parent
	 * @param childName
	 * @param text
	 * @return
	 */
	public static Element createTextNode(Document doc, String childName, String text) {
		Element child = doc.createElement(childName);
		Node textNode = doc.createTextNode(text);
		child.appendChild(textNode);
		return child;
	}
	
	/**
	 * Searches the children of the given node for an element with the given name, then parses text text
	 * content of that node and attempts to return it as a double
	 * @param el
	 * @param childName
	 * @return
	 * @throws XMLConversionError
	 */
	public static Double parseDoubleFromChild(Element el, String childName) throws XMLConversionError {
		String valStr = XMLDataFile.getTextFromChild(el, childName);
		if (valStr == null)
			throw new XMLConversionError("No node with name " + childName, el);
		Double val = Double.parseDouble(valStr);
		if (val == null)
			throw new XMLConversionError("Could not parse value from content: " + valStr, el);
		return val;
	}
	
	/**
	 * Searches the given element for a child with the given name, then obtains its text content
	 * and parses it to an array of doubles
	 * @param el
	 * @param childName
	 * @return
	 * @throws XMLConversionError
	 */
	public static double[] readDoubleArray(Element el, String childName) throws XMLConversionError {
		String list = XMLDataFile.getTextFromChild(el, childName);
		StringTokenizer xToks = new StringTokenizer(list, ",");
		List<Double> vals = new ArrayList<Double>();

		while (xToks.hasMoreTokens()) {
			String xStr = xToks.nextToken();
			Double val = Double.parseDouble(xStr);
			vals.add(val);
		}

		double[] arr = new double[vals.size()];
		for(int i=0; i<arr.length; i++) {
			arr[i] = vals.get(i);
		}
		return arr;
	}
	
	/**
	 * Create a new element with the given name containing a text version of the given array
	 * @param doc
	 * @param childName
	 * @param list
	 * @return
	 */
	public static Element createDoubleArrayChild(Document doc, String childName, double[] list) {
		StringBuilder str = new StringBuilder();
		for(int i=0; i<list.length; i++) {
			str.append(list[i] + ",");
		}
		return createTextNode(doc, childName, str.toString());
	}
	
	
	public static Element createIntegerArrayChild(Document doc, String childName, int[] list) {
		StringBuilder str = new StringBuilder();
		for(int i=0; i<list.length; i++) {
			str.append(list[i] + ",");
		}
		return createTextNode(doc, childName, str.toString());
	}
}

