package sequence;

/**
 * A collection of static methods for computing various values from an alignment
 * @author brendano
 *
 */
public class AlignmentMetrics {

	/**
	 * Computes the total number of columns at which a gap or unknown ('N') value is present
	 * @param aln
	 * @return
	 */
	public static int getNumGappedColumns(Alignment aln) {
		int gaps = 0;
		for(int i=0; i<aln.getSequenceLength(); i++) {
			if (aln.hasGapOrUnknown(i)) {
				gaps++;
			}
		}
		return gaps;
	}
	
	/**
	 * Returns the total number of alignment columns at which a polymorphism exists 
	 * @param aln
	 * @return
	 */
	public static int getPolymorphicColumns(Alignment aln) {
		int polys = 0;
		for(int i=0; i<aln.getSequenceLength(); i++) {
			if (isPolymorphic(aln, i)) {
				polys++;
			}
		}
		return polys;
	}
	
	/**
	 * Returns true if there are multiple bases present at this alignment column (sequences with gaps are ignored) 
	 * @param aln
	 * @param col
	 * @return
	 */
	public static boolean isPolymorphic(Alignment aln, int col) {
		boolean oneFound = false;
		int[] counts = aln.getBaseCounts(col);
		for(int i=0; i<4; i++) {
			if (counts[i] > 0 && oneFound)
				return true;
			if (counts[i] > 0)
				oneFound = true;
		}
		return false;
	}
	
	
	/**
	 * Returns nucleotide diversity (aka Pi) over the whole Alignment 
	 * 
	 */
	public static double getNucleotideDiversity(Alignment aln) {
		int i = 0;
		double sum = 0;
		double[] freqs = new double[5];
		for(i=0; i<aln.getSequenceLength(); i++) {
			freqs = getColumnBaseFreqs(aln, i, freqs);
			sum += 1-freqs[DNAUtils.A]*freqs[DNAUtils.A]-freqs[DNAUtils.C]*freqs[DNAUtils.C]-freqs[DNAUtils.T]*freqs[DNAUtils.T]-freqs[DNAUtils.G]*freqs[DNAUtils.G];
		}

		return sum;
	}
	
	public static double[] getColumnBaseFreqs(Alignment aln, int col, double[] freqs) {
		int[] counts = aln.getBaseCounts(col);
		for(int i=0; i<counts.length; i++) {
			freqs[i] = (double)counts[i] / (double)aln.getSequenceCount();
		}
		
		return freqs;
	}
	
	
	/**
	 * Computes the values of Tajima's D for the given alignment
	 * @param aln
	 * @return
	 */
	public static double getTajimasD(Alignment aln) {
			double pi = getNucleotideDiversity(aln);
			double S = getPolymorphicColumns(aln);
			double a1 =0;
			double a2 =0;
			
			if (S < 2)
				return 0;
			
			double n = aln.getSequenceCount();
			
			for(int i=1; i<n; i++) {
				a1 += 1.0/(double)i;
				a2 += 1.0/(double)(i*i);
			}
				
			
			double b1 = (n+1.0)/(3.0*(n-1.0));
			double b2 = 2.0*(n*n + n + 3.0)/(9.0*n*(n-1.0));
			double c1 = b1 - 1.0/a1;
			double c2 = b2 - (n+2.0)/(a1*n) + a2/(a1*a1);
		
			double e1 = c1/a1;
			double e2 = c2/(a1*a1 + a2);
			
			
			double dif = pi-S/a1;
			double D = dif / Math.sqrt(e1*S + e2*S*(S-1.0) );
			
			return D;
		}
}
