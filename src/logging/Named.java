package logging;

/**
 * Interface for all things with a 'name', which is basically an arbitrary label
 * mostly used for UI purposes
 * @author brendano
 *
 */
public interface Named {

	/**
	 * Obtain a user-friendly label for the element
	 * @return
	 */
	public String getName();
	
}
