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
public class DoublePriorModel extends ModelElement {

	enum PriorType { Uniform, Exponential, Gamma, Gaussian };
	private String label;
	private PriorType type = PriorType.Uniform;
	private double mean = 1.0;
	private double stdev = 1.0;
	private DoubleParamElement param;
	
	
	public DoublePriorModel(DoubleParamElement param) {
		this.param = param;
		label = param.getElementName() + "Prior";
	}
	
	@Override
	public List<Node> getElements(ACGDocument doc) throws InputConfigException {
		List<Node> nodes = new ArrayList<Node>();
		
		Class priorClass = UniformPrior.class;
		switch(type) {
		case Uniform : priorClass = UniformPrior.class; break;
		case Exponential : priorClass = ExponentialPrior.class; break;
		case Gamma : priorClass = GammaPrior.class; break;
		case Gaussian : priorClass = GaussianPrior.class; break;
		}
		
		Element el = createElement(doc, label, priorClass);
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
		el.appendChild( doc.createElement( param.getElementName() ));
		
		nodes.add(el);
		return nodes;
	}

	@Override
	public void readElements(ACGDocument doc) throws InputConfigException {
		throw new IllegalStateException("Cannot read elements from document");
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
