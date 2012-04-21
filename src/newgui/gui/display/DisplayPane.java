package newgui.gui.display;


import java.awt.BorderLayout;
import java.awt.Graphics;

import javax.swing.JComponent;
import javax.swing.JPanel;

import newgui.gui.widgets.fancyTabPane.FTabClosingListener;
import newgui.gui.widgets.fancyTabPane.FTabPane;

/**
 * This is the panel that houses and manages zero or more Displays. Currently, it's
 * just a pretty thin wrapper for an FTabPane
 * 
 * @author brendan
 *
 */
public class DisplayPane extends JPanel {

	private FTabPane tabPane = new FTabPane();
	
	public DisplayPane() {
		setOpaque(false);
		setLayout(new BorderLayout());
		
		tabPane.setOpaque(false);
	}
	
	
	public void addDisplay(final Display display) {
		add(tabPane, BorderLayout.CENTER);
		tabPane.addTabClosingListener(new FTabClosingListener() {
			@Override
			public boolean tabWouldLikeToClose(JComponent comp) {
				if (comp == display)
					return display.displayWouldLikeToClose();
				else
					return true;
			}

			@Override
			public void tabClosed(JComponent comp) {
				if (comp == display)
					display.displayClosed();
			}
		});
		tabPane.addComponent(display.getTitle(), display);
		revalidate();
		repaint();
	}
	
	public Display getDisplayForClass(Class displayClass) {
		for(int i=0; i<tabPane.getTabCount(); i++) {
			JComponent comp = tabPane.getComponentAtIndex(i);
			if (comp.getClass().equals( displayClass) && comp instanceof Display) {
				return (Display)comp;
			}
			else {
				System.out.println("Component with class : " + comp.getClass() + " is not a " + displayClass);
			}
		}
		return null;
	}
	
	/**
	 * Make the current display the 'selected' one, if it exists in this DisplayPane, if not no action is taken
	 * @param disp
	 */
	public void selectComponent(Display disp) {
		if (tabPane.contains(disp)) {
			tabPane.showComponent(disp);
		}
	}
	
	/**
	 * Attempt to close (remove) the given Display - the display may prevent the 
	 * removal through its displayWouldLikeToCloseMethod. Returns true if the 
	 * display was actually removed, false otherwise.  
	 * @param display
	 * @return True if display was actually closed / not aborted by user
	 */
	public boolean removeDisplay(Display display) {
		boolean closeOK = display.displayWouldLikeToClose();
		if (closeOK) {
			tabPane.removeComponent(display);
			display.displayClosed();
			if (tabPane.getTabCount()==0) {
				this.remove(tabPane);
				revalidate();
				repaint();
			}
		}
		
		return closeOK;
	}
	

}
