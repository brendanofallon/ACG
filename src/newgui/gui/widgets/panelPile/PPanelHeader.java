package newgui.gui.widgets.panelPile;


import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JPanel;

import newgui.UIConstants;
import newgui.gui.ViewerWindow;

public class PPanelHeader extends JPanel {
	
	private Font font = ViewerWindow.sansFont;
	private Color shadowColor = new Color(0.9f, 0.9f, 0.9f);
	private GradientPaint gradient = new GradientPaint(0, 0, Color.white, 0, 25, new Color(0.8f, 0.8f, 0.85f));
	private Color textColor = new Color(0.3f, 0.3f, 0.3f);
	private String text;
	private boolean moving = false;
	
	public PPanelHeader(String text) {
		this.text = text;
		setOpaque(true);
		this.setPreferredSize(new Dimension(150, 21));
		this.setMinimumSize(new Dimension(15, 21));
		this.setMaximumSize(new Dimension(15000, 21));
		font = font.deriveFont(13.0f);
	}
	
	public void setMoving(boolean isMoving) {
		this.moving = isMoving;
	}
	
	public void paintComponent(Graphics g) {
		Graphics2D g2d = (Graphics2D)g;

		//Plain background
		g2d.setPaint( UIConstants.componentBackground );
		g2d.fillRect(0, 0, getWidth()+1, getHeight()+1);
		
		if (!moving) {
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
			
			g2d.setPaint(gradient);
			g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
		}
	
		g2d.setColor(Color.LIGHT_GRAY);
		g2d.drawRoundRect(0, 1, getWidth()-1, getHeight(), 8, 8);
		
		g2d.setFont(font);
		g2d.setPaint(shadowColor);
		g2d.drawString(text, 13, getHeight()-4);
		
		g2d.setPaint(textColor);
		g2d.drawString(text, 12, getHeight()-3);
		
	}

}
