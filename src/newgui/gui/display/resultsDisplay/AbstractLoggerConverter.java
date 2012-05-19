package newgui.gui.display.resultsDisplay;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import logging.PropertyLogger;

import newgui.datafile.XMLConversionError;
import newgui.datafile.XMLDataFile;
import newgui.datafile.resultsfile.HistogramElementReader;
import newgui.datafile.resultsfile.XYSeriesElementReader;
import newgui.datafile.resultsfile.XYSeriesInfo;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Base class of things that can write the state of a logger to XML and read the data from
 * back from XML, usually in the form of a LoggerResultDisplay, which displays the data
 * @author brendan
 *
 */
public abstract class AbstractLoggerConverter implements LoggerElementConverter {

	public static final String SERIES_LABEL = "label";
	public static final String SERIES_UPPER95 = "upper95";
	public static final String SERIES_MEAN = "mean";
	public static final String SERIES_LOWER95 = "lower95";
	
	public static final String TREE = "consensus.tree";
	public static final String TREES_COUNTED = "trees.counted";
	//Identifies elements that can be parsed to a PropertyLogger 
	public static final String LOGGER_ELEMENT = "logger.element";
	public static final String TREE_ELEMENT = "tree.element";

	//Attribute containing class of PropertyLogger
	public static final String LOGGER_CLASS = "logger.class";
	//Arbitrary label for logger element
	public static final String LOGGER_LABEL = "logger.label";
	
	public static final String MATRIX = "matrix";
	public static final String ROW_COUNT = "row.count";
	public static final String COL_COUNT = "col.count";
	public static final String ROW = "row";

	
	/**
	 * Add children containing actual data from this logger to the given element
	 * @param el
	 */
	public abstract void addChildren(Document doc, Element el, PropertyLogger logger);
	
	
	public Element createElement(Document doc, PropertyLogger logger, String label) {
		Element el = doc.createElement(LOGGER_ELEMENT);
		el.setAttribute(LOGGER_CLASS, logger.getClass().getCanonicalName());
		el.setAttribute(LOGGER_LABEL, label);
	
		addChildren(doc, el, logger);
		
		return el;
	}
		
	
	/**
	 * Return a collection of plottable FigureElements from the given element
	 * @param el
	 * @return
	 */
	protected LoggerFigInfo parseFigElements(Element el) throws XMLConversionError {
		NodeList children = el.getChildNodes();
		LoggerFigInfo figInfo = new LoggerFigInfo();
		for(int i=0; i<children.getLength(); i++) {
			Node node = children.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().equals(XYSeriesElementReader.XML_SERIES)) {
				XYSeriesInfo seriesInfo = XYSeriesElementReader.readFromElement( (Element)node);
				figInfo.seriesInfo.add(seriesInfo);
				figInfo.xAxisTitle = "Sequence position";
				figInfo.yAxisTitle = "Value";
			}
			if (node.getNodeType()==Node.ELEMENT_NODE && node.getNodeName().equals(HistogramElementReader.HISTOGRAM)) {
				figInfo.histo = HistogramElementReader.readHistogramFromElement((Element)node);
				figInfo.xAxisTitle = "Density";
				figInfo.xAxisTitle = "Sequence position";
			}
		}
		figInfo.setTitle(el.getAttribute(LOGGER_LABEL));
		return figInfo;
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
	 * Find the child whose name is equal to the given name and return it,
	 * or null if there is no such child
	 * @param parent
	 * @param nodeName
	 * @return
	 */
	public static Element getChildForName(Element parent, String nodeName) {
		NodeList children = parent.getChildNodes();
		for(int i=0; i<children.getLength(); i++) {
			Node child = children.item(i);
			if (child.getNodeName().equals(nodeName)) {
				return (Element)child;
			}
		}
		return null;
	}
	
	/**
	 * Searches the given element for a child with the given name, then obtains its text content
	 * and parses it to an array of doubles
	 * @param el
	 * @param childName
	 * @return
	 * @throws XMLConversionError
	 */
	public static double[] readDoubleArrayChild(Element el, String childName) throws XMLConversionError {
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
	 * Obtain the text from this child and attempt to parse it into an array of doubles
	 * @param el
	 * @return
	 */
	public static double[] readDoubleArray(Element el) {
		String list = getText(el);
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
	 * Returns the text from the first child that is of type TEXT_NODE from the
	 * given element
	 * @param el
	 * @return
	 */
	public static String getText(Element el) {
		NodeList children = el.getChildNodes();
		for(int i=0; i<children.getLength(); i++) {
			Node child = children.item(i);
			if (child.getNodeType() == Node.TEXT_NODE) {
				return child.getNodeValue();
			}
		}
		return null;
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
	
	protected static Element createMatrixChild(Document doc, double[][] matrix) {
		Element matrixEl = doc.createElement(MATRIX);
		matrixEl.setAttribute(ROW_COUNT, "" + matrix.length);
		matrixEl.setAttribute(COL_COUNT, "" + matrix[0].length);
		
		for(int i=0; i<matrix.length; i++) {
			Element rowChild = createDoubleArrayChild(doc, ROW, matrix[i]);
			matrixEl.appendChild(rowChild);
		}
		
		return matrixEl;
	}
	
	protected static double[][] readMatrix(Element el) throws XMLConversionError {
		if (! el.getNodeName().equals(MATRIX)) {
			throw new IllegalArgumentException("Element is not a matrix element");
		}
		String rowStr = el.getAttribute(ROW_COUNT);
		if (rowStr == null || rowStr.trim().length()==0)
			throw new XMLConversionError("No row count specified", el);
		String colStr = el.getAttribute(COL_COUNT);
		if (colStr == null || colStr.trim().length()==0)
			throw new XMLConversionError("No column count specified", el);
		
		Integer rows = Integer.parseInt(rowStr);
		Integer cols = Integer.parseInt(colStr);
		
		double[][] matrix = new double[rows][cols];
		NodeList children = el.getChildNodes();
		int rowsFound = 0;
		for(int i=0; i<children.getLength(); i++) {
			Node child = children.item(i);
			if (child.getNodeType() == Node.ELEMENT_NODE && child.getNodeName().equals(ROW)) {
				double[] rowData = readDoubleArray( (Element)child);
				matrix[rowsFound] = rowData;
				rowsFound++;
			}
		}
		
		return matrix;
	}
}
