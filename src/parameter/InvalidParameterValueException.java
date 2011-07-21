package parameter;

/**
 * Exceptions of this type are thrown when an invalid value is supplied as the argument to setValue to a parameter
 * @author brendan
 *
 */
public class InvalidParameterValueException extends Exception {

	Parameter source;
	String message;
	
	public InvalidParameterValueException(Parameter source, String message) {
		super(message);
		this.source = source;
		this.message = message;
	}
	
	public Parameter getSource() {
		return source;
	}
	
}
