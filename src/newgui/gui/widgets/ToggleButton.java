package newgui.gui.widgets;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.ImageIcon;

import newgui.UIConstants;

public class ToggleButton extends BorderlessButton {

	private ImageIcon leftIcon = UIConstants.getIcon("gui/icons/toggleleft.png");
	private ImageIcon rightIcon = UIConstants.getIcon("gui/icons/toggleright.png");
	
	private boolean leftState = true;
	
	public ToggleButton (ImageIcon leftIcon, ImageIcon rightIcon) {
		super(leftIcon);
		this.leftIcon = leftIcon;
		this.rightIcon = rightIcon;
		
		addMouseListener(new MouseListener() {
			
			@Override
			public void mouseClicked(MouseEvent arg0) {
				toggle();
			}

			@Override
			public void mouseExited(MouseEvent arg0) {	
			}
			
			@Override
			public void mouseEntered(MouseEvent arg0) {	
			}
			
			@Override
			public void mousePressed(MouseEvent arg0) {
			}

			@Override
			public void mouseReleased(MouseEvent arg0) {
			}
		});
	}
	



	protected void toggle() {
		leftState = ! leftState;
		if (leftState)
			this.icon = leftIcon;
		else
			this.icon = rightIcon;
	}

}
