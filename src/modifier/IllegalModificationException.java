package modifier;

/**
 * These are thrown when we attempt to modify a parameter that is not in a valid state
 * @author brendan
 *
 */
public class IllegalModificationException extends Exception {

	public IllegalModificationException(String message) {
		super(message);
	}

}
