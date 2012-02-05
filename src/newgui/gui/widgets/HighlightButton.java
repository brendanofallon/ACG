package newgui.gui.widgets;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.ImageIcon;

public class HighlightButton extends BorderlessButton {

	final ImageIcon normalIcon;
	final ImageIcon highlightIcon;
	
	
	public HighlightButton(ImageIcon normalIcon, ImageIcon highlightIcon) {
		super(normalIcon);
		this.normalIcon = normalIcon;
		this.highlightIcon = highlightIcon;
		addMouseListener(new MouseListener() {

			@Override
			public void mouseEntered(MouseEvent arg0) {
				useHighlightIcon();
			}

			@Override
			public void mouseExited(MouseEvent arg0) {
				useNormalIcon();
			}
			

			@Override
			public void mouseClicked(MouseEvent arg0) {
				
			}

			

			@Override
			public void mousePressed(MouseEvent arg0) {
			}

			@Override
			public void mouseReleased(MouseEvent arg0) {
			}
			
		});
	}


	protected void useHighlightIcon() {
		this.icon = highlightIcon;
		repaint();
	}


	protected void useNormalIcon() {
		this.icon = normalIcon;
		repaint();
	}


	
}
