package newgui.gui.widgets;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
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
import javax.swing.JComponent;
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

	public static final int TEXT_BELOW = 0;
	public static final int TEXT_RIGHT = 1;
	protected int textPosition = TEXT_BELOW;
	
	
	protected ImageIcon icon = null;
	protected ImageIcon disabledIcon = null;
	protected String[] text = null;
	private boolean drawBorder = false;
	private boolean clicking = false;
	
	//Allows nudgeing of image a bit so its in right spot
	private int yDif = 0;
	private int xDif = 0;
	
	//Pixels between icon and text
	private int iconGap = 5;
	private int yStart = 1; //y-position to start drawing text
	
	private float horTextAlignment = Component.LEFT_ALIGNMENT;

	List<ActionListener> actionListeners = new ArrayList<ActionListener>();
	
	private static Font defaultFont = null;
	private boolean useDefaultFont = true; //Unless font has been explicitly set we use the default
	
	public BorderlessButton(String label) {
		this(label, null);
	}
	
	public BorderlessButton(ImageIcon enabledIcon, ImageIcon disabledIcon) {
		this(enabledIcon);
		this.disabledIcon = disabledIcon;
	}
	
	public BorderlessButton(String label, ImageIcon icon) {
		this.icon = icon;
		initialize(label);
	}
	
	public BorderlessButton(ImageIcon icon) {
		this.icon= icon;
		initialize(null);
	}
	
	private void initialize(String label) {
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.setOpaque(false);

		if (label != null)
			this.text = label.split("\\n");
		else
			this.text = new String[]{""};
		
		setBorder(BorderFactory.createEmptyBorder(4, 2, 4, 2));
		int pWidth = 0;
		int pHeight = 0;
		if (icon != null) {
			pWidth += icon.getIconWidth()+3;
			pHeight += icon.getIconHeight()+5;
			yStart = pHeight+4;
		}
		
		if (label != null) {
			pWidth = Math.max(text[0].length()*10+10, pWidth+10);
			pHeight += Math.max(pHeight, 28);
		}
		
		
		setPreferredSize(new Dimension(pWidth, pHeight));
		this.add(Box.createRigidArea(new Dimension(pWidth, pHeight)));
		Listener listener = new Listener();
		addMouseListener(listener);
		addMouseMotionListener(listener);
	}
	
	/**
	 * Set the font used by all BorderlessButtons. Can be null to turn this option off
	 * @param font
	 */
	public static void setDefaultFont(Font font) {
		defaultFont = font;
	}
	
	public static Font getDefaultFont() {
		return defaultFont;
	}
	
	
	/**
	 * Determine the position for the text in this element - either below or right of the
	 * icon. If there's no icon this has no effect
	 * @param pos
	 */
	public void setTextPosition(int pos) {
		if (pos != TEXT_BELOW && pos != TEXT_RIGHT) 
			throw new IllegalArgumentException("Invalid text position : " + pos);
		this.textPosition = pos;
	}
	
	/**
	 * Set horizontal alignment of text, permissible values are Component.LEFT_ALIGNMENT, 
	 * Component.CENTER_ALIGNMENT, and COmponent.RIGHT_ALIGNMENT
	 * @param alignment
	 */
	public void setHorizontalTextAlignment(float alignment) {
		this.horTextAlignment = alignment;
	}
	
	public void setFont(Font font) {
		super.setFont(font);
		useDefaultFont = false;
	}
	
	public int getIconGap() {
		return iconGap;
	}

	public void setIconGap(int iconGap) {
		this.iconGap = iconGap;
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
	
	public void setIcon(ImageIcon newIcon) {
		this.icon = newIcon;
	}
	
	public void paintComponent(Graphics g) {
		Graphics2D g2d = (Graphics2D)g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
		
		if (defaultFont != null && useDefaultFont)
			setFont(defaultFont);
		
		int strWidth = g2d.getFontMetrics().stringWidth(text[0]); //Width of text 
		int boxWidth = strWidth + 10; //Width of box around text
		if (icon != null) {
			if (textPosition == TEXT_BELOW)
				boxWidth = Math.max(boxWidth, icon.getIconWidth()+6);
			else
				boxWidth = boxWidth+icon.getIconWidth()+6;
		}
		
		//Width of box should never be greater than width of component
		boxWidth = Math.min(boxWidth, getWidth()-1);
		
		//X value at which to start drawing bounding box
		int boxX = 1;
		if (horTextAlignment == Component.RIGHT_ALIGNMENT)
			boxX = getWidth()-strWidth-12;
		
		if (this.isEnabled() && drawBorder) {

			g2d.setColor(Color.white);
			g2d.drawRoundRect(boxX, 1, boxWidth, getHeight()-3, 8, 8);
			
			GradientPaint gp;
			if (clicking)
				gp = new GradientPaint(1, 0, new Color(0.79f, 0.79f, 0.79f), 3, getHeight(), new Color(0.88f, 0.88f, 0.88f));
			else
				gp = new GradientPaint(1, 0, new Color(1f, 1f, 1f), 3, getHeight(), new Color(0.88f, 0.88f, 0.88f));
			g2d.setPaint(gp);
			
			g2d.fillRoundRect(boxX, 1, boxWidth, getHeight()-3, 5, 2);
		}
		else {
			super.paintComponent(g);
		}
		
		int dx = 1;
		
		//Draw icon
		if (icon != null) {
			Image image = icon.getImage();
			if (! this.isEnabled() && disabledIcon != null)
				image = disabledIcon.getImage();
			if (textPosition == TEXT_BELOW)
				g2d.drawImage(image, Math.max(0, getWidth()/2-icon.getIconWidth()/2)+xDif, Math.max(0, getHeight()/2 - icon.getIconHeight()/2)+yDif , null);
			if (textPosition == TEXT_RIGHT)
				g2d.drawImage(image, 3+xDif, Math.max(0, getHeight()/2 - icon.getIconHeight()/2)+yDif , null);
		}
		else {
			yStart = Math.min(getHeight()-2, getHeight()/2 - 8 );
		}
		
		//Draw text
		if (text != null) {
			if (textPosition == TEXT_BELOW) {
				g2d.setFont(getFont());
				for(int i=0; i<text.length; i++) {
					int textXPos = 1;
					if (horTextAlignment == Component.LEFT_ALIGNMENT)
						textXPos = 5;
					if (horTextAlignment == Component.CENTER_ALIGNMENT)
						textXPos = getWidth()/2-strWidth/2;
					if (horTextAlignment == Component.RIGHT_ALIGNMENT)
						textXPos = getWidth() - strWidth - 7;


					g2d.setColor(new Color(0.99f, 0.99f, 0.99f, 0.4f));
					g2d.drawString(text[i], Math.max(1, textXPos+1), yStart + (i+1)*14+1 /*getHeight()-(i+1)*13 */);
					if (this.isEnabled())
						g2d.setColor(new Color(0.2f, 0.2f, 0.2f));
					else
						g2d.setColor(new Color(0.5f, 0.5f, 0.5f));
					g2d.drawString(text[i], Math.max(0, textXPos), yStart + (i+1)*14 /*getHeight()-(i+1)*14 */);	
				}
			}//drawing text below icon
			
			if (textPosition == TEXT_RIGHT) {
				g2d.setFont(getFont());
				int iconWidth = 0;
				if (icon != null)
					iconWidth = icon.getIconWidth()+6;
				yStart = 8;
				for(int i=0; i<text.length; i++) {
					int textXPos = 1 + iconWidth;
					if (horTextAlignment == Component.LEFT_ALIGNMENT)
						textXPos = 5 + iconWidth;
					if (horTextAlignment == Component.CENTER_ALIGNMENT)
						textXPos = getWidth()/2-strWidth/2 + iconWidth;
					if (horTextAlignment == Component.RIGHT_ALIGNMENT)
						textXPos = getWidth() - strWidth - 7 ;



					g2d.setColor(new Color(0.99f, 0.99f, 0.99f, 0.4f));
					g2d.drawString(text[i], Math.max(1, textXPos+1), yStart + (i+1)*14+1 );
					if (this.isEnabled())
						g2d.setColor(new Color(0.2f, 0.2f, 0.2f));
					else
						g2d.setColor(new Color(0.5f, 0.5f, 0.5f));
					g2d.drawString(text[i], Math.max(0, textXPos), yStart + (i+1)*14);	
				}
			}//drawing text to the right of the icon
		}
		
		if (this.isEnabled() && drawBorder) {
			g2d.setColor(new Color(0.99f, 0.99f, 0.99f, 0.35f));
			g2d.drawRoundRect(boxX, 1, boxWidth, getHeight()-3, 7, 7);
			
			g2d.setColor(new Color(0.69f, 0.69f, 0.69f, 0.90f));
			g2d.drawRoundRect(boxX-1, 0, boxWidth, getHeight()-3, 7, 7);
		}
	}


	public int getYDif() {
		return yDif;
	}

	public void setYDif(int yDif) {
		this.yDif = yDif;
	}

	public int getXDif() {
		return xDif;
	}

	public void setXDif(int xDif) {
		this.xDif = xDif;
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
			setDrawBorder(true);
		}
		
		public void mouseExited(MouseEvent me) {
			setDrawBorder(false);
		}
		
	}
	
}

