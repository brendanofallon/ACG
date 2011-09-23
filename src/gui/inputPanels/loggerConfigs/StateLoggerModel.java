package gui.inputPanels.loggerConfigs;

import gui.document.ACGDocument;
import gui.inputPanels.Configurator.InputConfigException;
import logging.StateLogger;

import org.w3c.dom.Element;

public class StateLoggerModel extends LoggerModel {

	private boolean echoToScreen = false;

	public boolean getEchoToScreen() {
		return echoToScreen;
	}

	public void setEchoToScreen(boolean echoToScreen) {
		this.echoToScreen = echoToScreen;
	}

	@Override
	public void readElements(ACGDocument doc) throws InputConfigException {
		Element el = getSingleElementForClass(doc, StateLogger.class);
		readFilename(el);
		readBurnin(el);
		String echoStr = el.getAttribute(StateLogger.XML_ECHOTOSCREEN);
		if (echoStr != null && echoStr.length()>0) {
			Boolean echo = Boolean.parseBoolean(echoStr);
			if (echo != null)
				setEchoToScreen(echo);
		}
		
	}

	@Override
	protected Element getElement(ACGDocument doc) {
		Element el = createElement(doc, getModelLabel(), logging.StateLogger.class );
		el.setAttribute(StateLogger.XML_FILENAME, getOutputFilename());
		el.setAttribute(StateLogger.XML_FREQUENCY, "" + getLogFrequency());
		el.setAttribute(StateLogger.XML_ECHOTOSCREEN, "" + getEchoToScreen());
		return el;
	}

	@Override
	public String getDefaultLabel() {
		return "StateLogger";
	}

}
