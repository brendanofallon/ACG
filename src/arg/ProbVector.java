package arg;

/**
 * Things that are associated with a vector of probability states should implement this interface
 * @author brendan
 *
 */
public interface ProbVector {

	public int getStateCount(); //The number of possible character states, typically 4 for nucleotide data
	
	public int getPatternCount(); //The number of patterns this node holds, which is the number of states prob vectors we maintain (don't confuse this with the total number of sites / column in the data matrix)
	
	public double[] getStates(int siteIndex); //Should probably be indexed so we can get the states for site i
	
	public void setStates(double[] newStates, int siteIndex); //
	
}
