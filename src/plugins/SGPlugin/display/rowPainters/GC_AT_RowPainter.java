package plugins.SGPlugin.display.rowPainters;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.HashMap;
import java.util.Map;

import element.sequence.*;

public class GC_AT_RowPainter extends AbstractRowPainter {

	

	private static Color GColor = new Color(69, 118, 231, 155);
	private static Color CColor =  new Color(69, 118, 231, 205);
	private static Color AColor = new Color(51, 227, 28,  155);
	private static Color TColor =  new Color(51, 227, 28,  255);

	
	private static Color SColor =  new Color(69, 180, 231, 225); //Either C or G
	private static Color RColor = new Color(10, 250, 250, 205); //Either A or G
	
	private static Color YColor = new Color(250, 250, 25,  155);
	private static Color MColor =  new Color(201, 27, 208,  205); //Either A or C
	private static Color WColor =  new Color(51, 227, 28, 205); //Either A or T
	
	private static Color gapColor =  new Color(230, 227, 230); //Not anything else
	
	private static Color unknownColor =  new Color(230, 227, 230); //Not anything else
	
	Map<Character, Color> baseColorMap;
	
	public GC_AT_RowPainter(SequenceGroup sg) {
		super(sg);
		fillBaseColors();
	}
	
	public AbstractRowPainter getNew(SequenceGroup sg) {
		return new GC_AT_RowPainter(sg);
	}
	
	/**
	 * Create the base-color map 
	 */
	private void fillBaseColors() {
		baseColorMap = new HashMap<Character, Color>();
		baseColorMap.put('A', AColor);
		baseColorMap.put('C', CColor);
		baseColorMap.put('G', GColor);
		baseColorMap.put('T', TColor);
		baseColorMap.put('S', SColor);
		baseColorMap.put('R', RColor);
		baseColorMap.put('Y', YColor);
		baseColorMap.put('M', MColor);
		baseColorMap.put('W', WColor);
		baseColorMap.put('-', gapColor);
	}
	
	public static String getIdentifier() {
		return "GC+AT";
	}
	
	@Override
	
	protected void drawImageForBases(Graphics2D g2d, int colWidth, int height, char[] bases) {
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		
		g2d.setFont(font);
		int x = 1;
		for(int i=0; i<bases.length; i++) {
			Color bColor = baseColorMap.get(bases[i]);
			if (bColor == null) {
				bColor = unknownColor;
			}
			g2d.setColor(bColor);
			g2d.fillRect(i*colWidth, 0, colWidth, height);

			g2d.setColor(shadowColor);
			g2d.drawChars(new char[]{bases[i]}, 0, 1, x+1, height-3);
			g2d.setColor(Color.black);
			g2d.drawChars(new char[]{bases[i]}, 0, 1, x, height-4);
			x += colWidth;
		}
	}
	
	protected void drawBackground(Graphics2D g2d, int x, int y2, int colWidth, int rowHeight,
			int row, int site, Sequence seq) {
		Color bColor = baseColorMap.get(seq.at(site));
		if (bColor == null) {
			bColor = unknownColor;
		}
		g2d.setColor(bColor);
		g2d.fillRect(site*colWidth, rowHeight*row, colWidth, rowHeight);
		
	}
	
	public void paintRow(Graphics2D g2d, 
						int row, 
						int firstCol,
						int lastCol, 
						int x, 
						int y, 
						int cellWidth,
						int rowHeight) {
		Sequence seq = currentSG.get(row);
		base = new char[1];
		
		setCellSize(cellWidth, rowHeight);
	
		int firstDrawCol = firstCol - firstCol%hashBlockSize;
		
		for(int blockStart=firstDrawCol; blockStart<Math.min(lastCol, seq.length()); blockStart+=hashBlockSize) {			
			drawBaseGroup(g2d, blockStart*cellWidth, y, cellWidth, rowHeight, row, blockStart, seq);	
		}
		
	}

	public static class Instantiator extends AbstractRowPainter.Instantiator {
		
		public AbstractRowPainter getNewRowPainter(SequenceGroup sg) {
			return new GC_AT_RowPainter(sg);
		}
		
	}
}
