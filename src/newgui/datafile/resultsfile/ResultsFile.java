package newgui.datafile.resultsfile;

import gui.ErrorWindow;
import gui.document.ACGDocument;
import gui.figure.series.XYSeries;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jobqueue.ExecutingChain;
import jobqueue.JobState;
import logging.BreakpointDensity;
import logging.BreakpointLocation;
import logging.ConsensusTreeLogger;
import logging.HistogramCollector;
import logging.MarginalTreeLogger;
import logging.MemoryStateLogger;
import logging.PropertyLogger;
import logging.RootHeightDensity;
import math.Histogram;
import mcmc.MCMC;
import modifier.AbstractModifier;

import newgui.datafile.PropertiesElementReader;
import newgui.datafile.XMLConversionError;
import newgui.datafile.XMLDataFile;
import newgui.gui.display.Display;
import newgui.gui.display.primaryDisplay.PrimaryDisplay;
import newgui.gui.display.resultsDisplay.AbstractLoggerConverter;
import newgui.gui.display.resultsDisplay.LoggerConverterFactory;
import newgui.gui.display.resultsDisplay.LoggerElementConverter;
import newgui.gui.display.resultsDisplay.LoggerFigInfo;
import newgui.gui.display.resultsDisplay.LoggerResultDisplay;
import newgui.gui.display.resultsDisplay.ResultsDisplay;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import tools.StringUtilities;

/**
 * The ResultsFile is a large xml-formatted DataFile that contains multiple top-level elements
 * describing various aspects of the results of an analysis - including charts from the loggers,
 *  properties of the run itself (run length, completion state, etc), and traces of likelihoods and parameter values 
 * @author brendano
 *
 */
public class ResultsFile extends XMLDataFile {
	
	public static final String MODIFIER_SUMMARY = "mod.summary";
	public static final String MODIFIER_INFO = "mod.info";
	public static final String MODIFIER_LABEL = "mod.label";
	public static final String MODIFIER_CLASS = "mod.class";
	public static final String MODIFIER_CALLS = "mod.calls";
	public static final String MODIFIER_RATE = "mod.rate";
	
	
	public static final String MCMC_RUNLENGTH = "run.length";
	public static final String MCMC_PROPOSED = "states.proposed";
	public static final String MCMC_ACCEPTED = "states.accepted";
	public static final String MCMC_ACCEPTEDRATIO = "acceptance.ratio";
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
	public void addAllResults(ExecutingChain chain, ACGDocument acgDoc, MemoryStateLogger memLogger) throws XMLConversionError {
		Map<String, String> propsMap = new HashMap<String, String>();
		propsMap.put(MCMC_RUNLENGTH, StringUtilities.formatWithCommas( chain.getTotalRunLength()));
		
		
		Element memLoggerEl = StateElementConverter.createElement(memLogger, doc);
		doc.getDocumentElement().appendChild(memLoggerEl);
		
		DecimalFormat smallFormatter = new DecimalFormat("00.00");
		List<String> mcLabels = acgDoc.getLabelForClass(MCMC.class);
		if (mcLabels.size()>0) {
			try {
				MCMC mc = (MCMC) acgDoc.getObjectForLabel(mcLabels.get(0));
				propsMap.put(MCMC_PROPOSED, StringUtilities.formatWithCommas( mc.getStatesProposed()));
				propsMap.put(MCMC_ACCEPTED,  StringUtilities.formatWithCommas( mc.getStatesAccepted()));
				propsMap.put(MCMC_CHAINCOUNT, "" + chain.getChainCount());
				propsMap.put(MCMC_THREADCOUNT, "" + chain.getThreadCount());
				propsMap.put(MCMC_ACCEPTEDRATIO, "" + smallFormatter.format((100.0*mc.getStatesAccepted() / mc.getStatesProposed())));
				
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
			propsMap.put(MCMC_STARTTIME, formatDate(startTime));
		else 
			propsMap.put(MCMC_STARTTIME, "unknown");
		
		
		Date endTime = chain.getEndTime();
		if (endTime != null)
			propsMap.put(MCMC_ENDTIME, formatDate(endTime));
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
					
					LoggerElementConverter converter = LoggerConverterFactory.getConverter(logger.getClass());
					Element loggerEl = converter.createElement(doc, logger, label);
					doc.getDocumentElement().appendChild(loggerEl);
					
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
		
		
		//find all modifiers
		Element modifierSummaryEl = doc.createElement(MODIFIER_SUMMARY);
		doc.getDocumentElement().appendChild(modifierSummaryEl);
		for(String modLabel : acgDoc.getLabelForClass(AbstractModifier.class)) {
			try {
				AbstractModifier mod = (AbstractModifier) acgDoc.getObjectForLabel(modLabel);
				attachModifierInfo(mod, modLabel, modifierSummaryEl);		
				
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
	
	
	public static String formatDate(Date date) {
		String formatStr = "k:m:ss MMMM d, yyyy";
		SimpleDateFormat dateFormat = new SimpleDateFormat(formatStr);		
		return dateFormat.format(date);
	}
	
	/**
	 * Creates a new element that stores some information about the given modifier, and attaches
	 * it to the parent element provided
	 * @param mod
	 * @param label
	 * @param parent
	 */
	private void attachModifierInfo(AbstractModifier mod, String label, Element parent) {
		Element modInfoEl = doc.createElement(MODIFIER_INFO);

		modInfoEl.setAttribute(MODIFIER_CLASS, mod.getClass().getCanonicalName());
		modInfoEl.setAttribute(MODIFIER_LABEL, label);
		modInfoEl.setAttribute(MODIFIER_CALLS, StringUtilities.formatWithCommas(mod.getTotalCalls()));
		modInfoEl.setAttribute(MODIFIER_RATE, "" + mod.getTotalAcceptanceRatio());
		parent.appendChild(modInfoEl);
	}
	
	/**
	 * Obtain a list of all the series names that were recorded from the mem logger
	 * @return
	 */
	public List<String> getStateSeriesNames() {
		//Find the state logger element
		Element el = this.getTopLevelElement(StateElementConverter.XML_STATELOGGER);
		if (el == null)
			return null;
		else
			return StateElementConverter.getSeriesNames(el);
	}
	
	/**
	 * Obtain an XYSeriesInfo that describes the data series associated with the given series name
	 * @param seriesName
	 * @return
	 * @throws XMLConversionError
	 */
	public XYSeriesInfo getStateSeries(String seriesName) throws XMLConversionError {
		Element el = this.getTopLevelElement(StateElementConverter.XML_STATELOGGER);
		if (el == null)
			return null;
		else {
			XYSeriesInfo seriesInfo = StateElementConverter.getSeriesForName(el, seriesName);
			return seriesInfo;
		}
	}
	
	/**
	 * Obtain a list of ModInfo objects containing information about the modifiers
	 * stored in this file
	 * @return
	 */
	public List<ModInfo> getModifierData() {
		List<ModInfo> info = new ArrayList<ModInfo>();
		Element modSummaryEl = this.getTopLevelElement(MODIFIER_SUMMARY);
		NodeList children = modSummaryEl.getChildNodes();
		for(int i=0; i<children.getLength(); i++) {
			Node child = children.item(i);
			if (child instanceof Element) {
				Element modEl = (Element)child;
				//String label = modEl.getNodeName();
				String className = modEl.getAttribute(MODIFIER_CLASS);
				String calls = modEl.getAttribute(MODIFIER_CALLS);
				String ratioStr = modEl.getAttribute(MODIFIER_RATE);
				Double ratio = Double.parseDouble(ratioStr);
				ModInfo modInfo = new ModInfo();
				modInfo.label = modEl.getAttribute(MODIFIER_LABEL);
				modInfo.className = className;
				modInfo.calls = calls;
				modInfo.ratio = ratio;
				info.add(modInfo);
			}
		}
		
		return info;
	}
	

	
	public List<String> getLoggerLabels() throws XMLConversionError {
		List<Element> propLogEls = this.getTopLevelElements();
		List<String> loggers = new ArrayList<String>();
		for(Element loggerElement : propLogEls) {
			if (loggerElement.getNodeName().equals(AbstractLoggerConverter.LOGGER_ELEMENT))
				loggers.add(loggerElement.getAttribute(AbstractLoggerConverter.LOGGER_LABEL));
		}
		return loggers;
	}
	
	/**
	 * Returns the first top-level DOM element found that has a node name of LOGGER_ELEMENT and
	 * a label="label" attribute, where label is the given argument
	 * @param label
	 * @return
	 */
	public Element getLoggerElementForLabel(String label) {
		List<Element> propLogEls = this.getTopLevelElements();
		for(Element loggerElement : propLogEls) {
			if (loggerElement.getNodeName().equals(AbstractLoggerConverter.LOGGER_ELEMENT));
				String loggerLabel = loggerElement.getAttribute(AbstractLoggerConverter.LOGGER_LABEL);
				if (loggerLabel.equals(label))
					return loggerElement;
			
		}
		return null;
	}
	
	/**
	 * Obtain a string representing the class of the logger element with the given label
	 * @return
	 */
	protected String getClassForLoggerLabel(String label) {
		List<Element> propLogEls = this.getTopLevelElements();
		for(Element loggerElement : propLogEls) {
			if (loggerElement.getNodeName().equals(AbstractLoggerConverter.LOGGER_ELEMENT));
				String loggerLabel = loggerElement.getAttribute(AbstractLoggerConverter.LOGGER_LABEL);
				if (loggerLabel.equals(label)) {
					String className = loggerElement.getAttribute(AbstractLoggerConverter.LOGGER_CLASS);
					return className;
				}
					
		}
		return null;
	}
	
	/**
	 * Create a LoggerResultDisplay to display the contents of the logger with the given label
	 * @param label
	 * @return
	 * @throws XMLConversionError 
	 */
	public LoggerResultDisplay getDisplayForLogger(String label) throws XMLConversionError {
		String  loggerClass = this.getClassForLoggerLabel(label);
		try {
			Class clz = ClassLoader.getSystemClassLoader().loadClass(loggerClass);
			LoggerElementConverter converter = LoggerConverterFactory.getConverter(clz);
			if (converter == null) {
				System.out.println("No converter found for logger of class : " + clz.getCanonicalName());
				return null;
			}
				
			return converter.getLoggerFigure(this.getLoggerElementForLabel(label));
		}
		catch (ClassNotFoundException ex) {
			ErrorWindow.showErrorWindow(ex, "Could not load information from logger: " + label);
		}
		return null;
	}
	
//	public LoggerFigInfo getFigElementsForChartLabel(String label) throws XMLConversionError {
//		List<Element> propLogEls = this.getTopLevelElements();
//		for(Element loggerElement : propLogEls) {
//			String loggerLabel = loggerElement.getAttribute(LOGGER_LABEL);
//			if (loggerLabel != null && loggerLabel.equals(label)) {
//				LoggerFigInfo info = parseFigElements(loggerElement);
//				info.setTitle(loggerLabel);
//				return info;
//			}
//		}
//		
//		throw new XMLConversionError("No element found with class equal to " + label, null);
//	}
//	

	
	
	/**
	 * Container for some basic information about a Modifier
	 * @author brendan
	 *
	 */
	public class ModInfo {
		String label;
		String className;
		String calls;
		double ratio;
		
		public String getLabel() {
			return label;
		}
		
		public String getCalls() {
			return calls;
		}
		
		public double getAcceptRatio() {
			return ratio;
		}
		
	}


}
