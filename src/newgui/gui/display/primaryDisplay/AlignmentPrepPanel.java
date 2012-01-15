package newgui.gui.display.primaryDisplay;


import gui.document.ACGDocument;
import gui.inputPanels.AnalysisModel;
import gui.inputPanels.Configurator.InputConfigException;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.xml.transform.TransformerException;

import sequence.Alignment;
import sequence.BasicSequenceAlignment;
import sequence.Sequence;

import net.miginfocom.swing.MigLayout;
import newgui.UIConstants;
import newgui.alignment.AlignmentSummary;
import newgui.analysisTemplate.AnalysisTemplate;
import newgui.analysisTemplate.BasicAnalysis;
import newgui.gui.display.Display;
import newgui.gui.widgets.BorderlessButton;
import newgui.gui.widgets.TextButton;
import newgui.gui.widgets.VerticalTextButtons;


/**
 * The first panel that appears when an alignmetn is selected. This displays the alignment,
 * shows a few configuration options, and allows the user to transition to creating an
 * analysis based around this alignment
 * @author brendan
 *
 */
public class AlignmentPrepPanel extends JPanel {

	//private AlignmentView alignmentView;
	private AlignmentContainerPanel alnContainer;
	private JPanel bottomHalf;
	private PrimaryDisplay displayParent;

	public AlignmentPrepPanel(PrimaryDisplay displayParent) {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBackground(Display.defaultDisplayBackground);
		this.displayParent = displayParent;
		
		alnContainer = new AlignmentContainerPanel();
		alnContainer.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
		alnContainer.setBackground(Display.defaultDisplayBackground);
		alnContainer.setPreferredSize(new Dimension(500, 100));
		alnContainer.setMaximumSize(new Dimension(10000, 300));
		
		this.add(alnContainer);
		
		JPanel alnButtons = new JPanel();
		alnButtons.setBackground(Display.defaultDisplayBackground);
		alnButtons.setLayout(new BoxLayout(alnButtons, BoxLayout.X_AXIS));
		BorderlessButton addAlnButton = new BorderlessButton("Add alignment");
		alnButtons.add(Box.createHorizontalStrut(20));
		alnButtons.add(addAlnButton);
		alnButtons.add(Box.createHorizontalGlue());
		this.add(alnButtons);
		this.add(new JSeparator(JSeparator.HORIZONTAL));
		
		bottomHalf = new JPanel();
		bottomHalf.setBackground(Display.defaultDisplayBackground);
		
		bottomHalf.setMinimumSize(new Dimension(400, 400));
		bottomHalf.setPreferredSize(new Dimension(500, 300));
		
		bottomHalf.setLayout(new FlowLayout(FlowLayout.LEFT));
		bottomHalf.setBorder(BorderFactory.createEmptyBorder(6, 20, 0, 0));
	
		JPanel bottomLeftPanel  = new JPanel();
		JPanel bottomRightPanel = new JPanel();
		
		bottomRightPanel = new JPanel();
		bottomRightPanel.setOpaque(false);
		bottomLeftPanel.setOpaque(false);
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
		bottomButtonPanel.setOpaque(false);
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
		
		bottomHalf.add(bottomLeftPanel);
		bottomHalf.add(bottomRightPanel);
	
		this.add(bottomHalf);
		this.add(Box.createVerticalGlue());
	}

	
	/**
	 * Called when chooseButton has been pressed. We grab the current analysisTemplate,
	 * inject alignment data into it, and do.... something to be determined 
	 */
	protected void chooseCurrentAnalysis() {
		if (selectedTemplate != null) {
			AnalysisModel model = selectedTemplate.getModel();
			
			if (alnContainer.getAlignments().size()==0)
				throw new IllegalArgumentException("No alignments have been added");
			if (alnContainer.getAlignments().size()>1)
				throw new IllegalArgumentException("We currently cannot handle multiple alignments");
			
			List<Sequence> seqs = new ArrayList<Sequence>();
			Alignment aln = alnContainer.getAlignments().get(0);
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

	
	/**
	 * Add a new alignment to those tracked by this prep panel
	 * @param aln
	 * @param title
	 */
	public void addAlignment(Alignment aln, String title) {
		alnContainer.addAlignment(aln, title);
		revalidate();
		repaint();
	}
	
	public void removeAlignment(Alignment aln) {
		alnContainer.removeAlignment(aln);
	}
	
	public List<Alignment> getAlignments() {
		return alnContainer.getAlignments();
	}
	
	protected void showConfigAnalysisWindow() {
		displayParent.showAnalysisPanel();
	}

	public List<AlignmentSummary> getAlnSummaries() {
		return alnContainer.getSummaries();
	}
	
	
	private JPanel topPanel;
	private JTextArea analDescBox; //Shows descriptions of analysis types
	private AnalysisTemplate selectedTemplate = null;
	private BorderlessButton chooseButton;

}
