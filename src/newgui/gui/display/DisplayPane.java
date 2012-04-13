package newgui.gui.display;


import java.awt.BorderLayout;
import java.awt.Graphics;

import javax.swing.JComponent;
import javax.swing.JPanel;

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
	
	
	public void addDisplay(Display display) {
		add(tabPane, BorderLayout.CENTER);
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
	
	public void removeDisplay(Display display) {
		tabPane.removeComponent(display);
		if (tabPane.getTabCount()==0) {
			this.remove(tabPane);
			revalidate();
			repaint();
		}
	}
	

}
