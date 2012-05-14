package newgui.gui.display.resultsDisplay;

import logging.PropertyLogger;

import newgui.datafile.XMLConversionError;
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


	public static final String TMRCA_LABEL = "label";
	public static final String TMRCA_UPPER95 = "upper95";
	public static final String TMRCA_MEAN = "mean";
	public static final String TMRCA_LOWER95 = "lower95";
	
	public static final String TREE = "consensus.tree";
	public static final String TREES_COUNTED = "trees.counted";
	//Identifies elements that can be parsed to a PropertyLogger 
	public static final String LOGGER_ELEMENT = "logger.element";
	public static final String TREE_ELEMENT = "tree.element";

	//Attribute containing class of PropertyLogger
	public static final String LOGGER_CLASS = "logger.class";
	//Arbitrary label for logger element
	public static final String LOGGER_LABEL = "logger.label";

	
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
}
