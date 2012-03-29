package newgui.datafile.resultsfile;

import gui.figure.series.XYSeries;
import logging.BreakpointDensity;
import logging.PropertyLogger;

import newgui.datafile.XMLConversionError;
import newgui.datafile.XMLDataFile;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Handles the conversion of a BPDensityLogger into an XML DOM element and back
 * @author brendano
 *
 */
public class BPDensityConverter extends AbstractLoggerConverter {

	@Override
	public Element convertLoggerToElement(PropertyLogger logger, Document doc) throws XMLConversionError {
		Element el = doc.createElement(LOGGER_ELEMENT);
		el.setAttribute(LOGGER_CLASS, BreakpointDensity.class.getCanonicalName());
		BreakpointDensity bpDensity = (BreakpointDensity)logger;
		
		Element histogram = createHistogramElement(doc, bpDensity.getHistogram());
		el.appendChild(histogram);
		
		return null;
	}

	@Override
	public PropertyLogger convertElementToLogger(Element el) {
		BreakpointDensity bpDensity = new BreakpointDensity( ); 
		return null;
	}

}
