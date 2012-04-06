package newgui.datafile.resultsfile;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import math.Histogram;
import newgui.datafile.XMLConversionError;
import newgui.datafile.XMLDataFile;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import gui.figure.series.XYSeries;

/**
 * Small utility class to convert XYSeries into a DOM (xml) Element and back 
 * @author brendano
 *
 */
public class XYSeriesElementReader {

	public static final String XML_SERIES = "series";
	public static final String XML_XDATA = "x.data";
	public static final String XML_YDATA = "y.data";
	public static final String XML_COLOR = "color";
	public static final String XML_WIDTH = "width";
	public static final String XML_LABEL = "label";
	
	/**
	 * Construct a new XYSeries from the data in the given element
	 * @param el
	 * @return
	 * @throws XMLConversionError 
	 */
	public static XYSeriesInfo readFromElement(Element xySeriesElement) throws XMLConversionError {
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
		
		//Parse additional attributes like color, width, etc
		String colorStr = xySeriesElement.getAttribute(XML_COLOR);
		Color seriesColor = parseColor(colorStr);
		String widthStr = xySeriesElement.getAttribute(XML_WIDTH);
		Float width = Float.parseFloat(widthStr);
		
		String label = xySeriesElement.getAttribute(XML_LABEL);
		
		
		XYSeries series = new XYSeries(points, label);
		
		XYSeriesInfo seriesInfo = new XYSeriesInfo();
		seriesInfo.series = series;
		seriesInfo.color = seriesColor;
		seriesInfo.width = width;
		
		return seriesInfo;
	}
	
	/**
	 * Create a new Element owned by the given document and write the given series data to it, using
	 * blue as the color and 1.0f as the width
	 * @param doc
	 * @param series
	 * @return
	 */
	public static Element createElement(Document doc, XYSeries series) {
		return createElement(doc, series, Color.blue, 1f);
	}
	
	/**
	 * Create a new Element owned by the given document and write the given series data to it
	 * @param doc
	 * @param series
	 * @return
	 */
	public static Element createElement(Document doc, XYSeries series, Color color, float width) {
		Element xySeriesElement = doc.createElement(XML_SERIES);
		assignData(doc, xySeriesElement, series);
		
		xySeriesElement.setAttribute(XML_COLOR, colorToString(color));
		xySeriesElement.setAttribute(XML_WIDTH, "" + width);
		xySeriesElement.setAttribute(XML_LABEL, "" + series.getName());
		
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
	}
	
	/**
	 * Parse a color object from the given string, which is assumed to be three comma-separated values
	 * @param str
	 * @return
	 */
	public static Color parseColor(String str) {
		String[] rgbVals = str.split(",");
		if (rgbVals.length != 3) {
			throw new IllegalArgumentException("Invalid input string, expected three comma separated values");
		}
		int r = Integer.parseInt(rgbVals[0]);
		int g = Integer.parseInt(rgbVals[1]);
		int b = Integer.parseInt(rgbVals[2]);
		
		return new Color(r,g,b);
	}
	
	/**
	 * Create a comma-separated string reflecting the r,g,b value in the given color
	 * @param color
	 * @return
	 */
	public static String colorToString(Color color) {
		int r = color.getRed();
		int g = color.getGreen();
		int b = color.getBlue();
		return r + "," + g + "," + b;
	}
}
