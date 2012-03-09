package plugins.SGPlugin.display.rowPainters;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.HashMap;
import java.util.Map;

import element.sequence.*;
import element.codon.GeneticCode;
import element.codon.GeneticCode.AminoAcid;
import element.sequence.Sequence;
import errorHandling.ErrorWindow;

public class AminoAcidRowPainter extends AbstractRowPainter {

	static Map<AminoAcid, Color> aaColors;
	
	public AminoAcidRowPainter(SequenceGroup sg) {
		super(sg);

		if (aaColors == null) {
			aaColors = new HashMap<AminoAcid, Color>();
//			aaColors.put(AminoAcid.Ile, new Color(252, 10, 10)); // Hydropathy 4.5
//			aaColors.put(AminoAcid.Val, new Color(252, 15, 50)); // Hydropathy 4.2
//			aaColors.put(AminoAcid.Leu, new Color(252, 15, 90)); // 3.8
//			aaColors.put(AminoAcid.Phe, new Color(252, 15, 130)); // 2.8
//			aaColors.put(AminoAcid.Cys, new Color(252, 15, 145)); // 2.5
//			aaColors.put(AminoAcid.Met, new Color(252, 15, 175)); // 1.9
//			aaColors.put(AminoAcid.Ala, new Color(252, 15, 185)); // 1.8
//			aaColors.put(AminoAcid.Gly, new Color(252, 15, 225)); // -0.4
//			aaColors.put(AminoAcid.Thr, new Color(252, 15, 235)); // -0.7
//			aaColors.put(AminoAcid.Ser, new Color(245, 15, 245)); // -0.8
//			aaColors.put(AminoAcid.Trp, new Color(238, 15, 252)); // -0.9
//			aaColors.put(AminoAcid.Tyr, new Color(190, 15, 252)); // -1.3
//			aaColors.put(AminoAcid.Pro, new Color(156, 15, 252)); // -1.6
//			aaColors.put(AminoAcid.His, new Color(105, 15, 252)); // -3.2
//			aaColors.put(AminoAcid.Asn, new Color(95, 15, 252));  // -3.5
//			aaColors.put(AminoAcid.Asp, new Color(65, 30, 252));  // -3.5
//			aaColors.put(AminoAcid.Glu, new Color(35, 60, 252));  // -3.5
//			aaColors.put(AminoAcid.Gln, new Color(15, 95, 252)); //  -3.5
//			aaColors.put(AminoAcid.Lys, new Color( 15, 135, 252)); // -3.9
//			aaColors.put(AminoAcid.Arg, new Color( 15, 200, 252)); //-4.5
			
			aaColors.put(AminoAcid.Ile, new Color(10, 252, 10)); // Hydropathy 4.5
			aaColors.put(AminoAcid.Val, new Color(15, 252, 50)); // Hydropathy 4.2
			aaColors.put(AminoAcid.Leu, new Color(15, 252, 90)); // 3.8
			aaColors.put(AminoAcid.Phe, new Color(15, 252, 130)); // 2.8
			aaColors.put(AminoAcid.Cys, new Color(15, 252, 145)); // 2.5
			aaColors.put(AminoAcid.Met, new Color(15, 252, 175)); // 1.9
			aaColors.put(AminoAcid.Ala, new Color(15, 252, 185)); // 1.8
			aaColors.put(AminoAcid.Gly, new Color(15, 252, 225)); // -0.4
			aaColors.put(AminoAcid.Thr, new Color(15, 252, 235)); // -0.7
			aaColors.put(AminoAcid.Ser, new Color(15, 245, 245)); // -0.8
			aaColors.put(AminoAcid.Trp, new Color(15, 238, 252)); // -0.9
			aaColors.put(AminoAcid.Tyr, new Color(15, 190, 252)); // -1.3
			aaColors.put(AminoAcid.Pro, new Color(15, 156, 252)); // -1.6
			aaColors.put(AminoAcid.His, new Color(15, 105, 252)); // -3.2
			aaColors.put(AminoAcid.Asn, new Color(15, 95, 252));  // -3.5
			aaColors.put(AminoAcid.Asp, new Color(15, 65, 252));  // -3.5
			aaColors.put(AminoAcid.Glu, new Color(15, 35, 252));  // -3.5
			aaColors.put(AminoAcid.Gln, new Color(15, 15, 252)); //  -3.5
			aaColors.put(AminoAcid.Lys, new Color(15, 15, 202)); // -3.9
			aaColors.put(AminoAcid.Arg, new Color(15, 15, 152)); //-4.5

			aaColors.put(AminoAcid.Stop, Color.DARK_GRAY);
		}
	}

	public static AbstractRowPainter getNew(SequenceGroup sg) {
		return new AminoAcidRowPainter(sg);
	}
	
	public static String getIdentifier() {
		return "Amino acid";
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
		
		g2d.setFont(font);
		g2d.setColor(Color.black);
		for(int i=firstCol; i<Math.min(lastCol, seq.length()); i++) {
			base[0] = seq.at(i);
			
			if ( (i-frame-2)%3==0 && (i >= (frame+2)) && (i-frame < (currentSG.get(row).length()-2))) {
//				String codon = currentSG.get(row).toString().substring(i-2, i-2+3);
//				if (i<5) {
//					System.out.println("Translating codon at i=" + i + " frame: " + frame + " which is: " + codon);
//				}
				AminoAcid aa = null;
				if (revComp)
					aa = translator.translateRevComp(currentSG.get(row), i-2 );
				else
					aa = translator.translate(currentSG.get(row), i-2 );
				
				if (aa==null) {
					//System.err.println("Could not translate codon.. got null AA");
					
					drawChar(g2d, x+(i)*cellWidth+1, y+rowHeight, cellWidth, row, '?');
				}
				else {
					Color color = aaColors.get(aa);
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
			return new AminoAcidRowPainter(sg);
		}
		
	}

}
