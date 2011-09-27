package gui.inputPanels;

import gui.document.ACGDocument;
import gui.inputPanels.Configurator.InputConfigException;
import gui.inputPanels.DoubleModifierElement.ModType;

import java.util.ArrayList;
import java.util.List;

import mcmc.MCMC;
import mcmc.mc3.ChainHeats;
import mcmc.mc3.ExpChainHeats;
import mcmc.mc3.MC3;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Model implementation for MCMC elements
 * @author brendano
 *
 */
public class MCMCModelElement extends ModelElement {

	String label = "MCMC";
	private long runLength;
	private boolean verbose = false;
	
	private boolean useMC3 = false;
	private int threads = 4;
	private int chains = 8;
	private int swapSteps = 500;
	private DoubleParamElement lambda;
	private boolean useAdaptiveMC3 = true;
	private String mc3Label = "MC3";
	


	List<Element> paramRefs = new ArrayList<Element>();
	List<Element> likeRefs = new ArrayList<Element>();
	List<Element> listenerRefs = new ArrayList<Element>();
	
	public MCMCModelElement() {
		lambda = new DoubleParamElement();
		lambda.setValue(0.001);
		lambda.setUpperBound(0.5);
		lambda.setLowerBound(1e-10);
		lambda.setLabel("Lambda");
		lambda.setModifierLabel("ChainHeatsModifier");
	}
	
	public void clearReferences() {
		paramRefs.clear();
		likeRefs.clear();
		listenerRefs.clear();
	}

	public String getMc3Label() {
		return mc3Label;
	}

	public void setMc3Label(String mc3Label) {
		this.mc3Label = mc3Label;
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
	
	public boolean isUseMC3() {
		return useMC3;
	}

	public void setUseMC3(boolean useMC3) {
		this.useMC3 = useMC3;
	}

	public int getThreads() {
		return threads;
	}

	public void setThreads(int threads) {
		this.threads = threads;
	}

	public int getChains() {
		return chains;
	}

	public void setChains(int chains) {
		this.chains = chains;
	}

	public DoubleParamElement getLambda() {
		return lambda;
	}

	public void setLambda(DoubleParamElement lambda) {
		this.lambda = lambda;
	}

	public boolean isUseAdaptiveMC3() {
		return useAdaptiveMC3;
	}

	public void setUseAdaptiveMC3(boolean useAdaptiveMC3) {
		this.useAdaptiveMC3 = useAdaptiveMC3;
		if (useAdaptiveMC3) {
			lambda.setModifierType(ModType.Scale);
		}
		else {
			lambda.setModifierType(null);
		}
	}

	public long getRunLength() {
		return runLength;
	}

		
	@Override
	public List<Node> getElements(ACGDocument doc) throws InputConfigException {
		List<Node> nodes = new ArrayList<Node>();
		
		Element mcEl = createElement(doc, label, MCMC.class);
		mcEl.setAttribute(MCMC.XML_RUNLENGTH, "" + runLength);
		mcEl.setAttribute(MCMC.XML_RUNNOW, "true"); //Will get set to false if MC3
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
		
		if (! useMC3) {
			Element listenerList = createList(doc, "MCListeners");
			mcEl.appendChild(listenerList);
			for(Element listener : listenerRefs) {
				listenerList.appendChild( doc.createElement(listener.getNodeName()) );
			}
		}
		else {
			Element mc3El = createElement(doc, getMc3Label(), MC3.class);
			mc3El.appendChild( doc.createElement( mcEl.getNodeName() ));
			mcEl.setAttribute(MCMC.XML_RUNNOW, "false");
			mc3El.setAttribute(MC3.XML_CHAINS, "" + chains);
			mc3El.setAttribute(MC3.XML_THREADS, "" + threads);
			mc3El.setAttribute(MC3.XML_SWAPSTEPS, "" + swapSteps);
			mc3El.setAttribute(MCMC.XML_RUNLENGTH, "" + runLength);
			
			Element chainHeatsEl = createElement(doc, "ChainHeats", ExpChainHeats.class);
			chainHeatsEl.setAttribute(ChainHeats.XML_CHAINNUMBER, "" + chains);
			chainHeatsEl.appendChild( lambda.getElement(doc) );
			mc3El.appendChild( chainHeatsEl );
			
			Element listenerList = createList(doc, "MCListeners");
			mc3El.appendChild(listenerList);
			for(Element listener : listenerRefs) {
				listenerList.appendChild( doc.createElement(listener.getNodeName()) );
			}
			
			nodes.add(mc3El);
		}
		
		return nodes;
	}

	@Override
	public void readElements(ACGDocument doc) throws InputConfigException {
		List<String> mcLabels = doc.getLabelForClass(MCMC.class);
		if (mcLabels.size()==0)
			throw new InputConfigException("Could not find any MCMC object in document");
		
		Element mcEl = doc.getElementForLabel( mcLabels.get(0) );
		
		List<String> mc3Labels = doc.getLabelForClass(MC3.class);
		if (mc3Labels.size() > 0) {
			Element mc3El = doc.getElementForLabel(mc3Labels.get(0));
			useMC3 = true;
			Integer attrThreads = super.getOptionalIntegerAttribute(mc3El, MC3.XML_THREADS);
			if (attrThreads != null)
				this.threads = attrThreads;
			Integer attrChains = super.getOptionalIntegerAttribute(mc3El, MC3.XML_CHAINS);
			if (attrChains != null)
				this.chains = attrChains;
			Integer attrSwapSteps = super.getOptionalIntegerAttribute(mc3El, MC3.XML_SWAPSTEPS);
			if (attrSwapSteps != null) 
				this.swapSteps = attrSwapSteps;
			
			
			Element chainHeatsEl = getChild(doc, mc3El, 1);
			if ( getChildCount(doc, chainHeatsEl) != 0) {
				Element lambdaEl = getChild(doc, chainHeatsEl, 0);
				if ( DoubleParamElement.isAcceptable(lambdaEl)) {
					lambda = new DoubleParamElement( lambdaEl );
					if (lambda.getModType() != null) {
						useAdaptiveMC3 = true;
					}
					
				}
			}
			
			Element listenerListEl = getChild(doc, mc3El, 2);
			NodeList childList = listenerListEl.getChildNodes();
			for(int i=0; i<childList.getLength(); i++) {
				Node child = childList.item(i);
				if (child.getNodeType() == Node.ELEMENT_NODE)
					addListenerRef( (Element)child );
			}
		}
		else {
			useMC3 = false;
		}
		
		int childCount = getChildCount(doc, mcEl);
		if (childCount < 2 || childCount > 3) {
			throw new InputConfigException("Need exactly three children of type list for MCMC object");
		}
		
		Element paramListEl = getChild(doc, mcEl, 0);
		NodeList childList = paramListEl.getChildNodes();
		for(int i=0; i<childList.getLength(); i++) {
			Node child = childList.item(i);
			if (child.getNodeType() == Node.ELEMENT_NODE)
				addParamRef( (Element)child );
		}
				
		Element likeListEl = getChild(doc, mcEl, 1);
		childList = likeListEl.getChildNodes();
		for(int i=0; i<childList.getLength(); i++) {
			Node child = childList.item(i);
			if (child.getNodeType() == Node.ELEMENT_NODE)
				addLikelihoodRef( (Element)child );
		}
		
		if (childCount > 2) {
			Element listenerListEl = getChild(doc, mcEl, 2);
			childList = listenerListEl.getChildNodes();
			for(int i=0; i<childList.getLength(); i++) {
				Node child = childList.item(i);
				if (child.getNodeType() == Node.ELEMENT_NODE)
					addListenerRef( (Element)child );
			}
		}
		
		
	}
	
	

	@Override
	public List<DoubleParamElement> getDoubleParameters() {
		return new ArrayList<DoubleParamElement>();
	}


}
