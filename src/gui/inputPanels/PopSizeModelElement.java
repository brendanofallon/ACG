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

import coalescent.ConstantPopSize;
import coalescent.DemographicParameter;
import coalescent.ExponentialGrowth;

public class PopSizeModelElement extends ModelElement {

	//Default label for this node
	String modelLabel = "PopulationSize";

	enum PopSizeModel { Constant, ExpGrowth }
	private PopSizeModel modelType = PopSizeModel.Constant;
	private double initialPopSize = 0.001;
	
	//Used for Constant pop size case
	private DoubleModifierElement modifierElement = null;
	
	private DoubleParamElement baseSizeElement = null;
	private DoubleParamElement growthRateElement = null;
	private String baseSizeLabel = "BaseSize";
	private String growthRateLabel = "GrowthRate";
	
	public PopSizeModelElement() {
		//Don't think theres any initialization?
	}
	
	public void setModelType(PopSizeModel type) {
		this.modelType = type;
		if (modelType == PopSizeModel.ExpGrowth && baseSizeElement == null) {
			baseSizeElement = new DoubleParamElement();
			baseSizeElement.setLabel(baseSizeLabel);
			baseSizeElement.setValue(0.005);
			baseSizeElement.setLowerBound(0);
			baseSizeElement.setModifierType(ModType.Scale);
			
			growthRateElement = new DoubleParamElement();
			growthRateElement.setLabel(growthRateLabel);
			growthRateElement.setLowerBound(Double.NEGATIVE_INFINITY);
			growthRateElement.setValue(0.0); //Zero is OK if we use a 'simple' modifier 
			growthRateElement.setModifierType(ModType.Simple);
		}
	}
	
	public PopSizeModel getModelType() {
		return modelType;
	}
	
	@Override
	public List<Node> getElements(ACGDocument doc) throws InputConfigException {
		List<Node> nodes = new ArrayList<Node>();
		
		Element popEl = doc.createElement(modelLabel);
		nodes.add(popEl);
		
		if (modelType == PopSizeModel.Constant) {
			popEl.setAttribute(XMLLoader.CLASS_NAME_ATTR, ConstantPopSize.class.getCanonicalName());
			popEl.setAttribute(DoubleParameter.XML_VALUE, "" + initialPopSize);
			if (modifierElement != null)
				popEl.appendChild( modifierElement.getElement(doc));
			
		}

		
		if (modelType == PopSizeModel.ExpGrowth) {
			popEl.setAttribute(XMLLoader.CLASS_NAME_ATTR, ExponentialGrowth.class.getCanonicalName());
			popEl.appendChild( baseSizeElement.getElement(doc));
			popEl.appendChild( growthRateElement.getElement(doc));
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
					modifierElement = new DoubleModifierElement(childEl);
				}
			}
		}
		
		
		if (className.equals(ExponentialGrowth.class.getCanonicalName())) {
			setModelType(PopSizeModel.ExpGrowth);
			if ( getChildCount(doc, popSizeElement) != 2) 
				throw new InputConfigException("Exponential pop size requires exactly two child elements (base size and growth rate)");
			
			baseSizeElement = new DoubleParamElement( getChild(doc, popSizeElement, 0));
			growthRateElement = new DoubleParamElement( getChild( doc, popSizeElement, 1));
		}
	}

	
	public String getModelLabel() {
		return modelLabel;
	}

	public void setModelLabel(String modelLabel) {
		this.modelLabel = modelLabel;
	}

	public double getInitialPopSize() {
		return initialPopSize;
	}

	public void setInitialPopSize(double initialPopSize) {
		this.initialPopSize = initialPopSize;
	}
}
