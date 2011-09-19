package gui.inputPanels;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import parameter.DoubleParameter;

import sequence.Alignment;

import xml.XMLLoader;
import dlCalculation.siteRateModels.ConstantSiteRates;
import dlCalculation.siteRateModels.GammaSiteRates;
import dlCalculation.substitutionModels.BaseFrequencies;
import dlCalculation.substitutionModels.F84Matrix;
import dlCalculation.substitutionModels.TN93Matrix;

import gui.ErrorWindow;
import gui.document.ACGDocument;
import gui.inputPanels.Configurator.InputConfigException;

/**
 * A class for reading and writing site model info to/ from XML
 * @author brendano
 *
 */
public class SiteModelElement {

	enum MutModelType { JC69, K2P, F84, TN93 };
	
	enum RateModelType { Constant, Gamma, Custom };
	
	AlignmentElement alignmentRef = null;
	
	List<Element> allParams = new ArrayList<Element>();
	List<Element> allLikes = new ArrayList<Element>();
	
	private MutModelType modelType = MutModelType.F84;
	private RateModelType rateModelType = RateModelType.Constant;
	
	private DoubleParamElement ttRatioElement = null;
	
	private DoubleParamElement kappaRElement = null;
	private DoubleParamElement kappaYElement = null;
	
	//If there's only one rate, this is it
	private double constantRate = 1.0;
	
	
	private int gammaRateClasses = 4;
	private boolean estimateAlpha = true;
	private double initialAlpha = 1.0; //Also used for constant alpha value
	
	
	public void setConstantRate(double rate) {
		this.constantRate = rate;
	}
	
	public void setRateModelType(RateModelType rateModel) {
		this.rateModelType = rateModel;
	}
	
	public void setAlignmentRef(AlignmentElement alnEl) {
		this.alignmentRef = alnEl;
	}
	
	public void setSiteModelType(MutModelType type) {
		this.modelType = type;
	}
	
	
	public void readElement(ACGDocument doc) throws InputConfigException {
		//We expect exactly one of either F84Matrix or TN93 matrix
		List<String> f84Labels = doc.getLabelForClass(F84Matrix.class);
		List<String> tn93Labels = doc.getLabelForClass(TN93Matrix.class);

		int sum = f84Labels.size() + tn93Labels.size();
		if (sum == 0) {
			throw new InputConfigException("Could not find any mutation model in document");
		}
		
		if (sum > 1) {
			throw new InputConfigException("Found multiple mutation models, we can only handle one at a time now");
		}
		
		String nodeLabel; 
		if (f84Labels.size()>0)
			nodeLabel = f84Labels.get(0);
		else
			nodeLabel = tn93Labels.get(0);
		
		
		try {
			Object mutModelObj = doc.getObjectForLabel(nodeLabel);
			if (mutModelObj instanceof F84Matrix) {
				F84Matrix mat = (F84Matrix)mutModelObj;
				setSiteModelType(MutModelType.F84);
				List<String> refs = doc.getChildrenForLabel(nodeLabel);
				
				for (String ref : refs) {
					Object refObj = doc.getObjectForLabel(ref);
					if (refObj instanceof DoubleParameter) {
						//Must be TT ratio
						ttRatioElement = new DoubleParamElement();
						kappaRElement = null;
						kappaYElement = null;
						
						Element ttEl = doc.getFirstElement(ref);
						ttRatioElement.readSettings( ttEl );
						
					}
					
					if (refObj instanceof BaseFrequencies) {
						
					}
				}
			}
			
			
			if (mutModelObj instanceof TN93Matrix) {
				TN93Matrix mat = (TN93Matrix)mutModelObj;
				setSiteModelType(MutModelType.TN93);
				
			}
			
			
		} catch (Exception ex) {
			ErrorWindow.showErrorWindow(ex);
		}
		
	}
	
	public List<Element> getElements(ACGDocument doc) {
		List<Element> elements = new ArrayList<Element>();
		
		try {
			Element rateModelNode = createRateModelNode(doc);
			elements.add(rateModelNode);
		} catch (InputConfigException e) {
			ErrorWindow.showErrorWindow(e);
		}
		
		Element mutModelNode = createMutNode(doc);
		elements.add(mutModelNode);
		return elements;
	}
	
	private Element createRateModelNode(ACGDocument doc) throws InputConfigException {
		Element siteNode = doc.createElement("siteModel");
		if (rateModelType == RateModelType.Constant) {
			siteNode.setAttribute(XMLLoader.CLASS_NAME_ATTR,  ConstantSiteRates.class.getCanonicalName());
			siteNode.setAttribute(ConstantSiteRates.XML_RATE, "" + constantRate);
			return siteNode;
		}
		
		if (rateModelType == RateModelType.Gamma) {
			siteNode.setAttribute(XMLLoader.CLASS_NAME_ATTR,  GammaSiteRates.class.getCanonicalName());
			siteNode.setAttribute(GammaSiteRates.XML_CATEGORIES, "" + gammaRateClasses);
			
			Element alpha = createDoubleParamElement(doc, "alpha", initialAlpha, 0.01, 50);
			if (estimateAlpha) {
				Element alphaMod = doc.createElement("alphaModifier");
				allParams.add(alpha);
				alphaMod.setAttribute(XMLLoader.CLASS_NAME_ATTR,  modifier.ScaleModifier.class.getCanonicalName());
				alpha.appendChild(alphaMod);
				
				//TODO Prior for alpha?
			}
			else {
				alpha.setAttribute("value", "" + initialAlpha);
				
			}
			siteNode.appendChild(alpha);
			
			return siteNode;
		}
		
		if (rateModelType == RateModelType.Custom) {
			siteNode.setAttribute(XMLLoader.CLASS_NAME_ATTR, ConstantSiteRates.class.getCanonicalName());
			throw new InputConfigException("Not implemented yet");
			//return null;
		}
		
		System.out.println("Hmm, returning null for the site node");
		return null;
	}

	private Element createMutNode(ACGDocument doc) {
		if (modelType == MutModelType.F84) {
			Element el = doc.createElement("F84Model");
			el.setAttribute(XMLLoader.CLASS_NAME_ATTR,  dlCalculation.substitutionModels.F84Matrix.class.getCanonicalName());

			Element baseEl = doc.createElement("BaseFrequencies");
			baseEl.setAttribute(XMLLoader.CLASS_NAME_ATTR, dlCalculation.substitutionModels.BaseFrequencies.class.getCanonicalName());
			baseEl.setAttribute(BaseFrequencies.XML_PARAM_FREQUENCY, "0.1");
			baseEl.setAttribute(TN93Matrix.XML_STATIONARIES, "0.25 0.25 0.25 0.25");
			
			Element baseModEl = doc.createElement("BaseFreqsMod");
			baseModEl.setAttribute(XMLLoader.CLASS_NAME_ATTR, modifier.DirichletModifier.class.getCanonicalName());
			baseEl.appendChild(baseModEl);
			allParams.add(baseEl);
			
			Element kappa = ttRatioElement.getElement(doc);
			
	
			allParams.add(kappa);
			
			Element modEl = doc.createElement("KappaMod");
			modEl.setAttribute(XMLLoader.CLASS_NAME_ATTR,  modifier.SimpleModifier.class.getCanonicalName());
			
			kappa.appendChild(modEl);
			el.appendChild(baseEl);
			el.appendChild(kappa);
			return el;
		}
		
		if (modelType == MutModelType.TN93) {
			Element el = doc.createElement("TN93Model");
			el.setAttribute(XMLLoader.CLASS_NAME_ATTR, dlCalculation.substitutionModels.TN93Matrix.class.getCanonicalName());
			
			Element baseEl = doc.createElement("BaseFrequencies");
			baseEl.setAttribute(XMLLoader.CLASS_NAME_ATTR, dlCalculation.substitutionModels.BaseFrequencies.class.getCanonicalName());
			baseEl.setAttribute(BaseFrequencies.XML_PARAM_FREQUENCY, "0.1");
			baseEl.setAttribute(TN93Matrix.XML_STATIONARIES, "0.25 0.25 0.25 0.25");
			
			Element baseModEl = doc.createElement("BaseFreqsMod");
			baseModEl.setAttribute(XMLLoader.CLASS_NAME_ATTR, modifier.DirichletModifier.class.getCanonicalName());
			baseEl.appendChild(baseModEl);
			allParams.add(baseEl);
			
			Element kappaR = createDoubleParamElement(doc, "KappaR", 2.0, 0.5, 500);
			Element modREl = doc.createElement("KappaRMod");
			allParams.add(kappaR);
			modREl.setAttribute(XMLLoader.CLASS_NAME_ATTR,  modifier.SimpleModifier.class.getCanonicalName());

			Element kappaY = createDoubleParamElement(doc, "KappaY", 2.0, 0.5, 500);
			allParams.add(kappaY);
			Element modYEl = doc.createElement("KappaYMod");
			modYEl.setAttribute(XMLLoader.CLASS_NAME_ATTR,  modifier.SimpleModifier.class.getCanonicalName());
			
			kappaR.appendChild(modREl);
			kappaY.appendChild(modYEl);
			
			el.appendChild(baseEl);
			el.appendChild(kappaR);
			el.appendChild(kappaY);
			
			return el;
		}
		
		return null;
	}
	
	
//	private static Element createDoubleParamElement(ACGDocument doc, String label, double value, double lowerBound, double upperBound) {
//		Element el = doc.createElement(label);
//		el.setAttribute(XMLLoader.CLASS_NAME_ATTR,  parameter.DoubleParameter.class.getCanonicalName());
//		el.setAttribute("value", "" + value);
//		el.setAttribute("lowerBound", "" + lowerBound);
//		el.setAttribute("upperBound", "" + upperBound);
//		
//		return el;
//	}

}
