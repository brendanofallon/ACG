package newgui.gui.display.resultsDisplay;

import gui.figure.series.XYSeries;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import logging.PopSizeLogger;
import logging.PropertyLogger;
import newgui.datafile.XMLConversionError;
import newgui.datafile.resultsfile.XYSeriesElementReader;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Converts XML to LoggerResultDisplay for PopSizeLogger loggers
 * @author brendan
 *
 */
public class PopSizeConverter extends AbstractLoggerConverter {

	@Override
	public LoggerResultDisplay getLoggerFigure(Element el) throws XMLConversionError {
		LoggerFigInfo info = this.parseFigElements(el);
		LoggerSeriesDisplay display = new LoggerSeriesDisplay();
		display.initialize(info);
		
		return display;
	}
	

	@Override
	public void addChildren(Document doc, Element el, PropertyLogger logger) {
		PopSizeLogger popSizeLogger = (PopSizeLogger)logger;
		double[] upper95s = popSizeLogger.getUpper95s();
		double[] means = popSizeLogger.getMeans();
		double[] lower95s = popSizeLogger.getLower95s();
		double[] sites = popSizeLogger.getBinPositions();
		
		List<Point2D> upperList = new ArrayList<Point2D>();
		for(int i=0; i<upper95s.length; i++) {
			upperList.add(new Point2D.Double(sites[i], upper95s[i]));
		}
		XYSeries upperSeries = new XYSeries(upperList, "Upper 95%");
		Element upperEl = XYSeriesElementReader.createElement(doc, upperSeries, Color.blue, 0.75f);
		upperEl.setAttribute(TMRCA_LABEL, TMRCA_UPPER95);
		el.appendChild(upperEl);
		
		List<Point2D> meanList = new ArrayList<Point2D>();
		for(int i=0; i<upper95s.length; i++) {
			meanList.add(new Point2D.Double(sites[i], means[i]));
		}
		XYSeries meanSeries = new XYSeries(meanList, "Mean TMRCA");
		Element meanEl = XYSeriesElementReader.createElement(doc, meanSeries, Color.blue, 1.5f);
		meanEl.setAttribute(TMRCA_LABEL, TMRCA_MEAN);
		el.appendChild(meanEl);
		
		List<Point2D> lowerList = new ArrayList<Point2D>();
		for(int i=0; i<lower95s.length; i++) {
			lowerList.add(new Point2D.Double(sites[i], lower95s[i]));
		}
		XYSeries lowerSeries = new XYSeries(lowerList, "Lower 95%");
		Element lowerEl = XYSeriesElementReader.createElement(doc, lowerSeries, Color.blue, 0.75f);
		lowerEl.setAttribute(TMRCA_LABEL, TMRCA_LOWER95);
		el.appendChild(lowerEl);
	}

}
