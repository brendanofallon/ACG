package arg;

import java.util.Map;

/**
 * A simple XML-friendly wrapper for a newick string. Actually, one could put any string in here. But the intent
 * is that this object is easily passed to the arg constructor to facilitate initial arg generation
 * @author brendan
 *
 */
public class Newick {

	String newick;
	
	public Newick(String newickString) {
		this.newick = newickString;
	}
	
	public Newick(Map<String, String> attrs) {
		newick = attrs.get("content");
		//System.out.println("Found newick string : " +  newick);
	}	
	
	public String getNewick() {
		return newick;
	}
}
