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
	 * Obtain a String representing the bases in this sequence, using the given map for conversion from int to char
	 * @param map
	 * @return
	 */
	public String getSequenceString(BaseMap map);

	/**
	 * Set the label of this sequence
	 * @param string
	 */
	public void setLabel(String string);
}
