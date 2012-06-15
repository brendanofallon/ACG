package newgui.gui.display.resultsDisplay;

import logging.BreakpointDensity;
import logging.PropertyLogger;
import math.Histogram;
import newgui.datafile.XMLConversionError;
import newgui.datafile.resultsfile.HistogramElementReader;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Converter for BPDensity loggers
 * @author brendan
 *
 */
public class BPDensityConverter extends AbstractLoggerConverter {

	@Override
	public LoggerResultDisplay getLoggerFigure(Element el) throws XMLConversionError {
		LoggerFigInfo info = this.parseFigElements(el);
		info.setTitle("Density of recombinations");
		LoggerSeriesDisplay display = new LoggerSeriesDisplay();
		display.initialize(info);
		
		return display;
	}

	@Override
	public void addChildren(Document doc, Element el, PropertyLogger logger) {
		BreakpointDensity bpDensity = (BreakpointDensity)logger;
		Histogram histo = bpDensity.getHistogram();
		Element histoEl = HistogramElementReader.createHistogramElement(doc, histo);
		el.appendChild(histoEl);		
	}

}
