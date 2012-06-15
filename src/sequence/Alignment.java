package sequence;

import java.util.List;

import newgui.datafile.AlignmentFile;


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
	 * Obtain the source alignmentFile associated with this file, may be null if no source 
	 * has been set
	 * @return
	 */
	public AlignmentFile getSourceFile();
	
	/**
	 * Set the source file associated with this alignment file.
	 * @param source
	 */
	public void setSourceFile(AlignmentFile source);
	
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

	/**
	 * Obtain an array of bases across all sequences at the given alignment column
	 * @param column
	 * @return
	 */
	public char[] getColumn(int column);

	/**
	 * True if there is a gap at the given column
	 * @param i
	 * @return
	 */
	public boolean hasGapOrUnknown(int i);

	/**
	 * Remove the given columns from this alignment
	 * @param cols
	 */
	public void removeCols(int[] cols);

	/**
	 * Remove the given rows (sequences) from this alignment
	 * @param cols
	 */
	public void removeRows(int[] rows);
	
	
	/**
	 * Create a new alignment with the same sequences/labels, but only the given columns
	 * @param cols
	 */
	public Alignment newAlignmentFromColumns(int[] cols);
	
	/**
	 * Obtain the AlignmentMask associated with this Alignment, may be null
	 * if no mask has been set
	 * @return
	 */
	public AlignmentMask getMask();
	
	/**
	 * Clear the current AlignmentMask and set the mask to be the given
	 * object
	 * @param mask
	 */
	public void setAlignmentMask(AlignmentMask mask);
	
	
	/**
	 * Permanently convert sites in all currently masked columns to N's in all sequences 
	 * in this alignment
	 */
	public void applyMask();
	
	/**
	 * Return an array containing the number of sequences containing the corresponding base at the given
	 * alignment column. For instance, array[DNAUtils.A] is the number of sequences containing an A
	 * @param col
	 * @return
	 */
	public int[] getBaseCounts(int col);
	
}
