package plugins.SGPlugin.display.rowPainters;

import java.awt.Color;
import java.awt.Graphics2D;

import plugins.SGPlugin.analyzer.SequenceGroupCalculator;
import element.sequence.*;
import element.sequence.SequenceGroupChangeListener.ChangeType;

/**
 * A row painter that colors bases based on their column frequency. Non-polymorphic columns are all colored blue, but the color of polymorphic 
 * columns is a function of the minor allele frequency. The LOWER the MAF, the more brightly alleles matching that state are colored. Gaps
 * and unknowns are colored gray. 
 * 
 * Instead of looking at each column individually, this tabulates which columns need special attention at first (via a call to recalculateFreqs), 
 * and then just colors one big blue block over the whole thing and then draws the special columns on top of that. 
 * @author brendano
 *
 */
public class FrequencyRowPainter extends AbstractRowPainter {
 

	int[][] freqs;
	boolean[] colorIt; //Array indicating whether or not column is polymorphic, which indicates that we need to draw it with different colors
	
	boolean recalculateFreqs = true;
	
	static int colorArraySize = 10;

    static Color freqColorArray[] = new Color[colorArraySize];

    int prevColSize = 0;
    int prevRowSize = 0;
    
    
	public FrequencyRowPainter(SequenceGroup sg) {
		super(sg);
		
	  	for(int i=0; i<colorArraySize; i++) {
    		freqColorArray[i] = new Color(1.0f-(float)i/(float)colorArraySize, 0.3f+0.3f*(float)i/(float)colorArraySize, 0.5f+0.5f*(float)i/(float)colorArraySize);
	  	}
	  	
    }
	
	public static AbstractRowPainter getNew(SequenceGroup sg) {
		return new FrequencyRowPainter(sg);
	}
	
	public static String getIdentifier() {
		return "Frequency";
	}
	
	private void recalculateFreqs() {
		int columnCount = currentSG.getMaxSeqLength();
		colorIt = new boolean[columnCount];
		freqs = new int[columnCount][4];
		for(int i=0; i<columnCount; i++) {
			float[] frequencies = getColumnBaseFreqs(currentSG, i);
			boolean colorCol = currentSG.hasGap(i) || currentSG.hasUnknown(i); //If there's a gap or unknown we color this column
			for(int j=0; j<4; j++) {
				freqs[i][j] = (int)((colorArraySize-1)*frequencies[j]);
				if (frequencies[j] > 0.0f && frequencies[j] < 1.0f) {
					colorCol = true;
				}
			}
			
			colorIt[i] = colorCol;
			
		}
		
		//System.out.println("Recalculating frequencies");
		recalculateFreqs = false;
	}
	
	/**
	 * Force recalculation of frequencies array, this should happen after any rows / cols have been added or removed 
	 */
	public void setRecalculateFreqs() {
		recalculateFreqs = true;
	}
	
	/**
	 * If the SequenceGroup ever changes recalculate all frequencies
	 */
	public void sgChanged(SequenceGroup source, ChangeType type) {
		setRecalculateFreqs();
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
		
		if (freqs == null || prevRowSize != currentSG.size() || prevColSize != currentSG.getMaxSeqLength() || recalculateFreqs) {
			recalculateFreqs();
			prevRowSize = currentSG.size();
			prevColSize = currentSG.getMaxSeqLength();
		}
	
		setCellSize(cellWidth, rowHeight);
	
		Color color = freqColorArray[ freqColorArray.length-1];
		g2d.setColor(color);
		g2d.fillRect(x+firstCol*cellWidth, y, cellWidth*(lastCol-firstCol), rowHeight);
		
		Sequence seq = currentSG.get(row);
		int colPos = x+firstCol*cellWidth;
		for(int col=firstCol; col < Math.min(lastCol, seq.length()); col++) {
			if (colorIt[col]) {
				int baseIndex = -1;
				color = Color.LIGHT_GRAY;

				Integer value = baseIntMap.get(seq.at(col));
				if (value != null)
					baseIndex = value;

				if (baseIndex>-1) {
					int[] freqCol = freqs[col];
					color = Color.LIGHT_GRAY;
					if (baseIndex < freqCol.length) {
						int colorIndex = freqCol[baseIndex];
						color = freqColorArray[ colorIndex ];
					}
					
				}
				
				g2d.setColor(color);
				g2d.fillRect(colPos, y, cellWidth, rowHeight);
			}
				
			colPos += cellWidth;
		}
		
		int firstDrawCol = firstCol - firstCol%hashBlockSize;
		for(int blockStart=firstDrawCol; blockStart<Math.min(lastCol, seq.length()); blockStart+=hashBlockSize) {			
			drawBaseGroup(g2d, blockStart*cellWidth, y, cellWidth, rowHeight, row, blockStart, seq);	
		}

		
	}

	
	public static float[] getColumnBaseFreqs(SequenceGroup seqs, int colNum) {
		float[] freqs = new float[4];
		char[] col = seqs.getColumn(colNum);
		double counted = 0;

		for(int i=0; i<col.length; i++)
			switch (col[i]) {
				case 'A' :
					freqs[A]++;
					counted++;
					break;
				case 'C' : 
					freqs[C]++;
					counted++;
					break;
				case 'T' : 
					freqs[T]++;
					counted++;
					break;
				case 'G' : 
					freqs[G]++;
					counted++;
					break;
				case 'M' :
					freqs[A]+=0.5;
					freqs[C]+=0.5;
					counted++;
					break;
				case 'R' :
					freqs[A]+=0.5;
					freqs[G]+=0.5;
					counted++;
					break;
				case 'W' :
					freqs[A]+=0.5;
					freqs[T]+=0.5;
					counted++;
					break;
				case 'S' :
					freqs[G]+=0.5;
					freqs[C]+=0.5;
					counted++;
					break;
				case 'Y' :
					freqs[C]+=0.5;
					freqs[T]+=0.5;
					counted++;
					break;
				case 'V' :
					freqs[A]+=0.3333;
					freqs[C]+=0.3333;
					freqs[G]+=0.3333;
					counted++;
					break;
				case 'H' :
					freqs[A]+=0.3333;
					freqs[C]+=0.3333;
					freqs[T]+=0.3333;
					counted++;
					break;
				case 'D' :
					freqs[A]+=0.3333;
					freqs[G]+=0.3333;
					freqs[T]+=0.3333;
					counted++;
					break;
				case 'B' :
					freqs[C]+=0.3333;
					freqs[G]+=0.3333;
					freqs[T]+=0.3333;
					counted++;
					break;
				case 'N' :
					freqs[A]+=0.25;
					freqs[C]+=0.25;
					freqs[G]+=0.25;
					freqs[T]+=0.25;
					counted++;
					break;
				case 'X' :
					freqs[A]+=0.25;
					freqs[C]+=0.25;
					freqs[G]+=0.25;
					freqs[T]+=0.25;
					counted++;
					break;
			}

		for(int i=0; i<4; i++)
			freqs[i] /= counted;

		return freqs;
	}


	public static class Instantiator extends AbstractRowPainter.Instantiator {
		
		public AbstractRowPainter getNewRowPainter(SequenceGroup sg) {
			return new FrequencyRowPainter(sg);
		}
		
	}
}
