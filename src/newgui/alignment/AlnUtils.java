package newgui.alignment;

import java.util.HashMap;
import java.util.Map;

/**
 * Some handy static utilities for sequence alignments
 * @author brendan
 *
 */
public class AlnUtils {

	/**
	 * Returns the integer corresponding to the most frequently observed base
	 * at the given column. If multiple bases are seen with equal frequency a
	 * random one is selected. 
	 * @param col
	 * @return
	 */
	public static int getConsensusForCol(int[] col) {
		Map<Integer, Integer> counts = new HashMap<Integer, Integer>();
		int maxKey = col[0];
		int maxCount = 1;
		
		for(int i=0; i<col.length; i++) {
			Integer key = col[i];
			Integer count = counts.get(key);
			if (count == null) 
				count = 1;
			else
				count++;
			
			counts.put(key, count);
			if (count > maxCount) {
				maxCount = count;
				maxKey = key;
			}
		}
		
		
		return maxKey;
	}
	
}
