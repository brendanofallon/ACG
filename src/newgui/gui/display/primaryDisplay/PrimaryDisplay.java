package newgui.gui.display.primaryDisplay;

import gui.document.ACGDocument;
import gui.inputPanels.AnalysisModel;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import sequence.Alignment;

import newgui.gui.display.Display;


public class PrimaryDisplay extends Display {

	
	private static final String ALN_PREP = "Aln prep panel";
	private static final String ANALYSIS_PREP = "Analysis prep";
	private static final String ANALYSIS_DETAILS = "Analysis details";
	private static final String RUN_JOB = "Run joberroo";
	
	private JPanel mainPanel;
	private AlignmentPrepPanel alnPrepPanel;
	private AnalysisPrepPanel analPrepPanel;
	private AnalysisDetailsPanel analDetailsPanel;
	private RunningJobPanel runJobPanel;
	
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
		analPrepPanel = new AnalysisPrepPanel(this);
		analDetailsPanel = new AnalysisDetailsPanel(this);
		runJobPanel = new RunningJobPanel(this);
		mainPanel.add(alnPrepPanel, ALN_PREP);
		mainPanel.add(analPrepPanel, ANALYSIS_PREP);
		mainPanel.add(analDetailsPanel, ANALYSIS_DETAILS);
		mainPanel.add(runJobPanel, RUN_JOB);
		this.add(mainPanel, BorderLayout.CENTER);
		
	}
	
	public void showAnalysisPanel() {
		analPrepPanel.initialize(alnPrepPanel.getAlnSummaries());
		CardLayout cl = (CardLayout)(mainPanel.getLayout());
		cl.show(mainPanel, ANALYSIS_PREP);
	}
	
	public void showAnalysisDetails(AnalysisModel model) {
		analDetailsPanel.initialize(model);
		CardLayout cl = (CardLayout)(mainPanel.getLayout());
		cl.show(mainPanel, ANALYSIS_DETAILS);
	}

	public void showAlignmentPrepPanel() {
		CardLayout cl = (CardLayout)(mainPanel.getLayout());
		cl.show(mainPanel, ALN_PREP);
	}

	public void showJobPanel(ACGDocument acgDocument) {
		CardLayout cl = (CardLayout)(mainPanel.getLayout());
		runJobPanel.runJob(acgDocument);
		cl.show(mainPanel, RUN_JOB);		
	}
	
}
