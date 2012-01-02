package newgui.gui;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.MultipleGradientPaint.CycleMethod;
import java.awt.RadialGradientPaint;
import java.awt.geom.Point2D;

import javax.swing.JPanel;

public class ViewerBackground extends JPanel {

	private static final Color darkColor = new Color(0.9f, 0.9f, 0.9f);
	private static final Color lightColor = new Color(1f, 1f, 1f);
    private final float radius = 350;
    private final float[] dist = {0.0f, 1.0f};
    private final Color[] colors = {darkColor, lightColor};
	
	public void paintComponent(Graphics g) {
		Graphics2D g2d = (Graphics2D)g;
		
		//GradientPaint gp = new GradientPaint(1f, 1f, new Color(0.9f, 0.9f, 0.9f), 1f, getHeight(), Color.white);
		
	     Point2D center = new Point2D.Float(Math.max(0, getWidth()-80), getHeight());
	     RadialGradientPaint gp =
	         new RadialGradientPaint(center, radius, dist, colors, CycleMethod.NO_CYCLE);
	     
		g2d.setPaint(gp);
		g2d.fillRect(0, 0, getWidth(), getHeight());
	}
}
