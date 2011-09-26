package gui.inputPanels.loggerConfigs;

import logging.BreakpointDensity;
import logging.PropertyLogger;
import logging.RootHeightDensity;

import org.w3c.dom.Element;

import gui.document.ACGDocument;
import gui.inputPanels.Configurator;
import gui.inputPanels.Configurator.InputConfigException;


public class RootHeightModel extends LoggerModel {

	private int bins = 500;
	
	public int getBins() {
		return bins;
	}

	public void setBins(int bins) {
		this.bins = bins;
	}
	
	@Override
	public Class getLoggerClass() {
		return RootHeightDensity.class;
	}

	@Override
	public String getDefaultLabel() {
		return "MarginalTMRCA";
	}

	@Override
	protected Element getElement(ACGDocument doc) throws InputConfigException {
		if (argRef == null)
			throw new InputConfigException("ARG reference not set for BreakpointDensity");
		
		Element el = createElement(doc, getModelLabel(), logging.RootHeightDensity.class );
		el.setAttribute(PropertyLogger.FILENAME, getOutputFilename());
		el.setAttribute(PropertyLogger.FREQUENCY, "" + getLogFrequency());
		el.setAttribute(PropertyLogger.BURNIN, "" + getBurnin());
		el.setAttribute(BreakpointDensity.BINS, "" + bins);
		
		Element argEl = doc.createElement( argRef.getModelLabel() );
		el.appendChild(argEl);
		return el;
	}

	@Override
	public void readElements(ACGDocument doc) throws InputConfigException {
		Element el = getSingleElementForClass(doc, RootHeightDensity.class);
		readFilename(el);
		readBurnin(el);
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
