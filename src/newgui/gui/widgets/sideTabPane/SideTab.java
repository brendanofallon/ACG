package newgui.gui.widgets.sideTabPane;

import java.awt.BasicStroke;
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
		
		setMinimumSize(new Dimension(1, 60));
		setMaximumSize(new Dimension(32000, 60));
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
	
	     if (selected) {
	    	
	    	 g.setColor(UIConstants.lightBackground);
	    	 g.fillRoundRect(0, 0, getWidth()+4, getHeight()-1, 14, 14);
	    	 g.setColor(Color.LIGHT_GRAY);
	    	 g.drawRoundRect(0, 0, getWidth()+4, getHeight()-1, 14, 14);
	    	 
	    	 g2d.setStroke(new BasicStroke(1.45f));
	    	 g2d.setColor(new Color(0.5f, 0.5f, 0.5f, 0.3f));
	    	 g2d.drawRoundRect(0, 0, getWidth()+10, getHeight()+10, 14, 14);
	    	 g2d.setStroke(new BasicStroke(1.0f));
	    	     	 
	     }
	     
	     
	     
	     g.drawImage(icon.getImage(), Math.max(1, getWidth()/2 - icon.getIconWidth()/2), 2, null);

	     //Draw text
	     g.setFont(font);
	     int strWidth = g.getFontMetrics().stringWidth(text);
	     g.setColor(new Color(1f, 1f, 1f, 0.3f));
	     g.drawString(text, Math.max(1, getWidth()/2 - strWidth/2)+1, getHeight()-7+1);
	     
	     g.setColor(Color.DARK_GRAY);
	     g.drawString(text, Math.max(1, getWidth()/2 - strWidth/2), getHeight()-7);

	     //Draw divider line
	     if (! selected) {
	    	 g.setColor(new Color(1f, 1f, 1f, 0.5f));
	    	 g.drawLine(2, getHeight()-1, getWidth()-2, getHeight()-1);
	    	 g.setColor(Color.LIGHT_GRAY);
	    	 g.drawLine(2, getHeight()-2, getWidth()-2, getHeight()-2);

	    	 g.setColor(Color.DARK_GRAY);
	    	 g.drawLine(getWidth()-1, 0, getWidth()-1, getHeight());
	     }
	     else {
	    	 g.setColor(Color.DARK_GRAY);
	    	 int mid = this.getHeight()/2;
	    	 int triTop = mid-TRIANGLE_SIZE/2;
	    	 int triBottom = mid+TRIANGLE_SIZE/2;
	    	 
	    	 g.drawLine(getWidth()-1, 0, getWidth()-1, triTop);
	    	 g.drawLine(getWidth()-1, triBottom, getWidth()-1, getHeight());
	    	 g.drawLine(getWidth()-TRIANGLE_SIZE, mid, getWidth()-1, triTop);
	    	 g.drawLine(getWidth()-TRIANGLE_SIZE, mid, getWidth()-1, triBottom);
	     }
	}
	
	public static final int TRIANGLE_SIZE = 10;
}
