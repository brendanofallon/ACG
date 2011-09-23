package gui.inputPanels;

import gui.document.ACGDocument;
import gui.inputPanels.Configurator.InputConfigException;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import parameter.DoubleParameter;
import priors.ExponentialPrior;
import priors.GammaPrior;
import priors.GaussianPrior;
import priors.UniformPrior;

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
	private DoubleParamElement param;
	
	
	public DoublePriorModel(DoubleParamElement param) {
		this.param = param;
		
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
