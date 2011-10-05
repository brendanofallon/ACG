package gui.widgets;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

/**
 * An individual button in a ButtonBar. These things fire ActionEvents to all listeners when they are clicked.
 * @author brendano
 *
 */
public class ButtonBarItem extends JPanel {

	private List<ActionListener> listeners;
	private String text = "";
	private ImageIcon icon = null;
	static final Color textShadowColor = new Color(0.95f, 0.95f, 0.95f, 0.5f);
	static final Color textColor = Color.DARK_GRAY;
	private Dimension defaultSize = new Dimension(70, 55);
	static final Color backgroundColor = new Color(0.7f, 0.7f, 0.7f);
	static final Color hoverColor = new Color(0.75f, 0.75f, 0.75f); //Color when mouse hovers
	static final Color pressedTopColor = new Color(0.85f, 0.85f, 0.85f); //Color when mouse is pressed
	static final Color pressedBottomColor = new Color(0.92f, 0.92f, 0.92f); //Color when mouse is pressed
	private boolean mouseIsOver = false; //True when mouse has entered but not exited
	private boolean mouseIsPressed = false; //True when mouse button is depressed
	
	public ButtonBarItem(String label) {
		this.text = label;
		setMinimumSize(defaultSize);
		setPreferredSize(defaultSize);
		setMaximumSize(defaultSize);
		setBackground( backgroundColor);
		MListener listener = new MListener();
		this.addMouseListener( listener );
		this.addMouseMotionListener(listener);
	}
	
	public ButtonBarItem(String label, ImageIcon icon) {
		this(label);
		this.icon = icon;
	}
	
	public void setIcon(ImageIcon icon) {
		this.icon = icon;
	}
	
	public void buttonClicked() {
		if (listeners != null) {
			ActionEvent evt = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, text + " buttonbar item clicked");
			for(ActionListener listener : listeners) {
				listener.actionPerformed(evt);
			}
		}
	}
	
	public void addActionListener(ActionListener listener) {
		if (listeners == null)
			listeners = new ArrayList<ActionListener>(4);
		listeners.add(listener);
	}
	
	public void removeListener(ActionListener listener) {
		if (listeners != null)
			listeners.remove(listener);
	}
	
	public void paintComponent(Graphics g) {
		Graphics2D g2d = (Graphics2D)g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
		
		//Gradient background
		Color bottomColor = ButtonBar.darkColor;
		Color topColor = ButtonBar.lightColor;
		if (mouseIsOver) {
			bottomColor = hoverColor;
		}
		
		if (mouseIsPressed) {
			topColor = pressedTopColor;
			bottomColor = pressedBottomColor;
		}
		

		GradientPaint gp = new GradientPaint(getWidth()/2, 0, topColor, getWidth()/2+1, getHeight()+10, bottomColor);
		g2d.setPaint(gp);
		g2d.fillRect(0, 1, getWidth(), getHeight()-2);
		
		
		
		//Darker bar on edge
		g2d.setColor( backgroundColor );
		g2d.drawLine(getWidth()-2, 0, getWidth()-2, getHeight()+2);
		if ( ! mouseIsPressed) {
			g2d.setColor(  new Color(0.98f, 0.98f, 0.98f, 0.5f) );
			g2d.drawLine(getWidth()-1, 0, getWidth()-1, getHeight());
		}
		
		//Icon painting
		int dy = getHeight()/2 + 10; //Y position for text, centered if no icon, otherwise at bottom
		
		if (icon != null) {
			g2d.drawImage(icon.getImage(), Math.max(1, getWidth()/2 - icon.getIconWidth()/2), Math.max(1, getHeight()/2-icon.getIconHeight()/2-5), null);
			dy = getHeight()-4;
		}
		
		//Text painting
		
		int strWidth = g2d.getFontMetrics().stringWidth(text);
		g2d.setColor(textShadowColor);
		g2d.drawString(text, Math.max(2, getWidth()/2-strWidth/2+1), Math.min(getHeight(), dy+1));
		g2d.setColor( textColor );
		g2d.drawString(text, Math.max(1, getWidth()/2-strWidth/2), Math.min(getHeight(), dy));
		
		
	}
	
	class MListener extends MouseAdapter {
		
		public void mouseEntered(MouseEvent me) {
			mouseIsOver = true;
			repaint();
		}
		
		public void mouseExited(MouseEvent me) {
			mouseIsOver = false;
			mouseIsPressed = false;
			repaint();
		}
		
		public void mousePressed(MouseEvent me) {
			mouseIsPressed = true;
			repaint();
		}
		
		public void mouseReleased(MouseEvent me) {
			mouseIsPressed = false;
			repaint();
		}
		
		public void mouseClicked(MouseEvent me) {
			buttonClicked();
			repaint();
		}
	}
}
