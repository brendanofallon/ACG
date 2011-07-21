package figure;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;

import javax.swing.JTextField;

import element.DoubleRectangle;

/**
 * A horizontal string on a Figure
 * @author brendan
 *
 */
public class TextElement extends FigureElement {
	
	String text = "";
	Font font;
	Font selectedFont;
	
	JTextField configureTextField;
	
	
	public TextElement(String txt, Figure parent) {
		super(parent);
		font = new Font("Sans", Font.PLAIN, 12);
		selectedFont = new Font("Sans", Font.BOLD, 12);
		this.text = txt;
	}
	
	
	public void setText(String txt) {
		this.text = txt;
	}
	
	public String getText() {
		return text;
	}
	
	public void popupConfigureTool(java.awt.Point pos) {
		configureTextField = new JTextField();
		Rectangle textBounds = new Rectangle();
		textBounds.x = round(bounds.x*xFactor);
		textBounds.y = round(bounds.y*yFactor);
		textBounds.width = round(Math.max(40, bounds.width*xFactor));
		textBounds.height = round(Math.max(24, bounds.height*yFactor));
		
		configureTextField.setBounds(textBounds);
		configureTextField.setText(text);
		parent.add(configureTextField);
		configureTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
            	doneEditingText();
            }
        });
		configureTextField.setVisible(true);
	}
	
	protected void doneEditingText() {
		configureTextField.setVisible(false);
		parent.remove(configureTextField);
		text = configureTextField.getText();
		parent.repaint();
	}


	public void setFont(Font newFont) {
		this.font = newFont;
	}
	
	public void setFontSize(int size) {
		font = new Font(font.getFamily(), Font.PLAIN, size);
	}
	
	public DoubleRectangle getBounds(Graphics g) {
		DoubleRectangle boundaries = new DoubleRectangle();
		boundaries.x = bounds.x;
		boundaries.y = bounds.y;
		FontMetrics fm = g.getFontMetrics();
		
		boundaries.width = fm.getStringBounds(text, 0, text.length(), g).getWidth()/xFactor; 
		boundaries.height = fm.getStringBounds(text, 0, text.length(), g).getHeight()/yFactor;
		return boundaries;
	}
	
	public void paint(Graphics2D g) {
		g.setColor(foregroundColor);
		if (isSelected)
			g.setFont(selectedFont);
		else
			g.setFont(font);
		
	    Rectangle2D stringBounds = g.getFontMetrics().getStringBounds(text, 0, text.length(), g);
	    
		bounds.width = (double)stringBounds.getWidth()/xFactor; 
		bounds.height = (double)stringBounds.getHeight()/yFactor;

		g.drawString(text, toPixelX(0), toPixelY(1.0));
				
	}

}
