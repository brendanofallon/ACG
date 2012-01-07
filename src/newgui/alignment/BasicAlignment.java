package newgui.alignment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sequence.Alignment;
import sequence.AlnUtils;
import sequence.Sequence;
import sequence.SimpleSequence;

/**
 * A bare-bones alignment implementation, where no attempt is made to compress the alignments
 * @author brendan
 *
 */
public class BasicAlignment implements Alignment {

	protected List<Sequence> seqs = new ArrayList<Sequence>();
	
	public BasicAlignment(List<Sequence> seqs) {
		this.seqs = seqs;
	}
	
	public BasicAlignment() {
		//Blank on purpose, creates empty alignment
	}
	
	@Override
	public List<String> getLabels() {
		List<String> labels = new ArrayList<String>();
		for(Sequence s : seqs) {
			labels.add(s.getLabel());
		}
		return labels;
	}

	@Override
	public int getSequenceCount() {
		return seqs.size();
	}

	@Override
	public int getSequenceLength() {
		if (seqs.size()==0)
			return 0;
		else
			return seqs.get(0).getLength();
	}

	@Override
	public Sequence getSequenceForLabel(String label) {
		for(Sequence s : seqs) {
			if (s.getLabel().equals(label)) 
				return s;
		}
		return null;
	}

	@Override
	public Sequence getSequence(int index) {
		return seqs.get(index);
	}

	@Override
	public void addSequence(Sequence seq) {
		seqs.add(seq);
		seq.setReference(reference);
	}

	@Override
	public boolean removeSequence(Sequence seqToRemove) {
		return seqs.remove(seqToRemove);
	}


	public Sequence getReference() {
		return reference;
	}
	
	public void setReference(Sequence ref) {
		this.reference = ref;
		for(Sequence seq : seqs) {
			seq.setReference(ref);
		}
	}
	
	public Sequence getConsensus() {
		int[] refBases = new int[getSequenceLength()];
		for(int i=0; i<getSequenceLength(); i++) {
			refBases[i] = getConsensusForCol(i);
		}
		return new SimpleSequence("Consensus", refBases);
	}
	
	/**
	 * Returns the integer corresponding to the most frequently observed base
	 * at this column. If multiple bases are seen with equal frequency a
	 * random one is selected. 
	 * @param col
	 * @return
	 */
	private int getConsensusForCol(int col) {
		int[] colBases = new int[seqs.size()];
		for(int i=0; i<seqs.size(); i++) 
			colBases[i] = seqs.get(i).baseAt(col);
		return AlnUtils.getConsensusForCol(colBases);
		
//		Map<Integer, Integer> counts = new HashMap<Integer, Integer>();
//		int maxKey = seqs.get(0).baseAt(col);
//		int maxCount = 1;
//		
//		for(int i=0; i<seqs.size(); i++) {
//			Integer key = seqs.get(i).baseAt(col);
//			Integer count = counts.get(key);
//			if (count == null) 
//				count = 1;
//			else
//				count++;
//			
//			counts.put(key, count);
//			if (count > maxCount) {
//				maxCount = count;
//				maxKey = key;
//			}
//		}
//		
//		
//		return maxKey;
	}
	
	private Sequence reference = null;

}
