package plugins.SGPlugin.display.rowPainters;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import element.sequence.*;
import element.codon.GeneticCode.AminoAcid;
import element.sequence.Sequence;
import errorHandling.ErrorWindow;

/**
 * Paints amino acids and colors according to frequency
 * @author brendan
 *
 */
public class AAFreqRowPainter extends AbstractRowPainter {

	Map<AminoAcid, Float>[] freqMaps;
	
	boolean recalculateFreqs = true;
	
	static int colorArraySize = 20;

    static Color freqColorArray[] = new Color[colorArraySize];
	
    int prevLength = 0;
    int prevSize = 0;
    
	public AAFreqRowPainter(SequenceGroup sg) {
		super(sg);
		
		for(int i=0; i<colorArraySize; i++) {
			double frac = (double)i/(double)colorArraySize;
    		freqColorArray[i] = new Color((int)Math.round(250-100*frac), (int)Math.round(160*(frac)), (int)Math.round(160*(frac)));
	  	}

		sg.addSGChangeListener(new SequenceGroupChangeListener() {
			public void sgChanged(SequenceGroup source, ChangeType type) {
				recalculateFreqs();
			}
		});
	}

	public AbstractRowPainter getNew(SequenceGroup sg) {
		return new AAFreqRowPainter(sg);
	}
	
	public void setSequenceGroup(SequenceGroup sg) {
		currentSG = sg;
		recalculateFreqs();
	}
	

	public static String getIdentifier() {
		return "Frequency (AA)";
	}

	private Map<AminoAcid, Float> getMapForCodon(int site) {
		Map<AminoAcid, Float> map = new HashMap<AminoAcid, Float>(4);
		Float count = 0f;
		for(int i=0; i<currentSG.size(); i++) {
			AminoAcid aa;
			if ( (site >= (frame+2)) && (site-frame < (currentSG.get(i).length()-2))) {
				if (revComp)
					aa = translator.translateRevComp(currentSG.get(i), site);
				else
					aa = translator.translate(currentSG.get(i), site);
				count++;
				Float freq = map.get(aa);
				if (freq==null)
					map.put(aa, 1.0f);
				else
					map.put(aa, freq+1f);
			}
		}
		
		Set<AminoAcid> aas = map.keySet();
		for(AminoAcid aa : aas) {
			map.put(aa, (Float)(map.get(aa)/count));
			
		}
		return map;
	}
	
	private void recalculateFreqs() {
		freqMaps = new Map[currentSG.getMaxSeqLength()];
		int max = currentSG.getMaxSeqLength();
		for(int i=0; i<max; i++) {
			if ( (i-frame-2)%3==0 && (i >= (frame+2)) && (i-frame < (max-2))) {
				freqMaps[i] = getMapForCodon(i-2);
			}
		}
		recalculateFreqs = false;
	}
	
	@Override
	public void paintRow(Graphics2D g2d, int row, int firstCol, int lastCol,
			int x, int y, int cellWidth, int rowHeight) {

		if ((!translate) || translator == null) {
			ErrorWindow.showErrorWindow(new IllegalStateException("The Amino Acid painter must have a translator set before it can be used to paint"));
			return;
		}
		
		Sequence seq = currentSG.get(row);
		
		setCellSize(cellWidth, rowHeight);
		
		if (recalculateFreqs) {
			recalculateFreqs();
		}
		
		g2d.setFont(font);
		g2d.setColor(Color.black);
		for(int i=firstCol; i<Math.min(lastCol, seq.length()); i++) {
			base[0] = seq.at(i);
			
			if ( (i-frame-2)%3==0 && (i >= (frame+2)) && (i-frame < (currentSG.get(row).length()-2))) {

				AminoAcid aa = null;
				if (revComp)
					aa = translator.translateRevComp(currentSG.get(row), i-2 );
				else {
					System.out.println("Translating row " + row + " which is " + currentSG.get(row).length() + " sites, starting from " + (i-2));
					aa = translator.translate(currentSG.get(row), i-2 );
				}
				if (aa==null) {
					//System.err.println("Could not translate codon.. got null AA");
					
					drawChar(g2d, x+(i)*cellWidth+1, y+rowHeight, cellWidth, row, '?');
				}
				else {
					Color color = Color.white; 
					if (freqMaps[i]!=null) {
						Map<AminoAcid, Float> map = freqMaps[i]; 
						
						Float freq = map.get(aa);
						if (freq==null) {
							System.out.println("Freq is null, amino acid is: " + aa);
						}
						else {
							color = freqColorArray[ (int)Math.round((freqColorArray.length-1.0)*freq) ];
						}
					}
					g2d.setColor(color);
					g2d.fillRect(x+(i-2)*cellWidth, y, 3*cellWidth, rowHeight);
					
					char c = translator.aaToChar(aa);
					drawChar(g2d, x+(i)*cellWidth+1, y+rowHeight, cellWidth, row, c);	
				}
				
				
			}
				

		}

		
	}
	
	public static class Instantiator extends AbstractRowPainter.Instantiator {
		
		public AbstractRowPainter getNewRowPainter(SequenceGroup sg) {
			return new AAFreqRowPainter(sg);
		}
		
	}

}
