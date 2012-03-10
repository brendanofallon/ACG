package sequence;

import java.util.List;


/**
 * A collection of Sequences where all sequences have the same length
 * @author brendan
 *
 */
public interface Alignment {

	
	/**
	 * Return labels of all sequences
	 * @return
	 */
	public List<String> getLabels();
	
	/**
	 * The number of sequences in this alignment
	 * @return
	 */
	public int getSequenceCount();
	
	/**
	 * The length of the sequences in this alignment (typically they should all be the same)
	 * @return
	 */
	public int getSequenceLength();
	
	/**
	 * Obtain the sequence associated with the given label
	 * @param label
	 * @return
	 */
	public Sequence getSequenceForLabel(String label);
	
	/**
	 * Obtain the sequence at the given index
	 * @param index
	 * @return
	 */
	public Sequence getSequence(int index);
	
	/**
	 * Add given sequence to this alignment
	 * @param seq
	 */
	public void addSequence(Sequence seq);
	
	/**
	 * Remove this given sequence from this alignment
	 * @param seqToRemove
	 * @return True if sequence existed in this alignment, false otw
	 */
	public boolean removeSequence(Sequence seqToRemove);

	
//	/**
//	 * Get the reference sequence for this alignment
//	 * @return
//	 */
//	public Sequence getReference();
//	
//	/**
//	 * Set the reference sequence for this alignment and add Sequences it contains
//	 * @param ref
//	 */
//	public void setReference(Sequence ref);
//	
//	/**
//	 * Obtain a sequence representing the consensus of all sequences 
//	 * @return
//	 */
//	public Sequence getConsensus();
}
