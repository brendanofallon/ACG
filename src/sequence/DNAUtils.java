package sequence;

public class DNAUtils implements Alphabet {

	//Define these in alphabetical order
	public static final int T = 3;
	public static final int C = 1;
	public static final int A = 0;
	public static final int G = 2;
	public static final int GAP = 4;
	
	public static int intForBase(char base) {
		switch(base) {
		case 'A' : return A;
		case 'C' : return C;
		case 'T' : return T;
		case 'G' : return G;
		case '-' : return GAP;
		case '?' : return GAP;
		case 'N' : return GAP;
		}
		throw new IllegalArgumentException("Invalid base : '" + base + "'");
	}
	
	public static char baseForInt(int i) {
		if (i==A) return 'A';
		if (i==C) return 'C';
		if (i==T) return 'T';
		if (i==G) return 'G';
		if (i==GAP) return '?';
		throw new IllegalArgumentException("Invalid state number : " + i);
	}


	public int symbolToInt(char base) {
		return intForBase(base);
	}


	public char intToSymbol(int i) {
		return baseForInt(i);
	}


	public int getSymbolCount() {
		return 5;
	}
}
