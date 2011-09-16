package gui.inputPanels;

import gui.document.ACGDocument;

import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import modifier.AbstractModifier;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import xml.XMLLoader;

import arg.ARG;
import arg.Newick;

/**
 * Configurator for ARGs. This takes no user input and is constructed entirely
 * from choices made for the other options 
 * @author brendan
 *
 */
public class ARGConfigurator implements Configurator {

	String alnLabel = null;
	
	//If no input alignment, then the following params are required
	Integer tips = null;
	Integer sites = null;
	Double theta = null;
	
	Newick newickStr = null;
	String startingFilename = null;
	boolean useUPGMA = true;
	
	List<Element> params;
	List<Element> likelihoods;
	
	/**
	 * Associate an alignment with this ARG
	 * @param aln
	 */
	public void setAlignment(Element aln) {
		this.alnLabel = aln.getNodeName();
		tips = null;
		sites = null;
		theta = null;
	}
	
	public void setTipsSitesTheta(int tips, int sites, double theta) {
		this.tips = tips;
		this.sites = sites;
		this.theta = theta;
		this.alnLabel = null;
	}
	
	/**
	 * Set a starting newick string for the ARG
	 * @param newick
	 */
	public void setStartingNewick(Newick newick) {
		this.newickStr = newick;
		startingFilename = null;
		this.useUPGMA = false;
	}
	
	/**
	 * Set the filename that contains the starting tree
	 * @param filename
	 */
	public void setStartFileName(String filename) {
		this.startingFilename = filename;
		this.newickStr = null;
		this.useUPGMA = false;
	}
	
	
	@Override
	public Element[] getRootXMLNodes(Document doc)
			throws ParserConfigurationException, InputConfigException {
		
		params = new ArrayList<Element>();
		likelihoods = new ArrayList<Element>();
		
		Element arg = doc.createElement("ARG1");
		arg.setAttribute(XMLLoader.CLASS_NAME_ATTR, arg.ARG.class.getCanonicalName());
		
		if (useUPGMA) {
			arg.setAttribute(ARG.XML_USEUPGMA, "true");
		}
		else {
			arg.setAttribute(ARG.XML_USEUPGMA, "false");
		}
		
		
		if (alnLabel == null) {
			if (tips == null)
				throw new InputConfigException("No alignment found, please specify tips, sites and theta to create an ARG");
			
			arg.setAttribute(ARG.XML_SITES, "" + sites);
			arg.setAttribute(ARG.XML_TIPS, "" + tips);
			arg.setAttribute(ARG.XML_THETA, "" + theta);
			
		}
		else {
			//An input alignment was supplied
			params.add(arg); //In this case, the ARG is a parameter that we estimate 
			Element alnEl = doc.createElement(alnLabel);
			arg.appendChild(alnEl);
			arg.setAttribute(ARG.XML_PARAM_FREQUENCY, "30");
			
			Element modList = doc.createElement("argModifiers");
			modList.setAttribute(XMLLoader.CLASS_NAME_ATTR, XMLLoader.LIST_ATTR);
			appendModifier(modList, doc, modifier.RecombAddRemove.class.getCanonicalName(), "recombAddRemove", 5);
			appendModifier(modList, doc, modifier.RootHeightModifier.class.getCanonicalName(), "rootHeight", 1);
			appendModifier(modList, doc, modifier.NodeHeightModifier.class.getCanonicalName(), "nodeHeight", 25);
			appendModifier(modList, doc, modifier.SubtreeSwap.class.getCanonicalName(), "subTreeSwapper", 5);
			appendModifier(modList, doc, modifier.WideSwap.class.getCanonicalName(), "wideSwapper", 5);
			appendModifier(modList, doc, modifier.BreakpointShifter.class.getCanonicalName(), "bpShifter", 5);
			appendModifier(modList, doc, modifier.BreakpointSwapper.class.getCanonicalName(), "bpSwapper", 5);
			
			arg.appendChild(modList);
		}
		
		return new Element[]{arg};
	}
	
	private void appendModifier(Element mods, Document doc, String className, String elementName, int frequency) { 
		Element mod = doc.createElement(elementName);
		mod.setAttribute(XMLLoader.CLASS_NAME_ATTR, className);
		mod.setAttribute(AbstractModifier.XML_FREQUENCY, "" + frequency);
		mods.appendChild(mod);
	}

	
	@Override
	public Element[] getParameters() {
		return (Element[])params.toArray(new Element[]{});
	}


	@Override
	public Element[] getLikelihoods() {
		return (Element[])likelihoods.toArray(new Element[]{});
	}

	@Override
	public void readNodesFromDocument(ACGDocument doc)
			throws InputConfigException {
		// TODO Auto-generated method stub
		
	}
}
