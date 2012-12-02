/********************************************************************
*
* 	Copyright 2011 Brendan O'Fallon
*
*   Licensed under the Apache License, Version 2.0 (the "License");
*   you may not use this file except in compliance with the License.
*   You may obtain a copy of the License at
*
*       http://www.apache.org/licenses/LICENSE-2.0
*
*   Unless required by applicable law or agreed to in writing, software
*   distributed under the License is distributed on an "AS IS" BASIS,
*   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*   See the License for the specific language governing permissions and
*   limitations under the License.
*
***********************************************************************/


package newgui.gui.modelElements;


import java.util.ArrayList;
import java.util.List;

import newgui.gui.modelElements.Configurator.InputConfigException;
import newgui.gui.modelElements.DoubleModifierElement.ModType;
import newgui.gui.modelElements.DoublePriorModel.PriorType;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import parameter.DoubleParameter;
import xml.XMLLoader;

import coalescent.ConstantRecombination;
import coalescent.RecombinationParameter;
import document.ACGDocument;

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
		recModel.getPriorModel().setType(PriorType.Exponential);
		recModel.getPriorModel().setMean(100.0);
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
			recModel.setLowerBound(0.0);
			recModel.setUpperBound(0.0);
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
		
		boolean foundModifier = false;
		if ( getChildCount(doc, recRateEl) > 0) {
			Element child = getChild(doc, recRateEl, 0);
			if (DoubleModifierElement.isAssignable( child )) {
				DoubleModifierElement modEl = new DoubleModifierElement( child );
				recModel.setModifierFrequency( modEl.getFrequency() );
				recModel.setModifierLabel( modEl.getLabel() );
				recModel.setModifierType( modEl.getType() );
				foundModifier = true;
			}
		}
		
		if (! foundModifier) 
			recModel.setModifierType(null);
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
