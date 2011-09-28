package gui.inputPanels;

import gui.document.ACGDocument;
import gui.inputPanels.Configurator.InputConfigException;
import gui.inputPanels.DoubleModifierElement.ModType;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import parameter.DoubleParameter;
import xml.XMLLoader;

import coalescent.ConstantRecombination;
import coalescent.RecombinationParameter;

/**
 * Model implememtation for recombination rate. Right now only "ConstantRecombination" is supported,
 * and this is basically just a (very) thin wrapper for a DoubleParamElement 
 * @author brendano
 *
 */
public class RecombRateModel extends ModelElement {

	final DoubleParamElement recModel = new DoubleParamElement();
	
	public RecombRateModel() {
		initialize();
	}
	
	private void initialize() {
		recModel.setLabel("RecombinationRate");
		recModel.setValue( 1.0);
		recModel.setLowerBound(0);
		recModel.setUpperBound(Double.POSITIVE_INFINITY);
		recModel.setModifierType( ModType.Scale );
		recModel.setModifierLabel("RecombRateModifier");
		recModel.setModifierFrequency(0.1);
	}
	
	public String getModelLabel() {
		if (recModel == null)
			initialize();
		return recModel.getLabel();
	}
	
	public void setModelLabel(String label) {
		if (recModel == null)
			initialize();
		recModel.setLabel(label);
	}
	
	@Override
	public List<Node> getElements(ACGDocument doc) throws InputConfigException {
		List<Node> nodes = new ArrayList<Node>();
		params.clear();
		
		if (recModel != null) {
			Element recRate =  recModel.getElement(doc); 
			recRate.setAttribute(XMLLoader.CLASS_NAME_ATTR, ConstantRecombination.class.getCanonicalName());
			params.add(recModel);
			nodes.add( recRate );
		}
		return nodes;
	}

	@Override
	public void readElements(ACGDocument doc) throws InputConfigException {
		Element recRateEl = getOptionalElementForClass(doc, RecombinationParameter.class);
		if (recRateEl == null) {
			recModel.setValue(0);
			recModel.setModifierType(null);
			return;
		}
		
		setModelLabel(recRateEl.getNodeName());
		
		Double recRate = getDoubleAttribute(recRateEl, DoubleParameter.XML_VALUE);
		recModel.setValue( recRate );
		Double freq = getOptionalDoubleAttribute(recRateEl, DoubleParameter.XML_PARAM_FREQUENCY);
		if (freq != null)
			recModel.setFrequency(freq);
		Double lowerBound = getOptionalDoubleAttribute(recRateEl, DoubleParameter.XML_LOWERBOUND);
		if (lowerBound != null)
			recModel.setLowerBound(lowerBound);
		Double upperBound = getOptionalDoubleAttribute(recRateEl, DoubleParameter.XML_UPPERBOUND);
		if (upperBound != null)
			recModel.setUpperBound(upperBound);
		
		
		if ( getChildCount(doc, recRateEl) > 0) {
			Element child = getChild(doc, recRateEl, 0);
			if (DoubleModifierElement.isAssignable( child )) {
				DoubleModifierElement modEl = new DoubleModifierElement( child );
				recModel.setModifierFrequency( modEl.getFrequency() );
				recModel.setModifierLabel( modEl.getLabel() );
				recModel.setModifierType( modEl.getType() );
			}
		}
	}


	public void setUseModifier(boolean use) {
		if (use) {
			if (recModel == null)
				initialize();
			recModel.setModifierType( ModType.Scale );
		}
		else {
			recModel.setModifierType( null );		
		}
		
	}
	public double getInitialRecRate() {
		if (recModel == null)
			initialize();
		return recModel.getValue();
	}

	public void setInitialRecRate(double initialRecRate) {
		if (recModel == null)
			initialize();
		recModel.setValue( initialRecRate);
	}

	public String getModifierLabel() {
		if (recModel == null)
			initialize();
		return recModel.getModLabel();
	}

	public void setModifierLabel(String modifierLabel) {
		if (recModel == null)
			initialize();
		recModel.setModifierLabel(modifierLabel);
	}

	public DoubleParamElement getModel() {
		return recModel;
	}

	@Override
	public List<DoubleParamElement> getDoubleParameters() {
		return params;
	}

	List<DoubleParamElement> params = new ArrayList<DoubleParamElement>();
}
