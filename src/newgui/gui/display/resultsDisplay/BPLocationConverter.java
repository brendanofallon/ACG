package newgui.gui.display.resultsDisplay;

import logging.BreakpointLocation;
import logging.PropertyLogger;
import newgui.datafile.XMLConversionError;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class BPLocationConverter extends AbstractLoggerConverter {

	public static final String minTreeDepth = "min.tree.depth";
	public static final String maxTreeDepth = "max.tree.depth";
	public static final String minSite = "min.site";
	public static final String maxSite = "max.site";
	public static final String depthBins = "depth.bins";
	public static final String seqBins = "seq.bins";


	
	
	@Override
	public LoggerResultDisplay getLoggerFigure(Element el)
			throws XMLConversionError {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addChildren(Document doc, Element el, PropertyLogger logger) {
		BreakpointLocation bpLogger = (BreakpointLocation)logger;
		double[][] matrix = new double[bpLogger.getSeqBins()][bpLogger.getDepthBins()];
		bpLogger.getDensities(matrix);
		
		el.setAttribute(maxTreeDepth, "" + bpLogger.getTreeHeight());
		el.setAttribute(minTreeDepth, "0.0");
		el.setAttribute(minSite, "0");
		el.setAttribute(maxSite, "" + bpLogger.getARGSites());

		Element matrixEl = createMatrixChild(doc, matrix);
		el.appendChild(matrixEl);
	}


}
