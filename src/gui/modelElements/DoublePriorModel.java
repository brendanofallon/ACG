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


package gui.modelElements;

import gui.modelElements.Configurator.InputConfigException;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import document.ACGDocument;

import parameter.DoubleParameter;
import priors.ExponentialPrior;
import priors.GammaPrior;
import priors.GaussianPrior;
import priors.UniformPrior;
import xml.XMLLoader;

/**
 * Model implementation for priors that are applicable for DoubleParameters
 * @author brendano
 *
 */
public class DoublePriorModel {

	enum PriorType { Uniform, Exponential, Gamma, Gaussian };
	private String label = null;
	private PriorType type = PriorType.Uniform;
	private double mean = 1.0;
	private double stdev = 1.0;
	private final DoubleParamElement param;
	
	
	public DoublePriorModel(DoubleParamElement param) {
		this.param = param;
	}
	
	public PriorType getType() {
		return type;
	}
	
	public void setLabel(String label) {
		this.label = label;
	}
	
	public DoubleParamElement getParamModel() {
		return param;
	}
	
	public Element getElement(ACGDocument doc) throws InputConfigException {
	
		if (label == null) {
			setLabel(param.getLabel() + "Prior");
		}
		
		Class priorClass = UniformPrior.class;
		switch(type) {
		case Uniform : priorClass = UniformPrior.class; break;
		case Exponential : priorClass = ExponentialPrior.class; break;
		case Gamma : priorClass = GammaPrior.class; break;
		case Gaussian : priorClass = GaussianPrior.class; break;
		}
		
		Element el = ModelElement.createElement(doc, label, priorClass);
		if (type == PriorType.Uniform) {
			el.setAttribute(DoubleParameter.XML_LOWERBOUND, "" + param.getLowerBound());
			el.setAttribute(DoubleParameter.XML_UPPERBOUND, "" + param.getUpperBound());
		}
		if (type == PriorType.Exponential) {
			el.setAttribute(ExponentialPrior.XML_MEAN, "" + mean);
		}
		if (type == PriorType.Gamma) {
			el.setAttribute(GammaPrior.XML_MEAN, "" + mean);
			el.setAttribute(GammaPrior.XML_STDEV, "" + stdev);
		}
		
		if (type == PriorType.Gaussian) {
			el.setAttribute(GammaPrior.XML_MEAN, "" + mean);
			el.setAttribute(GammaPrior.XML_STDEV, "" + stdev);
		}
		
		//Reference to parameter
		el.appendChild( doc.createElement( param.getLabel() ));
		
		return el;
	}
	
	
	public void readSettings(Element el) throws InputConfigException {
		String className = el.getAttribute(XMLLoader.CLASS_NAME_ATTR);
		
		if (className == null)
			throw new InputConfigException("Could not read class for element with label : " + el.getNodeName());
		
		if ( el.getChildNodes().getLength() == 0) {
			throw new InputConfigException("Prior element with label " + el.getNodeName() + " does not refer to any parameter");
		}
		
		//Make sure this element is a prior for the right double param model
		NodeList nodes = el.getChildNodes();
		for(int i=0; i<nodes.getLength(); i++) {
			if (nodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
				Element child = (Element)nodes.item(i);
				if (child.getNodeName() != param.getLabel())
					throw new InputConfigException("Prior element with label " + el.getNodeName() + " does not refer to correct parameter (should be " + param.getLabel() + ", but its " + child.getNodeName());
			}
		}
		
		
		setLabel(el.getNodeName());
		
		String meanStr = el.getAttribute(GammaPrior.XML_MEAN);
		String stdevStr = el.getAttribute(GammaPrior.XML_STDEV);
		if (meanStr != null && meanStr.length()>0) {
			try {
				Double mean = Double.parseDouble(meanStr);
				setMean(mean);
			}
			catch (NumberFormatException nfe) {
				throw new InputConfigException("Could not parse mean for prior with label: " + el.getNodeName());
			}
		}
		
		if (stdevStr != null && stdevStr.length()>0) {
			try {
				Double stdev = Double.parseDouble(stdevStr);
				setStdev(stdev);
			}
			catch (NumberFormatException nfe) {
				throw new InputConfigException("Could not parse stdev for prior with label: " + el.getNodeName());
			}
		}
		
		if (className.equals( UniformPrior.class.getCanonicalName())) {
			setType(PriorType.Uniform);
			if (meanStr != null && meanStr.length()>0) {
				throw new InputConfigException("Cannot set mean for uniform prior (use param bounds instead)");
			}
			if (stdevStr != null && stdevStr.length()>0) {
				throw new InputConfigException("Cannot set std. dev. for uniform prior (use param bounds instead)");
			}
		}
		if (className.equals( GammaPrior.class.getCanonicalName())) {
			setType(PriorType.Gamma);	
		}
		if (className.equals( GaussianPrior.class.getCanonicalName())) {
			setType(PriorType.Gaussian);
		}
		if (className.equals( ExponentialPrior.class.getCanonicalName())) {
			setType(PriorType.Exponential);
			if (stdevStr != null && stdevStr.length()>0) {
				throw new InputConfigException("Cannot set std. dev. for exponential prior");
			}
		}
		
	}
	
	public void setType(PriorType type) {
		this.type = type;
	}

	public double getMean() {
		return mean;
	}

	public void setMean(double mean) {
		this.mean = mean;
	}

	public double getStdev() {
		return stdev;
	}

	public void setStdev(double stdev) {
		this.stdev = stdev;
	}
}
