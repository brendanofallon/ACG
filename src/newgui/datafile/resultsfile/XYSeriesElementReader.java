package newgui.datafile.resultsfile;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import newgui.datafile.XMLConversionError;
import newgui.datafile.XMLDataFile;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import gui.figure.series.XYSeries;

/**
 * Small utility class to convert XYSeries into a DOM (xml) element 
 * @author brendano
 *
 */
public class XYSeriesElementReader {

	public static final String XML_SERIES = "series";
	public static final String XML_XDATA = "x.data";
	public static final String XML_YDATA = "y.data";
	
	/**
	 * Construct a new XYSeries from the data in the given element
	 * @param el
	 * @return
	 * @throws XMLConversionError 
	 */
	public static XYSeries readFromElement(Element xySeriesElement) throws XMLConversionError {
		Element xDataEl = XMLDataFile.getChildByName(xySeriesElement, XML_XDATA);
		Element yDataEl = XMLDataFile.getChildByName(xySeriesElement, XML_YDATA);
		if (xDataEl == null)
			throw new XMLConversionError("No x data element found", xySeriesElement);
		if (yDataEl == null)
			throw new XMLConversionError("No y data element found", xySeriesElement);

		String xDataStr = XMLDataFile.getTextFromChild(xySeriesElement, XML_XDATA);
		String yDataStr = XMLDataFile.getTextFromChild(xySeriesElement, XML_YDATA);
		
		List<Point2D> points = new ArrayList<Point2D>();
		
		StringTokenizer xTokenizer = new StringTokenizer(xDataStr, ",");
		StringTokenizer yTokenizer = new StringTokenizer(yDataStr, ",");
		while (xTokenizer.hasMoreTokens() && yTokenizer.hasMoreTokens()) {
			String xStr = xTokenizer.nextToken();
			String yStr = yTokenizer.nextToken();
			Double xVal = null;
			Double yVal = null;
			try {
				xVal = Double.parseDouble(xStr);
			}
			catch (NumberFormatException nfe) {
				throw new XMLConversionError("Could not parse value from string: " + xStr, xDataEl);
			}
			try {
				yVal = Double.parseDouble(yStr);
			}
			catch (NumberFormatException nfe) {
				throw new XMLConversionError("Could not parse value from string: " + xStr, xDataEl);
			}
			points.add(new Point2D.Double(xVal, yVal));
		}
		
		
		
		XYSeries series = new XYSeries(points);
		
		return series;
	}
	
	/**
	 * Create a new Element owned by the given document and write the given series data to it
	 * @param doc
	 * @param series
	 * @return
	 */
	public static Element createElement(Document doc, XYSeries series) {
		Element xySeriesElement = doc.createElement(XML_SERIES);
		assignData(doc, xySeriesElement, series);
		return xySeriesElement;
	}
	
	
	
	/**
	 * Write the XYSeriesData into the given DOM element owned by the given document
	 * @param doc
	 * @param series
	 * @return
	 */
	public static void assignData(Document doc, Element xySeriesElement, XYSeries series) {
		Element xData;
		Element yData;
		NodeList children = xySeriesElement.getChildNodes();
		for(int i=0; i<children.getLength(); i++)
			xySeriesElement.removeChild(children.item(i));
		
		xData = doc.createElement(XML_XDATA);
		yData = doc.createElement(XML_YDATA);

		xySeriesElement.appendChild(xData);
		xySeriesElement.appendChild(yData);
		doc.getDocumentElement().appendChild(xySeriesElement);

		
		StringBuilder xStr = new StringBuilder();
		StringBuilder yStr = new StringBuilder();
		for(int i=0; i<series.size(); i++) {
			xStr.append(series.getX(i) + ",");
			yStr.append(series.getY(i) + ",");
		}
		
		xData.setTextContent(xStr.toString());
		yData.setTextContent(yStr.toString());
		//return xySeriesElement;
	}
	
	
}
