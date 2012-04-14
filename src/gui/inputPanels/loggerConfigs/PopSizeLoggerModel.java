package gui.inputPanels.loggerConfigs;

import gui.document.ACGDocument;
import gui.inputPanels.Configurator.InputConfigException;
import logging.BreakpointDensity;
import logging.PopSizeLogger;
import logging.PropertyLogger;

import org.w3c.dom.Element;

public class PopSizeLoggerModel extends LoggerModel {

	private int bins = 250;
	
	public int getBins() {
		return bins;
	}

	public void setBins(int bins) {
		this.bins = bins;
	}
	
	@Override
	public Class getLoggerClass() {
		return PopSizeLogger.class;
	}

	@Override
	public String getDefaultLabel() {
		return "Population size";
	}

	@Override
	protected Element getElement(ACGDocument doc) throws InputConfigException {
		if (argRef == null)
			throw new InputConfigException("ARG reference not set for PopSizeLogger");
		
		Element el = createElement(doc, getModelLabel(), logging.PopSizeLogger.class );
		el.setAttribute(PropertyLogger.FILENAME, getOutputFilename());
		el.setAttribute(PropertyLogger.FREQUENCY, "" + getLogFrequency());
		el.setAttribute(PropertyLogger.BURNIN, "" + getBurnin());
		el.setAttribute(BreakpointDensity.BINS, "" + bins);
		el.setAttribute(PropertyLogger.LABEL, getModelLabel());
		Element argEl = doc.createElement( argRef.getModelLabel() );
		el.appendChild(argEl);
		return el;
	}

	@Override
	public void readElements(ACGDocument doc) throws InputConfigException {
		Element el = getSingleElementForClass(doc, PopSizeLogger.class);
		readFilename(el);
		readFrequency(el);
		readBurnin(el);
		readLabel(el);
		String echoStr = el.getAttribute(BreakpointDensity.BINS);
		if (echoStr != null && echoStr.length()>0) {
			try {
				Integer bins = Integer.parseInt(echoStr);
				setBins(bins);
			}
			catch (NumberFormatException nfe) {
				throw new InputConfigException("Could not parse number of bins for element " + el.getNodeName());
			}
		}
	}

}
