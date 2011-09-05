package gui.inputPanels;

import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import xml.XMLLoader;

/**
 * A small class to create a DataLikelihood calculator element from an ARG and a MutationModel, and
 * (optionally) a siteModel
 * @author brendan
 *
 */
public class DLConfigurator implements Configurator {
	
	private String argLabel = null;
	private String mutModelLabel = null;
	private String siteModelLabel = null;
	
	List<Element> params = new ArrayList<Element>();
	List<Element> likelihoods = new ArrayList<Element>();;
	
	public void setARG(Element argEl) {
		this.argLabel = argEl.getNodeName();
	}
	
	public void setMutModel(Element mutModelEl) {
		this.mutModelLabel = mutModelEl.getNodeName();
	}
	
	public void setSiteModel(Element siteModelEl) {
		this.siteModelLabel = siteModelEl.getNodeName();
	}
	
	@Override
	/**
	 * Returns a DataLikelihood element containing the  
	 */
	public Element[] getRootXMLNodes(Document doc)
			throws ParserConfigurationException, InputConfigException {
		
		if (mutModelLabel == null)
			throw new InputConfigException("No mutation model has been specified");
		if (argLabel == null)
			throw new InputConfigException("No ARG has been specified");
		
		params = new ArrayList<Element>();
		likelihoods = new ArrayList<Element>();
		
		Element dlElement = doc.createElement("DataLikelihood");
		dlElement.setAttribute(XMLLoader.CLASS_NAME_ATTR, dlCalculation.DataLikelihood.class.getCanonicalName());
		likelihoods.add(dlElement);
		
		Element mutModelRef = doc.createElement(mutModelLabel);
		dlElement.appendChild(mutModelRef);
		if (siteModelLabel != null) {
			Element siteModelRef = doc.createElement(siteModelLabel);
			dlElement.appendChild(siteModelRef);
		}
		
		Element argRef = doc.createElement(argLabel);
		dlElement.appendChild(argRef);
		
		return new Element[]{dlElement};
	}
	
	
	@Override
	public Element[] getParameters() {
		return params.toArray(new Element[]{});
	}

	@Override
	public Element[] getLikelihoods() {
		return likelihoods.toArray(new Element[]{});
	}

}

