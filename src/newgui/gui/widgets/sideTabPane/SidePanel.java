package newgui.gui.widgets.sideTabPane;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JPanel;

import newgui.UIConstants;

/**
 * Just draws the background for the tabs at the side of the SideTabPanel
 * @author brendano
 *
 */
public class SidePanel extends JPanel {

	public static final int rightPadding = 5;
	static final Color shadowColor = new Color(0.6f, 0.6f, 0.6f, 0.3f);
	static final Color lightShadowColor = new Color(0.9f, 0.9f, 0.9f, 0f);
	private SideTabPane ownerPane;
	
	public SidePanel(SideTabPane owner) {
		this.ownerPane = owner;
	}
	
	public void paint(Graphics g) {
		super.paint(g);
		
		Graphics2D g2d = (Graphics2D)g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
		
		SideTab tab = ownerPane.getSelectedTab();
		if (tab != null) {
			g2d.setColor(SideTab.lineColor);
			g2d.drawLine(getWidth()-1, 0, getWidth()-1, tab.getY());
			g2d.drawLine(getWidth()-1, tab.getY() + tab.getHeight(), getWidth()-1, getHeight());
			
		}
		
	}

	
	
}
