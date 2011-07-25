package gui.document;

import org.w3c.dom.Element;

/**
 * These are thrown when a non-critical document parsing error is encountered
 * while reading an ACGDocument.
 * @author brendan
 *
 */
public class StructureWarningException extends Exception {
	
	Element offendingElement = null;
	
	
	public StructureWarningException(String message) {
		super(message);
	}
	
	public StructureWarningException(Element offendingElement, String message) {
		super(message);
		this.offendingElement = offendingElement;
	}
	
	public Element getOffendingElement() {
		return offendingElement;
	}

}
