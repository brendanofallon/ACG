package gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

public class MonitorHeader extends JPanel {

	protected String text = "";
	protected String label = "";
	protected Font font = new Font("Sans", Font.PLAIN, 12);
	
	
	Color[] gradient = new Color[25];
	Color bottomLight = new Color(0.98f, 0.98f, 0.98f, 0.7f);
	Color bottomDark = new Color(0.7f, 0.7f, 0.7f, 0.4f);
	
	public MonitorHeader() {
		this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		this.setMinimumSize(new Dimension(10, 24));
		this.add(Box.createRigidArea(new Dimension(10, 20)));
		setBackground(Color.white);
		
		float fadeStart = 0.7f;
		float fadeEnd = 1.0f;
		for(int i=0; i<gradient.length; i++) {
			float c = fadeStart + (fadeEnd-fadeStart)*(1.0f-(float)i/(float)(gradient.length-1));
			gradient[i] = new Color(c, c, c, 0.6f);
		}
	}
	
	public void setText(String text) {
		this.text = text;
		repaint();
	}
	
	public void setLabel(String label) {
		this.label = label;
		repaint();
	}
	
	public void setFont(Font font) {
		this.font = font;
	}
	
	public void setFontSize(float size) {
		font = font.deriveFont(size);
		repaint();
	}
	
	public void paintComponent(Graphics g) {
		Graphics2D g2d = (Graphics2D)g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
		
		g2d.setColor(Color.white);
		g2d.fillRect(0, 0, getWidth(), getHeight());
		
		
		for(int i=Math.max(1, getHeight()-gradient.length); i<getHeight(); i++) {
			g2d.setColor(gradient[i]);
			g2d.drawLine(0, i, getWidth(), i);
		}
		
		int textX = Math.min(getWidth()/4, 20);
		int textY = getHeight()-4;
		
		g2d.setFont(font);
		
		//Text and shadow
		g2d.setColor(new Color(0.9f, 0.9f, 0.9f, 0.7f));		
		g2d.drawString(label + "  " + text, textX+1, textY+1);
		
		g2d.setColor(Color.black);
		g2d.drawString(label + "  " + text, textX, textY);
		
	}
	
}
