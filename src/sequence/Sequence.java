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

import java.util.Collections;
import java.util.List;
import java.util.Map;

import xml.XMLLoader;
import xml.XMLUtils;

/**
 * A sequence of ambiguous type (just a string of characters) with a label (also just a string of characters).
 * Not really any functionality here. 
 * @author brendano
 *
 */
public class Sequence {

	String seq;
	String label;
	
	public static final char GAP = '-';
	public static final char UNKNOWN = '?';
	public static final char UNKNOWN2 = 'N';
	public static final String LABEL = XMLLoader.NODE_ID;
	
	/**
	 * XML-happy constructor
	 * @param attrs
	 */
	public Sequence(Map<String, String> attrs) {
		this.label = XMLUtils.getStringOrFail(LABEL, attrs);
		this.seq = XMLUtils.getStringOrFail("content", attrs).trim();
	}
	
	public Sequence(String label, String seq) {
		this.seq = seq.trim();
		this.label = label;
	}
	
	public int getLength() {
		return seq.length();
	}
	
	public char getCharAt(int i) {
		return seq.charAt(i);
	}
	
	public String getLabel() {
		return label;
	}
	
	/**
	 * Remove sites at the given indices
	 * @param sites
	 */
	public void remove(List<Integer> sites) {
    	StringBuilder buf = new StringBuilder(seq);

    	Collections.sort(sites);
    	
    	String empty = "";
    	//Must run from end of columns backward so indices stay the same
    	for(int i=sites.size()-1; i>=0; i--) {
    		if (i==0 || sites.get(i) != sites.get(i-1))
    			buf.replace(sites.get(i), sites.get(i)+1, empty);
    	}
    	
    	seq = buf.toString();
	}
	
	/**
	 * Converts the symbol at the given site into a Sequence.UNKNOWN
	 * @param site
	 */
	public void mask(int site) {
		char[] newChars = new char[seq.length()];
		seq.getChars(0, seq.length(), newChars, 0);
		newChars[site] = Sequence.UNKNOWN;
		seq = new String(newChars);
	}
	
	
	/**
	 * Converts symbols at each of the given sites to a Sequence.UNKNOWN
	 * @param site
	 */
	public void mask(List<Integer> sites) {
		char[] newChars = new char[seq.length()];
		seq.getChars(0, seq.length(), newChars, 0);
		for(Integer site : sites)
			newChars[site] = Sequence.UNKNOWN;
		seq = new String(newChars);
	}
	
	
	public String getSequence() {
		return seq;
	}
	
	/**
	 * Removes the character at the given site. 
	 * @param site
	 */
	public void remove(int site) {
		seq = seq.substring(0, site) + seq.substring(site+1, seq.length());
	}
	
	public void checkValidity() throws BadSequenceException {
		for(int i=0; i<seq.length(); i++) {
			char c = Character.toUpperCase( seq.charAt(i) ); 
			if (c != 'A' && c != 'G' && c != 'T' && c != 'C' && c != GAP && c != UNKNOWN && c != UNKNOWN2) {
				throw new BadSequenceException("Sequence contains invalid characters (" + c + ") .");
			}
		}
	}

	
}
