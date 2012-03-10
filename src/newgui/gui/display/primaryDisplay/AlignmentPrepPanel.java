package newgui.gui.display.primaryDisplay;


import gui.document.ACGDocument;
import gui.inputPanels.AnalysisModel;
import gui.inputPanels.Configurator.InputConfigException;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.xml.transform.TransformerException;

import sequence.Alignment;
import sequence.BasicSequenceAlignment;
import sequence.Sequence;

import net.miginfocom.swing.MigLayout;
import newgui.UIConstants;
import newgui.alignment.AlignmentSummary;
import newgui.analysisTemplate.AnalysisTemplate;
import newgui.analysisTemplate.BasicAnalysis;
import newgui.gui.alignmentViewer.SGContentPanel;
import newgui.gui.alignmentViewer.rowPainters.FrequencyRowPainter;
import newgui.gui.alignmentViewer.rowPainters.GC_AT_RowPainter;
import newgui.gui.display.Display;
import newgui.gui.filepanel.ChooseAlignmentListener;
import newgui.gui.filepanel.ChooseAlignmentPanel;
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
	private JPanel alnPanel;
	private SGContentPanel contentPanel;
	private JPanel bottomHalf;
	private PrimaryDisplay displayParent;
	private JScrollPane sgScrollPane;
	
	public AlignmentPrepPanel(PrimaryDisplay displayParent) {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBackground(Display.defaultDisplayBackground);
		this.displayParent = displayParent;
		
		contentPanel = new SGContentPanel();
		contentPanel.setMinimumSize(new Dimension(100, 500));
		sgScrollPane = new JScrollPane( contentPanel );
        JPanel corner1 = new JPanel();
        corner1.setBackground(Color.white);
        sgScrollPane.setCorner(JScrollPane.UPPER_RIGHT_CORNER, corner1);
        JPanel corner2 = new JPanel();
        corner2.setBackground(Color.white);
        sgScrollPane.setCorner(JScrollPane.LOWER_LEFT_CORNER, corner2);
        JPanel corner3 = new JPanel();
        corner3.setBackground(Color.white);
        sgScrollPane.setCorner(JScrollPane.UPPER_LEFT_CORNER, corner3);
        
		alnPanel = new JPanel();
		alnPanel.setOpaque(false);
		alnPanel.setLayout(new BorderLayout());
		alnPanel.add(sgScrollPane, BorderLayout.CENTER);
		
		this.add(alnPanel);
		
		JPanel topPanel = new JPanel();
		topPanel.setOpaque(false);
		topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));
		
		topPanel.add(Box.createHorizontalGlue());
		alnPanel.add(topPanel, BorderLayout.NORTH);
		
		String[] colorSchemes = new String[]{GC_AT_RowPainter.getIdentifier(), FrequencyRowPainter.getIdentifier()};
		JComboBox colorSchemeBox = new JComboBox(colorSchemes);
		topPanel.add(colorSchemeBox);
		topPanel.add(Box.createHorizontalGlue());
		
		zoomSlider = new JSlider();
		zoomSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				zoomValueChanged();
			}
		});
		zoomSlider.setPreferredSize(new Dimension(100, 20));
		zoomSlider.setMaximumSize(new Dimension(100, 50));
		zoomSlider.setToolTipText("Change column width");
		zoomSlider.setFont(new Font("Sans", Font.PLAIN, 0));
		zoomSlider.setPaintLabels(false);
		zoomSlider.setPaintTicks(false);
		topPanel.add(zoomSlider);
		
		JPanel alnButtons = new JPanel();
		alnButtons.setBackground(Display.defaultDisplayBackground);
		alnButtons.setLayout(new BoxLayout(alnButtons, BoxLayout.X_AXIS));
		BorderlessButton addAlnButton = new BorderlessButton("Choose alignment");
		addAlnButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showAlignmentChoicePanel();
			}
		});
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
	 * Displays a dialog that allows the user to choose a new Alignment
	 */
	protected void showAlignmentChoicePanel() {
		ChooseAlignmentPanel.chooseAlignment(new ChooseAlignmentListener() {
			public void alignmentChosen(Alignment aln) {
				if (aln != null)
					replaceAlignment(aln);
			}
		});
	}


	protected void replaceAlignment(Alignment aln) {
		contentPanel.setAlignment(aln);
		repaint();
	}


	/**
	 * Called when chooseButton has been pressed. We grab the current analysisTemplate,
	 * inject alignment data into it, and do.... something to be determined 
	 */
	protected void chooseCurrentAnalysis() {
		if (selectedTemplate != null) {
			AnalysisModel model = selectedTemplate.getModel();
			
//			if (alnContainer.getAlignments().size()==0)
//				throw new IllegalArgumentException("No alignments have been added");
//			if (alnContainer.getAlignments().size()>1)
//				throw new IllegalArgumentException("We currently cannot handle multiple alignments");
			
			List<Sequence> seqs = new ArrayList<Sequence>();
			Alignment aln = contentPanel.getAlignment();
			for(int i=0; i<aln.getSequenceCount(); i++) {
				seqs.add( aln.getSequence(i) );
			}
			BasicSequenceAlignment basicAln = new BasicSequenceAlignment(seqs);
			
			model.setAlignment( basicAln );
			
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
		contentPanel.setAlignment(aln);
		contentPanel.setToNaturalSize();
		sgScrollPane.getVerticalScrollBar().setUnitIncrement(contentPanel.getRowHeight());
		sgScrollPane.getHorizontalScrollBar().setUnitIncrement(Math.max(5, contentPanel.getColumnWidth()));
		sgScrollPane.setPreferredSize(new Dimension(100, 200));
		sgScrollPane.setMinimumSize(new Dimension(100, 200));
		revalidate();
		repaint();
	}
	
	protected void zoomValueChanged() {
		int val = (int)Math.round(zoomSlider.getValue()/10.0)+1;
		contentPanel.setColumnWidth(val);
		sgScrollPane.getHorizontalScrollBar().setUnitIncrement(Math.max(5, contentPanel.getColumnWidth()));
	}
	
	public Alignment getAlignment() {
		return contentPanel.getAlignment();
	}
	
//	protected void showConfigAnalysisWindow() {
//		displayParent.showAnalysisPanel();
//	}

//	public List<AlignmentSummary> getAlnSummaries() {
//		return alnContainer.getSummaries();
//	}
	
	
	private JPanel topPanel;
	private JTextArea analDescBox; //Shows descriptions of analysis types
	private AnalysisTemplate selectedTemplate = null;
	private BorderlessButton chooseButton;
	private JSlider zoomSlider;
	
}
