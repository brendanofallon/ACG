package newgui.datafile.resultsfile;

import gui.document.ACGDocument;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jobqueue.ExecutingChain;
import jobqueue.JobState;
import logging.BreakpointDensity;
import logging.PropertyLogger;

import newgui.datafile.PropertiesElementReader;
import newgui.datafile.XMLConversionError;
import newgui.datafile.XMLDataFile;
import newgui.gui.display.Display;
import newgui.gui.display.primaryDisplay.PrimaryDisplay;
import newgui.gui.display.resultsDisplay.ResultsDisplay;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * The ResultsFile is a large xml-formatted DataFile that contains multiple top-level elements
 * describing various aspects of the results of an analysis - including charts from the loggers
 * and some properties of the run itself (run length, completion state, etc) 
 * @author brendano
 *
 */
public class ResultsFile extends XMLDataFile {

	public static final String XML_CHART = "chart";
	
	Element propertiesElement;
	List<Element> charts = new ArrayList<Element>();
	
	/**
	 * Create a new results file that attempts to read results from the given file
	 * @param file
	 */
	public ResultsFile(File file) {
		super(file);
		propertiesElement = getTopLevelElement(PropertiesElementReader.XML_PROPERTIES);
		charts = getTopLevelElements(XML_CHART);
	}

	/**
	 * Create a new results file with no data
	 */
	public ResultsFile() {
		propertiesElement = doc.createElement(PropertiesElementReader.XML_PROPERTIES);
		doc.getDocumentElement().appendChild(propertiesElement);
	}
	
	/**
	 * Add the given property (key=value pair) to the list of properties 
	 * @param key
	 * @param value
	 */
	public void addProperty(String key, String value) {
		Element newEntry = PropertiesElementReader.createEntryElement(doc, key, value);
		propertiesElement.appendChild(newEntry);
	}
	
	/**
	 * Construct a new map containing all key/value pairs that are the properties of this
	 * file
	 * @param key
	 * @return
	 * @throws XMLConversionError
	 */
	public Map<String, String> getPropertiesMap() throws XMLConversionError {
		return PropertiesElementReader.readProperties(propertiesElement);
	}
	
	
	@Override
	public Display getDisplay() {
		String title = getSourceFile().getName().replace(".xml", "");
		if (source != null) 
			title = source.getName();
		ResultsDisplay display = new ResultsDisplay();
		display.setTitle(title);
		//ResultsDisplay.addAlignment(getAlignment(), title);
		return display;
	}
	
	/**
	 * Clear all current properties and then assign all properties in the given map
	 * to this results file properties
	 * @param map
	 */
	public void setProperties(Map<String, String> map) {
		clearProperties();
		for(String key : map.keySet()) {
			addProperty(key, map.get(key));
		}
	}
	
	/**
	 * Remove all properties from this data file
	 */
	public void clearProperties() {
		NodeList children = propertiesElement.getChildNodes();
		for(int i=0; i<children.getLength(); i++)
			propertiesElement.removeChild(children.item(i));
	}
	
	/**
	 * Add all of the results from the given chain and document to this ResultsFile, this destroys
	 * all data current stored in this file
	 * @param chain
	 * @param acgDoc
	 * @throws XMLConversionError
	 */
	public void addAllResults(ExecutingChain chain, ACGDocument acgDoc) throws XMLConversionError {
		Map<String, String> propsMap = new HashMap<String, String>();
		propsMap.put("run.length", "" + chain.getTotalRunLength());
		
		Date startTime = chain.getStartTime();
		if (startTime != null)
			propsMap.put("start.time", startTime.toString());
		else 
			propsMap.put("start.time", "unknown");
		
		
		//Add all loggers 
		for(String label : acgDoc.getLabelForClass(PropertyLogger.class)) {
				try {
					PropertyLogger logger = (PropertyLogger) acgDoc.getObjectForLabel(label);
					addChartElement(logger);
				} catch (InstantiationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
		}
		setProperties(propsMap);
	}
	
	protected void addChartElement(PropertyLogger logger) throws XMLConversionError {
		// TODO Auto-generated method stub
		//Not sure what to do here... would like to be able to save and read all chart data...
		if (logger instanceof BreakpointDensity) {
			BPDensityConverter converter = new BPDensityConverter();
			Element el = converter.convertLoggerToElement(logger, doc);
			addChartElement(el);
		}
	}
	
	private void addChartElement(Element el) {
		if (el.getNodeName().equals(AbstractLoggerConverter.LOGGER_ELEMENT)) {
			charts.add(el);
			doc.getDocumentElement().appendChild(el);
		}
		else {
			throw new IllegalArgumentException("Element does not appear to be a logger");
		}
	}

//	private PropertyLogger readChartElement(Element el) {
//		
//	}
	
	public static void main(String[] args) {
		Map<String, String> map = new HashMap<String, String>();
		map.put("akey", "somevalue");
		map.put("anotherkey", "othervalue");
		map.put("somekey", "17");
		
		ResultsFile file = new ResultsFile(new File("resultstest.xml"));
//		file.setProperties(map);
//		
//		try {
//			file.saveToFile(new File("resultstest.xml"));
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}		
	}

}
