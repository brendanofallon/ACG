package newgui.gui.widgets.sideTabPane;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

import newgui.UIConstants;
import newgui.gui.widgets.fancyTabPane.FTabPane;

public class SideTab extends JPanel {

	private boolean selected = false;
	private String text;
	private ImageIcon icon;
	private Font font = UIConstants.sansFont;
	
	public SideTab(String label, ImageIcon icon) {
		this.text = label;
		this.icon = icon;
		
		setMinimumSize(new Dimension(1, SideTabPane.tabHeight));
		setMaximumSize(new Dimension(32000, SideTabPane.tabHeight));
	}
	
	public String getLabel() {
		return text;
	}
	
	protected void setSelected(boolean selected) {
		this.selected = selected;
		repaint();
	}
	
	/**
	 * Returns true if this is the selected tab
	 * @return
	 */
	public boolean isSelected() {
		return selected;
	}
	
	public void paintComponent(Graphics g) {
		Graphics2D g2d = (Graphics2D)g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

	     
	     GradientPaint gp;
	     if (selected)
	    	 gp = new GradientPaint(2, 2, lightColor, 2, getHeight(), UIConstants.componentBackground);
	     else
	    	 gp = new GradientPaint(2, 2, lighterColor, 2, getHeight(), darkColor);

	     g.setColor(shadowColor);
	     g2d.setStroke(shadowStroke);
	     g2d.fillRoundRect(7, 5, getWidth(), getHeight(), 8, 8);
	     
	     g2d.setPaint(gp);
	     g.fillRoundRect(2, 2, getWidth()+10, getHeight()-3, 8, 8);
	     

	     g2d.setStroke(normalStroke);
	     g2d.setColor(Color.LIGHT_GRAY);
		 g.drawRoundRect(2, 2, getWidth()+10, getHeight()-4, 8, 8);

	     g.drawImage(icon.getImage(), Math.max(1, getWidth()/2 - icon.getIconWidth()/2), 4, null);

	     //Draw text
	     g.setFont(font);
	     int strWidth = g.getFontMetrics().stringWidth(text);
	     g.setColor(new Color(1f, 1f, 1f, 0.3f));
	     g.drawString(text, Math.max(1, getWidth()/2 - strWidth/2)+1, getHeight()-7+1);
	     
	     g.setColor(Color.DARK_GRAY);
	     g.drawString(text, Math.max(1, getWidth()/2 - strWidth/2), getHeight()-7);

	}
	
	
	public static final Color darkColor = new Color(0.80f, 0.80f, 0.80f);
	public static final Color lighterColor = new Color(0.90f, 0.90f, 0.90f);
	public static final Color lightColor = new Color(1f, 1f, 1f);
	public final static Color gray2 = new Color(250, 250, 250, 150);
	public final static Color shadowColor = new Color(0f, 0f, 0f, 0.18f);
	public	final static Color lineColor = new Color(200, 200, 200);
	public final static Stroke shadowStroke = new BasicStroke(2.1f);
	public final static Stroke highlightStroke = new BasicStroke(1.2f);
	public final static Stroke normalStroke = new BasicStroke(1.0f);


}
