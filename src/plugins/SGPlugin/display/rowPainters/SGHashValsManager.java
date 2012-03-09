package plugins.SGPlugin.display.rowPainters;

import java.util.HashMap;
import java.util.Map;

import element.sequence.Sequence;
import element.sequence.SequenceChangeListener;
import element.sequence.SequenceGroup;

/**
 * A small class that handles mappings from sequences to lists of hash values
 * @author brendan
 *
 */
public class SGHashValsManager implements SequenceChangeListener {

	private int blockSize;
	private Map<Integer, int[]> hashMapMap = new HashMap<Integer, int[]>();
	private Map<Character, Integer> baseIntMap = new HashMap<Character, Integer>();
	
	public SGHashValsManager(int blockSize) {
		this.blockSize = blockSize;
		constructBaseIntMap();
	}
	
	/**
	 * Add all sequences in the SequenceGroup to those tracked 
	 * @param sg
	 */
	public void addSequences(SequenceGroup sg) {
		for(Sequence seq : sg.getSequences()) {
			addSequence(seq);
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
		//Don't bother if it's already in there
		if (hashMapMap.containsKey(seq.myNumber))
			return false;
		
		seq.addSequenceChangeListener(this);
		int[] hashValues = new int[seq.length()/blockSize];
		constructSequenceHash(seq, hashValues);
		hashMapMap.put(seq.myNumber, hashValues);
		return true;
	}
	
	/**
	 * Returns the integer array of hash values associated with blocks of contiguous bases from
	 * this sequence. 
	 * @param seq
	 * @return
	 */
	public int[] getHashForSequence(Sequence seq) {
		return hashMapMap.get(seq.myNumber);
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
	 * @param seq 	
	 * @param index
	 * @param size
	 * @return An integer unique to the subsequence from index..index+size of the sequence
	 */
	public int createBaseHash(Sequence seq, int index, int size) {
		int hash = 0;
		int prod = 1;
		for(int i=index; i<(index+size); i++) {
			Integer val = baseIntMap.get(seq.at(i));
			if (val == null)
				return -1;
			hash += val*prod;
			prod *= 10;
		}
		return hash;
	}
	
	@Override
	/**
	 * Handles sequence change events. We remove the old hash values from the map, and then re-add
	 * the sequence. 
	 */
	public void sequenceChanged(Sequence source, SequenceEventType eventType) {
//		if (! hashMapMap.containsKey(source.myNumber))
//			throw new IllegalArgumentException("HashValuesManager recieved a sequence change event from a sequence we don't track!");
//		
//		hashMapMap.remove(source.myNumber);
		
		int[] hashValues = new int[source.length()/blockSize];
		constructSequenceHash(source, hashValues);
		hashMapMap.put(source.myNumber, hashValues);
		//System.out.println("SGHashValsManager : Sequence " + source.getName() + " has changed, updating hash values");
	}
	
	public void removeSequence(Sequence seq) {
		hashMapMap.remove(seq.myNumber);
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
