package newgui.alignment;

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
	 * Set a reference sequence to which this will be compared for polymorphism
	 * @param ref
	 */
	public void setReference(Sequence ref);
	
	public Sequence getReference();
	
	/**
	 * Returns true if this sequence differs from the reference at the given site
	 * @param col
	 * @return
	 */
	public boolean differsFromReference(int col);
}
