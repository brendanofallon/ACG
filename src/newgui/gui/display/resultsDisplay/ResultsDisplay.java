package newgui.gui.display.resultsDisplay;

import gui.ErrorWindow;
import gui.figure.treeFigure.DrawableTree;
import gui.figure.treeFigure.SquareTree;
import gui.figure.treeFigure.TreeFigure;

import java.awt.BorderLayout;
import java.io.File;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;

import logging.BreakpointDensity;
import logging.ConsensusTreeLogger;
import logging.MarginalTreeLogger;
import logging.PropertyLogger;
import logging.RootHeightDensity;

import newgui.UIConstants;
import newgui.datafile.XMLConversionError;
import newgui.datafile.resultsfile.LoggerFigInfo;
import newgui.datafile.resultsfile.ResultsFile;
import newgui.gui.display.Display;
import newgui.gui.display.primaryDisplay.loggerVizualizer.AbstractLoggerViz;
import newgui.gui.display.primaryDisplay.loggerVizualizer.BPDensityViz;
import newgui.gui.display.primaryDisplay.loggerVizualizer.ConsensusTreeViz;
import newgui.gui.display.primaryDisplay.loggerVizualizer.TMRCAViz;
import newgui.gui.modelViews.loggerViews.DefaultLoggerView;
import newgui.gui.widgets.sideTabPane.SideTabPane;

public class ResultsDisplay extends Display {

	public ResultsDisplay() {
		initComponents();
	}
	
	private void initComponents() {
		this.setLayout(new BorderLayout());
		tabPane = new SideTabPane();
		this.add(tabPane, BorderLayout.CENTER);
		
		bottomPanel = new JPanel();
		bottomPanel.add(new JLabel("the bottom panel"));
		this.add(bottomPanel, BorderLayout.SOUTH);
	}

	public void showResultsFile(ResultsFile resFile) {
		File source = resFile.getSourceFile();
		if (source != null) {
			String title = resFile.getSourceFile().getName();
			title = title.replace(".xml", "");
			setTitle(title);
		}
		
		RunSummaryPanel summaryPanel = new RunSummaryPanel();
		tabPane.addTab("Run Summary", UIConstants.reload, summaryPanel);
		summaryPanel.initialize(resFile);
		
		
		StateLoggerPanel stateLoggerPanel = new StateLoggerPanel();
		stateLoggerPanel.initialize(resFile);
		tabPane.addTab("State", UIConstants.writeData, stateLoggerPanel);
		
		List<String> chartLabels = null;
		try {
			chartLabels = resFile.getChartLabels();
		} catch (XMLConversionError e) {
			ErrorWindow.showErrorWindow(e, "Could not read chart labels for results file");
			e.printStackTrace();
		}
		
		try {
			for(String chartLabel : chartLabels) {
				LoggerFigInfo figInfo = resFile.getFigElementsForChartLabel(chartLabel);
				addLoggerFigure(figInfo);
			}
		} catch (XMLConversionError e) {
			ErrorWindow.showErrorWindow(e, "Error reading chart information from file");
			e.printStackTrace();
		}

		
		try {
			List<String> treeLabels = resFile.getTreeLabels();
			for(String treeLabel : treeLabels) {
				String newickTree = resFile.getNewickForTreeElement(treeLabel);
				addTreeFigure(newickTree, treeLabel);
			}
		} catch (XMLConversionError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	private void addTreeFigure(String newick, String label) {
		TreeFigure treeFig = new TreeFigure();
		SquareTree drawableTree = new SquareTree(newick);
		treeFig.removeAllTrees();
		treeFig.addTree(drawableTree);
		if (treeFig.getScaleType()==DrawableTree.NO_SCALE_BAR)
			treeFig.setScaleType(DrawableTree.SCALE_AXIS);
		treeFig.repaint();
		
		tabPane.addTab(label, UIConstants.grayRightArrow, treeFig);
		repaint();
	}
	
	private void addLoggerFigure(LoggerFigInfo info) {
		LoggerResultDisplay loggerFig = new LoggerResultDisplay();
		loggerFig.initialize(info);
		tabPane.addTab(info.getTitle(), UIConstants.grayRightArrow, loggerFig);
		repaint();
	}

	SideTabPane tabPane;
	JPanel bottomPanel;
}
