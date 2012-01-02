package newgui.gui.widgets;


import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * A vertically oriented group of text buttons
 * @author brendan
 *
 */
public class VerticalTextButtons extends JPanel {

	private int horizontalTextAlignment = SwingConstants.LEFT;
	private List<TextButton> buttons = new ArrayList<TextButton>();
	private int buttonPadding = 4;
	
	public VerticalTextButtons() {
		BoxLayout layout = new BoxLayout(this, BoxLayout.Y_AXIS);
		setLayout(layout);
		setOpaque(false);
	}
	
	
	
	public void setHorizontalTextPostion(int pos) {
		this.horizontalTextAlignment = pos;
		for(TextButton button : buttons) {
			button.setHorizontalTextPosition(pos);
			button.repaint();
		}
		repaint();
	}
	
	public void addTextButton(TextButton button) {
		this.add(Box.createVerticalStrut(buttonPadding/2));
		this.add(button);
		this.add(Box.createVerticalStrut(buttonPadding/2));
		buttons.add(button);
		button.setHorizontalTextPosition(horizontalTextAlignment);
		
	}
	
	public void setPadding(int padding) {
		this.buttonPadding = padding;
		redoLayout();
	}
	
	public void redoLayout() {
		this.removeAll();
		for(TextButton button : buttons) {
			this.add(Box.createVerticalStrut(buttonPadding/2));
			this.add(button);
			this.add(Box.createVerticalStrut(buttonPadding/2));
		}
		revalidate();
		repaint();
	}
	
}
