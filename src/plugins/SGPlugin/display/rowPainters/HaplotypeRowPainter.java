package plugins.SGPlugin.display.rowPainters;

import java.awt.Color;
import java.awt.Graphics2D;

import plugins.SGPlugin.display.ColorDefaults;
import element.sequence.*;

public class HaplotypeRowPainter extends AbstractRowPainter {

	
	public HaplotypeRowPainter(SequenceGroup sg) {
		super(sg);
	}
	public static AbstractRowPainter getNew(SequenceGroup sg) {
		return new HaplotypeRowPainter(sg);
	}
	
	public static String getIdentifier() {
		return "Haplotypes";
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

		int hap = currentSG.getHaplotypeForRow(row);
		setCellSize(cellWidth, rowHeight);
	
		g2d.setFont(font);
		
		Color[] pColors = ColorDefaults.haplotypeColors;  
		Color color = pColors[hap%pColors.length]; 
		
		Sequence seq = currentSG.get(row);
		//int colPos = x + firstCol*cellWidth;
		
		g2d.setColor(color);	
		g2d.fillRect(x, y, (lastCol-firstCol)*cellWidth, rowHeight);
		
		int firstDrawCol = firstCol - firstCol%hashBlockSize;
		for(int blockStart=firstDrawCol; blockStart<Math.min(lastCol, seq.length()); blockStart+=hashBlockSize) {			
			drawBaseGroup(g2d, blockStart*cellWidth, y, cellWidth, rowHeight, row, blockStart, seq);	
		}
		
//		for(int col=firstCol; col<Math.min(lastCol, seq.length()); col++) {
//			base[0] = seq.at(col);
//			g2d.setColor(color);	
//			g2d.fillRect(colPos, y, cellWidth, rowHeight);
//
//			drawBase(g2d, colPos, y+rowHeight-1, cellWidth, row, col, seq);
//			colPos += cellWidth;
//			
//		}
	}

	
	public static class Instantiator extends AbstractRowPainter.Instantiator {
		
		public AbstractRowPainter getNewRowPainter(SequenceGroup sg) {
			return new HaplotypeRowPainter(sg);
		}
		
	}
}
