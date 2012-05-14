package newgui.gui.display.resultsDisplay;

import gui.figure.treeFigure.DrawableTree;
import gui.figure.treeFigure.SquareTree;
import gui.figure.treeFigure.TreeFigure;

import java.util.List;

import logging.ConsensusTreeLogger;
import logging.PropertyLogger;

import newgui.datafile.XMLConversionError;
import newgui.datafile.XMLDataFile;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class ConsensusTreeConverter extends AbstractLoggerConverter {

	@Override
	public LoggerResultDisplay getLoggerFigure(Element el) throws XMLConversionError {
		LoggerTreeDisplay treeDisplay = new LoggerTreeDisplay();
		String newick = getNewickForTreeElement(el);
		treeDisplay.setNewick(newick);
		return treeDisplay;
	}

	@Override
	public void addChildren(Document doc, Element el, PropertyLogger logger) {
		ConsensusTreeLogger treeLogger = (ConsensusTreeLogger)logger;
		String tree = treeLogger.getSummaryString();
		
		Element treeEl = doc.createElement(TREE);
		Node treeText = doc.createTextNode(tree);
		treeEl.appendChild(treeText);
		el.appendChild(treeEl);
	}

	public String getNewickForTreeElement(Element el) throws XMLConversionError {
		return XMLDataFile.getTextFromChild(el, TREE);
	}
}
