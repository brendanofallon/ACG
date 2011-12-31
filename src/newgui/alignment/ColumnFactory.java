package newgui.alignment;

import java.util.List;

/**
 * A class for quickly creating the columns of an alignment
 * @author brendan
 *
 */
public class ColumnFactory {

	private final Alignment aln;
	
	public ColumnFactory(Alignment aln) {
		this.aln = aln;
	}
	
	public ColumnFactory(List<Sequence> seqs) {
		this.aln = new BasicAlignment(seqs);
	}
	
	public int getColumnCount() {
		return aln.getSequenceLength();
	}
	
	public int getColumnSize() {
		return aln.getSequenceCount();
	}
	
	/**
	 * Obtain the base-values (obtained by baseAt(pos)) of all sequences at the given column
	 * @param pos
	 * @return
	 */
	public int[] getColumn(int pos) {
		int[] col = new int[getColumnSize()];
		for(int i=0; i<col.length; i++) {
			col[i] = aln.getSequence(i).baseAt(pos);
		}
		return col;
	}

	/**
	 * Load the base-values (obtained by baseAt(pos)) of all sequences at the given position
	 * into the given array
	 * @param pos Position at which to get column
	 */
	public void getColumn(int pos, int[] col) {
		for(int i=0; i<col.length; i++) {
			col[i] = aln.getSequence(i).baseAt(pos);
		}
	}
	
}
