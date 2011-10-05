package gui.widgets;

import gui.ACGFrame;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JPanel;

public class ButtonBar extends JPanel {

	private int leftPadding = 15;
	private int rightPadding = 10;
	private int topPadding = 10;
	private int bottomPadding = 5;
	private int edgeInset = 5; //Pixels between left edge of bar and start of first button
	
	
	static final Color lightColor = new Color(0.98f, 0.98f, 0.98f);
	static final Color darkColor = new Color(0.7f, 0.7f, 0.7f);
	static final Color edgeColor = new Color(0.9f, 0.9f, 0.9f, 0.7f);
	
	private Component glueBox;
	
	private ImageIcon rightIcon = null;
	
	//List of buttons that have been added. Not actually used at this point. 
	List<ButtonBarItem> buttons = new ArrayList<ButtonBarItem>();
	
	public ButtonBar() {
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		setBorder(BorderFactory.createEmptyBorder(topPadding, leftPadding, bottomPadding, rightPadding));
		setBackground(ACGFrame.backgroundColor);
		glueBox = Box.createHorizontalGlue();
	}
	
	
	public void addButton(ButtonBarItem button) {
		this.remove(glueBox);
		buttons.add(button);
		this.add(button);
		this.add(Box.createHorizontalStrut(1));
		this.add(glueBox);
		revalidate();
		repaint();
	}
	
	public void setRightIcon(ImageIcon icon) {
		this.rightIcon = icon;
	}
	
	public void paintComponent(Graphics g) {
		Graphics2D g2d = (Graphics2D)g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
		
		//Dark border background
		g2d.setColor( ACGFrame.backgroundColor );
		g2d.fillRect(0, 0, getWidth(), getHeight());
		
		//Main area gradient
		GradientPaint gp = new GradientPaint(getWidth()/2, 0, lightColor, getWidth()/2+1, getHeight()+10, darkColor);
		g2d.setPaint(gp);
		g2d.fillRoundRect(leftPadding-edgeInset, topPadding, getWidth()-(leftPadding+rightPadding+edgeInset), getHeight()-(topPadding+bottomPadding), 15, 15);
		
		if (rightIcon != null) {
			g2d.drawImage(rightIcon.getImage(), getWidth() - rightIcon.getIconWidth()-rightPadding-leftPadding - 5, (getHeight()-topPadding-bottomPadding)/2 + topPadding -rightIcon.getIconHeight()/2, null);
		}
		
		//Border around main area
		g2d.setColor( edgeColor );
		g2d.setStroke(new BasicStroke(0.8f));
		g2d.drawRoundRect(leftPadding-edgeInset, topPadding, getWidth()-(leftPadding+rightPadding+edgeInset)-1, getHeight()-(topPadding+bottomPadding)-1, 15, 15);
	}
}
