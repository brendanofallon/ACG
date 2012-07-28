package newgui.gui.display;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Stroke;

import javax.swing.JPanel;

import java.awt.Graphics2D;

import newgui.UIConstants;


/**
 * A Display is the base class of objects that are shown in the DisplayPane, typically the big panel
 * the dominates 
 * @author brendan
 *
 */
public abstract class Display extends JPanel {

	private String title; //Label for this display, appears in tab at top of component
	
	public Display() {
		this.setOpaque(false);
	}

	/**
	 * Called when the user has attempted to close this display. If true is returned
	 * the display will close. If false, the closing will be aborted. 
	 * @return
	 */
	public boolean displayWouldLikeToClose() {
		return true;
	}
	
	/**
	 * Called when the display has been closed
	 */
	public void displayClosed() {
		
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
