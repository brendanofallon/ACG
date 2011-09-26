package gui.inputPanels.loggerConfigs;

import logging.ConsensusTreeLogger;
import logging.PropertyLogger;
import gui.document.ACGDocument;
import gui.inputPanels.Configurator.InputConfigException;

import org.w3c.dom.Element;

public class ConsensusTreeModel extends LoggerModel {
	
	//Site at which to consense trees
	int site = 0;

	public int getSite() {
		return site;
	}

	public void setSite(int site) {
		this.site = site;
	}

	@Override
	public Class getLoggerClass() {
		return ConsensusTreeLogger.class;
	}

	@Override
	public String getDefaultLabel() {
		return "ConsensusTree";
	}

	@Override
	protected Element getElement(ACGDocument doc) throws InputConfigException {
		if (argRef == null)
			throw new InputConfigException("ARG reference not set for ConsensusTree logger");
		
		Element el = createElement(doc, getModelLabel() + site, logging.ConsensusTreeLogger.class );
		el.setAttribute(PropertyLogger.FILENAME, getOutputFilename());
		el.setAttribute(PropertyLogger.FREQUENCY, "" + getLogFrequency());
		el.setAttribute(PropertyLogger.BURNIN, "" + getBurnin());
		el.setAttribute(ConsensusTreeLogger.XML_SITE, "" + site);
		
		Element argEl = doc.createElement( argRef.getModelLabel() );
		el.appendChild(argEl);
		return el;
	}

	@Override
	public void readElements(ACGDocument doc) throws InputConfigException {
		Element el = getSingleElementForClass(doc, ConsensusTreeLogger.class);
		readFilename(el);
		readBurnin(el);
		readFrequency(el);
		String echoStr = el.getAttribute(ConsensusTreeLogger.XML_SITE);
		if (echoStr != null && echoStr.length()>0) {
			try {
				Integer bins = Integer.parseInt(echoStr);
				setSite(bins);
			}
			catch (NumberFormatException nfe) {
				throw new InputConfigException("Could not parse site for element " + el.getNodeName());
			}
		}
	}

}
