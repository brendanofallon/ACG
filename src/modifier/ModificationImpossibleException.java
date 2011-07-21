package modifier;

/**
 * These are thrown if a modifier cannot perform its operation on the parameter. This signals to the MCMC
 * that no new state should be logged. 
 * @author brendan
 *
 */
public class ModificationImpossibleException extends Exception {

	public ModificationImpossibleException(String message) {
		super(message);
	}
}
