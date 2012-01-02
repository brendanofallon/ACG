package newgui.gui.display.primaryDisplay;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import newgui.alignment.Alignment;
import newgui.gui.display.Display;


public class PrimaryDisplay extends Display {

	
	private static final String ALN_PREP = "Aln prep panel";
	private static final String ANALYSIS_PREP = "Analysis prep";
	private static final String RUN_JOB = "Run joberroo";
	
	private JPanel mainPanel;
	private AlignmentPrepPanel alnPrepPanel;
	private AnalysisPrepPanel analPrepPanel;
	private RunJobPanel runJobPanel;
	
	public PrimaryDisplay() {
		
		initComponents();
	
		 CardLayout cl = (CardLayout)(mainPanel.getLayout());
		 cl.show(mainPanel, ALN_PREP);
		 repaint();
	}
	
	public void addAlignment(Alignment aln, String title) {
		alnPrepPanel.addAlignment(aln, title);
	}
	
	
	private void initComponents() {
		setLayout(new BorderLayout());
		mainPanel = new JPanel();
		
		mainPanel.setLayout(new CardLayout());
		
		alnPrepPanel = new AlignmentPrepPanel(this);
		analPrepPanel = new AnalysisPrepPanel();
		runJobPanel = new RunJobPanel();
		mainPanel.add(alnPrepPanel, ALN_PREP);
		mainPanel.add(analPrepPanel, ANALYSIS_PREP);
		mainPanel.add(runJobPanel, RUN_JOB);
		this.add(mainPanel, BorderLayout.CENTER);
		
	}
	
	public void showAnalysisPanel() {

		analPrepPanel.initialize(alnPrepPanel.getAlnSummaries());
		CardLayout cl = (CardLayout)(mainPanel.getLayout());
		cl.show(mainPanel, ANALYSIS_PREP);
	}
	
}
