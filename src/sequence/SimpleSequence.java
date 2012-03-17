package sequence;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import xml.XMLLoader;
import xml.XMLUtils;

import newgui.alignment.UnrecognizedBaseException;

/**
 * A generic implementation of Sequence, where the entire sequence is stored as an array of ints
 * 
 * @author brendan
 *
 */
public class SimpleSequence implements Sequence {
	
	private static int sequenceCount = 0;
	private int myNumber = sequenceCount;
	
	protected String label = null;
	protected int[] bases;
	
	private Sequence reference = null;
	
	/**
	 * XML-approved constructor. Label comes from NODE_ID attribute, bases come
	 * from text content of node (or whatever's in attrs.get(XMLLoader.TEXT_CONTENT) )
	 */
	public SimpleSequence(Map<String, String> attrs) {
		label = XMLUtils.getStringOrFail(XMLLoader.NODE_ID, attrs);
		String seqStr = attrs.get(XMLLoader.TEXT_CONTENT).trim();
		bases = new int[seqStr.length()];
		for(int i=0; i<seqStr.length(); i++) {
			bases[i] = DNAUtils.intForBase(seqStr.charAt(i));
		}
		sequenceCount++;
	}
	
	public SimpleSequence(String label, int[] bases) {
		this.label = label;
		this.bases = new int[bases.length];
		System.arraycopy(bases, 0, this.bases, 0, bases.length);
		sequenceCount++;
	}
	
	public SimpleSequence(String label, String seq) throws UnrecognizedBaseException {
		this.label = label;
		bases = new int[seq.length()];
		for(int i=0; i<seq.length(); i++) {
			bases[i] = DNAUtils.intForBase(seq.charAt(i));
		}
		sequenceCount++;
	}

	public void setReference(Sequence ref) {
		this.reference = ref;
	}
	
	public Sequence getReference() {
		return reference;
	}
	
	public boolean differsFromReference(int col) {
		return this.bases[col] != reference.baseAt(col);
	}
	
	@Override
	public Sequence newFromColumns(int[] cols) {
		int[] newBases = new int[cols.length];
		for(int i=0; i<cols.length; i++) {
			newBases[i] = bases[cols[i]];
		}
		return new SimpleSequence(getLabel(), newBases);
	}
	
	@Override
	public String getLabel() {
		return label;
	}

	@Override
	public int getLength() {
		return bases.length;
	}

	@Override
	public int baseAt(int pos) {
		return bases[pos];
	}

	public String toString() {
		StringBuffer buf = new StringBuffer();
		for(int i=0; i<bases.length; i++)
			buf.append( DNAUtils.baseForInt(bases[i]) );
		return label + "\t" + buf;
	}

	public String getSequenceString() {
		StringBuffer buf = new StringBuffer();
		for(int i=0; i<bases.length; i++)
			buf.append( DNAUtils.baseForInt(bases[i]) );
		return buf.toString();
	}
	

	@Override
	public char charAt(int pos) {
		return DNAUtils.baseForInt( baseAt(pos));
	}

	@Override
	public void setLabel(String newLabel) {
		this.label = newLabel;
	}

	@Override
	public void mask(int pos) {
		bases[pos] = DNAUtils.N;
	}

	@Override
	public int uniqueNumber() {
		return myNumber;
	}

	public void removeCols(int[] cols) {
		List<Integer> newBases = new ArrayList<Integer>();
		for(int i=0; i<bases.length; i++)
			newBases.add(bases[i]);
		
		Arrays.sort(cols);

		//Must run from end of columns backward so indices stay the same
		for(int i=cols.length-1; i>=0; i--) {
			newBases.remove(cols[i]);
//			if (i==0 || cols[i] != cols[i-1])
//				buf.replace(cols[i], cols[i]+1, empty);
		}
		
		bases = new int[newBases.size()];
		for(int i=0; i<newBases.size(); i++) {
			bases[i] = newBases.get(i);
		}
	}

	
	@Override
	public void toCharArray(int start, int end, char[] output) {
		for(int i=start; i<end; i++) {
			output[i-start] = DNAUtils.baseForInt( bases[i]);
		}
	}
	
}
