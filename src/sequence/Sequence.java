package sequence;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import xml.XMLUtils;

/**
 * A sequence of ambiguous type (just a string of characters) with a label (also just a string of characters).
 * Not really any funtionality here. 
 * @author brendano
 *
 */
public class Sequence {

	String seq;
	String label;
	
	public static final char GAP = '-';
	public static final char UNKNOWN = '?';
	public static final char UNKNOWN2 = 'N';
	public static final String LABEL = "xml.label";
	
	/**
	 * XML-happy constructor
	 * @param attrs
	 */
	public Sequence(Map<String, String> attrs) {
		this.label = XMLUtils.getStringOrFail(LABEL, attrs);
		this.seq = XMLUtils.getStringOrFail("content", attrs);
	}
	
	public Sequence(String label, String seq) {
		this.seq = seq;
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
			if (c != 'A' && c != 'G' && c != 'T' && c != 'C') {
				throw new BadSequenceException("Sequence contains invalid characters.");
			}
		}
	}

	
}
