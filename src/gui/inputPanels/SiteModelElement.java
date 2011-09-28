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

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import xml.XMLLoader;
import dlCalculation.DataLikelihood;
import dlCalculation.siteRateModels.ConstantSiteRates;
import dlCalculation.siteRateModels.GammaSiteRates;
import dlCalculation.substitutionModels.F84Matrix;
import dlCalculation.substitutionModels.TN93Matrix;

import gui.ErrorWindow;
import gui.document.ACGDocument;
import gui.inputPanels.Configurator.InputConfigException;
import gui.inputPanels.DoubleModifierElement.ModType;

/**
 * A class for reading and writing the mutation model and site rate model info to/ from XML
 * @author brendano
 *
 */
public class SiteModelElement extends ModelElement {

	enum MutModelType { JC69, K2P, F84, TN93 };
	
	enum RateModelType { Constant, Gamma, Custom };
	
	AlignmentElement alignmentRef = null;
	ARGModelElement argRef = null;
	
	private MutModelType modelType = MutModelType.F84;
	private RateModelType rateModelType = RateModelType.Constant;
	
	//Important that we don't reassign these since views have references to these as well
	private final BaseFreqsModelElement baseFreqsElement = new BaseFreqsModelElement();
	private final DoubleParamElement ttRatioElement = new DoubleParamElement();
	private final DoubleParamElement kappaRElement = new DoubleParamElement();
	private final DoubleParamElement kappaYElement = new DoubleParamElement();
	private final DoubleParamElement alphaParamElement = new DoubleParamElement();
	
	//Default labels for some parameters
	private String ttRatioLabel = "Kappa";
	private String kappaRLabel = "KappaR";
	private String kappaYLabel = "KappaY";
	
	private String defaultRateModelLabel = "RateModel";
	private String rateModelLabel = defaultRateModelLabel;
	
	private String defaultModelLabel = "MutationModel";
	private String mutModelLabel = defaultModelLabel;
	
	//If there's only one rate, this is it
	private double constantRate = 1.0;
	
	private int rateCatCount = 4;
	private String defaultAlphaParamLabel = "AlphaShape";
	
	
	
	public SiteModelElement() {
		ttRatioElement.setLabel(ttRatioLabel);
		ttRatioElement.setModifierLabel(ttRatioLabel + "Mod");
		ttRatioElement.setModifierType(ModType.Scale);
		ttRatioElement.setLowerBound(0);
		
		kappaRElement.setLabel(kappaRLabel);
		kappaRElement.setModifierLabel(kappaRLabel + "Mod");
		kappaRElement.setModifierType(ModType.Scale);
		kappaRElement.setLowerBound(0);
		
		kappaYElement.setModifierLabel(kappaYLabel + "Mod");
		kappaYElement.setLabel(kappaYLabel);
		kappaYElement.setModifierType(ModType.Scale);
		kappaYElement.setLowerBound(0);
		
		alphaParamElement.setLabel(defaultAlphaParamLabel);
		alphaParamElement.setValue(1.0);
		alphaParamElement.setLowerBound(0.01);
		alphaParamElement.setUpperBound(50.0);
		alphaParamElement.setModifierLabel(defaultAlphaParamLabel + "Mod");
		alphaParamElement.setModifierType(ModType.Simple);
	}
	
	public void setConstantRate(double rate) {
		this.constantRate = rate;
	}
	
	public void setRateModelType(RateModelType rateModel) {
		this.rateModelType = rateModel;
	}
	
	public void setAlignmentRef(AlignmentElement alnEl) {
		this.alignmentRef = alnEl;
	}
	
	public void setEstimateBaseFreqs(boolean estimate) {
		baseFreqsElement.setEstimate(estimate);
	}
	
	/**
	 * Set whether or not to estimate the TT ratio / KappaR KappaY values
	 * @param estimate
	 */
	public void setEstimateMutModel(boolean estimate) {
		ModType modType;
		if (estimate)
			modType = ModType.Scale;
		else
			modType = null;
		
		ttRatioElement.setModifierType(modType);
		kappaRElement.setModifierType(modType);
		kappaYElement.setModifierType(modType);
	}
	
	public void setMutModelType(MutModelType type) {
		this.modelType = type;
	}
	
	public void setMutModelLabel(String label) {
		this.mutModelLabel = label;
	}

	public void setEstimateAlpha(boolean estimate) {
		if (estimate)
			alphaParamElement.setModifierType(ModType.Scale);
		else
			alphaParamElement.setModifierType(null);
	}
	
	public void setInitialAlpha(double alpha) {
		alphaParamElement.setValue(alpha);
	}
	
	public void setRateCategories(int categs) {
		this.rateCatCount = categs;
	}
	
	public MutModelType getModelType() {
		return modelType;
	}

	public RateModelType getRateModelType() {
		return rateModelType;
	}

	public DoubleParamElement getTtRatioElement() {
		return ttRatioElement;
	}

	public DoubleParamElement getKappaRElement() {
		return kappaRElement;
	}

	public DoubleParamElement getKappaYElement() {
		return kappaYElement;
	}

	public String getRateModelLabel() {
		return rateModelLabel;
	}

	public String getMutModelLabel() {
		return mutModelLabel;
	}

	public double getConstantRate() {
		return constantRate;
	}

	public void readElements(ACGDocument doc) throws InputConfigException {		
		readMutModelSettings(doc);
		readRateModelSettings(doc);		
	}
	
	private void readMutModelSettings(ACGDocument doc) throws InputConfigException {
		//We expect exactly one of either F84Matrix or TN93 matrix
		List<String> f84Labels = doc.getLabelForClass(F84Matrix.class);
		List<String> tn93Labels = doc.getLabelForClass(TN93Matrix.class);

		int sum = f84Labels.size() + tn93Labels.size();
		if (sum == 0) {
			throw new InputConfigException("Could not find any mutation model in document");
		}
		
		if (sum > 1) {
			//Since F84 is a type of TN93 any F84 model will also appear in the TN93 model list. 
			//This is kind of an ugly wart, the real solution would be to have F84 and TN93 be separate models
			//..but for now we just handle this as a special case
			//TODO fix this!!
			if (f84Labels.size()==1 && tn93Labels.size()==1 && f84Labels.get(0).equals( tn93Labels.get(0))) {
				tn93Labels.clear();
				sum--;
			}
			else {
				throw new InputConfigException("Found multiple mutation models, we can only handle one at a time now");
			}
		}
		
		String nodeLabel; 
		if (f84Labels.size()>0)
			nodeLabel = f84Labels.get(0);
		else
			nodeLabel = tn93Labels.get(0);
		
		this.mutModelLabel = nodeLabel;
		
		try {
			Element mutModelElement = doc.getElementForLabel(nodeLabel);
			String mutModelClass = mutModelElement.getAttribute(XMLLoader.CLASS_NAME_ATTR);
			if (mutModelClass == null)
				throw new InputConfigException("Could not find class for mutation model with label: " + nodeLabel);
			
			if (mutModelClass.equals( F84Matrix.class.getCanonicalName() )) {
				setMutModelType(MutModelType.F84);
				
				if ( getChildCount(doc, mutModelElement) != 2)
					throw new InputConfigException("Expected exactly two children of node " + mutModelElement.getNodeName() + ", base frequencies and tt ratio");
				baseFreqsElement.readSettings( getChild(doc, mutModelElement, 0));
				ttRatioElement.readSettings( getChild(doc, mutModelElement, 1) );
			}
			
			
			if (mutModelClass.equals( TN93Matrix.class.getCanonicalName() )) {
				setMutModelType(MutModelType.TN93);
					
				if ( getChildCount(doc, mutModelElement) != 3)
					throw new InputConfigException("Expected exactly three children of node " + mutModelElement.getNodeName() + ", base frequencies, kappaR and kappa Y");

				baseFreqsElement.readSettings( getChild(doc, mutModelElement, 0));
				kappaRElement.readSettings( getChild(doc, mutModelElement, 1) );
				kappaYElement.readSettings( getChild(doc, mutModelElement, 2) );
			
			}
			
			
		} catch (Exception ex) {
			ErrorWindow.showErrorWindow(ex);
		}
	}
	
	private void readRateModelSettings(ACGDocument doc) throws InputConfigException {
		List<String> rateModelLabels = doc.getLabelForClass(ConstantSiteRates.class);
		rateModelLabels.addAll( doc.getLabelForClass(GammaSiteRates.class) );
		//rateModelLabels.addAll( doc.getLabelForClass(CustomSiteRates.class) );
		
		if (rateModelLabels.size()==0) { //This is OK, we use a single rate model with rate = 1
			setRateModelType(RateModelType.Constant);
			setConstantRate(1.0);
			return;
		}
		
		if (rateModelLabels.size()>1) {
			throw new InputConfigException("Found multiple site rate models. right now we can only handle a single model");
		}
		
		String rateModelLabel = rateModelLabels.get(0);
		this.rateModelLabel = rateModelLabel;
		
		Element rateEl = doc.getElementForLabel(rateModelLabel);
		String rateModelClass = rateEl.getAttribute(XMLLoader.CLASS_NAME_ATTR);
		if (rateModelClass == null)
			throw new InputConfigException("No class found for rate model element : " + rateModelLabel);
		
		if (rateModelClass.equals( ConstantSiteRates.class.getCanonicalName())) {
			setRateModelType(RateModelType.Constant);
			
			String rateStr = rateEl.getAttribute(ConstantSiteRates.XML_RATE);
			try {
				Double rate = Double.parseDouble(rateStr);
				setConstantRate(rate);
			}
			catch (NumberFormatException nfe) {
				throw new InputConfigException("Could not parse rate from element with label: " + rateModelLabel + ", found : " + rateStr);
			}
			
			return;
		}
		
		
		if (rateModelClass.equals( GammaSiteRates.class.getCanonicalName())) {
			setRateModelType(RateModelType.Gamma);
			
			
			List<String> refLabels = doc.getChildrenForLabel(rateModelLabel);
			if (refLabels.size() == 0) {
				//If there are no children, we require a number of categories and a shape (alpha) parameter
				String categories = rateEl.getAttribute(GammaSiteRates.XML_CATEGORIES);
				try {
					Integer categs = Integer.parseInt(categories);
					setRateCategories(categs);
				}
				catch (NumberFormatException nfe) {
					throw new InputConfigException("Could not read number of rate categories from element : " + rateModelLabel);
				}
				
				String alphaStr = rateEl.getAttribute(GammaSiteRates.XML_ALPHA);
				try {
					Double alpha = Double.parseDouble(alphaStr);
					setInitialAlpha(alpha);
				}
				catch (NumberFormatException nfe) {
					throw new InputConfigException("Could not read value for alpha from element : " + rateModelLabel);
				}
			}
			else {
				
				//If there's a child, it must be a DoubleParameter
				Element childEl = doc.getElementForLabel( refLabels.get(0));
				if (DoubleParamElement.isAcceptable(childEl)) {
					alphaParamElement.readSettings( childEl);
				}
			}
			
		}
		
	}
	
	
	
	public List<Node> getElements(ACGDocument doc) throws InputConfigException {
		List<Node> elements = new ArrayList<Node>();
		params.clear();
		try {
			Element rateModelNode = createRateModelNode(doc);
			elements.add(rateModelNode);
		} catch (InputConfigException e) {
			ErrorWindow.showErrorWindow(e);
		}
		
		Element mutModelNode = createMutNode(doc);
		elements.add(mutModelNode);
		
		Element dlNode = createElement(doc, "DataLikelihood", DataLikelihood.class);
		dlNode.appendChild( doc.createElement(mutModelNode.getNodeName()) );
		dlNode.appendChild( doc.createElement( argRef.getModelLabel() ) );
		elements.add( dlNode );
		
		return elements;
	}
	
	private Element createRateModelNode(ACGDocument doc) throws InputConfigException {
		Element siteNode = doc.createElement(rateModelLabel);
		if (rateModelType == RateModelType.Constant) {
			siteNode.setAttribute(XMLLoader.CLASS_NAME_ATTR,  ConstantSiteRates.class.getCanonicalName());
			siteNode.setAttribute(ConstantSiteRates.XML_RATE, "" + constantRate);
			return siteNode;
		}
		
		if (rateModelType == RateModelType.Gamma) {
			siteNode.setAttribute(XMLLoader.CLASS_NAME_ATTR,  GammaSiteRates.class.getCanonicalName());
			siteNode.setAttribute(GammaSiteRates.XML_CATEGORIES, "" + rateCatCount);
			
			
			Element alpha = alphaParamElement.getElement(doc);
			params.add(alphaParamElement);
			
			siteNode.appendChild(alpha);
			
			return siteNode;
		}
		
		if (rateModelType == RateModelType.Custom) {
			siteNode.setAttribute(XMLLoader.CLASS_NAME_ATTR, ConstantSiteRates.class.getCanonicalName());
			throw new InputConfigException("Not implemented yet");
			//return null;
		}
		
		return null;
	}

	private Element createMutNode(ACGDocument doc) throws InputConfigException {
		Element baseEl = baseFreqsElement.getElement(doc);
		
		if (modelType == MutModelType.F84) {
			Element el = createElement(doc, mutModelLabel, dlCalculation.substitutionModels.F84Matrix.class);			
			el.appendChild(baseEl);
			el.appendChild(ttRatioElement.getElement(doc));
			params.add(ttRatioElement);
			return el;
		}
		
		if (modelType == MutModelType.TN93) {
			Element el = createElement(doc, "TN93Model", dlCalculation.substitutionModels.TN93Matrix.class);
						
			el.appendChild(baseEl);
			el.appendChild(kappaRElement.getElement(doc));
			el.appendChild(kappaYElement.getElement(doc));
			
			params.add(kappaRElement);
			params.add(kappaYElement);
			return el;
		}
		
		return null;
	}
	
	public void setARGRef(ARGModelElement argRef) {
		this.argRef = argRef;
	}

	public DoubleParamElement getAlphaParamElement() {
		return alphaParamElement;
	}

	public int getRatCatgeoryCount() {
		return rateCatCount;
	}

	@Override
	public List<DoubleParamElement> getDoubleParameters() {
		return params;
	}
	
	//Maintains list of all double parameters we use
	private List<DoubleParamElement> params = new ArrayList<DoubleParamElement>();
}
