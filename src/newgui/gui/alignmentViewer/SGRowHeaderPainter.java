package newgui.gui.alignmentViewer;

import java.awt.Font;
import java.awt.Graphics2D;

import element.sequence.SequenceGroup;

public interface SGRowHeaderPainter {
	
	public void setFont(Font font);

	public void paintHeaderCell(Graphics2D g2d, int row, int x, int y, int width, int height, SequenceGroup sg);
}
