package newgui.gui.alignmentViewer;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;

import sequence.Alignment;

/**
 * A small class that implements the row header painter interface and which handles painting
 * individual rows in the row header. 
 */
class DefaultHeaderPainter implements SGRowHeaderPainter {
	
	Font font = new Font("Sans", Font.BOLD, 12);
	
	@Override
	public void paintHeaderCell(Graphics2D g2d, int row, int x, int y, int width,
			int height, Alignment sg) {

		g2d.setFont(font);
		g2d.setColor(Color.GRAY);
		g2d.drawString(String.valueOf(row+1) + ".", x, y+height-2);
		
		g2d.setColor(Color.BLACK);
		FontMetrics fm = g2d.getFontMetrics();
		int strWidth = fm.stringWidth(sg.getSequence(row).getLabel());
		g2d.drawString(sg.getSequence(row).getLabel(), Math.max(25, width-strWidth-3) , y-2+height);
		
		g2d.setColor(new Color(255, 255, 255, 155));
		g2d.fillRect(width-3, y, 3, height);
		g2d.setColor(Color.white);
		g2d.fillRect(width-1, y, 1, height);
	}

	@Override
	public void setFont(Font font) {
		this.font = font;
	}
	
}
