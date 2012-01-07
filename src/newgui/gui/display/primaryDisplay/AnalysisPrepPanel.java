package newgui.gui.display.primaryDisplay;

import gui.document.ACGDocument;
import gui.inputPanels.AnalysisModel;
import gui.inputPanels.Configurator.InputConfigException;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.xml.transform.TransformerException;

import sequence.Alignment;
import sequence.BasicSequenceAlignment;
import sequence.Sequence;

import net.miginfocom.swing.MigLayout;
import newgui.alignment.AlignmentSummary;
import newgui.analysisTemplate.AnalysisTemplate;
import newgui.analysisTemplate.BasicAnalysis;
import newgui.gui.widgets.BorderlessButton;

/**
 * A panel that allows the user to select from one of several AnalysisTemplate types, and to
 * create an analysis ready to submit to .... somewhere. 
 * @author brendan
 *
 */
public class AnalysisPrepPanel extends JPanel {

	//Stores all alignments / summaries that have been added
	private List<AlignmentSummary> alignmentSums = new ArrayList<AlignmentSummary>();
	
	private PrimaryDisplay displayParent;
	
	public AnalysisPrepPanel(PrimaryDisplay displayParent) {
		this.displayParent = displayParent;
		initComponents();
	}
	
	/**
	 * Clear all current alignments / summaries and add the given list
	 * to this panel
	 * @param alnSums
	 */
	public void initialize(List<AlignmentSummary> alnSums) {
		alignmentSums.clear();
		for(AlignmentSummary sum : alnSums) {
			topPanel.add(sum);
			alignmentSums.add(sum);
			
		}
		topPanel.revalidate();
		repaint();
	}
	
	
	private void initComponents() {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		topPanel = new JPanel();
		bottomPanel = new JPanel();
		
		topPanel.setMaximumSize(new Dimension(30000, 100));
		topPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		this.add(topPanel);
		
		this.add(new JSeparator(JSeparator.HORIZONTAL));
		this.add(bottomPanel);
		
		bottomPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		JPanel bottomLeftPanel  = new JPanel();
		JPanel bottomRightPanel = new JPanel();
		
		bottomRightPanel = new JPanel();
		bottomRightPanel.setLayout(new BorderLayout());
		bottomRightPanel.setPreferredSize(new Dimension(400, 300));
		analDescBox = new JTextArea();
		analDescBox.setOpaque(false);
		analDescBox.setPreferredSize(new Dimension(400, 100));
		analDescBox.setBorder(BorderFactory.createEmptyBorder(6, 6, 10, 6));
		analDescBox.setLineWrap(true);
		analDescBox.setWrapStyleWord(true);
		analDescBox.setEditable(false);
		//analDescBox.setFont(ViewerWindow.sansFont.deriveFont(16f));
		bottomRightPanel.add(analDescBox, BorderLayout.CENTER);
		JPanel bottomButtonPanel = new JPanel();
		bottomButtonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		chooseButton = new BorderlessButton("Choose");
		chooseButton.setEnabled(false);
		chooseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				chooseCurrentAnalysis();
			}
		});
		bottomButtonPanel.add(chooseButton);
		bottomRightPanel.add(bottomButtonPanel, BorderLayout.SOUTH);

		
		bottomPanel.add(bottomLeftPanel);
		bottomPanel.add(bottomRightPanel);
		
		bottomLeftPanel.setLayout(new MigLayout());
		
		BorderlessButton quickButton = new BorderlessButton("Quick analysis");
		quickButton.setPreferredSize(new Dimension(150, 50));
		bottomLeftPanel.add(quickButton, "wrap");

		BorderlessButton simpleButton = new BorderlessButton("Basic analysis");
		simpleButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				chooseButton.setEnabled(true);
				selectedTemplate = new BasicAnalysis();
				analDescBox.setText(selectedTemplate.getDescription() );
				revalidate();
				repaint();
			}
		});
		simpleButton.setPreferredSize(new Dimension(150, 50));
		bottomLeftPanel.add(simpleButton, "wrap");
		
		BorderlessButton demoButton = new BorderlessButton("Demographic analysis");
		demoButton.setPreferredSize(new Dimension(150, 50));
		bottomLeftPanel.add(demoButton, "wrap");
		
		BorderlessButton wackoButton = new BorderlessButton("Wacko analysis");
		wackoButton.setPreferredSize(new Dimension(150, 50));
		bottomLeftPanel.add(wackoButton, "wrap");
		
	}
	
	
	/**
	 * Called when chooseButton has been pressed. We grab the current analysisTemplate,
	 * inject alignment data into it, and do.... something to be determined 
	 */
	protected void chooseCurrentAnalysis() {
		if (selectedTemplate != null) {
			AnalysisModel model = selectedTemplate.getModel();
			if (alignmentSums.size()==0)
				throw new IllegalArgumentException("No alignments have been added");
			if (alignmentSums.size()>1)
				throw new IllegalArgumentException("We currently cannot handle multiple alignments");
			
			List<Sequence> seqs = new ArrayList<Sequence>();
			Alignment aln = alignmentSums.get(0).getAlignment();
			for(int i=0; i<aln.getSequenceCount(); i++) {
				seqs.add( aln.getSequence(i) );
			}
			BasicSequenceAlignment basicAln = new BasicSequenceAlignment(seqs);
			
			model.setAlignment( basicAln );
			try {
				ACGDocument doc = model.getACGDocument();
				String xmlStr = doc.getXMLString();
				System.out.println(xmlStr);
			} catch (InputConfigException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (TransformerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			//Switch showing panel to the 'analysis details' panel
			displayParent.showAnalysisDetails(model);
			
		}
	}


	private JPanel topPanel;
	private JPanel bottomPanel;
	private JTextArea analDescBox; //Shows descriptions of analysis types
	private AnalysisTemplate selectedTemplate = null;
	private BorderlessButton chooseButton;
}
