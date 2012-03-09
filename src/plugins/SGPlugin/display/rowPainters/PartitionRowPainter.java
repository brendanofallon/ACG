package plugins.SGPlugin.display.rowPainters;

import java.awt.Color;
import java.awt.Graphics2D;

import plugins.SGPlugin.display.ColorDefaults;

import element.sequence.*;

public class PartitionRowPainter extends AbstractRowPainter {

	public PartitionRowPainter(SequenceGroup sg) {
		super(sg);
	}
	
	public static AbstractRowPainter getNew(SequenceGroup sg) {
		return new PartitionRowPainter(sg);
	}
	
	@Override
	public void paintRow(Graphics2D g2d, 
			int row, 
			int firstCol,
			int lastCol, 
			int x, 
			int y, 
			int cellWidth,
			int rowHeight) {

		Sequence seq = currentSG.get(row);
		setCellSize(cellWidth, rowHeight);
		
		g2d.setFont(font);
		
		Color[] pColors = ColorDefaults.partitionColors;
		int colPos = x+ firstCol*cellWidth;
		for(int i=firstCol; i<Math.min(lastCol, seq.length()); i++) {
			int part = currentSG.getPartitionNumForSite(i);
			
			base[0] = seq.at(i);
			Color color = pColors[part%pColors.length]; 

			g2d.setColor(color);

			g2d.fillRect(colPos, y, cellWidth, rowHeight);
			//drawBase(g2d, colPos+1, y+rowHeight, cellWidth,row, i, seq);
			colPos += cellWidth;
		}
		
		int firstDrawCol = firstCol - firstCol%hashBlockSize;
		for(int blockStart=firstDrawCol; blockStart<Math.min(lastCol, seq.length()); blockStart+=hashBlockSize) {			
			drawBaseGroup(g2d, blockStart*cellWidth, y, cellWidth, rowHeight, row, blockStart, seq);	
		}
	}

	public static String getIdentifier() {
		return "Partitions";
	}
	
	public static class Instantiator extends AbstractRowPainter.Instantiator {
		
		public AbstractRowPainter getNewRowPainter(SequenceGroup sg) {
			return new PartitionRowPainter(sg);
		}
		
	}

}