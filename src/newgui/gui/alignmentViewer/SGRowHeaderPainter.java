package newgui.gui.alignmentViewer;

import java.awt.Font;
import java.awt.Graphics2D;

import sequence.Alignment;

/**
 * Interface for objects that can paint the row header for the AlignmentViewer panel
 * @author brendan
 *
 */
public interface SGRowHeaderPainter {
	
	public void setFont(Font font);

	public void paintHeaderCell(Graphics2D g2d, int row, int x, int y, int width, int height, Alignment sg);
}
