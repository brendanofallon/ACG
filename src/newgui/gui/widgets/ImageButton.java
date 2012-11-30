package newgui.gui.widgets;

import java.awt.BasicStroke;
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

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.event.MouseInputAdapter;

/**
 * A button that displays as an image (no text label) and has no other UI elements
 * @author brendanofallon
 *
 */
public class ImageButton extends JPanel {

	private ImageIcon image;
	private ImageIcon pressedImage;
	private List<ActionListener> actionListeners = new ArrayList<ActionListener>();
	private boolean clicking = false;
	private boolean hovering = false;
	
	public ImageButton(ImageIcon normalImage, ImageIcon pressedImage) {
		this.image = normalImage;
		this.pressedImage = pressedImage;
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		this.add(Box.createRigidArea(new Dimension(normalImage.getIconWidth()+2, normalImage.getIconHeight()+2)) );
		Listener listener = new Listener();
		this.addMouseListener(listener);
		this.addMouseMotionListener(listener);
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
	
	public void paintComponent(Graphics g) {

		
		//Always paint over background
		g.setColor(getBackground());
		g.fillRect(0, 0, getWidth(), getHeight());
		
		
		if (hovering) {
			Graphics2D g2d = (Graphics2D)g;
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
	                RenderingHints.VALUE_ANTIALIAS_ON);
			g.setColor(new Color(0.75f, 0.75f, 0.75f, 0.6f));
			g2d.setStroke(new BasicStroke(2.0f));
			g.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 10, 10);
		}
		
		if (clicking)
			g.drawImage(pressedImage.getImage(), 0, 1, null);
		else
			g.drawImage(image.getImage(), 0, 1, null);
	}
	
	
	class Listener extends MouseInputAdapter {
		
		public void mouseClicked(MouseEvent me) {
			if (isEnabled())
				fireActionEvent(me);
		}
		
		public void mousePressed(MouseEvent me) {
			if (isEnabled()) {
				clicking = true;
				repaint();
			}
		}
		
		public void mouseReleased(MouseEvent me) {
			if (isEnabled()) {
				clicking = false;
				repaint();
			}
		}
		
		public void mouseEntered(MouseEvent me) {
			hovering = true;
			repaint();
		}
		
		public void mouseExited(MouseEvent me) {
			hovering = false;
			repaint();
		}
	}
}
