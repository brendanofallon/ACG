package newgui.gui;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.MultipleGradientPaint.CycleMethod;
import java.awt.RadialGradientPaint;
import java.awt.geom.Point2D;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

import newgui.UIConstants;

public class ViewerBackground extends JPanel {

	private static final Color darkColor = new Color(0.9f, 0.9f, 0.9f);
	private static final Color lightColor = new Color(1f, 1f, 1f);
    private final float radius = 350;
    private final float[] dist = {0.0f, 1.0f};
    private final Color[] colors = {darkColor, lightColor};
	
    ImageIcon backgroundImage = UIConstants.getIcon("gui/icons/mainBackground.png");
    
    public ViewerBackground() {
    }
    
	public void paintComponent(Graphics g) {
		g.setColor(Color.white);
		g.fillRect(0, 0, getWidth(), getHeight());
		g.drawImage(backgroundImage.getImage(), getWidth()-backgroundImage.getIconWidth(), getHeight()-backgroundImage.getIconHeight(), null);
		//super.paintComponent(g);
	}
}
