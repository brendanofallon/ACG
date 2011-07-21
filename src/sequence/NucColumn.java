package sequence;

import java.util.BitSet;

/**
 * A column of nucleotides. Each character has only 4 possible states. These are immutable.  
 * @author brendan
 *
 */
public class NucColumn implements CharacterColumn {

	private final BitSet bits;
	
	private final int size;
	
	//First bit is 0..chars.length-1, next bit is in chars.length..2*chars.length-1
	//A = 0, 0
	//G = 0, 1
	//C = 1, 0
	//T = 1, 1
	
	public NucColumn(char[] chars) {
		bits = new BitSet(2*chars.length);
		size = chars.length;
		for(int i=0; i<chars.length; i++) {
			if (chars[i]=='A') {
				//dont actually do anything - both are zero
				continue;
			}
			if (chars[i]=='G') {
				bits.set(i+size);
			}
			if (chars[i]=='C') {
				bits.set(i);
			}
			if (chars[i]=='T') {
				bits.set(i);
				bits.set(i+size);
			}
		}
	}
	
	public char getSymbol(int which) {
		boolean first = bits.get(which);
		boolean second = bits.get(which+size);
		if (!first && !second) {
			return 'A';
		}
		if (!first && second) {
			return 'G';
		}
		if (first && !second) {
			return 'C';
		}
		if (first && second) {
			return 'T';
		}
		return '?';
	}
	

	public boolean isEqual(CharacterColumn col) {
		return isEqualBetween(col, 0, 2*size);	
	}


	public boolean isEqualBetween(CharacterColumn col, int start, int end) {
		BitSet otherBits = col.getBits(); 
		int i = bits.nextSetBit(start);
		int j = otherBits.nextSetBit(start);
		while (i==j && i>=0 && i<end) {
			i = bits.nextSetBit(i+1);
			j = otherBits.nextSetBit(j+1);	
		}
		
		return i==j;
	}

	public boolean isEqualAt(CharacterColumn col, int row) {
		return (col.getBits().get(row)==bits.get(row)) && (col.getBits().get(row+size)==bits.get(row+size) ); 
	}
	

	public BitSet getBits() {
		return bits;
	}

	@Override
	public int size() {
		return size;
	}
	

}
