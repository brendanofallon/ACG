package gui.inputPanels;

import gui.document.ACGDocument;
import gui.inputPanels.Configurator.InputConfigException;

import java.util.ArrayList;
import java.util.List;

import mcmc.MCMC;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Model implementation for MCMC elements
 * @author brendano
 *
 */
public class MCMCModelElement extends ModelElement {

	String label = "MCMC";
	

	int runLength;
	boolean verbose = false;
	
	
	List<Element> paramRefs = new ArrayList<Element>();
	List<Element> likeRefs = new ArrayList<Element>();
	List<Element> listenerRefs = new ArrayList<Element>();
	
	public void clearReferences() {
		paramRefs.clear();
		likeRefs.clear();
		listenerRefs.clear();
	}
	
	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}
	
	public void addParamRef(Element param) {
		paramRefs.add(param);
	}
	
	public void addLikelihoodRef(Element like) {
		likeRefs.add(like);
	}
	
	public void addListenerRef(Element listener) {
		listenerRefs.add(listener);
	}
	
	public void setRunLength(int runLength) {
		this.runLength = runLength;
	}
	
	@Override
	public List<Node> getElements(ACGDocument doc) throws InputConfigException {
		List<Node> nodes = new ArrayList<Node>();
		
		Element mcEl = createElement(doc, label, MCMC.class);
		mcEl.setAttribute(MCMC.XML_RUNLENGTH, "" + runLength);
		mcEl.setAttribute(MCMC.XML_RUNNOW, "true");
		nodes.add(mcEl);
		
		Element paramList = createList(doc, "MCParams");
		mcEl.appendChild(paramList);
		for(Element param : paramRefs) {
			paramList.appendChild( doc.createElement(param.getNodeName()) );
		}
		
		Element likeList = createList(doc, "MCLikelihoods");
		mcEl.appendChild(likeList);
		for(Element like : likeRefs) {
			likeList.appendChild( doc.createElement(like.getNodeName()) );
		}
		
		Element listenerList = createList(doc, "MCListeners");
		mcEl.appendChild(listenerList);
		for(Element listener : listenerRefs) {
			listenerList.appendChild( doc.createElement(listener.getNodeName()) );
		}
		
		return nodes;
	}

	@Override
	public void readElements(ACGDocument doc) throws InputConfigException {
		// TODO Auto-generated method stub
		
	}

}
