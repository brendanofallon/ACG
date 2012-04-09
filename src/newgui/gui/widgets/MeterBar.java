package newgui.gui.widgets;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JPanel;

/**
 * A colored bar like a progress bar but whose length doesn't usually change
 * @author brendan
 *
 */
public class MeterBar extends JPanel {

	private double value = 0.50; //Amount of bar to fill 
	private Color barColor = Color.blue;
	private Color topColor = Color.DARK_GRAY;
	private Color bottomColor = Color.gray;
	private Color lightColor = new Color(1f, 1f, 1f, 0.4f);
	private Color transparentColor = new Color(1f, 1f, 1f, 0.0f);
	
	public void setValue(double val) {
		this.value = val;
		if (value > 1)
			value = 1.0;
		repaint();
	}
	
	public void setBarColor(Color col) {
		this.barColor = col;
		repaint();
	}
	
	
	public void paintComponent(Graphics g) {
		Graphics2D g2d = (Graphics2D)g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
		

		//paint background
		GradientPaint gp = new GradientPaint(1, 1, topColor, 1, getHeight(), bottomColor);
		g2d.setPaint(gp);
		g2d.fillRoundRect(1, 1, getWidth()-2, getHeight()-2, 6, 6);
		g2d.setColor(Color.gray);
		g2d.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 6, 6);
		
		//now paint the bar
		g2d.setColor(barColor);
		int barLengthPixels = (int)Math.min(getWidth()-2, getWidth()*value);
		g2d.fillRoundRect(2, 2, barLengthPixels, getHeight()-3, 5, 5);
		
		gp = new GradientPaint(1, 1, lightColor, 1, getHeight(), transparentColor);
		g2d.setPaint(gp);
		g2d.fillRoundRect(2, 2, barLengthPixels, getHeight()-3, 10, 10);
	}
}
