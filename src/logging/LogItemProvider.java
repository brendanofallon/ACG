package logging;

/**
 * An interface for objects that can be queried for items to log. The idea is that various things, notably
 * parameters, may provide zero or more items to be written to log files. These items have names, such as
 * 'population size', 'recombination rate', etc, and it is helpful to be able to query the current value based
 * on the name, so we could, for instance, ask a parameter to get the value of the item whose name is 'popSize', or
 * whatever. 
 * 
 * @author brendan
 *
 */
public interface LogItemProvider {
	
	/**
	 * Obtain the number of loggable items given by this provider
	 * @return
	 */
	public int getKeyCount();
	
	/**
	 * Get the names (keys) of all loggable items provided by this provider
	 * @return
	 */
	public String[] getLogKeys();
	
	
	/**
	 * Get the current value of the loggable item associated with the given key
	 * @param key
	 * @return
	 */
	public Object getLogItem(String key);

}
