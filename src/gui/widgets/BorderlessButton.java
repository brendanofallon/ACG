package gui.widgets;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.event.MouseInputAdapter;

/**
 * A label with button-like functionality but no border
 * @author brendano
 *
 */
public class BorderlessButton extends JPanel {

	ImageIcon icon = null;
	String text = null;
	private boolean drawBorder = false;
	
	List<ActionListener> actionListeners = new ArrayList<ActionListener>();
	
	public BorderlessButton(String label, ImageIcon icon) {
		this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		this.setOpaque(false);
		this.text = label;
		this.icon = icon;
		
		setBorder(BorderFactory.createEmptyBorder(4, 2, 4, 5));
		int pWidth = 0;
		int pHeight = 0;
		if (icon != null) {
			pWidth += icon.getIconWidth()+5;
			pHeight += icon.getIconHeight()+5;
		}
		if (label != null) {
			pWidth += label.length()*10+4;
			pHeight = Math.max(24, icon.getIconHeight()+5);
		}
		
		
		setPreferredSize(new Dimension(pWidth, pHeight));
		this.add(Box.createRigidArea(new Dimension(pWidth, pHeight)));
		Listener listener = new Listener();
		addMouseListener(listener);
		addMouseMotionListener(listener);
	}
	
	public BorderlessButton(ImageIcon icon) {
		this(null, icon);
	}
	
	public void fireActionEvent(MouseEvent me) {
		ActionEvent evt = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "Button pressed");
		for(ActionListener listener : actionListeners ) {
			listener.actionPerformed(evt);
		}
	}
	
	public void addActionListener(ActionListener listener) {
		if (!actionListeners.contains(listener))
			actionListeners.add(listener);
	}
	
	public void removeActionListener(ActionListener listener) {
		actionListeners.remove(listener);
	}
	
	public void setDrawBorder(boolean drawIt) {
		this.drawBorder = drawIt;
		repaint();
	}
	
	class Listener extends MouseInputAdapter {
		
		public void mouseClicked(MouseEvent me) {
			fireActionEvent(me);
		}
		
		public void mouseEntered(MouseEvent me) {
			setDrawBorder(true);
		}
		
		public void mouseExited(MouseEvent me) {
			setDrawBorder(false);
		}
		
	}
	
	public void paintComponent(Graphics g) {
		Graphics2D g2d = (Graphics2D)g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
		
		if (drawBorder) {
			g2d.setColor(new Color(0.99f, 0.99f, 0.99f, 0.5f));
			g2d.fillRoundRect(2, 2, getWidth()-3, getHeight()-4, 5, 5);
		}
		else {
			super.paintComponent(g);
		}
		
		int dx = 1;
		if (icon != null) {
			g2d.drawImage(icon.getImage(), 2, Math.max(0, getHeight()/2-icon.getIconHeight()/2) , null);
			dx += icon.getIconWidth()+2;
		}
		if (text != null) {
			g2d.setColor(new Color(0.99f, 0.99f, 0.99f, 0.5f));
			g2d.drawString(text, dx+3, getHeight()/2+7);
			g2d.setColor(new Color(0.2f, 0.2f, 0.2f));
			g2d.drawString(text, dx+2, getHeight()/2+6);
		}
		
		if (drawBorder) {
			g2d.setColor(new Color(0.99f, 0.99f, 0.99f, 0.45f));
			g2d.drawRoundRect(1, 1, getWidth()-2, getHeight()-4, 5, 5);
			
			g2d.setColor(new Color(0.79f, 0.79f, 0.79f, 0.95f));
			g2d.drawRoundRect(0, 0, getWidth()-2, getHeight()-3, 5, 5);
		}
	}


	
	
}
