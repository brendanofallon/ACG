package sequence;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import newgui.alignment.UnrecognizedBaseException;



/**
 * Stores an alignment in a column-compressed form, where identical columns only appear once, and
 * there is a mapping from the 'global' (or absolute) site to the unique column
 * @author brendan
 *
 */
public class CompressedAlignment implements Alignment {

	private Sequence reference = null;
	private String[] seqLabels;
	private int[] colMap;
	private List<int[]> columns;
	
	public CompressedAlignment(Alignment aln) {
		List<Sequence> seqs = new ArrayList<Sequence>();
		for(int i=0; i<aln.getSequenceCount(); i++)
			seqs.add(aln.getSequence(i));
		buildMap(seqs);
	}
	
	public CompressedAlignment(List<Sequence> seqs) {
		buildMap(seqs);
	}
	
	/**
	 * Create a compressed alignment from already-compressed data, this is used by the AlignmentFile
	 * xml-converter, which stores information in a form that matches that used in this class
	 * @param labels
	 * @param colMap
	 * @param columns
	 */
	public CompressedAlignment(List<String> labels, int[] colMap, List<int[]> columns) {
		seqLabels = new String[labels.size()];
		for(int i=0; i<labels.size(); i++)
			seqLabels[i] = labels.get(i);
		
		this.colMap = colMap;
		this.columns = columns;
	}
	
	/**
	 * Construct the labels, columnMap, and columns data structures given the sequences
	 * provided
	 * @param seqs
	 */
	private void buildMap(List<Sequence> seqs) {
		//Rebuild labels array
		seqLabels = new String[seqs.size()];
		for(int i=0; i<seqs.size(); i++)
			seqLabels[i] = seqs.get(i).getLabel();
		
		colMap = new int[seqs.get(0).getLength()];
		columns = new ArrayList<int[]>(256);
		
		ColumnFactory colFactory = new ColumnFactory(seqs);
		int[] col = new int[seqs.size()];
		int alias = 0; //Used to build mapping, stores index of column in columns list
		
		for(int i=0; i<colFactory.getColumnCount(); i++) {
			colFactory.getColumn(i, col);
			int index = colIndex(columns, col);
			alias = index;
			if (index < 0) {
				alias = columns.size();
				//Add clone of col to columns list since we're going to overwrite info in col on next iteration
				int[] newCol = new int[col.length];
				System.arraycopy(col, 0, newCol, 0, col.length);
				columns.add(newCol);
			}
			
			colMap[i] = alias;	
		}
		
	}

	/**
	 * The number of unique columns in this alignment
	 * @return
	 */
	public int getUniqueColumnCount() {
		return columns.size();
	}
	
	/**
	 * Retrieve the unique column at the given index
	 * @param col
	 * @return
	 */
	public int[] getUniqueColumn(int col) {
		return columns.get(col);
	}
	
	/**
	 * Retrieve the column at the given absolute index
	 * @param col
	 * @return
	 */
	public int[] getAbsoluteColumn(int col) {
		return getUniqueColumn(colMap[col]);
	}
	
	/**
	 * Obtain the mapping from absolute column to unique column index
	 * @return
	 */
	public int[] getColumnMap() {
		return colMap;
	}
	
	/**
	 * Returns the index of the first column that matches the given key column 
	 * (where matches is defined by colsEqual(col1, col2) ) 
	 * @param cols
	 * @param key
	 * @return
	 */
	private static int colIndex(List<int[]> cols, int[] key) {
		for(int i=0; i<cols.size(); i++) {
			if (colsEqual(cols.get(i), key)) {
				return i;
			}
		}
		return -1;
	}
	
	private static boolean colsEqual(int[] col1, int[] col2) {
		for(int i=0; i<col1.length; i++) {
			if (col1[i] != col2[i])
				return false;
		}
		return true;
	}
	
	@Override
	public List<String> getLabels() {
		List<String> labs = new ArrayList<String>();
		for(int i=0; i<seqLabels.length; i++) {
			labs.add(seqLabels[i]);
		}
		return labs;
	}

	@Override
	public int getSequenceCount() {
		return seqLabels.length;
	}

	@Override
	public int getSequenceLength() {
		return colMap.length;
	}

	@Override
	public Sequence getSequenceForLabel(String label) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Sequence getSequence(int index) {
		String label = seqLabels[index];
		
		StringBuffer strb = new StringBuffer();
		for(int i=0; i<colMap.length; i++) {
			strb.append( DNAUtils.baseForInt( getAbsoluteColumn(i)[index]) );
		}
		Sequence seq;
		try {
			seq = new SimpleSequence(label, strb.toString());
			//seq.setReference(reference);
			return seq;
		} catch (UnrecognizedBaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void addSequence(Sequence seq) {
		throw new IllegalArgumentException("Sequences cannot be added on the fly to a compressed alignment, right now.");
	}

	@Override
	public boolean removeSequence(Sequence seqToRemove) {
		// TODO Auto-generated method stub
		return false;
	}

	public Sequence getReference() {
		return reference;
	}

	public void setReference(Sequence ref) {
		this.reference = ref;
	}

	public Sequence getConsensus() {
		int[] refBases = new int[getSequenceLength()];
		for(int i=0; i<getSequenceLength(); i++) {
			refBases[i] = AlnUtils.getConsensusForCol( getAbsoluteColumn(i));
		}
		return new SimpleSequence("Consensus", refBases);
	}
	
	public static void printColumn(int[] col) {
		for(int i=0; i<col.length; i++) {
			System.out.println( DNAUtils.baseForInt(col[i]) );
		}
	}
	
//	public static void main(String[] args) {
//		Alignment aln = new BasicAlignment();
//
//		
//		try {
//			Sequence s1 = new SimpleSequence("first",  "ACTGACTGACTACGCCTCCGTACG");
//													//	ACTGACTGACTACGCCTCCGTACG
//			Sequence s2 = new SimpleSequence("second", "ACTGACTGACTACGACTACGCACG");
//			Sequence s3 = new SimpleSequence("third",  "ACTGACTGACTACTACTATGTAAG");
//			Sequence s4 = new SimpleSequence("fourth", "ACGGACGGACTACGACTACGCACG");
//												      //ACGGACGGACTACGACTACGCACG
//
//			aln.addSequence(s1);
//			aln.addSequence(s2);
//			aln.addSequence(s3);
//			aln.addSequence(s4);
//			
//			CompressedAlignment caln = new CompressedAlignment(aln);
//			System.out.println("Number of unique columns: " + caln.getUniqueColumnCount());
//			int[] col = caln.getAbsoluteColumn(0);
//			printColumn(col);
//			
//			AlignmentFile alnFile = new AlignmentFile(caln);
//			try {
//				alnFile.saveToFile(new File("samplealn.xml"));
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			
//			File alnFile2 = new File("samplealn.xml");
//			AlignmentFile afile2 = new AlignmentFile(alnFile2);
//			Alignment newAln = afile2.getAlignment();
//			Sequence s = newAln.getSequence(3);
//			System.out.println(s);
//		}
//		catch (UnrecognizedBaseException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//	}
		
}