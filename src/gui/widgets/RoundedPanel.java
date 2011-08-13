package gui.widgets;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

/**
 * A panel with a rounded border a gradient background
 * @author brendan
 *
 */
public class RoundedPanel extends JPanel {

	private Color borderColor = Color.gray;
	private BasicStroke borderStroke = new BasicStroke(1.2f);
	private Color gradientBottom = new Color(0.75f, 0.75f, 0.75f);
	private Color gradientTop = new Color(0.95f, 0.95f, 0.95f);
	private GradientPaint gradient = null;
	
	public RoundedPanel() {
		this.setBorder(BorderFactory.createEmptyBorder(8, 4, 4, 4));
		this.setAlignmentY(CENTER_ALIGNMENT);
		this.setAlignmentX(CENTER_ALIGNMENT);
	}
	
	public void paintComponent(Graphics g) {
		Graphics2D g2d = (Graphics2D)g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
		
		super.paintComponent(g);
		
		if (gradient == null)
			gradient = new GradientPaint(new Point(10, 2), gradientTop, new Point(10, getHeight()-2), gradientBottom);
			
		g2d.setPaint(gradient);
		g2d.fillRoundRect(1, 1, getWidth()-2, getHeight()-2, 10, 10);
		
		g2d.setColor(borderColor);
		g2d.setStroke(borderStroke);
		g2d.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 10, 10);
		
	}
}
