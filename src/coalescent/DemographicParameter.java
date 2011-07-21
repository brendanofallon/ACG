package coalescent;

import parameter.AbstractParameter;

/**
 * Base class of things that specify a population size, potentially variable through time
 * @author brendan
 *
 */
public interface DemographicParameter {

	/**
	 * The integral of population size from time t0 to time t1 (t1 > t0)
	 * @param t0
	 * @param t1
	 * @return 
	 */
	public abstract double getIntegral(double t0, double t1);
	
	/**
	 * The population size at time t
	 * @param t
	 * @return
	 */
	public abstract double getPopSize(double t);
	
	
}
