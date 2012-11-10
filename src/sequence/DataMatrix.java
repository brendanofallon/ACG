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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Stores a list of CharacterColumns in a semi-efficient manner by making sure there are no redundant columns added, 
 * and instead only adding unique columns to the pattern list and maintaining a mapping 
 * @author brendan
 *
 */
public class DataMatrix {

	protected List<CharacterColumn> patterns = new ArrayList<CharacterColumn>(128);
	
	//A count of the occurrences of each pattern (that is, patterns.get(i) has patternCardinality.get(i) occurrences)
	protected List<Integer> patternCardinality = new ArrayList<Integer>(128);
	
	//A non-redundant but minimal list of sites at which unique patterns occur
	List<Integer> uniqueSiteList = new ArrayList<Integer>(128); 
	
	//A list of which sites map to which pattern. colToPattern.get(site) returns the index of the pattern
	//in the 'patterns' list. Using integers here means we're limited to 2^32-1 total columns... this is probably OK for now. 
	//Note that there's one entry in this list for every SITE, not every pattern, which means if we want 10*8 sites someday
	//this could get enormously big... is there a better way? Store it on disk and use a ssd?
	protected List<Integer> colToPattern = new ArrayList<Integer>(500);
	
	protected List<String> rowLabels = new ArrayList<String>();
	
	protected int totalColsAdded = 0;
	
	/**
	 * Create a new empty data matrix
	 */
	public DataMatrix(Map<String, String> attrMap, BasicSequenceAlignment data) {
		//TODO do something with attributes?
		this(data);
	}

	
	/**
	 * Create a new data matrix from the specified alignment assuming we have nucleotide data
	 * @param aln
	 */
	public DataMatrix(BasicSequenceAlignment aln) {
		for(int i=0; i<aln.getSiteCount(); i++) {
			CharacterColumn col = new GappedNucColumn( aln.getColumn(i) );
			addColumn(col);
		}
		
		for(int i=0; i<aln.getSequenceCount(); i++) {
			rowLabels.add( aln.getSequenceLabel(i) );
		}
		
		int sum = 0;
		for(int i=0; i<patterns.size(); i++) {
			sum += patternCardinality.get(i);
		}

		if (sum != totalColsAdded) {
			throw new IllegalStateException("Uh oh, the sum of pattern cardinalities did not match the total number of sites!");
		}
	}
	
	/**
	 * Add a column to the list of character columns tracked in this matrix. If the column is identical to another column, 
	 * we don't add it but just note there are now two (or more) columns of that type. We also maintain a mapping from 
	 * the order in which columns were added to the 
	 * 
	 * @param col
	 */
	public void addColumn(CharacterColumn col) {
		int index = find(col);
		if (index == -1) {
			colToPattern.add(patterns.size()); //ORDER IS IMPORTANT HERE!
			patterns.add(col);
			patternCardinality.add(1);
			uniqueSiteList.add(totalColsAdded);
		}
		else {
			//System.out.println("Site " + colToPattern.size() + " has same pattern as " + index);
			colToPattern.add(index);
			patternCardinality.set(index, patternCardinality.get(index)+1);
		}
		
		totalColsAdded++;
	}
	
	/**
	 * Returns a list of sites at which unique site patterns occur, this will always have as many entries
	 * as site patterns, and no two sites indexed by the list entries will have the same pattern. This
	 * just makes it handy to create the initial probability vectors 
	 * @return
	 */
	public List<Integer> getUniqueSites() {
		return uniqueSiteList;
	}
	
	/**
	 * A too-confusing method that creates a new array of doubles and assigns a '1' to the element that
	 * corresponds to the state given by the sequence with the given label at the given site, using the
	 * mapping provided by the given Alphabet. Thus if the sequence with label 'human' has an A at site 42,
	 * then we'll return a vector that looks like {0, 0, 1, 0}, if the given alphabet says A=2. This is 
	 * used to assign the probability vectors for tips of trees. 
	 * 
	 *  This method may be useful if at some point we want to allow for base errors, such that there's some
	 *  probability that a state was actually a T, even though we observed an A, for instance. Right
	 *  now this is not used. 
	 * 
	 * @param seqLabel
	 * @param site
	 * @param alpha
	 * @return
	 */
	public double[] getProbVector(String seqLabel, int site, Alphabet alpha) {
		CharacterColumn col = getPatternForSite(site);
		int row = rowForSequenceLabel(seqLabel);
		if (row==-1) {
			throw new IllegalArgumentException("Could not find sequence with label " + seqLabel + " in data matrix.");
		}
		char state = col.getSymbol(row);
		double[] probs = new double[alpha.getSymbolCount()];
		int which = alpha.symbolToInt(state);
		probs[which] = 1.0;
		
		return probs;
	}
	
	/**
	 * Similar to getProbVector, but this returns a vector of ints, where element i is the state at site i
	 * For DNA, which we assume universally right now,  this returns a long list of ints where each int is
	 * in 0..3, with length number of sites. This is used to initialize the "states" field for ARG tip nodes. 
	 * @param seqLabel
	 * @param dna
	 * @return
	 */
	public int[] getStateVector(String seqLabel, Alphabet dna) {
		int[] states = new int[getTotalColumnCount()];
		int row = rowForSequenceLabel(seqLabel);
		if (row==-1) {
			throw new IllegalArgumentException("Could not find sequence with label " + seqLabel + " in data matrix.");
		}
		
		for(int site=0; site<states.length; site++) {
			CharacterColumn col = getPatternForSite(site);
			char state = col.getSymbol(row);
			//System.out.println("Seq. label: " + seqLabel + " site: " + site + " symbol: "+ state);
			states[site] = dna.symbolToInt(state);
			
			//SPECIAL CASE WARNING: Right now, we treat gaps and Ns the same (as unobserved states)
			//so they get the same value
			if (states[site] == DNAUtils.N)
				states[site] = DNAUtils.GAP;
		}
		
		return states;
	}
	
	/**
	 * Return the row index for the sequence with the given label
	 * @param label
	 * @return
	 */
	public int rowForSequenceLabel(String label) {
		for(int i=0; i<rowLabels.size(); i++) {
			if (rowLabels.get(i).equals(label))
				return i;
		}
		return -1;
	}
	
	public int getTotalColumnCount() {
		return totalColsAdded;
	}
	
	public int getNumberOfPatterns() {
		return patterns.size();
	}
	
	public int getSequenceCount() {
		if (patterns.size()==0)
			return 0;
		else
			return patterns.get(0).size();
	}

	/**
	 * Return the number of times a particular pattern appears among all columns
	 * @param patternIndex
	 * @return
	 */
	public int getCardinalityForPatternIndex(int patternIndex) {
		return patternCardinality.get(patternIndex);
	}
	
	/**
	 * Return a list of integers that contains the global indices of all polymorphic sites
	 * @return
	 */
	public int[] getPolymorphicSites() {
		List<Integer> sites = new ArrayList<Integer>(1000);
		for(int i=0; i<getTotalColumnCount(); i++) {
			if (siteIsPolymorphic(i)) {
				sites.add(i);
			}
		}
		
		//Dump sites into int array
		int[] siteArray = new int[sites.size()];
		for(int i=0; i<sites.size(); i++)
			siteArray[i] = sites.get(i);
		
		return siteArray;
	}
	
	/**
	 * Returns true if the site (rather, the column with the given index) contains more than one symbol
	 * @param site
	 * @return
	 */
	public boolean siteIsPolymorphic(int site) {
		CharacterColumn col = getPatternForSite(site);
		char first = col.getSymbol(0);
		for(int i=0; i<col.size(); i++) {
			if (col.getSymbol(i) != first)
				return true;
		}
		
		return false;
	}
	
	/**
	 * Returns a list of integers such that list[i] is the number of sites with index < i that are invariant for 
	 * the given symbol. This speeds the computation of the number of invariant sites (of a given type) between
	 * two indices, since it can easily be computed as list[end] - list[start]
	 * @param symbol
	 * @return
	 */
	public int[] getInvariantCumulator(char symbol) {
		int[] sums = new int[getTotalColumnCount()+1];
		int sum = 0;
		for(int i=0; i<sums.length-1; i++) {
			sums[i] = sum;
			if ( ! siteIsPolymorphic(i) && getPatternForSite(i).getSymbol(0)==symbol) {
				sum++;
			}
		}
		
		sums[sums.length-1] = sum;
		
		return sums;
	}
	
	/**
	 * Obtain the list of character states at the given position in the pattern index list (not the site!)
	 * @param patternIndex
	 * @return
	 */
	public CharacterColumn getPatternForIndex(int patternIndex) {
		CharacterColumn col = patterns.get(patternIndex);
		return col;
	}
	
	/**
	 * Returns the pattern index that this site maps to
	 * @param site
	 * @return
	 */
	public int getPatternIndexForSite(int site) {
		return colToPattern.get(site);
	}
	
	/**
	 * Returns the actual column of character states associated with this site
	 * @param site
	 * @return
	 */
	public CharacterColumn getPatternForSite(int site) {
		int patternIndex = getPatternIndexForSite(site);
		CharacterColumn col = patterns.get(patternIndex);
		return col;
	}
	
	/**
	 * Returns the index in the patterns list of the pattern that matches the given column, or -1 if 
	 * no pattern .isEqual() to the column
	 * @param col
	 * @return
	 */
	public int find(CharacterColumn col) {
		for(int i=0; i<patterns.size(); i++) {
			if (patterns.get(i).isEqual(col)) {
				return i;
			}
		}
		return -1;
	}
	
	/**
	 * Returns true if the sequences at row i and and row j differ at absolute column # absoluteCol 
	 * @param absoluteCol
	 * @param seqI
	 * @param seqJ
	 * @return
	 */
	public boolean sequencesDiffer(int absoluteCol, int seqI, int seqJ) {
		CharacterColumn col = getPatternForSite(absoluteCol);
		return col.getSymbol(seqI) == col.getSymbol(seqJ);
	}
	
	/**
	 * Return a list of pattern indices 
	 * @param row1
	 * @param row2
	 */
//	public List<Integer> collectAliasesForPair(int row1, int row2) {
//		List<Integer> pairAliases = new ArrayList<Integer>();
//		
//		for(int i=0; i<patterns.size(); i++) {
//			CharacterColumn colI = patterns.get(i);
//			for(int j=i+1; j<patterns.size(); j++)
//				if (colI.isEqualAt(patterns.get(j), row1) && colI.isEqualAt(patterns.get(j), row2)) {
//					pairAliases.add(j);
//				}
//		}
//		
//		return pairAliases;
//	}
	
	/**
	 * Return the label of the ith sequence tracked by this DataMatrix
	 */
	public String getSequenceLabel(int i) {
		return rowLabels.get(i);
	}
}
