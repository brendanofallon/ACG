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
		
	}
	
	public List<Element> getSiteModelElements() {
		
	}
	
	public List<Element> getCoalescentModelElements() {
		
	}
	
	public List<Element> getDLElements() {
		
	}
	
	public List<Element> getRootLevelElements() {
		
	}
	
	public List<Element> getAllParameters() {
		
	}
	
	public List<Element> getAllLikelihoods() {
		
	}
	
	public List<Element> getAllListeners() {
		
	}
	
}
