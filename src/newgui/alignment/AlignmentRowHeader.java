package newgui.alignment;

import gui.ViewerWindow;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

/**
 * Works in an AlignmentView object to make a header for the rows containing the sequence names
 * @author brendan
 *
 */
public class AlignmentRowHeader extends JPanel {

	private List<String> rowNames = new ArrayList<String>();
	private int rowHeight = 20;
	private Font initialFont = ViewerWindow.sansFont.deriveFont(13.0f);
	private Color shadowColor = new Color(0.94f, 0.94f, 0.94f);
	private Color textShadow = new Color(0.98f, 0.98f, 0.98f);
	public static final Cursor edgeAdjustCursor = new Cursor(Cursor.W_RESIZE_CURSOR);
	
	public AlignmentRowHeader() {
		setLayout(new BorderLayout());
		setPreferredSize(new Dimension(100, 100));
		this.setFont(initialFont);
		this.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
	}
	
	
	
	public void setRowHeight(int height) {
		this.rowHeight = height;
	}
	
	public void addRowLabel(String label) {
		rowNames.add(label);
		repaint();
	}
	
	public void setRowLabels(List<String> labels) {
		rowNames.clear();
		rowNames.addAll(labels);
		repaint();
	}
	
	public void paintComponent(Graphics g) {
		Graphics2D g2d = (Graphics2D)g;
		g.setColor(getBackground());
		g.fillRect(0, 0, getWidth(), getHeight());
		
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

//		GradientPaint gp = new GradientPaint(0, 0, shadowColor, 55, 0, Color.white);
//		g2d.setPaint(gp);
//		g2d.fillRect(0, 0, getWidth(), getHeight());
		
		g2d.setFont(getFont());
		g2d.translate(0, -yTranslation);
		g2d.setColor(textShadow);
		g2d.setFont(getFont());
		int y=rowHeight+yOffset;
		for(int i=0; i<rowNames.size(); i++) {
			int strWidth = g2d.getFontMetrics().stringWidth(rowNames.get(i));
			g2d.drawString(rowNames.get(i), Math.max(0, getWidth()-3-strWidth)+1, y-5+1);
			y+= rowHeight;
		}
		
		g2d.setColor(Color.DARK_GRAY);
		
		y=rowHeight+yOffset;
		for(int i=0; i<rowNames.size(); i++) {
			int strWidth = g2d.getFontMetrics().stringWidth(rowNames.get(i));
			g2d.drawString(rowNames.get(i), Math.max(0, getWidth()-3-strWidth), y-5);
			y+= rowHeight;
		}
	}

	/**
	 * Remove all labels from the row header
	 */
	public void clearLabels() {
		rowNames.clear();
	}


	public void setYTranslate(int value) {
		yTranslation = value;
		repaint();
	}
	
	/**
	 * A constant number of pixels that will be added to all y-coords for drawing.
	 * In general this is helpful if the sequences aren't the first row in the view, 
	 * @param value
	 */
	public void setYOffset(int value) {
		yOffset = value;
		repaint();
	}
	
	private int yOffset = 0;
	private int yTranslation = 0;
}
