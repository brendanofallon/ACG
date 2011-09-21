package gui.inputPanels;

import gui.document.ACGDocument;
import gui.inputPanels.Configurator.InputConfigException;
import gui.inputPanels.DoubleModifierElement.ModType;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import parameter.DoubleParameter;

import coalescent.ConstantRecombination;
import coalescent.RecombinationParameter;

/**
 * Model implememtation for recombination rate. Right now only "ConstantRecombination" is supported. 
 * @author brendano
 *
 */
public class RecombRateModel extends ModelElement {

	private double initialRecRate = 1.0;
	private DoubleModifierElement modEl = null;
	private String modifierLabel = "RecRateModifier";
	private String modelLabel = "RecombinationRate";
	
	public RecombRateModel() {
		modEl = new DoubleModifierElement();
		modEl.setType(ModType.Scale);
		modEl.setFrequency(0.2);
		modEl.setLabel("RecRateModifier");
	}
	
	public String getModelLabel() {
		return modelLabel;
	}
	
	public void setModelLabel(String label) {
		this.modelLabel = label;
	}
	
	@Override
	public List<Node> getElements(ACGDocument doc) throws InputConfigException {
		List<Node> nodes = new ArrayList<Node>();
		Element el = createElement(doc, modelLabel, ConstantRecombination.class);
		el.setAttribute(DoubleParameter.XML_VALUE, "" + this.initialRecRate);
		
		if (modEl != null)
			el.appendChild(modEl.getElement(doc));
		
		nodes.add(el);
		return nodes;
	}

	@Override
	public void readElements(ACGDocument doc) throws InputConfigException {
		Element recRateEl = getOptionalElementForClass(doc, RecombinationParameter.class);
		setModelLabel(recRateEl.getNodeName());
		
		Double recRate = getOptionalDoubleAttribute(recRateEl, DoubleParameter.XML_VALUE);
		if (recRate != null)
			initialRecRate = recRate;
		
		if ( getChildCount(doc, recRateEl) > 0) {
			Element child = getChild(doc, recRateEl, 0);
			if (DoubleModifierElement.isAssignable( child )) {
				modEl= new DoubleModifierElement( child );
			}
		}
	}

	public void setUseModifier(boolean use) {
		if (use && modEl == null) {
			modEl = new DoubleModifierElement();
			modEl.setFrequency(0.2);
			modEl.setLabel(modifierLabel);
			modEl.setType(ModType.Scale);
		}
		if ( (!use) && modEl != null) {
			modEl = null;
		}
	}
	public double getInitialRecRate() {
		return initialRecRate;
	}

	public void setInitialRecRate(double initialRecRate) {
		this.initialRecRate = initialRecRate;
	}

	public String getModifierLabel() {
		return modifierLabel;
	}

	public void setModifierLabel(String modifierLabel) {
		this.modifierLabel = modifierLabel;
	}
}
