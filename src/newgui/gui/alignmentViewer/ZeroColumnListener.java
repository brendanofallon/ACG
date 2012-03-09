package newgui.gui.alignmentViewer;

import element.sequence.*;

/**
 * Interface for objects that listen to changes in the 'zero column', which is the column displayed as #0 
 * in the sgDisplay display 
 * @author brendan
 *
 */
public interface ZeroColumnListener {
	
	public void zeroColumnChanged(int newCol);
	
}
