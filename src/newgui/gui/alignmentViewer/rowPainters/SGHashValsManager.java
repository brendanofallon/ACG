package newgui.gui.alignmentViewer.rowPainters;

import java.util.HashMap;
import java.util.Map;

import sequence.Alignment;
import sequence.Sequence;


/**
 * A small class that handles mappings from sequences to lists of hash values
 * 
 * Optimization: This thing stupidly stores a map from char to int, but chars ARE Int's! 
 * 
 * @author brendan
 *
 */
public class SGHashValsManager {

	private int blockSize;
	private Map<Integer, int[]> hashMapMap = new HashMap<Integer, int[]>();
	private Map<Character, Integer> baseIntMap = new HashMap<Character, Integer>();
	
	public SGHashValsManager(int blockSize) {
		this.blockSize = blockSize;
		constructBaseIntMap();
	}
	
	/**
	 * Add all sequences in the Alignment to those tracked 
	 * @param sg
	 */
	public void addSequences(Alignment sg) {
		for(int i=0; i<sg.getSequenceCount(); i++) {
			addSequence(sg.getSequence(i));
		}
	}
	
	/**
	 * Add the sequence to the list of those sequences whose hash values we know. This checks for
	 * redundancy and makes sure that we never add the same sequence more than once. We also
	 * listen for change events for this sequence, so if the sequence changes we update the 
	 * hash values 
	 * @param seq
	 * @return
	 */
	public boolean addSequence(Sequence seq) {		
		int[] hashValues = new int[seq.getLength()/blockSize];
		constructSequenceHash(seq, hashValues);
		hashMapMap.put(seq.uniqueNumber(), hashValues);
		return true;
	}
	
	/**
	 * Returns the integer array of hash values associated with blocks of contiguous bases from
	 * this sequence. 
	 * @param seq
	 * @return
	 */
	public int[] getHashForSequence(Sequence seq) {
		return hashMapMap.get(seq.uniqueNumber());
	}
	
	/**
	 * Compute hash values for blocks of bases along this sequences, storing the values in 'hashes'
	 * This does not compute the hash for the last (seq.length % hashBlockSize) bases in the 
	 * sequence 
	 * @param seq
	 * @param hashes
	 */
	public void constructSequenceHash(Sequence seq, int[] hashes) {
		int startSite = 0;
		for(int i=0; i<hashes.length; i++) {
			hashes[i] = createBaseHash(seq, startSite, blockSize);
			startSite += blockSize;
		}
	}
	
	/**
	 * A hash function that returns an integer for a subsequence of the given sequence starting 
	 * at position index and extending for size bases. If the sequence contains bases that are
	 * 'unknown', then the hash function returns -1, indicating that we should paint this block
	 * of sequences one base at a time
	 * 
	 * TODO: Given that chars ARE integers, do we need a baseIntMap? 
	 * @param seq 	
	 * @param index
	 * @param size
	 * @return An integer unique to the subsequence from index..index+size of the sequence
	 */
	public int createBaseHash(Sequence seq, int index, int size) {
		int hash = 0;
		int prod = 1;
		for(int i=index; i<(index+size); i++) {
			Integer val = baseIntMap.get(seq.baseAt(i));
			if (val == null)
				return -1;
			hash += val*prod;
			prod *= 10;
		}
		return hash;
	}
	
	
	public void removeSequence(Sequence seq) {
		hashMapMap.remove(seq.uniqueNumber());
	}
	
	private void constructBaseIntMap() {
		baseIntMap.clear();
		baseIntMap.put('A', A);
		baseIntMap.put('C', C);
		baseIntMap.put('G', G);
		baseIntMap.put('T', T);
		baseIntMap.put('S', S);
		baseIntMap.put('R', R);
		baseIntMap.put('Y', Y);
		baseIntMap.put('M', M);
		baseIntMap.put('W', W);
		baseIntMap.put('-', GAP);
		baseIntMap.put(' ', GAP);
		baseIntMap.put('?', GAP);
	}
	
	public static final int A = 0;
	public static final int C = 1;
	public static final int G = 2;
	public static final int T = 3;
	public static final int S = 4;
	public static final int R = 5;
	public static final int Y = 6;
	public static final int M = 7;
	public static final int W = 8;
	public static final int GAP = 9;

	
}
