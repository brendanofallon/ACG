package priors;

import java.util.Map;

import math.RandomSource;
import cern.jet.random.Gamma;

import component.LikelihoodComponent;

import parameter.DoubleParameter;
import xml.XMLUtils;

/**
 * A gamma-distributed prior with user-defined mean and stdev
 * @author brendano
 *
 */
public class GammaPrior extends AbstractPrior {

	public static final String XML_MEAN = "mean";
	public static final String XML_STDEV = "stdev";
	
	Gamma gamma;
	DoubleParameter param;
	
	public GammaPrior(Map<String, String> attrs, DoubleParameter param) {
		super(attrs, param);
		this.param = param;
		double mean = XMLUtils.getDoubleOrFail(XML_MEAN, attrs);
		double stdev = XMLUtils.getDoubleOrFail(XML_STDEV, attrs);
		double var = stdev*stdev;
		
		gamma = new Gamma(mean*mean/var , var/mean, RandomSource.getEngine());
		
		proposedLogLikelihood = computeProposedLikelihood();
		this.stateAccepted();
	}
	

	
	@Override
	public Double computeProposedLikelihood() {
		return Math.log( gamma.pdf(param.getValue()));
	}

	@Override
	public String getLogHeader() {
		return "GammaPrior[" + param.getName() + "]";
	}

}
