package sequence;

import newgui.alignment.UnrecognizedBaseException;

/**
 * A simple mapping between characters representing nucleotide bases and integers
 * @author brendan
 *
 */
public class BaseMap {
	
	public static final int A = 0;
	public static final int C = 1;
	public static final int G = 2;
	public static final int T = 3;
	public static final int N = 4;
	public static final int R = 5;
	public static final int Y = 6;
	public static final int GAP = 7;
	
	private static BaseMap defaultBaseMap = null;
	
	/**
	 * Obtain the default BaseMap. This is mostly just so we aren't creating a new BaseMap
	 * every time we want to convert a single character or int
	 * @return
	 */
	public static BaseMap getDefaultBaseMap() {
		if (defaultBaseMap == null)
			defaultBaseMap = new BaseMap();
		return defaultBaseMap;
	}
	
	
	/**
	 * Obtain the integer representation of the given base symbol, case insensitive
	 * @param base Base to obtain value for, any case is OK
	 * @return Value representing the base
	 * @throws UnrecognizedBaseException Symbol could not be interpreted
	 */
	public int valForBase(char base) throws UnrecognizedBaseException {
		char c = Character.toUpperCase(base);
		if (c == 'A') return A;
		if (c == 'C') return C;
		if (c == 'T') return T;
		if (c == 'G') return G;
		if (c == 'N') return N;
		if (c == 'R') return R;
		if (c == 'Y') return Y;
		if (c == '-') return GAP;
		
		throw new UnrecognizedBaseException("Base " + base + " not recognized");
	}
	
	/**
	 * Obtain a character representing the given the numerical version of a base, this is the
	 * opposite mapping of valForBase
	 * @param val
	 * @return Character representing the base
	 * @throws UnrecognizedBaseException
	 */
	public char baseForVal(int val) {
		if (val == A) return 'A';
		if (val == C) return 'C';
		if (val == G) return 'G';
		if (val == T) return 'T';
		if (val == N) return 'N';
		if (val == R) return 'R';
		if (val == Y) return 'Y';
		if (val == GAP) return '-';
		return '?';
	}
	
	/**
	 * Convert an array of integer base values to an array of character bases
	 * @param vals
	 * @return
	 */
	public char[] basesForVals(int[] vals) {
		char[] bases = new char[vals.length];
		for(int i=0; i<vals.length; i++) {
			bases[i] = baseForVal(vals[i]);
		}
		return bases;
	}
	
}
