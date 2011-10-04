package gui.widgets;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

public class ButtonBar extends JPanel {

	private int leftPadding = 10;
	private int rightPadding = 10;
	private int topPadding = 10;
	private int bottomPadding = 5;
	private int edgeInset = 5; //Pixels between left edge of bar and start of first button
	
	
	static final Color lightColor = new Color(0.95f, 0.95f, 0.95f);
	static final Color darkColor = new Color(0.88f, 0.88f, 0.88f);
	static final Color edgeColor = new Color(0.98f, 0.98f, 0.98f, 0.5f);
	
	public ButtonBar() {
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		setBorder(BorderFactory.createEmptyBorder(topPadding, leftPadding, bottomPadding, rightPadding));
		setBackground(Color.LIGHT_GRAY);
	}
	
	
	List<ButtonBarItem> buttons = new ArrayList<ButtonBarItem>();
	
	public void addButton(ButtonBarItem button) {
		buttons.add(button);
		this.add(button);
		this.add(Box.createHorizontalStrut(1));
		revalidate();
		repaint();
	}
	
	public void paintComponent(Graphics g) {
		Graphics2D g2d = (Graphics2D)g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
		
		//Dark border background
		g2d.setColor(getBackground());
		g2d.fillRect(0, 0, getWidth(), getHeight());
		
		//Main area gradient
		GradientPaint gp = new GradientPaint(0, 0, lightColor, 1, getHeight(), darkColor);
		g2d.setPaint(gp);
		g2d.fillRoundRect(leftPadding-edgeInset, topPadding, getWidth()-(leftPadding+rightPadding+edgeInset), getHeight()-(topPadding+bottomPadding), 15, 15);
		
		//Border around main area
		g2d.setColor( edgeColor );
		g2d.setStroke(new BasicStroke(1.2f));
		g2d.drawRoundRect(leftPadding-edgeInset, topPadding, getWidth()-(leftPadding+rightPadding+edgeInset), getHeight()-(topPadding+bottomPadding), 15, 15);
	}
}
