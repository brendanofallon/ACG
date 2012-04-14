package newgui.gui.widgets;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JPanel;

import newgui.UIConstants;

/**
 * A prettified panel with a vertical gradient
 * @author brendan
 *
 */
public class ToolbarPanel extends JPanel {

	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D)g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
	
	    GradientPaint gp;
	    gp = new GradientPaint(0, 0, transparentColor, 0, getHeight(), lighterColor);
	    g2d.setPaint(gp);
	    g2d.fillRect(0, 0, getWidth(), getHeight());
	    
	    g2d.setColor(lineColor);
	    g2d.drawLine(0, getHeight()-1, getWidth(), getHeight()-1);
	}
	
	
	static final Color transparentColor = new Color(1f, 1f, 1f, 0f);
	static final Color lighterColor = new Color(1f, 1f, 1f, 0.4f);
	static final Color lineColor = Color.LIGHT_GRAY;
	
}
