package tools.alnGen;

import java.util.ArrayList;
import java.util.List;

import newgui.alignment.UnrecognizedBaseException;

import sequence.DNAUtils;
import sequence.Sequence;
import sequence.SimpleSequence;


/**
 * A mutable sequence that is used to temporarily store sequence data for use
 * with the AlignmentGenerator
 * @author brendan
 *
 */
public class ProtoSequence {

	public static final String GAP = "-";
	
	//Creates a mapping from reference position to index in the sequence
	//It basically just contains a list of pairs of numbers that describe what the reference
	//pos is at actual sites, so we can easily look up a real index given a reference pos
	List<Pair> refMap = new ArrayList<Pair>();
	StringBuilder seq;
	String sampleName = null;
	
	public ProtoSequence(String str) {
		seq = new StringBuilder(str);
		Pair first = new Pair(1, 0); //Since the StringBuider is zero-indexed and the variants are 1-sequenced
		refMap.add(first);
	}
	
	public ProtoSequence(String str, int refStartPos) {
		seq = new StringBuilder(str);
		Pair first = new Pair(refStartPos, 0); //Since the StringBuider is zero-indexed and the variants are 1-sequenced
		refMap.add(first);
	}
	
	/**
	 * Associate an arbitrary sample name with this proto sequence
	 * @param name
	 */
	public void setSampleName(String name) {
		this.sampleName = name;
	}
	
	/**
	 * Returns the lowest index in this sequence that corresponds to the given reference
	 * position 
	 * @param refPos
	 * @return
	 */
	public int seqIndexForRefPos(int refPos) {
		Pair prev = refMap.get(0);
		for(Pair p : refMap) {
			
			//Sanity check, this can maybe be removed sometime
			if (prev.refPos > p.refPos || prev.actualIndex > p.actualIndex) {
				throw new IllegalStateException("Ref-map list is corrupted...");
			}
			
			if (prev.refPos <= refPos && p.refPos > refPos) {
				return prev.actualIndex + (refPos - prev.refPos);
			}
			prev = p;
		}
		return prev.actualIndex + (refPos - prev.refPos);
	}
	
	public char getBaseForRef(int refPos) {
		return seq.charAt( seqIndexForRefPos(refPos) );
	}
	
	public void applyVariant(Variant var, int phase) {
		applyVariant(var, phase, emptyFilterList);
	}
	
	/**
	 * Cause the variation described in the variant object to be applied to this sequence. 
	 * Currently, insertions are not supported, and deletions cause the affected sites to be replaced 
	 * with gaps
	 * @param var
	 * @param phase
	 */
	public void applyVariant(Variant var, int phase, List<VariantFilter> filters) {
		String alt = null;
		if (phase==0) {
			alt = var.getAlt0();
		}
		if (phase==1)
			alt = var.getAlt1();
		if (alt.equals( var.getRef() )) {
			//System.out.println("Alt equals ref at this site, ignoring (alt:" + alt  + " ref:" + var.getRef() +")");
			return; //no variant, do nothing
		}
		
		boolean maskVariant = false;
		for(VariantFilter filter : filters) {
			if (! filter.variantPasses(var)) {
				maskVariant = true;
				break;
			}
		}
		
		//Altering wont change mapping
		if (alt.length()==1) {
			int refPos = var.getPos(); //This number is in reference coords
			int seqPos = seqIndexForRefPos(refPos); //Ref coords converted to indices for string
			
			if (maskVariant)
				alt = "N";
			
			System.out.println("Replacing base at site " + seqPos + " with " + alt);
			seq.replace(seqPos, seqPos+1, alt);
		}
		else {
			System.out.println("WARNING: skipping insertion " + alt + " at pos: " + var.getPos());
		}
			
	}
	
	public String toString() {
		return ">" + sampleName + "\n" + seq.toString();
	}
	
	/**
	 * Obtain a for-real sequence object with the sample name and sequence in this proto-sequence
	 * @return
	 */
	public Sequence toSimpleSequence() {
		try {
			return new SimpleSequence(sampleName, seq.toString());
		} catch (UnrecognizedBaseException e) {
			//It would be very strange if this happened
			e.printStackTrace();
		}
		//
		return null;
	}
	
	class Pair {
		int refPos;
		int actualIndex;
		
		public Pair(int ref, int actual) {
			this.refPos = ref;
			this.actualIndex = actual;
		}
	}
	
	//Placeholder list of variant filters for when none are added
	private List<VariantFilter> emptyFilterList = new ArrayList<VariantFilter>();
}
