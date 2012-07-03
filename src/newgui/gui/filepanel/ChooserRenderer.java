package newgui.gui.filepanel;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;

import newgui.UIConstants;

public class ChooserRenderer extends JPanel implements ListCellRenderer {

	private String text = "";
	boolean selected = false;
	
	public ChooserRenderer() {
		this.setMinimumSize(new Dimension(1, 40));
		this.setMaximumSize(new Dimension(32768, 40));
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		add(Box.createRigidArea(new Dimension(40, 40)));
		
		this.setFont(UIConstants.sansFontBold);
	}
	
	@Override
	public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {

		text = value.toString();
		this.selected = isSelected;
		return this;
	}

	
	public void paintComponent(Graphics g) {
		((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		
		g.setColor(shadowColor);
		((Graphics2D)g).setStroke(shadowStroke);
		g.drawRoundRect(4, 4, getWidth()-5, getHeight()-5, 8, 8);
		
		g.setColor(bgColor);
		((Graphics2D)g).setStroke(normalStroke);
		g.fillRoundRect(1, 1, getWidth()-3, getHeight()-2, 5, 5);
		
		g.setColor(bgColor);
		((Graphics2D)g).setStroke(normalStroke);
		//A gradient
		float gradMax = Math.min(200, Math.max( getHeight()/3f, 20));
		g.setColor(gray2);
		g.drawLine(3, 2, getWidth()-4, 2);
		g.setColor(dark1);
		g.drawLine(3, 3, getWidth()-4, 3);
		g.drawLine(2, 4, getWidth()-2, 4);
		for(float i=5; i<gradMax; i++) {
			float newVal = topDark + (0.99f-topDark)*(1-(gradMax-i)/gradMax );

			g.setColor( new Color(newVal, newVal, newVal));
			g.drawLine(1, (int)i, getWidth()-2, (int)i);
		}
		

		
		g.setColor(lineColor);
		g.drawRoundRect(1, 1, getWidth()-3, getHeight()-3, 5, 5);

		if (selected) {
			g.setColor(selectedColor);
			((Graphics2D)g).setStroke(thickStroke);
			g.drawRoundRect(2, 2, getWidth()-5, getHeight()-5, 8, 8);
		}
		
		g.setColor(lineColor);
		((Graphics2D)g).setStroke(normalStroke);
		g.setFont(getFont());
		g.drawString(text, 15+1, 25+1);
		
		g.setColor(Color.DARK_GRAY);
		g.drawString(text, 15, 25);
	}
	
	

	
		final static Color bgColor = new Color(253, 253, 253);
		final static Color gray1 = Color.white;
		final static Color gray2 = new Color(250, 250, 250, 100);
		final static float topDark = 0.935f;
		final static Color dark1 = new Color(topDark, topDark, topDark);
		final static Color dark2 = new Color(220, 220, 220, 100);
		final static Color shadowColor = new Color(0f, 0f, 0f, 0.1f);
		final static Color lineColor = new Color(200, 200, 200);
		final static Color selectedColor = new Color(43, 65, 225);
		final static Stroke thickStroke = new BasicStroke(2.2f);
		final static Stroke shadowStroke = new BasicStroke(1.6f);
		final static Stroke normalStroke = new BasicStroke(1.0f);
}
