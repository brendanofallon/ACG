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
import logging.HistogramCollector;
import logging.PropertyLogger;
import math.Histogram;
import mcmc.MCMC;

import newgui.datafile.PropertiesElementReader;
import newgui.datafile.XMLConversionError;
import newgui.datafile.XMLDataFile;
import newgui.gui.display.Display;
import newgui.gui.display.primaryDisplay.PrimaryDisplay;
import newgui.gui.display.resultsDisplay.ResultsDisplay;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * The ResultsFile is a large xml-formatted DataFile that contains multiple top-level elements
 * describing various aspects of the results of an analysis - including charts from the loggers
 * and some properties of the run itself (run length, completion state, etc) 
 * @author brendano
 *
 */
public class ResultsFile extends XMLDataFile {

	//Identifies elements that can be parsed to a PropertyLogger 
	public static final String LOGGER_ELEMENT = "logger.element";
		
	//Attribute containing class of PropertyLogger
	public static final String LOGGER_CLASS = "logger.class";
	//Arbitrary label for logger element
	public static final String LOGGER_LABEL = "logger.label";
	
	public static final String MCMC_RUNLENGTH = "run.length";
	public static final String MCMC_PROPOSED = "states.proposed";
	public static final String MCMC_ACCEPTED = "states.accepted";
	public static final String MCMC_STARTTIME = "start.time";
	public static final String MCMC_ENDTIME = "end.time";
	public static final String MCMC_CHAINCOUNT = "chain.count";
	public static final String MCMC_THREADCOUNT = "thread.count";
	public static final String MCMC_RUNTIMEMS = "run.time.ms";
	
	Element propertiesElement;
	
	/**
	 * Create a new results file that attempts to read results from the given file
	 * @param file
	 */
	public ResultsFile(File file) {
		super(file);
		propertiesElement = getTopLevelElement(PropertiesElementReader.XML_PROPERTIES);
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
		display.showResultsFile(this);
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
		propsMap.put(MCMC_RUNLENGTH, "" + chain.getTotalRunLength());
		
		List<String> mcLabels = acgDoc.getLabelForClass(MCMC.class);
		if (mcLabels.size()>0) {
			try {
				MCMC mc = (MCMC) acgDoc.getObjectForLabel(mcLabels.get(0));
				propsMap.put(MCMC_PROPOSED, "" + mc.getStatesProposed());
				propsMap.put(MCMC_ACCEPTED, "" + mc.getStatesAccepted());
				propsMap.put(MCMC_CHAINCOUNT, "" + chain.getChainCount());
				propsMap.put(MCMC_THREADCOUNT, "" + chain.getThreadCount());
				
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		}
		
		Date startTime = chain.getStartTime();
		if (startTime != null)
			propsMap.put(MCMC_STARTTIME, startTime.toString());
		else 
			propsMap.put(MCMC_STARTTIME, "unknown");
		
		
		Date endTime = chain.getEndTime();
		if (endTime != null)
			propsMap.put(MCMC_ENDTIME, endTime.toString());
		else
			propsMap.put(MCMC_ENDTIME, "unknown");
		
		
		long elapsedMillis = endTime.getTime() - startTime.getTime();
		propsMap.put(MCMC_RUNTIMEMS, "" + elapsedMillis);
		
		
		//Add all loggers 
		for(String loggerLabel : acgDoc.getLabelForClass(PropertyLogger.class)) {
				try {
					PropertyLogger logger = (PropertyLogger) acgDoc.getObjectForLabel(loggerLabel);
					String label = logger.getName();
					if (label == null)
						label = loggerLabel.replace(".class", "");
					addChartElement(logger, label);
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
	
	/**
	 * Create a new XML element reflecting the information in the given logger
	 * @param logger
	 * @throws XMLConversionError
	 */
	protected void addChartElement(PropertyLogger logger, String label) throws XMLConversionError {
		Element loggerEl = doc.createElement(LOGGER_ELEMENT);
		loggerEl.setAttribute(LOGGER_CLASS, logger.getClass().getCanonicalName());
		loggerEl.setAttribute(LOGGER_LABEL, label);
		
		if (logger instanceof BreakpointDensity) {
			BreakpointDensity bpDensity = (BreakpointDensity)logger;
			Histogram histo = bpDensity.getHistogram();
			Element histoEl = HistogramElementReader.createHistogramElement(doc, histo);
			loggerEl.appendChild(histoEl);
		}
		
		
		addChartElement(loggerEl);
	}
	
	private void addChartElement(Element el) {
		if (el.getNodeName().equals(LOGGER_ELEMENT)) {
			doc.getDocumentElement().appendChild(el);
		}
		else {
			throw new IllegalArgumentException("Element does not appear to be a logger");
		}
	}
	
	public List<String> getChartLabels() throws XMLConversionError {
		List<Element> propLogEls = this.getTopLevelElements();
		List<String> loggers = new ArrayList<String>();
		for(Element loggerElement : propLogEls) {
			String className = loggerElement.getAttribute(LOGGER_CLASS);
			if (className != null)
				loggers.add( loggerElement.getAttribute(LOGGER_LABEL) );
		}
		return loggers;
	}
	
	
	public LoggerFigInfo getFigElementsForChartLabel(String label) throws XMLConversionError {
		List<Element> propLogEls = this.getTopLevelElements();
		for(Element loggerElement : propLogEls) {
			String loggerLabel = loggerElement.getAttribute(LOGGER_LABEL);
			if (loggerLabel != null && loggerLabel.equals(label)) {
				LoggerFigInfo info = parseFigElements(loggerElement);
				info.setTitle(loggerLabel);
				return info;
			}
		}
		
		throw new XMLConversionError("No element found with class equal to " + label, null);
	}
	
	/**
	 * Return a collection of plottable FigureElements from the given element
	 * @param el
	 * @return
	 */
	private LoggerFigInfo parseFigElements(Element el) throws XMLConversionError {
		NodeList children = el.getChildNodes();
		LoggerFigInfo figInfo = new LoggerFigInfo();
		for(int i=0; i<children.getLength(); i++) {
			Node node = children.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().equals(XYSeriesElementReader.XML_SERIES)) {
				XYSeriesInfo seriesInfo = XYSeriesElementReader.readFromElement( (Element)node);
				figInfo.seriesInfo.add(seriesInfo);
			}
			if (node.getNodeType()==Node.ELEMENT_NODE && node.getNodeName().equals(HistogramElementReader.HISTOGRAM)) {
				figInfo.histo = HistogramElementReader.readHistogramFromElement((Element)node);
			}
		}
		
		return figInfo;
	}
	


	
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
