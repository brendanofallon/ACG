package newgui.gui.widgets;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.SwingConstants;

/**
 * A button that is just some floating text with no border, but some mouse over
 * and mouse clicking feedback
 * @author brendan
 *
 */
public class TextButton extends JLabel {

	protected List<ActionListener> listeners;
	private boolean mouseIsOver = false;
	private boolean mouseClicking = false;
	private Font normalFont;
	private Font mouseOverFont;
	private Font shadowFont;
	private Color shadowColor;
	private AffineTransform trans; //Used to transform shadow from normal font
	
	public TextButton(String text) {
		super(text);
		
		//Listen for click and mouseOver events
		PointerListener pListener = new PointerListener();
		this.addMouseListener( pListener );
		this.addMouseMotionListener(pListener);
		normalFont = getFont();
		mouseOverFont = normalFont.deriveFont(Font.BOLD);
		trans = new AffineTransform();
		shadowColor = new Color(0.85f, 0.85f, 0.85f, 0.8f);
	}
	
	public void paintComponent(Graphics g) {
		Graphics2D g2d = (Graphics2D)g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
		
		if (mouseIsOver) {
			setFont(mouseOverFont);
			
		}
		else {
			setFont(normalFont);
		}
		g2d.setFont(getFont());
		
		int strWidth = g2d.getFontMetrics().stringWidth(getText());
		int textXPos = 1;
		int textYPos = getHeight()/2+3;
		
		if (this.getHorizontalTextPosition() == SwingConstants.LEFT || this.getHorizontalTextPosition() == SwingConstants.LEADING) {
			textXPos = 1;
		}
		if (this.getHorizontalTextPosition() == SwingConstants.CENTER) {
			textXPos = Math.max(0, getWidth()/2 - strWidth/2);
		}
		if (this.getHorizontalTextPosition() == SwingConstants.RIGHT || this.getHorizontalTextPosition() == SwingConstants.TRAILING) {
			textXPos = Math.max(0, getWidth()-strWidth);
		}


		if (mouseIsOver) {
			shadowFont = mouseOverFont.deriveFont(mouseOverFont.getSize2D()+0.2f);
		}
		else {
			shadowFont = normalFont.deriveFont(normalFont.getSize2D()+0.2f);
		}
		
		g2d.setFont(shadowFont);
		g2d.setColor(shadowColor);
		g2d.drawString(getText(), textXPos+1, textYPos+1);
		
		if (mouseClicking) {
			textXPos++;
			textYPos++;
		}
		
		g2d.setColor(getForeground());
		if (mouseIsOver) {
			setFont(mouseOverFont);
		}
		else {
			setFont(normalFont);
		}
		g2d.setFont(getFont());
		g2d.drawString(getText(), textXPos, textYPos);
		
	}
	
	public void fireActionEvent() {
		ActionEvent evt = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "TextButtonClicked");
		if (listeners != null) {
			for(ActionListener l : listeners) {
				l.actionPerformed(evt);
			}
		}
	}
	
	public void addActionListener(ActionListener l) {
		if (listeners == null)
			listeners = new ArrayList<ActionListener>(4);
		listeners.add(l);
	}
	
	public boolean removeActionListener(ActionListener l) {
		if (listeners == null)
			return false;
		else 
			return listeners.remove(l);
	}
	
	
	class PointerListener implements MouseListener, MouseMotionListener {
		
		@Override
		public void mouseClicked(MouseEvent e) {
			mouseClicking = true;
			fireActionEvent();
			repaint();
			mouseClicking = false;
			repaint();
		}

		@Override
		public void mousePressed(MouseEvent e) {
			mouseIsOver = true;
			mouseClicking = true;
			repaint();
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			mouseClicking = false;
			repaint();
			
		}

		@Override
		public void mouseEntered(MouseEvent e) {
			mouseIsOver = true;
			repaint();
		}

		@Override
		public void mouseExited(MouseEvent e) {
			mouseClicking = false;
			mouseIsOver = false;
			repaint();
		}


		@Override
		public void mouseDragged(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mouseMoved(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}

		
		
	}
}
