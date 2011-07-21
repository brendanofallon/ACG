package arg.argIO;

/**
 * These are thrown when there's an error trying to construct an ARG from xml
 * @author brendan
 *
 */
public class ARGParseException extends Exception {

	public ARGParseException(String message) {
		super(message);
	}
	
}
