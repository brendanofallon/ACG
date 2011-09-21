package gui.inputPanels;

import java.util.List;

import org.w3c.dom.Element;

/**
 * A document member is a self-contained collection of likelihoods, parameters, and at least one alignment or tree/ARG. 
 * Typically, it will contain the following elements:
 *  
 *  An alignment
 *  A site model
 *  A coalescent likelihood model
 *  A DL element
 *  
 *  These elements are currently stored as XML DOM objects, since that provides a handy tree-oriented way to describe the collection
 *  
 * @author brendano
 *
 */
public class DocumentMember {

	
	
	
	public Element getAlignmentElement() {
		return null;
	}
	
	public List<Element> getSiteModelElements() {
		return null;
	}
	
	public List<Element> getCoalescentModelElements() {
		return null;
	}
	
	public List<Element> getDLElements() {
		return null;
	}
	
	public List<Element> getRootLevelElements() {
		return null;
	}
	
	public List<Element> getAllParameters() {
		return null;
	}
	
	public List<Element> getAllLikelihoods() {
		return null;
	}
	
	public List<Element> getAllListeners() {
		return null;
	}
	
}
