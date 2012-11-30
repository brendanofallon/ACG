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
import java.util.Collection;
import java.util.List;

import newgui.gui.modelElements.Configurator.InputConfigException;
import newgui.gui.modelElements.DoubleModifierElement.ModType;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import parameter.DoubleParameter;

import xml.XMLLoader;

import coalescent.ConstantPopSize;
import coalescent.DemographicParameter;
import coalescent.ExponentialGrowth;
import document.ACGDocument;

public class PopSizeModelElement extends ModelElement {

	//Default label for this node
	String modelLabel = "PopulationSize";

	public enum PopSizeModel { Constant, ExpGrowth }
	private PopSizeModel modelType = PopSizeModel.Constant;
	
	//Following elements must not be reassigned since views store references to original models
	private final DoubleParamElement constSizeModel = new DoubleParamElement(); //Used to store info for constant pop size model
	private final DoubleParamElement baseSizeElement = new DoubleParamElement();
	private final DoubleParamElement growthRateElement = new DoubleParamElement();
	private final String constSizeLabel = "PopulationSize";
	private String baseSizeLabel = "BaseSize";
	private String growthRateLabel = "GrowthRate";
	
	public PopSizeModelElement() {
		constSizeModel.setLabel(constSizeLabel);
		constSizeModel.setValue(0.005);
		constSizeModel.setLowerBound(0);
		constSizeModel.setUpperBound(Double.POSITIVE_INFINITY);
		constSizeModel.setModifierType(ModType.Scale);
		constSizeModel.setModifierLabel(constSizeLabel + "Modifier");

		baseSizeElement.setLabel(baseSizeLabel);
		baseSizeElement.setValue(0.005);
		baseSizeElement.setLowerBound(0);
		baseSizeElement.setModifierType(ModType.Scale);
		baseSizeElement.setModifierLabel(baseSizeLabel + "Modifier");
		
		growthRateElement.setLabel(growthRateLabel);
		growthRateElement.setLowerBound(Double.NEGATIVE_INFINITY);
		growthRateElement.setValue(0.0); //Zero is OK if we use a 'simple' modifier 
		growthRateElement.setModifierType(ModType.Simple);
		growthRateElement.setModifierLabel(growthRateLabel + "Modifier");
	}
	
	public void setModelType(PopSizeModel type) {
		this.modelType = type;
	}
	
	public PopSizeModel getModelType() {
		return modelType;
	}
	
	@Override
	public List<Node> getElements(ACGDocument doc) throws InputConfigException {
		List<Node> nodes = new ArrayList<Node>();
		params.clear();
		
		Element popEl = doc.createElement(modelLabel);
		nodes.add(popEl);
		
		if (modelType == PopSizeModel.Constant) {
			popEl.setAttribute(XMLLoader.CLASS_NAME_ATTR, ConstantPopSize.class.getCanonicalName());
			popEl.setAttribute(DoubleParameter.XML_VALUE, "" + constSizeModel.getValue());
			popEl.setAttribute(DoubleParameter.XML_PARAM_FREQUENCY, "" + constSizeModel.getFrequency());
			popEl.setAttribute(DoubleParameter.XML_LOWERBOUND, "" + constSizeModel.getLowerBound());
			popEl.setAttribute(DoubleParameter.XML_UPPERBOUND, "" + constSizeModel.getUpperBound());
			if (constSizeModel.getModType() != null) {
				DoubleModifierElement modEl = new DoubleModifierElement();
				modEl.setLabel( constSizeModel.getModLabel() );
				modEl.setFrequency( constSizeModel.getModifierFrequency() );
				modEl.setType( constSizeModel.getModType() );
				popEl.appendChild( modEl.getElement(doc) );
			}
			constSizeModel.setLabel(modelLabel); //Make sure it has the same name as
			params.add( constSizeModel );
		}

		
		if (modelType == PopSizeModel.ExpGrowth) {
			popEl.setAttribute(XMLLoader.CLASS_NAME_ATTR, ExponentialGrowth.class.getCanonicalName());
			Element baseEl = baseSizeElement.getElement(doc);
			Element growthEl = growthRateElement.getElement(doc);
			popEl.appendChild( baseEl );
			popEl.appendChild( growthEl );
			params.add(baseSizeElement);
			params.add(growthRateElement);
		}
		
		return nodes;
	}

	@Override
	public void readElements(ACGDocument doc) throws InputConfigException {
		Element popSizeElement = getSingleElementForClass(doc, DemographicParameter.class);
		String className = popSizeElement.getAttribute(XMLLoader.CLASS_NAME_ATTR);
		
		//Constant pop size model. May be mutable or not
		if (className.equals(ConstantPopSize.class.getCanonicalName())) {
			setModelType(PopSizeModel.Constant);	
			
			List<String> children = doc.getChildrenForLabel(popSizeElement.getNodeName());
			for(String childLabel : children) {
				Element childEl = doc.getElementForLabel(childLabel);
				if (DoubleModifierElement.isAssignable(childEl)) {
					DoubleModifierElement modEl = new DoubleModifierElement(childEl);
					constSizeModel.setModifierType( modEl.getType() );
					constSizeModel.setModifierLabel( modEl.getLabel() );
					constSizeModel.setModifierFrequency( modEl.getFrequency() );
				}
				constSizeModel.readSettings(doc, popSizeElement);
			}
		}
		
		
		if (className.equals(ExponentialGrowth.class.getCanonicalName())) {
			setModelType(PopSizeModel.ExpGrowth);
			if ( getChildCount(doc, popSizeElement) != 2) 
				throw new InputConfigException("Exponential pop size requires exactly two child elements (base size and growth rate)");
			
			baseSizeElement.readSettings(doc, getChild(doc, popSizeElement, 0));
			growthRateElement.readSettings(doc, getChild( doc, popSizeElement, 1));
		}
	}

	
	public String getModelLabel() {
		return modelLabel;
	}

	public void setModelLabel(String modelLabel) {
		this.modelLabel = modelLabel;
	}

	public double getInitialPopSize() {
		return constSizeModel.getValue();
	}

	public void setInitialPopSize(double initialPopSize) {
		this.constSizeModel.setValue( initialPopSize );
	}
	
	/**
	 * The model element that provides into for the Constant Size model
	 * @return
	 */
	public DoubleParamElement getConstSizeModel() {
		return constSizeModel;
	}
	
	public DoubleParamElement getBaseSizeModel() {
		return baseSizeElement;
	}
	
	public DoubleParamElement getGrowthRateModel() {
		return growthRateElement;
	}

	public List<DoubleParamElement> getDoubleParameters() {
		return params;
	}
	
	//Maintains list of all double parameters we use
	private List<DoubleParamElement> params = new ArrayList<DoubleParamElement>();

}
