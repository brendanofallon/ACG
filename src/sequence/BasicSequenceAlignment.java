/********************************************************************
*
* 	Copyright 2011 Brendan O'Fallon
*
*   Licensed under the Apache License, Version 2.0 (the "License");
*   you may not use this file except in compliance with the License.
*   You may obtain a copy of the License at
*
*       http://www.apache.org/licenses/LICENSE-2.0
*
*   Unless required by applicable law or agreed to in writing, software
*   distributed under the License is distributed on an "AS IS" BASIS,
*   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*   See the License for the specific language governing permissions and
*   limitations under the License.
*
***********************************************************************/


package sequence;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import newgui.alignment.UnrecognizedBaseException;
import newgui.datafile.AlignmentFile;

import xml.InvalidInputFileException;

/**
 * A group of sequences which are of the same length. The length is determined by the first sequence added. Columns of this
 * are assumed to represent homologous characters
 * 
 * @author brendan
 *
 */
public class BasicSequenceAlignment implements Alignment {

	public static final String SEQUENCE_FILE_ATTR = "filename";
	private AlignmentFile sourceFile = null;
	
	//Stores information about the alignment, like the locations of polymorphic sites and the global aliases
	//Remains null until first call to getDataMatrix
	protected DataMatrix dataMatrix = null;
	
	protected List<Sequence> seqs = new ArrayList<Sequence>();
	
	//Object used to mask out (set to N) columns in a flexible manner
	protected AlignmentMask mask = new AlignmentMask();
	
	/**
	 * XML-reading compatible constructor, assumes second arg is list of Sequences
	 * @param attributes
	 */
	public BasicSequenceAlignment(Map<String, String> attrs, List<Object> seqObjs) {
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
	public BasicSequenceAlignment(Map<String, String> attributes) {
		String fileName = attributes.get(SEQUENCE_FILE_ATTR);
		if (fileName == null) {
			throw new IllegalArgumentException("Cannot create an alignment, no sequence file attribute found");
		}
		File file = new File(fileName);
		if (!file.exists()) {
			throw new InvalidInputFileException("Could not find alignment file: " + fileName);
		}
		parseSequencesFromFile(file);
	}
	
	/**
	 * A constructor that attempts to load sequences from the given file, and which can currently parse fasta and phylip-formatted
	 * sequences
	 * @param fileToRead
	 */
	public BasicSequenceAlignment(File fileToRead) {
		parseSequencesFromFile(fileToRead);
	}

	/**
	 * Construct a new alignment using the given file name
	 * @param filename
	 */
	public BasicSequenceAlignment(String filename) {
		this(new File(filename));
	}
	
	/**
	 * Create a new, empty alignment
	 */
	public BasicSequenceAlignment() {
		//Nothing to do
	}
	
	/**
	 * Construct a new alignment from given list of sequences
	 * @param sequences
	 */
	public BasicSequenceAlignment(List<Sequence> sequences) {
		for(Sequence seq : sequences) {
			seqs.add(seq);
		}
	}
	
	public BasicSequenceAlignment(Alignment sourceAln) {
		for(int i=0; i<sourceAln.getSequenceCount();i++) {
			seqs.add(sourceAln.getSequence(i));
		}
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
	
	/**
	 * Add a sequence to this alignment. This no longer performs error checking on the sequence, so make
	 * sure it's valid before adding
	 */
	public void addSequence(Sequence seqToAdd) {
		
		if (seqs.size()==0) {
			//seqToAdd.checkValidity();
			seqs.add(seqToAdd);
		}
		else {
			if ( containsLabel(seqToAdd.getLabel()) ) {
				seqToAdd.setLabel(seqToAdd.getLabel() + "(2)");
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
			if (seq.getLabel().equals(label))
				return true;
		}
		return false;
	}
	
	/**
	 * Return the most frequent symbol at this column
	 * @param col
	 * @return
	 */
	public char getMaxFreqBase(int col) {
		Map<Character, Integer> map = new HashMap<Character, Integer>();
		for(Sequence seq : seqs) {
			char c= seq.charAt(col);
			Integer count = map.get(c);
			if (count == null)
				map.put(c, 1);
			else
				map.put(c, count+1);
		}
		
		//For find the key that has the greatest value associated with it
		int max = 0;
		Character maxKey = null;
		for(Character key : map.keySet()) {
			Integer val = map.get(key);
			if (val > max) {
				max = val;
				maxKey = key;
			}
			
		}
		return maxKey;
	}
	
	/**
	 * Masks bases not matching the given symbol at the given column
	 * @param nonMaskSymbol If a sequence has this character it will NOT be masked
	 * @param col Column to replace characters in
	 */
//	public void conditionalMask(char nonMaskSymbol, int col) {
//		for(Sequence seq : seqs) {
//			char c = seq.charAt(col);
//			if (c != nonMaskSymbol)
//				seq.mask(col);
//		}
//	}
	
	/**
	 * Returns the data matrix object associated with this alignment
	 * @return
	 */
	public DataMatrix getDataMatrix() {
		if (dataMatrix == null) {
			System.out.println("CREATING SEQUENCE DATA MATRIX");
			dataMatrix = new DataMatrix(this);
		}
		else {
			System.out.println("RETURNING ALREADY CREATED DATA MATRIX");
		}
		
		return dataMatrix;
	}
	
	/**
	 * Returns an array of indices at which polymorphic sites occur, in which gaps or unknown do NOT count for polymorphism.
	 * Right now this is used by Masker to mask out seemingly erroneous regions of X-chromosome data. 
	 * @return
	 */
	public int[] getNonGapPolymorphicSites() {
		List<Integer> sites = new ArrayList<Integer>(100);
		for(int i=0; i<this.getSiteCount(); i++) {
			if ( this.isNonGapPolymorphic(i))
				sites.add(i);
		}
		
		int[] arr = new int[sites.size()];
		for(int i=0; i<sites.size(); i++)
			arr[i] = sites.get(i);
		
		return arr;
	}
	
	public boolean isNonGapPolymorphic(int site) {
		char c = seqs.get(0).charAt(site);
		for(Sequence seq : seqs) {
			char comp = seq.charAt(site);
			if (comp != c && (comp != DNAUtils.GAP) && (comp != DNAUtils.N) )
				return true;
		}
		return false;
	}
		
	/**
	 * Returns a list of those columns that contain one or more sequences with gaps (defined by hasGap(site))
	 * @return
	 */
//	private List<Integer> getGapColumns() {
//		List<Integer> gapCols = new ArrayList<Integer>();
//		for(int i=0; i<seqs.get(0).getLength(); i++) {
//			if (hasGap(i))
//				gapCols.add(i);
//		}
//		return gapCols;
//	}
//	
//	/**
//	 * Returns a list of those columns that contain one or more sequences with unknowns (as defined by hasUnknown(site))
//	 * @return
//	 */
//	private List<Integer> getUnknownColumns() {
//		List<Integer> gapCols = new ArrayList<Integer>();
//		for(int i=0; i<seqs.get(0).getLength(); i++) {
//			if (hasUnknown(i))
//				gapCols.add(i);
//		}
//		return gapCols;
//	}
	
	/**
	 * Returns true if any sequence contains a gap (defined by Sequence.GAP) at the given site
	 * @param site
	 * @return
	 */
	public boolean hasGap(int site) {
		for(Sequence seq : seqs) {
			if (seq.charAt(site)==DNAUtils.GAP) {
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
			if (seq.charAt(site)==DNAUtils.N) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Returns true if there is a gap or unknown character at any site
	 * @return
	 */
	public boolean hasGapOrUnknown() {
		for(int i=0; i<seqs.get(0).getLength(); i++) {
			if (hasGap(i) || hasUnknown(i))
				return true;
		}
		return false;
	}
	
	public boolean hasGapOrUnknown(int column) {
		if (hasGap(column) || hasUnknown(column))
			return true;
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
			chars[i] = seqs.get(i).charAt(col);
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

				//System.out.println("Adding sequence " + label + " : " + sq.toString().toUpperCase());
				try {
					seq = new SimpleSequence(label, sq.toString().toUpperCase());
					addSequence(seq);
				} catch (UnrecognizedBaseException e) {
					throw new FileParseException("Invalid characters found in sequence : " + label + e.getMessage());
				}

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
            	try {
                	Sequence seq = new SimpleSequence(seqNames[i], newSeqs[i].toString().toUpperCase());
					addSequence(seq);
				} catch (UnrecognizedBaseException e) {
					throw new FileParseException("Invalid characters found in sequence with label: " + seqNames[i]);
				}
            }
	}
	
	/**
	 * Returns a fasta-looking string 
	 */
	public String toString() {
		StringBuilder str = new StringBuilder();
		for(Sequence seq : seqs) {
			str.append(">" + seq.getLabel() + "\n" + seq.getSequenceString() +"\n");
		}
		return str.toString();
	}

	
	//  ****************** Alignment implementation *************************************** //
	@Override
	public List<String> getLabels() {
		List<String> labels = new ArrayList<String>(seqs.size());
		for(Sequence seq : seqs) {
			labels.add(seq.getLabel());
		}
		return labels;
	}

	@Override
	public int getSequenceLength() {
		if (seqs.size()==0)
			return 0;
		else
			return seqs.get(0).getLength();
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
	
	/**
	 * Return a list of sequences in this alignment. 
	 * @return
	 */
	public List<Sequence> getSequences() {
		return seqs;
	}
	
	@Override
	public sequence.Sequence getSequenceForLabel(String label) {
		for(Sequence seq : seqs) {
			if (seq.getLabel().equals(label))
				return seq;
		}
		return null;
	}

	@Override
	public boolean removeSequence(Sequence seqToRemove) {
		return seqs.remove(seqToRemove);
	}

	@Override
	public void removeCols(int[] cols) {
		for(Sequence seq : seqs) {
			seq.removeCols(cols);
		}
	}

	@Override
	public void removeRows(int[] rows) {
		Arrays.sort(rows);
		for(int i=rows.length-1; i>=0; i--) {
			if (seqs.size()>rows[i])
				seqs.remove(rows[i]);
		}
	}
	
	

	@Override
	public Alignment newAlignmentFromColumns(int[] cols) {
		Alignment newAln = new BasicSequenceAlignment();
		for(Sequence seq : seqs) {
			newAln.addSequence( seq.newFromColumns(cols));
		}
		return newAln;
	}

	@Override
	public AlignmentFile getSourceFile() {
		return sourceFile;
	}

	@Override
	public void setSourceFile(AlignmentFile source) {
		this.sourceFile = source;
	}

	@Override
	public AlignmentMask getMask() {
		return mask;
	}

	@Override
	public void setAlignmentMask(AlignmentMask mask) {
		this.mask = mask;
	}

	public void applyMask() {
		if (mask == null) 
			return;
		if (dataMatrix != null) {
			System.err.println("WARNING: DataMatrix has already been created for alignment, but we're applying a mask now so info in matrix will out outdated!");
		}
		
		System.out.println("APPLYING SEQUENCE MASK!");
		for(Sequence seq : seqs) {
			for(Integer col : mask.getMaskedColumns()) {
				seq.mask(col);
			}
		}
		
	}

	@Override
	public int[] getBaseCounts(int col) {
		int[] counts = new int[5];
		for(int i=0; i<seqs.size(); i++) {
			counts[ seqs.get(i).baseAt(col) ]++;
		}
		return counts;
	}
}
