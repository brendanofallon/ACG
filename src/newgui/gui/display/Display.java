package newgui.gui.display;

import java.awt.Color;

import javax.swing.JPanel;


/**
 * A Display is the base class of objects that are shown in the DisplayPane, typically the big panel
 * the dominates 
 * @author brendan
 *
 */
public abstract class Display extends JPanel {

	private String title; //Label for this display, appears in tab at top of component
	public static final Color defaultDisplayBackground = new Color(0.95f, 0.95f, 0.99f);;
	
	public Display() {
		this.setBackground(defaultDisplayBackground);
	}
	
	/**
	 * Set title for this display, shouldn't be too long
	 * @param title
	 */
	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getTitle() {
		return title;
	}
	
}
