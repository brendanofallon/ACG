package gui.inputPanels.loggerConfigs;

import logging.BreakpointDensity;
import logging.BreakpointLocation;
import logging.PropertyLogger;
import gui.document.ACGDocument;
import gui.inputPanels.Configurator.InputConfigException;

import org.w3c.dom.Element;

public class BPLocationModel extends LoggerModel {

	private int seqBins = 250;
	private int timeBins = 250;
	private Double maxDepth = null;
	
	public Double getMaxDepth() {
		return maxDepth;
	}

	public void setMaxDepth(Double maxDepth) {
		this.maxDepth = maxDepth;
	}

	public int getTimeBins() {
		return timeBins;
	}

	public void setTimeBins(int bins) {
		this.timeBins = bins;
	}
	public int getSeqBins() {
		return seqBins;
	}

	public void setSeqBins(int bins) {
		this.seqBins = bins;
	}

	@Override
	public String getDefaultLabel() {
		return "BreakpointLocation";
	}

	@Override
	protected Element getElement(ACGDocument doc) throws InputConfigException {
		if (argRef == null)
			throw new InputConfigException("ARG reference not set for BreakpointLocation");
		
		Element el = createElement(doc, getModelLabel(), BreakpointLocation.class );
		el.setAttribute(PropertyLogger.FILENAME, getOutputFilename());
		el.setAttribute(PropertyLogger.FREQUENCY, "" + getLogFrequency());
		el.setAttribute(BreakpointLocation.XML_SEQBINS, "" + seqBins);
		el.setAttribute(BreakpointLocation.XML_HEIGHTBINS, "" + timeBins);
		if (maxDepth != null)
			el.setAttribute(BreakpointLocation.XML_HEIGHT, "" + maxDepth);
		
		Element argEl = doc.createElement( argRef.getModelLabel() );
		el.appendChild(argEl);
		return el;
	}

	@Override
	public void readElements(ACGDocument doc) throws InputConfigException {
		Element el = getSingleElementForClass(doc, BreakpointLocation.class);
		readFilename(el);
		readBurnin(el);
		String echoStr = el.getAttribute(BreakpointLocation.XML_SEQBINS);
		if (echoStr != null && echoStr.length()>0) {
			try {
				Integer bins = Integer.parseInt(echoStr);
				setSeqBins(bins);
			}
			catch (NumberFormatException nfe) {
				throw new InputConfigException("Could not parse number of bins for element " + el.getNodeName());
			}
		}
		
		String timeBinStr = el.getAttribute(BreakpointLocation.XML_HEIGHTBINS);
		if (timeBinStr != null && timeBinStr.length()>0) {
			try {
				Integer bins = Integer.parseInt(timeBinStr);
				setTimeBins(bins);
			}
			catch (NumberFormatException nfe) {
				throw new InputConfigException("Could not parse number of bins for element " + el.getNodeName());
			}
		}
		
		
		String maxHeightStr = el.getAttribute(BreakpointLocation.XML_HEIGHT);
		if (maxHeightStr != null && maxHeightStr.length()>0) {
			try {
				Double height = Double.parseDouble(maxHeightStr);
				setMaxDepth(height);
			}
			catch (NumberFormatException nfe) {
				throw new InputConfigException("Could not parse number of bins for element " + el.getNodeName());
			}
		}
		else {
			setMaxDepth(null);
		}
	}

	@Override
	public Class getLoggerClass() {
		return BreakpointLocation.class;
	}

}
