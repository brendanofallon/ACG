package newgui.gui.display.primaryDisplay;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;
import newgui.alignment.Alignment;
import newgui.alignment.AlignmentSummary;


/**
 * A panel that contains one or more alignment summaries. This usually appears in the
 * upper half of the AlignmentPrepPanel. This also stores references to all of the alignments
 * we're currently tracking. 
 * @author brendan
 *
 */
public class AlignmentContainerPanel extends JPanel {

	private List<AlignmentSummary> alignments = new ArrayList<AlignmentSummary>();
	private int columns = 3; //Will wrap to a new row after this many columns have been added
	
	public AlignmentContainerPanel() {
		this.setOpaque(false);
		this.setLayout(new MigLayout());
	}
	
	private void layoutAlignments() {
		this.removeAll();
		int currentColumn = 0; 
		for(AlignmentSummary summary : alignments) {
			currentColumn++;
			if (currentColumn == columns) {
				this.add( summary, "wrap" );
				currentColumn = 0;
			}
			else {
				this.add( summary );
			}
			
		}
		revalidate();
		repaint();
	}
	
	/**
	 * Get all alignments tracked by this container
	 * @return
	 */
	public List<Alignment> getAlignments() {
		List<Alignment> alns = new ArrayList<Alignment>();
		for(AlignmentSummary sum : alignments) {
			alns.add(sum.getAlignment());
		}
		return alns;
	}
	
	/**
	 * Add a new alignment to those tracked by this container
	 * @param aln
	 * @param title
	 */
	public void addAlignment(Alignment aln, String title) {
		AlignmentSummary summary = new AlignmentSummary(title, aln);
		alignments.add(summary);
		layoutAlignments();
	}
	
	/**
	 * Removes the given alignment from this container
	 * @param aln
	 */
	public void removeAlignment(Alignment aln) {
		AlignmentSummary toRemove = null;
		for(AlignmentSummary summary : alignments) {
			if (summary.getAlignment() == aln) {
				toRemove = summary;
			}
		}
		if (toRemove != null) {
			alignments.remove(toRemove);
		}
		layoutAlignments();
	}
	
}
