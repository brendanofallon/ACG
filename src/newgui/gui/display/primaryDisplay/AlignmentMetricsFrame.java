package newgui.gui.display.primaryDisplay;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;

import logging.StringUtils;

import newgui.UIConstants;
import newgui.datafile.AlignmentFile;
import newgui.gui.ViewerWindow;

import sequence.Alignment;
import sequence.AlignmentMetrics;

/**
 * A Frame that shows a few basic stats about an alignment
 * @author brendano
 *
 */
public class AlignmentMetricsFrame extends JFrame {

	private Alignment aln;
	Font font = UIConstants.sansFont.deriveFont(12f);
	
	public AlignmentMetricsFrame(Alignment aln) {
		super("Alignment metrics");
		this.aln = aln;
		initComponents();
		initializeValues();
		setLocationRelativeTo(ViewerWindow.getViewer());
		setPreferredSize(new Dimension(400, 300));
		this.setMinimumSize(new Dimension(400, 300));
		this.getRootPane().setDefaultButton(doneButton);
		pack();
	}

	/**
	 * Compute values and add them to main frame
	 */
	private void initializeValues() {
		addRow("Number of samples:", aln.getSequenceCount() + "");
		addRow("Sequence length:", aln.getSequenceLength() + "");
		addRow("Polymorphic columns:", AlignmentMetrics.getNonGapPolymorphicColumnCount(aln) + "");
		addRow("Gapped columns:", AlignmentMetrics.getNumGappedColumns(aln) + "");
		addRow("Nucleotide diversity (Pi):", StringUtils.format( AlignmentMetrics.getNucleotideDiversity(aln)) );
		addRow("Watternson's Theta :", StringUtils.format( AlignmentMetrics.getWattersonsTheta(aln)) );
		addRow("Tajimas D:", StringUtils.format( AlignmentMetrics.getTajimasD(aln) ));	
	}

	private void initComponents() {
		JPanel rootPane = new JPanel();
		rootPane.setLayout(new BorderLayout());
		rootPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		
		JPanel topPanel = new JPanel();
		String alnName = "Unknown alignment";
		AlignmentFile alnFile = aln.getSourceFile();
		if (alnFile != null) {
			alnName = alnFile.getSourceFile() == null ? "Unknown alignment" : alnFile.getSourceFile().getName().replace(".xml", "");
		}
		
		topPanel.add(new JLabel("Alignment metrics for alignment : " + alnName));
		rootPane.add(topPanel, BorderLayout.NORTH);
		
		mainPanel = new JPanel();
		mainPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		mainLayout = new GridLayout(0, 2);
		mainPanel.setLayout(mainLayout);
		rootPane.add(mainPanel, BorderLayout.CENTER);
		
		JPanel bottomPanel = new JPanel();
		doneButton= new JButton("Done");
		doneButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				closeFrame();
			}
			
		});
		bottomPanel.add(doneButton);
		rootPane.add(bottomPanel, BorderLayout.SOUTH);
		
		setContentPane(rootPane);
	}
	
	protected void closeFrame() {
		setVisible(false);
		this.dispose();
	}

	private void addRow(String label, String value) {
		JLabel lab = new JLabel(label);
		lab.setFont(font);
		lab.setAlignmentX(Component.RIGHT_ALIGNMENT);
		mainPanel.add(lab);
		
		JLabel valLab = new JLabel(value);
		valLab.setFont(font);
		mainPanel.add(valLab);
		
		rowsAdded++;
	}

	private int rowsAdded = 0;
	GridLayout mainLayout;
	JPanel mainPanel;
	JButton doneButton;
}
