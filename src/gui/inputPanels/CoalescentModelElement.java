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


package gui.inputPanels;

import gui.document.ACGDocument;
import gui.inputPanels.Configurator.InputConfigException;
import gui.inputPanels.DoubleModifierElement.ModType;
import gui.inputPanels.PopSizeModelElement.PopSizeModel;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import coalescent.CoalescentLikelihood;

/**
 * Model implementation for Coalescent stuff - including pop size, recomb rate, etc. Mostly just a 
 * wrapper for a PopSizeModel and a recombination rate model, but also creates the coalescent likelihood
 * element. 
 * @author brendano
 *
 */
public class CoalescentModelElement extends ModelElement {
	
	private final PopSizeModelElement popSizeModel;
	private final RecombRateModel recRateModel;
	
	private ARGModelElement argRef;
	
	private String modelLabel = "CoalescentLikelihood";
	
	public CoalescentModelElement() {
		popSizeModel = new PopSizeModelElement();
		popSizeModel.setModelType(PopSizeModel.Constant);	
		recRateModel = new RecombRateModel();
	}

	@Override
	public List<Node> getElements(ACGDocument doc) throws InputConfigException {
		List<Node> nodes = new ArrayList<Node>();
		params.clear();
		
		nodes.addAll(popSizeModel.getElements(doc));
		params.addAll(popSizeModel.getDoubleParameters());
		nodes.addAll(recRateModel.getElements(doc));
		
		Element coalEl = createElement(doc, modelLabel, CoalescentLikelihood.class);
		coalEl.appendChild( doc.createElement(popSizeModel.getModelLabel()));
		
		coalEl.appendChild( doc.createElement(recRateModel.getModelLabel()));
		params.addAll(recRateModel.getDoubleParameters());
		
		coalEl.appendChild( doc.createElement(argRef.getModelLabel()));
		
		nodes.add(coalEl);
		return nodes;
	}

	@Override
	public void readElements(ACGDocument doc) throws InputConfigException {
		Element coalEl = getSingleElementForClass(doc, CoalescentLikelihood.class);
		this.setModelLabel(coalEl.getNodeName());
		
		popSizeModel.readElements(doc);
		recRateModel.readElements(doc);
	}
	
	public PopSizeModelElement getPopSizeModel() {
		return popSizeModel;
	}
	
	public RecombRateModel getRecombModel() {
		return recRateModel;
	}

	public void setARGRef(ARGModelElement argModel) {
		this.argRef = argModel;
	}

	/**
	 * Obtain whether or not the recomb model is 'on'. If not on, the value and both boundaries are equal to zero
	 * and there is no modifier. 
	 * @return
	 */
	public boolean getUseRecombination() {
		if (recRateModel.getModel().getValue()==0
			&& recRateModel.getModel().getLowerBound()==0
			&& recRateModel.getModel().getUpperBound()==0
			&& recRateModel.getModel().getModType()==null) {
			return false;
		}
		else
			return true;
	}
	/**
	 * Turn on/off recombination for this model. Off is equivalent to no modifier, and upper
	 * lower, and initial values all equal to zero
	 * @param useRecomb
	 */
	public void setUseRecombination(boolean useRecomb) {
		if (! useRecomb) {
			recRateModel.getModel().setValue(0);
			recRateModel.getModel().setModifierType(null);
			recRateModel.getModel().setLowerBound(0.0);
			recRateModel.getModel().setUpperBound(0.0);
		}
		else {
			recRateModel.getModel().setValue(1.0);
			recRateModel.getModel().setModifierType(ModType.Scale);
		}
	}
	
	public String getModelLabel() {
		return modelLabel;
	}

	public void setModelLabel(String modelLabel) {
		this.modelLabel = modelLabel;
	}

	public List<DoubleParamElement> getDoubleParameters() {
		return params;
	}
	
	//Maintains list of all double parameters we use
	private List<DoubleParamElement> params = new ArrayList<DoubleParamElement>();

}
