package sequence;

import java.util.BitSet;

public interface CharacterColumn {

	/**
	 * OK.. not really clear if this should be here, but we need a way to get the bits that describe the character col
	 * for speedy access
	 * @return
	 */
	public BitSet getBits();
	
	/**
	 * Return the number of symbols in this row. 
	 * @return
	 */
	public int size();
	/**
	 * Return the symbol (i.e. A,C,T,G) associated with the state at the given row
	 */
	public char getSymbol(int row);
	
	/**
	 * Returns true if this character column has identical character states to the given col
	 * @param col
	 * @return
	 */
	public boolean isEqual(CharacterColumn col);
	
	/**
	 * Returns true if this character col has identical characters to the other col from start..end, in a HALF-OPEN
	 * interval which includes all sites from start..end-1 
	 * @param col
	 * @param start
	 * @param end
	 * @return
	 */
	public boolean isEqualBetween(CharacterColumn col, int start, int end);
	
	/**
	 * Returns true if this column is identical to the other at the given row
	 * @param col
	 * @param row
	 * @return
	 */
	public boolean isEqualAt(CharacterColumn col, int row);
	
}
