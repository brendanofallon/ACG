package newgui.gui.alignmentViewer.rowPainters;

import java.awt.Color;
import java.awt.Graphics2D;

import sequence.Alignment;
import sequence.DNAUtils;
import sequence.Sequence;


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
    
    
	public FrequencyRowPainter(Alignment sg) {
		super(sg);
		
	  	for(int i=0; i<colorArraySize; i++) {
	  		float frac = (float)i / (float)colorArraySize;
    		freqColorArray[i] = new Color((1f-0.75f*frac), (0.25f+0.5f*frac), 0.3f+0.7f*frac);
	  	}
	  	
    }
	
	public static AbstractRowPainter getNew(Alignment sg) {
		return new FrequencyRowPainter(sg);
	}
	
	public static String getIdentifier() {
		return "Frequency";
	}
	
	private void recalculateFreqs() {
		int columnCount = currentSG.getSequenceLength();
		colorIt = new boolean[columnCount];
		freqs = new int[columnCount][4];
		for(int i=0; i<columnCount; i++) {
			float[] frequencies = getColumnBaseFreqs(currentSG, i);
			boolean colorCol = currentSG.hasGapOrUnknown(i); //If there's a gap or unknown we color this column
			for(int j=0; j<4; j++) {
				freqs[i][j] = (int)((colorArraySize-1)*frequencies[j]);
				if (frequencies[j] > 0.0f && frequencies[j] < 1.0f) {
					colorCol = true;
				}
			}
			
			colorIt[i] = colorCol;
			
		}
		
		recalculateFreqs = false;
	}
	
	/**
	 * Force recalculation of frequencies array, this should happen after any rows / cols have been added or removed 
	 */
	public void setRecalculateFreqs() {
		recalculateFreqs = true;
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
		
		if (freqs == null || prevRowSize != currentSG.getSequenceCount() || prevColSize != currentSG.getSequenceLength() || recalculateFreqs) {
			recalculateFreqs();
			prevRowSize = currentSG.getSequenceCount();
			prevColSize = currentSG.getSequenceLength();
		}
	
		setCellSize(cellWidth, rowHeight);
	
		Color color = freqColorArray[ freqColorArray.length-1];
		g2d.setColor(color);
		g2d.fillRect(x+firstCol*cellWidth, y, cellWidth*(lastCol-firstCol), rowHeight);
		
		Sequence seq = currentSG.getSequence(row);
		int colPos = x+firstCol*cellWidth;
		for(int col=firstCol; col < Math.min(lastCol, seq.getLength()); col++) {
			if (colorIt[col]) {
				int baseIndex = -1;
				color = Color.LIGHT_GRAY;

				Integer value = seq.baseAt(col);
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
		for(int blockStart=firstDrawCol; blockStart<Math.min(lastCol, seq.getLength()); blockStart+=hashBlockSize) {			
			drawBaseGroup(g2d, blockStart*cellWidth, y, cellWidth, rowHeight, row, blockStart, seq);	
		}

		
	}

	
	public static float[] getColumnBaseFreqs(Alignment seqs, int colNum) {
		float[] freqs = new float[4];
		char[] col = seqs.getColumn(colNum);
		double counted = 0;

		for(int i=0; i<col.length; i++)
			switch (col[i]) {
				case 'A' :
					freqs[DNAUtils.A]++;
					counted++;
					break;
				case 'C' : 
					freqs[DNAUtils.C]++;
					counted++;
					break;
				case 'T' : 
					freqs[DNAUtils.T]++;
					counted++;
					break;
				case 'G' : 
					freqs[DNAUtils.G]++;
					counted++;
					break;
				case 'M' :
					freqs[DNAUtils.A]+=0.5;
					freqs[DNAUtils.C]+=0.5;
					counted++;
					break;
				case 'R' :
					freqs[DNAUtils.A]+=0.5;
					freqs[DNAUtils.G]+=0.5;
					counted++;
					break;
				case 'W' :
					freqs[DNAUtils.A]+=0.5;
					freqs[DNAUtils.T]+=0.5;
					counted++;
					break;
				case 'S' :
					freqs[DNAUtils.G]+=0.5;
					freqs[DNAUtils.C]+=0.5;
					counted++;
					break;
				case 'Y' :
					freqs[DNAUtils.C]+=0.5;
					freqs[DNAUtils.T]+=0.5;
					counted++;
					break;
				case 'V' :
					freqs[DNAUtils.A]+=0.3333;
					freqs[DNAUtils.C]+=0.3333;
					freqs[DNAUtils.G]+=0.3333;
					counted++;
					break;
				case 'H' :
					freqs[DNAUtils.A]+=0.3333;
					freqs[DNAUtils.C]+=0.3333;
					freqs[DNAUtils.T]+=0.3333;
					counted++;
					break;
				case 'D' :
					freqs[DNAUtils.A]+=0.3333;
					freqs[DNAUtils.G]+=0.3333;
					freqs[DNAUtils.T]+=0.3333;
					counted++;
					break;
				case 'B' :
					freqs[DNAUtils.C]+=0.3333;
					freqs[DNAUtils.G]+=0.3333;
					freqs[DNAUtils.T]+=0.3333;
					counted++;
					break;
				case 'N' :
					freqs[DNAUtils.A]+=0.25;
					freqs[DNAUtils.C]+=0.25;
					freqs[DNAUtils.G]+=0.25;
					freqs[DNAUtils.T]+=0.25;
					counted++;
					break;
				case 'X' :
					freqs[DNAUtils.A]+=0.25;
					freqs[DNAUtils.C]+=0.25;
					freqs[DNAUtils.G]+=0.25;
					freqs[DNAUtils.T]+=0.25;
					counted++;
					break;
			}

		for(int i=0; i<4; i++)
			freqs[i] /= counted;

		return freqs;
	}


	public static class Instantiator extends AbstractRowPainter.Instantiator {
		
		public AbstractRowPainter getNewRowPainter(Alignment sg) {
			return new FrequencyRowPainter(sg);
		}
		
	}
}
