package newgui.gui.display.primaryDisplay;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JSeparator;

import net.miginfocom.swing.MigLayout;
import newgui.alignment.Alignment;
import newgui.alignment.AlignmentSummary;
import newgui.gui.widgets.BorderlessButton;


public class AnalysisPrepPanel extends JPanel {

	public AnalysisPrepPanel() {
		
		initComponents();
	}
	
	public void initialize(List<AlignmentSummary> alnSums) {
		for(AlignmentSummary sum : alnSums) {
			topPanel.add(sum);
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
		bottomPanel.add(bottomLeftPanel);
		bottomPanel.add(bottomRightPanel);
		
		bottomLeftPanel.setLayout(new MigLayout());
		
		BorderlessButton quickButton = new BorderlessButton("Quick analysis");
		quickButton.setPreferredSize(new Dimension(150, 50));
		bottomLeftPanel.add(quickButton, "wrap");

		BorderlessButton simpleButton = new BorderlessButton("Simple analysis");
		simpleButton.setPreferredSize(new Dimension(150, 50));
		bottomLeftPanel.add(simpleButton, "wrap");
		
		BorderlessButton demoButton = new BorderlessButton("Demographic analysis");
		demoButton.setPreferredSize(new Dimension(150, 50));
		bottomLeftPanel.add(demoButton, "wrap");
		
		BorderlessButton wackoButton = new BorderlessButton("Wacko analysis");
		wackoButton.setPreferredSize(new Dimension(150, 50));
		bottomLeftPanel.add(wackoButton, "wrap");
	}
	
	
	private JPanel topPanel;
	private JPanel bottomPanel;
}
