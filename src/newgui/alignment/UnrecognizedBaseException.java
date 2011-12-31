package newgui.alignment;

/**
 * Typically thrown when we come across some base symbol that we don't recognize 
 * @author brendan
 *
 */
public class UnrecognizedBaseException extends Exception {

	public UnrecognizedBaseException(String message) {
		super(message);
	}
	
}
