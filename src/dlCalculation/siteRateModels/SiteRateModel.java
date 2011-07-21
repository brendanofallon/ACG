package dlCalculation.siteRateModels;

import parameter.Parameter;

/**
 * A description of how evolutionary rate 
 * @author brendan
 *
 */
public interface SiteRateModel extends Parameter<SiteRates>{
	
	/**
	 * The number of rate categories
	 * @return
	 */
	public int getCategoryCount();
	
	/**
	 * The rate associated with the given category 
	 * @param category
	 * @return
	 */
	public double getRateForCategory(int category);
	
	/**
	 * Get the probability that a site in the given rate category
	 * @param category
	 * @return
	 */
	public double getProbForCategory(int category);
	
	


}
