package newgui.gui.widgets.fancyTabPane;

import javax.swing.JComponent;

public interface FTabClosingListener {

	public boolean tabWouldLikeToClose(JComponent comp);
	
	public void tabClosed(JComponent comp);
	
}
