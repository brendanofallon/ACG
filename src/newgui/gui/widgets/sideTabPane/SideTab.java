package newgui.gui.widgets.sideTabPane;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

import newgui.UIConstants;

public class SideTab extends JPanel {

	private boolean selected = false;
	private String text;
	private ImageIcon icon;
	private Font font = UIConstants.sansFont;
	
	public SideTab(String label, ImageIcon icon) {
		this.text = label;
		this.icon = icon;
		
		setMinimumSize(new Dimension(1, 50));
		setMaximumSize(new Dimension(32000, 50));
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
	
   	 	 g.setColor(SideTabPane.sidePanelBackground);
	     g.fillRect(0, 0, getWidth()+1, getHeight()+1);
	     
	     
	     if (selected) {
	    	 g.setColor(UIConstants.lightBackground);
	    	 g.fillRoundRect(0, 0, getWidth()+4, getHeight()-1, 14, 14);
	    	 g.setColor(Color.LIGHT_GRAY);
	    	 g.drawRoundRect(0, 0, getWidth()+4, getHeight()-1, 14, 14);
	     }
	     
	     
	     
	     g.drawImage(icon.getImage(), Math.max(1, getWidth()/2 - icon.getIconWidth()/2), 2, null);

	     g.setFont(font);
	     int strWidth = g.getFontMetrics().stringWidth(text);
	     g.setColor(new Color(1f, 1f, 1f, 0.2f));
	     g.drawString(text, Math.max(1, getWidth()/2 - strWidth/2)+1, getHeight()-10+1);
	     
	     g.setColor(Color.DARK_GRAY);
	     g.drawString(text, Math.max(1, getWidth()/2 - strWidth/2), getHeight()-10);

	     if (! selected) {
	    	 g.setColor(new Color(1f, 1f, 1f, 0.5f));
	    	 g.drawLine(2, getHeight()-1, getWidth()-2, getHeight()-1);
	    	 g.setColor(Color.LIGHT_GRAY);
	    	 g.drawLine(2, getHeight()-2, getWidth()-2, getHeight()-2);
	     }
	}
}
