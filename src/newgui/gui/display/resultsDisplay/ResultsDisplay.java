package newgui.gui.display.resultsDisplay;

import gui.ErrorWindow;
import gui.figure.treeFigure.DrawableTree;
import gui.figure.treeFigure.SquareTree;
import gui.figure.treeFigure.TreeFigure;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.io.File;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import logging.BreakpointDensity;
import logging.ConsensusTreeLogger;
import logging.MarginalTreeLogger;
import logging.PropertyLogger;
import logging.RootHeightDensity;

import newgui.UIConstants;
import newgui.datafile.XMLConversionError;
import newgui.datafile.resultsfile.ResultsFile;
import newgui.gui.display.Display;
import newgui.gui.display.primaryDisplay.loggerVizualizer.AbstractLoggerViz;
import newgui.gui.display.primaryDisplay.loggerVizualizer.BPDensityViz;
import newgui.gui.display.primaryDisplay.loggerVizualizer.ConsensusTreeViz;
import newgui.gui.display.primaryDisplay.loggerVizualizer.TMRCAViz;
import newgui.gui.modelViews.loggerViews.DefaultLoggerView;
import newgui.gui.widgets.ToolbarPanel;
import newgui.gui.widgets.sideTabPane.SideTabPane;

public class ResultsDisplay extends Display {

	public ResultsDisplay() {
		initComponents();
	}
	
	private void initComponents() {
		this.setLayout(new BorderLayout());

		JPanel topPanel = new ToolbarPanel();
		topPanel.add(Box.createRigidArea(new Dimension(20, 20)));
		this.add(topPanel, BorderLayout.NORTH);
		
		tabPane = new SideTabPane();
		this.add(tabPane, BorderLayout.CENTER);
		
		bottomPanel = new JPanel();
		bottomPanel.add(Box.createRigidArea(new Dimension(20, 20)));
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
		
		List<String> loggerLabels = null;
		try {
			loggerLabels = resFile.getLoggerLabels();
		} catch (XMLConversionError e) {
			ErrorWindow.showErrorWindow(e, "Could not read chart labels for results file");
			e.printStackTrace();
		}

		
		for(String label : loggerLabels) {
			LoggerResultDisplay resultDisplay;
			try {
				resultDisplay = resFile.getDisplayForLogger(label);
				if (resultDisplay == null)
					System.out.println("Cant find display for " + label);
				else
					addLoggerDisplay(label, resultDisplay, resultDisplay.getIcon());
				
			} catch (XMLConversionError e) {
				e.printStackTrace();
				ErrorWindow.showErrorWindow(e);
			}
			
		}
		
	}
		
	private void addLoggerDisplay(String title, LoggerResultDisplay resultDisplay, ImageIcon icon) {
		tabPane.addTab(title, icon, resultDisplay);
		repaint();
	}


	SideTabPane tabPane;
	JPanel bottomPanel;
}
