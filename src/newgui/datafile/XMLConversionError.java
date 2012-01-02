package newgui.datafile;

import org.w3c.dom.Element;

/**
 * Thrown when we can't parse a given element from a given xml element
 * @author brendan
 *
 */
public class XMLConversionError extends Exception {

	private final Element el;
	
	public XMLConversionError(String message, Element el) {
		super(message);
		this.el = el;
	}
	
	/**
	 * Obtain the Element that could not be properly parsed
	 * @return
	 */
	public Element getElement() {
		return el;
	}
	
}
