package sequence;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * A group of sequences which are of the same length. The length is determined by the first sequence added. Columns of this
 * are assumed to represent homologous characters
 * 
 * @author brendan
 *
 */
public class Alignment {

	public static final String SEQUENCE_FILE_ATTR = "filename";
	
	//Stores some information about the alignment, like the locations of polymorphic sites and the global aliases
	DataMatrix dataMatrix = null;
	
	List<Sequence> seqs = new ArrayList<Sequence>();
	
	/**
	 * XML-reading compatible constructor, assumes second arg is list of Sequences
	 * @param attributes
	 */
	public Alignment(Map<String, String> attrs, List<Object> seqObjs) {
		for(Object seqObj : seqObjs) {
			if (seqObj instanceof Sequence) {
				seqs.add( (Sequence)seqObj );
			}
			else {
				throw new IllegalArgumentException("Only objects of type sequence can be added to an alignment");
			}
		}
	}
	
	/**
	 * XML-reading compatible constructor
	 * @param attributes
	 */
	public Alignment(Map<String, String> attributes) {
		String fileName = attributes.get(SEQUENCE_FILE_ATTR);
		if (fileName == null) {
			throw new IllegalArgumentException("Cannot create an alignment, no sequence file attribute found");
		}
		File file = new File(fileName);
		if (!file.exists()) {
			System.out.println("Error : Could not find input file " + fileName);
			System.exit(1);
		}
		parseSequencesFromFile(file);
	}
	
	/**
	 * A constructor that attempts to load sequences from the given file, and which can currently parse fasta and phylip-formatted
	 * sequences
	 * @param fileToRead
	 */
	public Alignment(File fileToRead) {
		parseSequencesFromFile(fileToRead);
	}

	/**
	 * Construct a new alignment using the given file name
	 * @param filename
	 */
	public Alignment(String filename) {
		this(new File(filename));
	}
	
	private void parseSequencesFromFile(File fileToRead) {
		String name = fileToRead.getName();
		try {
			if (name.endsWith(".fas") || name.endsWith(".fasta")) {
				parseFasta(new BufferedReader(new FileReader(fileToRead)));
			}

			if (name.endsWith(".phy") || name.endsWith(".phylip")) {
				parsePhylip(new BufferedReader(new FileReader(fileToRead)));
			}
		}
		catch (Exception ex) {
			System.err.println("Error parsing file with name: " + fileToRead.getPath() + " after reading " + seqs.size() + " sequences. \n" + ex.getMessage() );
		}
		
		//System.out.println("Read in " + seqs.size() + " sequences of length " + getSiteCount() + " from file : " + fileToRead.getPath());
	}
	
	public void addSequence(Sequence seqToAdd) {
		if (seqs.size()==0) {
			//try {
				//seqToAdd.checkValidity();
				seqs.add(seqToAdd);
//			} catch (BadSequenceException e) {
//				System.out.println("Error adding sequence " + seqToAdd.label + " :\n" + e);
//				System.exit(0);
//			}
		}
		else {
			if ( containsLabel(seqToAdd.label) ) {
				seqToAdd.label = seqToAdd.label + "(2)";
			}
				
			if (seqToAdd.getLength()==seqs.get(0).getLength()) {
				seqs.add(seqToAdd);
			}
			else {
				throw new IllegalArgumentException("Cannot add sequences of differring length to an Alignment");
			}
		}
	}
	
	/**
	 * Returns true if any sequence in this alignment has the given label
	 * @param label
	 * @return
	 */
	public boolean containsLabel(String label) {
		for(Sequence seq : seqs) {
			if (seq.label.equals(label))
				return true;
		}
		return false;
	}
	
	/**
	 * Returns the data matrix object associated with this alignment
	 * @return
	 */
	public DataMatrix getDataMatrix() {
		if (dataMatrix == null)
			dataMatrix = new DataMatrix(this);
		
		return dataMatrix;
	}
	
	/**
	 * Remove all columns with either a gap or an unknown symbol from the alignment
	 * @return A list containing the indices of all columns removed
	 */
	public List<Integer> removeGapsAndUnknowns() {
		List<Integer> toRemove = getGapColumns();
		toRemove.addAll(getUnknownColumns());
		
		for(Sequence seq : seqs) {
			seq.remove(toRemove);
		}
				
		return toRemove;
	}
	
	/**
	 * Remove the given column from all sequences
	 * @param col
	 */
	public void removeColumn(int col) {
		for(Sequence seq : seqs) {
			seq.remove(col);
		}
	}
	
	/**
	 * Returns a list of those columns that contain one or more sequences with gaps (defined by hasGap(site))
	 * @return
	 */
	private List<Integer> getGapColumns() {
		List<Integer> gapCols = new ArrayList<Integer>();
		for(int i=0; i<seqs.get(0).seq.length(); i++) {
			if (hasGap(i))
				gapCols.add(i);
		}
		return gapCols;
	}
	
	/**
	 * Returns a list of those columns that contain one or more sequences with unknowns (as defined by hasUnknown(site))
	 * @return
	 */
	private List<Integer> getUnknownColumns() {
		List<Integer> gapCols = new ArrayList<Integer>();
		for(int i=0; i<seqs.get(0).seq.length(); i++) {
			if (hasUnknown(i))
				gapCols.add(i);
		}
		return gapCols;
	}
	
	/**
	 * Returns true if any sequence contains a gap (defined by Sequence.GAP) at the given site
	 * @param site
	 * @return
	 */
	public boolean hasGap(int site) {
		for(Sequence seq : seqs) {
			if (seq.seq.charAt(site)==Sequence.GAP) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns true if any sequence contains a unknown character (defined by Sequence.UNKNOWN) at the given site
	 * @param site
	 * @return
	 */
	public boolean hasUnknown(int site) {
		for(Sequence seq : seqs) {
			if (seq.seq.charAt(site)==Sequence.UNKNOWN || seq.seq.charAt(site)==Sequence.UNKNOWN2) {
				return true;
			}
		}
		return false;
	}
	
	public int getSequenceCount() {
		return seqs.size();
	}
	
	public int getSiteCount() {
            if (seqs.size()==0)
                return 0;
            else
		return seqs.get(0).getLength();
	}
	
	public char[] getColumn(int col) {
		char[] chars = new char[getSequenceCount()];
		for(int i=0; i<seqs.size(); i++) {
			chars[i] = seqs.get(i).getCharAt(col);
		}
		
		return chars;
	}
	
	
	
	/******************** Parsers for a few different types of file, these should be moved somewhere else sometime... *************************/
	
	private void parseFasta(BufferedReader file) throws FileParseException, IOException {
		int lineNumber = 0;

		String line = file.readLine();
		lineNumber++;

		while(line!=null && (! line.startsWith(">"))) {
			lineNumber++;
			line = file.readLine();
		}

		if (line == null)  {
			throw new FileParseException("Error parsing fasta file, could not find beginning of data.");
		}

		Sequence seq;
		while(line != null) {
			//System.out.println("Trying to read line " + lineNumber + " : " + line);
			line = line.trim();
			while(line!=null && line.trim().length()==0) {
				line = file.readLine();
				lineNumber++;
			}
			if (line == null)
				seq = null;
			else {
				if (line.charAt(0) != '>')  {
					throw new FileParseException("Error parsing fasta file, could not parse a sequence name from line #" + lineNumber);
				}

				String label = line.substring(1);
				StringBuffer sq = new StringBuffer();
				line = file.readLine();
				lineNumber++;
				if (line!=null)
					line = line.trim();
				while(line != null && line.length()>0 && line.charAt(0)!='>') {
					sq.append(line);
					line = file.readLine();
					lineNumber++;

					if (line!=null)
						line = line.trim();
				}

				seq = new Sequence(label, sq.toString().toUpperCase());
				//System.out.println("Adding sequence " + label + " : " + sq.toString().toUpperCase());
				addSequence(seq);

			}//else line was not null
		}//while reading new sequences
	}
	
	
	
	private void parsePhylip(BufferedReader file) throws FileParseException, IOException {
		int numSequences;
		int numCharacters;
		int lineNumber = 0;

            String line = file.readLine();
			lineNumber++;
			
            while(line.trim().length() == 0) {
            	lineNumber++;
                line = file.readLine();
            }
			
            String delims = "[ ]+";
            String[] tokens = line.trim().split(delims);
            if (tokens.length != 2) {
            	
            	throw new FileParseException("Error parsing phylip-formatted file, could not read sequence number and length from first line");
            }
            
            numSequences = Integer.parseInt(tokens[0]);
            numCharacters = Integer.parseInt(tokens[1]);
            	
            while(line.trim().length() == 0) {
            	lineNumber++;
                line = file.readLine();
            }
            
            //Now read the next numSequences lines
            StringBuffer[] newSeqs = new StringBuffer[numSequences];
            for(int i=0; i<numSequences; i++) {
            	newSeqs[i] = new StringBuffer();
            }
            String[] seqNames = new String[numSequences];
            for(int i=0; i<numSequences; i++) {
            	lineNumber++;
            	line = file.readLine();
            	tokens = line.split(delims);
            	if(tokens.length>=2) {
            		seqNames[i] = tokens[0];
            		for(int j=1; j<tokens.length; j++) 
            			if (tokens[j].trim().length()>0)
            				newSeqs[i].append(tokens[j]);
            	}
            	else {
            		System.err.println("Didn't find exactly two tokens at line #" + lineNumber);
            		for(int j=0; j<tokens.length; j++)
                		System.err.println(j + " : " + tokens[j]);
                	throw new FileParseException("Error parsing phylip-formatted file on line #" + lineNumber);
            	}
            }//for i
            
            line = file.readLine();
            while(line != null) {
                while(line!=null && line.trim().length() == 0) {
                	lineNumber++;
                    line = file.readLine();
                }
                
                for(int i=0; i<numSequences && line != null; i++) {
                	tokens = line.split(delims);
                	if (tokens.length==1) {
                		newSeqs[i].append(tokens[0]);
                	}
                	
                	if(tokens.length>=2) {
                		for(int j=1; j<tokens.length; j++) 
                			if (tokens[j].trim().length()>0)
                				newSeqs[i].append(tokens[j]);
                	}
                	
                	
                	lineNumber++;
                	line = file.readLine();
                }//for i
                
            }//while reading new lines
            
            for(int i=0; i<numSequences; i++) {
            	Sequence seq = new Sequence(seqNames[i], newSeqs[i].toString().toUpperCase());
            	addSequence( seq );
            }
	}

	/**
	 * Return the label of the ith sequence
	 * @param i
	 * @return
	 */
	public String getSequenceLabel(int i) {
		return seqs.get(i).getLabel();
	}

	public Sequence getSequence(int i) {
		return seqs.get(i);
	}
	
	public String toString() {
		StringBuilder str = new StringBuilder();
		for(Sequence seq : seqs) {
			str.append(">" + seq.label + "\n" + seq.seq +"\n");
		}
		return str.toString();
	}

	public static void main(String[] args) {
		Alignment aln = new Alignment("test.fas");
		System.out.println("Before : \n" + aln);
		
		List<Integer> removed = aln.removeGapsAndUnknowns();
		System.out.println("Removed : " + removed.size() + " cols");
		
		System.out.println("After : \n" + aln);
	}
}
