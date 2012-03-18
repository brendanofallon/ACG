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
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
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
import javax.swing.JSplitPane;
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
import newgui.datafile.AlignmentFile;
import newgui.gui.alignmentViewer.ColumnSelectionFrame;
import newgui.gui.alignmentViewer.SGContentPanel;
import newgui.gui.alignmentViewer.rowPainters.AG_CT_RowPainter;
import newgui.gui.alignmentViewer.rowPainters.FrequencyRowPainter;
import newgui.gui.alignmentViewer.rowPainters.GC_AT_RowPainter;
import newgui.gui.display.Display;
import newgui.gui.filepanel.ChooseAlignmentListener;
import newgui.gui.filepanel.ChooseAlignmentPanel;
import newgui.gui.filepanel.InputFilesManager;
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

	private JPanel alnPanel;
	private SGContentPanel contentPanel;
	private JPanel bottomHalf;
	private PrimaryDisplay displayParent;
	private JScrollPane sgScrollPane;
	private JComboBox colorSchemeBox;
	
	public AlignmentPrepPanel(PrimaryDisplay displayParent) {
		this.displayParent = displayParent;
		initComponents();
	}
	
	/**
	 * Switch coloring scheme to that in the colorSchemeBox
	 */
	protected void updateColorScheme() {
		if (colorSchemeBox.getSelectedItem().toString().equals(GC_AT_RowPainter.getIdentifier())) {
			contentPanel.setRowPainter(new GC_AT_RowPainter(contentPanel.getAlignment() ));
			contentPanel.repaint();
		}
		
		if (colorSchemeBox.getSelectedItem().toString().equals(FrequencyRowPainter.getIdentifier())) {
			contentPanel.setRowPainter(new FrequencyRowPainter(contentPanel.getAlignment() ));
			contentPanel.repaint();
		}
		
		if (colorSchemeBox.getSelectedItem().toString().equals(AG_CT_RowPainter.getIdentifier())) {
			contentPanel.setRowPainter(new AG_CT_RowPainter(contentPanel.getAlignment() ));
			contentPanel.repaint();
		}
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
	
	/**
	 * Called when the value of the zoom slider has been changed by the user
	 */
	protected void zoomValueChanged() {
		int val = (int)Math.round(zoomSlider.getValue()/10.0)+1;
		contentPanel.setColumnWidth(val);
		sgScrollPane.getHorizontalScrollBar().setUnitIncrement(Math.max(5, contentPanel.getColumnWidth()));
	}
	
	/**
	 * Obtain the current alignment 
	 * @return
	 */
	public Alignment getAlignment() {
		return contentPanel.getAlignment();
	}
	
	/**
	 * Create various GUI elements
	 */
	private void initComponents() {
		setLayout(new BorderLayout());
		setBackground(Display.defaultDisplayBackground);		
		
		contentPanel = new SGContentPanel();
		sgScrollPane = new JScrollPane( contentPanel );
		sgScrollPane.setViewportBorder(BorderFactory.createEmptyBorder());
		sgScrollPane.setBorder(BorderFactory.createEmptyBorder());
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
		
		
		JPanel topPanel = new JPanel();
		topPanel.setBorder(BorderFactory.createEmptyBorder(3, 4, 3, 4));
		topPanel.setOpaque(false);
		topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));
		alnPanel.add(topPanel, BorderLayout.NORTH);
		
		BorderlessButton addAlnButton = new BorderlessButton("Choose alignment");
		addAlnButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showAlignmentChoicePanel();
			}
		});
		
		topPanel.add(addAlnButton);
		topPanel.add(Box.createHorizontalStrut(10));
		
		BorderlessButton saveAlnButton = new BorderlessButton(UIConstants.saveGrayButton);
		saveAlnButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveAlignment();
			}
		});
		
		
		topPanel.add(saveAlnButton);
		topPanel.add(Box.createHorizontalGlue());

		BorderlessButton selectColsButton = new BorderlessButton("Select Cols");
		selectColsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showColumnSelectionFrame();
			}
		});
		topPanel.add(selectColsButton);
		
		BorderlessButton maskButton = new BorderlessButton("Mask");
		topPanel.add(maskButton);
		maskButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				maskSelectedColumns();
			}
		});

		BorderlessButton clearMaskButton = new BorderlessButton("Clear Mask");
		topPanel.add(clearMaskButton);
		clearMaskButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				clearAlignmentMask();
			}
		});
		
		String[] colorSchemes = new String[]{GC_AT_RowPainter.getIdentifier(), AG_CT_RowPainter.getIdentifier(), FrequencyRowPainter.getIdentifier()};
		colorSchemeBox = new JComboBox(colorSchemes);
		colorSchemeBox.setFont( colorSchemeBox.getFont().deriveFont(11.0f));
		colorSchemeBox.setPreferredSize(new Dimension(100, 28));
		colorSchemeBox.setMaximumSize(new Dimension(100, 30));
		colorSchemeBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				updateColorScheme();
			}
		});
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
		
		bottomHalf = new JPanel();
		bottomHalf.setBackground(Display.defaultDisplayBackground);
		
		//bottomHalf.setMinimumSize(new Dimension(400, 400));
		//bottomHalf.setPreferredSize(new Dimension(500, 300));
		
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
	
		
		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, alnPanel, bottomHalf);
		splitPane.setBorder(null);
		splitPane.setEnabled(true);
		this.add(splitPane, BorderLayout.CENTER);		
	}
	
	/**
	 * Open a dialog allowing the user to choose which columns to select
	 */
	protected void showColumnSelectionFrame() {
		ColumnSelectionFrame columnSelectionFrame = new ColumnSelectionFrame(contentPanel);
		columnSelectionFrame.setVisible(true);
	}

	/**
	 * Mask (revesibly convert to N) the given columns
	 */
	protected void maskSelectedColumns() {
		if (contentPanel.getNumSelectedColumns() > 0) {
			contentPanel.getAlignment().getMask().addColumns(contentPanel.getSelectedColumns());
			contentPanel.repaint();
		}
	}
	
	/**
	 * Remove the alignment mask
	 */
	protected void clearAlignmentMask() {
		contentPanel.getAlignment().getMask().clearMask();
		contentPanel.repaint();
	}

	protected void saveAlignment() {
		AlignmentFile source = contentPanel.getAlignment().getSourceFile();
		String name = "(enter file name)";
		if (source != null) {
			name = source.getSourceFile().getName().replace(".xml", "");
		}
		InputFilesManager.getManager().saveAlignment(contentPanel.getAlignment(), name);
	}

	private JPanel topPanel;
	private JTextArea analDescBox; //Shows descriptions of analysis types
	private AnalysisTemplate selectedTemplate = null;
	private BorderlessButton chooseButton;
	private JSlider zoomSlider;
	
}
