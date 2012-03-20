package newgui.datafile;

import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import gui.figure.series.XYSeries;

import org.w3c.dom.Element;

/**
 * This type of data file stores an XYSeries, and a little bit of additional data?
 * @author brendano
 *
 */
public class SeriesDataFile extends XMLDataFile {

	Element xySeriesElement = null;
	
	public SeriesDataFile(File file) {
		super(file);
	}
	
	public SeriesDataFile() {
		//blank on purpose
	}
	
	public XYSeries getDataSeries() throws XMLConversionError {
		if (xySeriesElement == null) {
			findSeriesNode();
			if (xySeriesElement == null)
				return null;
		}
		
		return XYSeriesElementReader.readFromElement(xySeriesElement);
	}

	/**
	 * Search the DOM document for a top level node with the node name "series" and
	 * assign xySeriesElement to it
	 */
	private void findSeriesNode() {
		xySeriesElement = getTopLevelElement(XYSeriesElementReader.XML_SERIES);
	}
	
	/**
	 * Erase current series data and set the data to reflect the information in 
	 * the given series
	 * @param series
	 */
	public void setDataSeries(XYSeries series) {
		if (xySeriesElement == null) {
			xySeriesElement = XYSeriesElementReader.createElement(doc, series);
		}
		else {
			XYSeriesElementReader.assignData(doc, xySeriesElement, series);
		}
	}
	
	
	public static void main(String[] args) {
//		List<Point2D> data = new ArrayList<Point2D>();
//		
//		data.add(new Point2D.Double(1.0, 2.0));
//		data.add(new Point2D.Double(2.0, 2.0));
//		data.add(new Point2D.Double(2.5, 10.0));
//		data.add(new Point2D.Double(3.0, 17.0));
//		data.add(new Point2D.Double(4.0, 4.0));
		
//		SeriesDataFile file = new SeriesDataFile();
//		
//		XYSeries series = new XYSeries(data);
//		file.setDataSeries(series);
//		
//		try {
//			file.saveToFile(new File("seriesdata.xml"));
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

		SeriesDataFile file = new SeriesDataFile(new File("seriesdata.xml"));
		try {
			XYSeries data = file.getDataSeries();
			System.out.println( data.toString() );
			
		} catch (XMLConversionError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
}
