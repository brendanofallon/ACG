package newgui.gui.display.primaryDisplay;


import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JSeparator;

import newgui.alignment.Alignment;
import newgui.gui.display.Display;
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
		this.displayParent = displayParent;
		
		alnContainer = new AlignmentContainerPanel();
		alnContainer.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
		alnContainer.setBackground(Display.defaultDisplayBackground);
		alnContainer.setPreferredSize(new Dimension(500, 200));
		alnContainer.setMaximumSize(new Dimension(10000, 300));
		
		this.add(alnContainer);
		
		bottomHalf = new JPanel();
		bottomHalf.setBackground(Display.defaultDisplayBackground);
		
		bottomHalf.setMinimumSize(new Dimension(300, 200));
		bottomHalf.setPreferredSize(new Dimension(500, 300));
		
		bottomHalf.setLayout(new BoxLayout(bottomHalf, BoxLayout.X_AXIS));
		bottomHalf.setBorder(BorderFactory.createEmptyBorder(6, 20, 0, 0));
		VerticalTextButtons buttonGroup = new VerticalTextButtons();
		buttonGroup.setPadding(10);
		TextButton addAlnButton = new TextButton("Add alignment");
		buttonGroup.addTextButton(addAlnButton);
		TextButton confButton = new TextButton("Configure alignment");
		buttonGroup.addTextButton(confButton);
		TextButton startButton = new TextButton("Start analysis");
		startButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showConfigAnalysisWindow();
			}
		});
		buttonGroup.addTextButton(startButton);
		bottomHalf.add(buttonGroup);
		
		this.add(new JSeparator(JSeparator.HORIZONTAL));
		this.add(bottomHalf);
		this.add(Box.createVerticalGlue());
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
}
