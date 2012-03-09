package newgui.gui.filepanel;

import sequence.Alignment;

/**
 * An interface for objects that listen to events from a ChooseAlignmentPanel
 * @author brendan
 *
 */
public interface ChooseAlignmentListener {

	/**
	 * Called when an alignment has been selected by the user, may be null
	 * @return
	 */
	public void alignmentChosen(Alignment aln);
	
}
