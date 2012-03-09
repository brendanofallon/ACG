package newgui.gui.alignmentViewer;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import element.sequence.SequenceGroup;

/**
 * A small frame that provides some choices for insertion of new sequences as columns into an existing SG
 * @author brendan
 *
 */
public class PasteColumnsFrame extends JFrame {

	SGContentPanelDisplay parentDisplay;
	SequenceGroup newSeqs;
	JSpinner positionSpinner;
	JCheckBox matchNamesBox;
	JComboBox unmatchedBox;
	
	public PasteColumnsFrame(SGContentPanelDisplay display, SequenceGroup newSeqs) {
		super("Paste Columns");
		parentDisplay = display;
		this.newSeqs = newSeqs;
		JPanel mainPanel = new JPanel();
		mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 4, 5));
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		
		JPanel p1 = new JPanel();
		SpinnerNumberModel model = new SpinnerNumberModel(1, 1, display.currentSG.getMaxSeqLength(), 1);
		positionSpinner = new JSpinner(model);
		positionSpinner.setMinimumSize(new Dimension(80, 10));
		positionSpinner.setPreferredSize(new Dimension(80, 28));
		p1.add(new JLabel("Insert at position :"));
		p1.add(positionSpinner);
		p1.setAlignmentX(Component.RIGHT_ALIGNMENT);
		mainPanel.add(p1);
		
		matchNamesBox = new JCheckBox("Match sequence names");
		matchNamesBox.setSelected(true);
		matchNamesBox.setAlignmentX(Component.RIGHT_ALIGNMENT);
		mainPanel.add(matchNamesBox);
		
		
		String[] unmatches = {"Ignore", "As new row"};
		unmatchedBox = new JComboBox(unmatches);
		JPanel p2 = new JPanel();
		p2.setLayout(new FlowLayout(FlowLayout.LEFT));
		p2.add(new JLabel("Unmatched sequences:"));
		p2.add(unmatchedBox);
		mainPanel.add(p2);
		mainPanel.add(Box.createVerticalStrut(10));
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BorderLayout());
		
		JButton cancelButton = new JButton();
        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancel();
            }
        });
        buttonPanel.add(cancelButton, BorderLayout.WEST);
        
        JButton pasteButton = new JButton();
        pasteButton.setText("Paste");
        pasteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                paste();
            }
        });
		buttonPanel.add(pasteButton, BorderLayout.EAST);
		mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
		setLayout(new BorderLayout());
		add(mainPanel);
		
		
		this.getRootPane().setDefaultButton(pasteButton);
		setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
		pack();
		setVisible(true);
	}

	protected void paste() {
		parentDisplay.insertSGAsColumns(newSeqs, (Integer)positionSpinner.getValue()-1, matchNamesBox.isSelected(), unmatchedBox.getSelectedIndex()==0 );
		this.dispose();
	}

	protected void cancel() {
		this.dispose();
	}
}
