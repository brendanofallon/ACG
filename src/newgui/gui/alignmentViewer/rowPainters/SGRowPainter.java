package newgui.gui.alignmentViewer.rowPainters;

import java.awt.Graphics2D;


/**
 * Interface for things that can paint a sequence. seqIndex is generally taken to refer to the index of a 
 * Sequence element in a Alignment
 * @author brendan
 *
 */
public interface SGRowPainter {
	
	public void paintRow(Graphics2D g2d, int seqIndex, int firstCol, int lastCol, int x, int y, int cellWidth, int rowHeight);
	
}
