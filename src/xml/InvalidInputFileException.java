package xml;

/**
 * These get thrown when theres a problem with an input file that is not XML-parsing or IO related
 *
 */
public class InvalidInputFileException extends RuntimeException {
	
	public InvalidInputFileException(String message) {
		super(message);
	}
}
