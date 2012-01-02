package newgui.alignment;

/**
 * A generic implementation of Sequence, where the entire sequence is stored as an array of ints
 * @author brendan
 *
 */
public class SimpleSequence implements Sequence {
	
	protected String label = null;
	protected final int[] bases;
	//Mapping from character (nucleotide) to integer
	private BaseMap baseMap = new BaseMap();
	
	private Sequence reference = null;
	
	public SimpleSequence(String label, int[] bases) {
		this.label = label;
		this.bases = new int[bases.length];
		System.arraycopy(bases, 0, this.bases, 0, bases.length);
	}
	
	public SimpleSequence(String label, String seq) throws UnrecognizedBaseException {
		this.label = label;
		bases = new int[seq.length()];
		for(int i=0; i<seq.length(); i++) {
			bases[i] = baseMap.valForBase(seq.charAt(i));
		}
		
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
		BaseMap map = new BaseMap();
		StringBuffer buf = new StringBuffer();
		for(int i=0; i<bases.length; i++)
			buf.append( map.baseForVal(bases[i]) );
		return label + "\t" + buf;
	}
}
