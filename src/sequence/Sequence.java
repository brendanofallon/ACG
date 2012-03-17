package sequence;

/**
 * Interface for all things that are a labelled nucleotide sequences
 * @author brendan
 *
 */
public interface Sequence {

	/**
	 * A (hopefully) unique label for this sequence
	 * @return
	 */
	public String getLabel();
	
	/**
	 * The number of elements (usually nucleotides) in this sequence
	 * @return
	 */
	public int getLength();
	
	/**
	 * Return the integer representation of the element (usually a nucleotide base) at the given position
	 * @param pos
	 * @return
	 */
	public int baseAt(int pos);
	
	/**
	 * Converts the character at the given position to BaseMap.N
	 * @param pos
	 */
	public void mask(int pos);
	
	/**
	 * Return the char representation of the base at the given position in this sequence
	 * @param pos
	 * @return
	 */
	public char charAt(int pos);
	
	/**
	 * Obtain a String representing the bases in this sequence
	 * @param map
	 * @return
	 */
	public String getSequenceString();

	/**
	 * Set the label of this sequence
	 * @param string
	 */
	public void setLabel(String string);
	
	/**
	 * A unique id value for this sequence
	 * @return
	 */
	public int uniqueNumber();
	
	/**
	 * Remove columns at given indices
	 * @param columns
	 */
	public void removeCols(int[] columns);
	
	/**
	 * Create a new Sequence that has the current sequence label but only those
	 * bases present at the given columns
	 * @param cols
	 * @return
	 */
	public Sequence newFromColumns(int[] cols);  
	
	/**
	 * Obtain an array of character bases representing the values from [start..end)
	 * @param start
	 * @param end
	 * @return
	 */
	public void toCharArray(int start, int end, char[] output);
}
