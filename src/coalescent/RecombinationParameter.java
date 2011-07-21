package coalescent;

/**
 * Species classes that can act as a recombination parameter, which specify how the recombination rate varies over sites and,
 * potentially, time. 
 * @author brendan
 *
 */
public interface RecombinationParameter {

	/**
	 * The rate of recombination of a single lineage at the given depth in the tree
	 * @param t
	 * @return
	 */
	public double getInstantaneousRate(double t);
	
	/**
	 * The integral of the recombination rate from start to end
	 * @param start
	 * @param end
	 * @return
	 */
	public double getIntegral(double start, double end);
	
	/**
	 * Returns a number proportional to the relative probability that a recombination occurs at the given site. 
	 * @param site
	 * @return
	 */
	public double getSiteProbability(int site);
}
