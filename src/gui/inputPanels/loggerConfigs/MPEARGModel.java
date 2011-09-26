package gui.inputPanels.loggerConfigs;

import logging.ConsensusTreeLogger;
import logging.MPEARG;
import logging.PropertyLogger;
import gui.document.ACGDocument;
import gui.inputPanels.Configurator.InputConfigException;

import org.w3c.dom.Element;

import sun.util.logging.resources.logging;

public class MPEARGModel extends LoggerModel {

	@Override
	public Class getLoggerClass() {
		return MPEARG.class;
	}

	@Override
	public String getDefaultLabel() {
		return "MPE_ARG";
	}

	@Override
	protected Element getElement(ACGDocument doc) throws InputConfigException {
		if (argRef == null)
			throw new InputConfigException("ARG reference not set for MPE ARG logger");
		
		Element el = createElement(doc, getModelLabel(), MPEARG.class );
		el.setAttribute(PropertyLogger.FILENAME, getOutputFilename());
		el.setAttribute(PropertyLogger.FREQUENCY, "" + getLogFrequency());
		el.setAttribute(PropertyLogger.BURNIN, "" + getBurnin());
		
		Element argEl = doc.createElement( argRef.getModelLabel() );
		el.appendChild(argEl);
		return el;
	}

	@Override
	public void readElements(ACGDocument doc) throws InputConfigException {
		Element el = getSingleElementForClass(doc, MPEARG.class);
		readFilename(el);
		readBurnin(el);
		readFrequency(el);
	}

}
