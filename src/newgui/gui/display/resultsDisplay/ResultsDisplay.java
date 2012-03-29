package newgui.gui.display.resultsDisplay;

import java.awt.BorderLayout;
import java.io.File;

import javax.swing.JLabel;
import javax.swing.JPanel;

import newgui.datafile.resultsfile.ResultsFile;
import newgui.gui.display.Display;
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
		
	}
	
	SideTabPane tabPane;
	JPanel bottomPanel;
}
