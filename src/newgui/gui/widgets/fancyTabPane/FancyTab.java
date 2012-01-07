package newgui.gui.widgets.fancyTabPane;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.MultipleGradientPaint.CycleMethod;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import newgui.gui.ViewerWindow;


public class FancyTab extends JPanel {

	private String text;
	private boolean selected = false;
	private static final Font normalFont = ViewerWindow.sansFont.deriveFont(13f);
	//private static final Font selectedFont = ViewerWindow.sansFont.deriveFont(Font.BOLD);
	private List<ChangeListener> listeners = new ArrayList<ChangeListener>();

	//A few constants for painting
	private static final Color darkColor = new Color(0.85f, 0.85f, 0.85f);
	private static final Color lighterColor = new Color(0.95f, 0.95f, 0.99f);
	private static final Color lightColor = new Color(1f, 1f, 1f);
	
	public FancyTab(String label) {
		if (label == null) {
			throw new IllegalArgumentException("Tab must have a non-null label");
		}
		this.text = label;
		this.setLayout(new BorderLayout());
		this.setPreferredSize(new Dimension(120, 30));
		this.setMinimumSize(new Dimension(20, 30));
		this.setMaximumSize(new Dimension(label.length()*6, 40));
		setFont(normalFont);
		this.addMouseListener(new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent e) {
				fireChangeEvent();
			}

			@Override
			public void mousePressed(MouseEvent e) { }

			@Override
			public void mouseReleased(MouseEvent e)  {	}

			@Override
			public void mouseEntered(MouseEvent e) { }

			public void mouseExited(MouseEvent e) { }
			
		});
	}
	
	public boolean isSelected() {
		return selected;
	}
	
	public void addListener(ChangeListener l) {
		this.listeners.add(l);
	}
	
	public void removeListener(ChangeListener l) {
		listeners.remove(l);
	}
	
	private void fireChangeEvent() {
		ChangeEvent evt = new ChangeEvent(this);
		for(ChangeListener l : listeners) {
			l.stateChanged(evt);
		}
	}
	
	public String getLabel() {
		return text;
	}
	
	public void setSelected(boolean selected) {
		this.selected = selected;
		
		if (selected) {
		//	setFont(selectedFont);
		}
		else {
			//setFont(normalFont);
		}
		repaint();
	}
	
	public void paintComponent(Graphics g) {
		Graphics2D g2d = (Graphics2D)g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
	
	     GradientPaint gp;
	     if (selected)
	    	 gp = new GradientPaint(0, 0, lightColor, 0, getHeight(), lighterColor);
	     else
	    	 gp = new GradientPaint(0, 0, lightColor, 0, getHeight(), darkColor);

	     g2d.setPaint(gp);
	     g.fillRoundRect(0, 2, getWidth()-1, getHeight()+10, 8, 8);
	     
	     g2d.setColor(Color.LIGHT_GRAY);
		g.drawRoundRect(0, 2, getWidth()-1, getHeight()+10, 8, 8);
		
		g2d.setColor(Color.DARK_GRAY);
		g.setFont(getFont());
		int strWidth = g.getFontMetrics().stringWidth(text);
		g.drawString(text, getWidth()/2 - strWidth/2 - 2, getHeight()-8);
	}
}
