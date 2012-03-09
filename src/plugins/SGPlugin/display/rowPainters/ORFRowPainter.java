package plugins.SGPlugin.display.rowPainters;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;

import element.Range;
import element.Range.SingleRange;
import element.sequence.*;
import element.sequence.SequenceAnnotation.Direction;

/**
 * A row painter that paints ORF annotations
 * @author brendan
 *
 */
public class ORFRowPainter extends AbstractRowPainter {

	Color forwardColor = new Color(0, 150, 150);
	Color backwardColor = new Color(0, 0, 250);
	
	private int[] yPoints = new int[3];
	private int[] xPoints = new int[3];
	
	public ORFRowPainter(SequenceGroup sg) {
		super(sg);
	}
	
	public static AbstractRowPainter getNew(SequenceGroup sg) {
		return new ORFRowPainter(sg);
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

		List<SequenceAnnotation> orfs = seq.getAnnotationsByType(SimpleORFAnnotator.annotationType);

		for(SequenceAnnotation orf : orfs) {
			List<SingleRange> ranges = orf.getRanges();
			if (orf.overlaps(firstCol, lastCol )) {
				for(SingleRange range : ranges) {

					if (orf.getDirection()==Direction.Forward) {
						g2d.setColor(forwardColor);
						g2d.fillRect( (int)(range.min*cellWidth), y+2, (int)(range.max-range.min-1)*cellWidth, rowHeight-2);


						int x0 = (int)Math.round((range.max-1)*cellWidth);
						xPoints[0] = x0;
						xPoints[1] = x0+2*cellWidth;
						xPoints[2] = x0;


						yPoints[0] = y+2;
						yPoints[1] = y+(int)Math.round((double)rowHeight/2.0+1);
						yPoints[2] = y+rowHeight;
						g2d.fillPolygon(xPoints, yPoints, 3);

						for(int i=2; i<(double)rowHeight/2.0; i++) {
							g2d.setColor(new Color(1.0f, 1.0f, 1.0f, (float)(0.5f*(1.0-2.0f*(float)i/(float)rowHeight))));
							g2d.drawLine((int)(range.min*cellWidth), y+i, (int)(range.max*cellWidth+i-4), i+y);
						}
					}
					else {
						g2d.setColor(backwardColor);
						g2d.fillRect( (int)((range.min+2)*cellWidth), y+2, (int)(range.max-range.min-2)*cellWidth, rowHeight-2);

						int[] xPoints = new int[3];
						int x0 = (int)Math.round(range.min*cellWidth);
						xPoints[0] = x0+2*cellWidth;
						xPoints[1] = x0;
						xPoints[2] = x0+2*cellWidth;

						int[] yPoints = new int[3];
						yPoints[0] = y+2;
						yPoints[1] = y+(int)Math.round((double)rowHeight/2.0);
						yPoints[2] = y+rowHeight;
						g2d.fillPolygon(xPoints, yPoints, 3);

						for(int i=2; i<(double)rowHeight/2.0; i++) {
							g2d.setColor(new Color(1.0f, 1.0f, 1.0f, (float)(0.5f*(1.0-2.0f*(float)i/(float)rowHeight))));
							g2d.drawLine((int)(range.min*cellWidth-cellWidth-2*i), y+i, (int)(range.max*cellWidth), i+y);
						}
					}

				}
			}	 //If the ORF was visible
			
			
			int firstDrawCol = firstCol - firstCol%hashBlockSize;
			
			for(int blockStart=firstDrawCol; blockStart<Math.min(lastCol, seq.length()); blockStart+=hashBlockSize) {			
				drawBaseGroup(g2d, blockStart*cellWidth, y, cellWidth, rowHeight, row, blockStart, seq);	
			}



		}
		
	}

	
	public static String getIdentifier() {
		return "ORFs";
	}
	
	public static class Instantiator extends AbstractRowPainter.Instantiator {
		
		public AbstractRowPainter getNewRowPainter(SequenceGroup sg) {
			return new ORFRowPainter(sg);
		}
		
	}

}
