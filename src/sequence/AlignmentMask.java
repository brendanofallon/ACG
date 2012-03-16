package sequence;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * An AlignmentMask is a set of columns that are to be 'masked' by converting all
 * of their symbols to N. Instead of actually changing each base to N in the 
 * alignment (which would be difficult to undo) we instead have a 'mask' be a 
 * seperate entity that can easily be altered / removed. 
 * @author brendan
 *
 */
public class AlignmentMask {

	private List<Integer> maskedColumns = new ArrayList<Integer>();
	
	public AlignmentMask() {
		
	}
	
	/**
	 * Erase the current mask and change the masked columns to be only those indicated
	 * @param mask
	 */
	public void setMask(List<Integer> mask) {
		maskedColumns.clear();
		addColumns(mask);
	}
	
	/**
	 * Erase the current mask and change the masked columns to be only those indicated
	 * @param mask
	 */
	public void setMask(int[] mask) {
		maskedColumns.clear();
		addColumns(mask);
	}
	
	/**
	 * Obtain the list of masked columns - this is a reference to the actual data
	 * so behavior is undefined if it gets modified in some way
	 * @return
	 */
	public Integer[] getMaskedColumns() {
		return (Integer[])(maskedColumns.toArray(new Integer[]{}));
	}
	
	/**
	 * Obtain the masked column with the lowest index - the leftmost or first
	 * column that is masked, or -1 if there are no masked columns
	 * @return
	 */
	public int getFirstMaskedColumn() {
		if (maskedColumns.size()==0) {
			return -1;
		}
		else {
			return maskedColumns.get(0);
		}
	}
	
	/**
	 * Obtain the index on the masked column with the highest index, or -1 if there
	 * are no masked columns
	 * @return
	 */
	public int getLastMaskedColumn() {
		if (maskedColumns.size()==0)
			return -1;
		else {
			return maskedColumns.get(maskedColumns.size()-1);
		}
	}
	
	/**
	 * Returns true if the given column is masked
	 * @param col
	 * @return
	 */
	public boolean columnIsMasked(int col) {
		int index = Collections.binarySearch(maskedColumns, col);
		return index >= 0;
	}
	
	public void clearMask() {
		maskedColumns.clear();
	}
	
	/**
	 * Add all given columns to the list of masked columns
	 * @param columnsToAdd
	 */
	public void addColumns(List<Integer> columnsToAdd) {
		maskedColumns.addAll(columnsToAdd);
		sortAndUniquify();
	}
	
	public void addColumns(int[] columnsToAdd) {
		for(int i=0; i<columnsToAdd.length; i++)
			maskedColumns.add(columnsToAdd[i]);
		sortAndUniquify();
	}
	
	/**
	 * Add the given column to the list of masked columns
	 * @param col
	 */
	public void addColumn(int col) {
		maskedColumns.add(col);
		sortAndUniquify();
	}
	
	/**
	 * Uniquify and sort list of columns
	 */
	private void sortAndUniquify() {
		Set<Integer> cols = new HashSet<Integer>(maskedColumns);
		maskedColumns = new ArrayList<Integer>(cols);
		Collections.sort(maskedColumns);
	}
	
	
}
