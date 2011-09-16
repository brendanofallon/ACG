package gui.inputPanels;

import gui.document.ACGDocument;
import gui.widgets.RoundedPanel;

import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
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
public class MCMCConfigurator extends RoundedPanel implements Configurator {

	List<String> paramRefs = new ArrayList<String>();
	List<String> likelihoodRefs = new ArrayList<String>();
	List<String> listenerRefs = new ArrayList<String>();
	
	Integer runLength = null;
	
	String[] mcTypes = new String[]{"Single chain", "Heated chains"};
	JComboBox mcTypeBox;
	JSpinner runLengthSpinner;
	
	JLabel chainNumberLabel;
	JSpinner chainNumberSpinner;
	
	public MCMCConfigurator() {
		setMaximumSize(new Dimension(1000, 50));
		setPreferredSize(new Dimension(500, 50));
		
		
		add(new JLabel("Chain type:"));
		mcTypeBox = new JComboBox(mcTypes);
		add(mcTypeBox);
		mcTypeBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent arg0) {
				mcTypeChanged(arg0);
			}
		});
		
		SpinnerNumberModel model = new SpinnerNumberModel(25000000, 1000, 2000000000, 100000);
		runLengthSpinner = new JSpinner(model);
		add(new JLabel("Run length:"));
		add(runLengthSpinner);
		
		chainNumberLabel = new JLabel("Chain number:");
		SpinnerNumberModel chainModel = new SpinnerNumberModel(4, 2, 32, 1);
		chainNumberSpinner = new JSpinner(chainModel);
		
		
	}
	
	
	protected void mcTypeChanged(ItemEvent arg0) {
		if (mcTypeBox.getSelectedIndex()==0) {
			remove(chainNumberSpinner);
			remove(chainNumberLabel);
		}
		else {
			add(chainNumberLabel);
			add(chainNumberSpinner);
		}
		this.getMainPanel().revalidate();
		repaint();
	}


	/**
	 * Add a reference to this parameter element to the parameters list 
	 * @param param
	 */
	public void addParameterRef(Element param) {
		//Don't add multiple elements with same label
		if (! containsLabel(param.getNodeName(), paramRefs))
			paramRefs.add(param.getNodeName());
	}
	
	private static boolean containsLabel(String label, List<String> list) {
		for(String str : list) {
			if (str.equals(label)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Clear all references to likelihoods, parameters, and listeners. This should happen between every call to 
	 * getRootXMLNodes
	 */
	public void clearAllReferences() {
		likelihoodRefs.clear();
		paramRefs.clear();
		listenerRefs.clear();
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

	
	@Override
	public Element[] getRootXMLNodes(Document doc)
			throws ParserConfigurationException, InputConfigException {

		runLength = (Integer)runLengthSpinner.getValue();

		if (paramRefs.isEmpty()) 
			throw new InputConfigException("No parameters have been set, cannot create MCMC");
		if (likelihoodRefs.isEmpty())
			throw new InputConfigException("No likelihood components have been set, cannot create MCMC");
		
		Element mcEl = doc.createElement("MarkovChain");
		mcEl.setAttribute(XMLLoader.CLASS_NAME_ATTR, mcmc.MCMC.class.getCanonicalName());
		
		mcEl.setAttribute(MCMC.XML_RUNLENGTH, "" + runLength);

		mcEl.setAttribute(MCMC.XML_RUNNOW, "true");
		 
		Element paramList = doc.createElement("parameters");
		paramList.setAttribute(XMLLoader.CLASS_NAME_ATTR, XMLLoader.LIST_ATTR);
		for(String param : paramRefs) {
			paramList.appendChild( doc.createElement(param));
		}
		mcEl.appendChild(paramList);
		
		Element likeList = doc.createElement("likelihoods");
		likeList.setAttribute(XMLLoader.CLASS_NAME_ATTR, XMLLoader.LIST_ATTR);
		for(String like : likelihoodRefs) {
			likeList.appendChild( doc.createElement(like));
		}
		mcEl.appendChild(likeList);
		
		Element listenerList = doc.createElement("listeners");
		listenerList.setAttribute(XMLLoader.CLASS_NAME_ATTR, XMLLoader.LIST_ATTR);
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


	@Override
	public void readNodesFromDocument(ACGDocument doc)
			throws InputConfigException {
		// TODO Auto-generated method stub
		
	}

}
