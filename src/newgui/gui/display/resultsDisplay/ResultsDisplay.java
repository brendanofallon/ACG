package newgui.gui.display.resultsDisplay;

import gui.ErrorWindow;

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
		
		List<PropertyLogger> loggers;
		try {
			loggers = resFile.getPropertyLoggers();
			for(PropertyLogger logger : loggers ) {
				addLogger(logger);
			}
		} catch (XMLConversionError e) {
			ErrorWindow.showErrorWindow(e);
			e.printStackTrace();
		}

		
	}
	
	private void addLogger(PropertyLogger logger) {
		AbstractLoggerViz view = null;
		String tabName = "?";
		if (logger instanceof BreakpointDensity) {
			view = new BPDensityViz();
			tabName = "Breakpoints";
			System.out.println("Creating new BPDensity view...");
		}
		if (logger instanceof ConsensusTreeLogger) {
			view = new ConsensusTreeViz();
			tabName = "Consensus tree";
		}
		if (logger instanceof RootHeightDensity) {
			view = new TMRCAViz();
			tabName = "TMRCA";
		}
		view.initialize(logger, false);
		view.actionPerformed(null);
		view.repaint();
		tabPane.addTab(tabName, UIConstants.grayRightArrow, view);
		tabPane.selectTab(0);
		repaint();
	}

	SideTabPane tabPane;
	JPanel bottomPanel;
}
