package figure;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

import javax.swing.JTextField;

import element.DoubleRectangle;

/**
 * An element that draws some text vertically. 
 * @author brendan
 *
 */
public class VerticalTextElement extends TextElement {

	AffineTransform transform;
	
	public VerticalTextElement(String txt, Figure parent) {
		super(txt, parent);
		transform = new AffineTransform();
		transform.rotate(Math.PI*1.5);
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
	
	public void popupConfigureTool(java.awt.Point pos) {
		configureTextField = new JTextField();
		Rectangle textBounds = new Rectangle();
		textBounds.x = round(bounds.x*xFactor);
		textBounds.y = round(bounds.y*yFactor);
		textBounds.width = round(Math.max(40, bounds.height*xFactor));
		textBounds.height = round(Math.max(24, bounds.width*yFactor));
		
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
	
	public void paint(Graphics2D g) {
		if (text.length()==0)
			return;
		
		g.setColor(foregroundColor);
		
		if (isSelected)
			g.setFont(selectedFont);
		else
			g.setFont(font);
		
		// Create a rotation transformation for the font.
		
		Font theFont = g.getFont();
		Font theDerivedFont = theFont.deriveFont(transform);
		g.setFont(theDerivedFont);
		
		Rectangle2D stringBounds = g.getFontMetrics().getStringBounds(text, 0, text.length(), g);
		bounds.height = (double)stringBounds.getWidth()/yFactor; 
		bounds.width = (double)stringBounds.getHeight()/xFactor;

		g.drawString(text, toPixelX(1.0), toPixelY(1.0));
		g.setFont(theFont);
	}

}
