package gui.inputPanels;

import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import mcmc.MCMC;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import xml.XMLLoader;

/**
 * Produces XML elements 
 * @author brendan
 *
 */
public class MCMCConfigurator implements Configurator {

	List<String> paramRefs = new ArrayList<String>();
	List<String> likelihoodRefs = new ArrayList<String>();
	List<String> listenerRefs = new ArrayList<String>();
	
	Integer runLength = null;
	
	/**
	 * Add a reference to this parameter element to the parameters list 
	 * @param param
	 */
	public void addParameterRef(Element param) {
		paramRefs.add(param.getNodeName());
	}
	
	/**
	 * Add a reference to the likelihood component described by the element  
	 * @param likelihood
	 */
	public void addLikelihoodRef(Element likelihood) {
		likelihoodRefs.add(likelihood.getNodeName());
	}
	
	/**
	 * Add a reference to the given listener 
	 * @param listener
	 */
	public void addListenerRef(Element listener) {
		listenerRefs.add(listener.getNodeName());
	}
	
	/**
	 * Set the number of states for which the chain will be run
	 * @param length
	 */
	public void setRunLength(Integer length) {
		this.runLength = length;
	}
	
	@Override
	public Element[] getRootXMLNodes(Document doc)
			throws ParserConfigurationException, InputConfigException {

		if (runLength == null)
			throw new InputConfigException("Run length has not been set, cannot create MCMC");
		if (paramRefs.isEmpty()) 
			throw new InputConfigException("No parameters have been set, cannot create MCMC");
		if (likelihoodRefs.isEmpty())
			throw new InputConfigException("No likelihood components have been set, cannot create MCMC");
		
		Element mcEl = doc.createElement("MarkovChain");
		mcEl.setAttribute(XMLLoader.CLASS_NAME_ATTR, mcmc.MCMC.class.getCanonicalName());
		mcEl.setAttribute(MCMC.XML_RUNLENGTH, "" + runLength);
		 
		Element paramList = doc.createElement("parameters");
		for(String param : paramRefs) {
			paramList.appendChild( doc.createElement(param));
		}
		mcEl.appendChild(paramList);
		
		Element likeList = doc.createElement("likelihoods");
		for(String like : likelihoodRefs) {
			likeList.appendChild( doc.createElement(like));
		}
		mcEl.appendChild(likeList);
		
		Element listenerList = doc.createElement("listeners");
		for(String listener : listenerRefs) {
			listenerList.appendChild( doc.createElement(listener));
		}
		mcEl.appendChild(listenerList);
		
		
		return new Element[]{mcEl};
	}

	@Override
	public Element[] getParameters() {
		return new Element[]{};
	}

	@Override
	public Element[] getLikelihoods() {
		return new Element[]{};
	}

}
