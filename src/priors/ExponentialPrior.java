package priors;

import java.util.Map;

import math.RandomSource;
import cern.jet.random.Exponential;
import parameter.DoubleParameter;
import xml.XMLUtils;
import component.LikelihoodComponent;

/**
 * A likelihood that takes a single DoubleParameter and returns 1/mean * Exp( val / mean ) - the exponential likelihood
 * @author brendano
 *
 */
public class ExponentialPrior extends LikelihoodComponent {

	double mean;
	Exponential exp; 
	DoubleParameter param;
	
	public ExponentialPrior(Map<String, String> attrs, DoubleParameter param) {
		addParameter(param);
		this.param = param;
		this.mean = XMLUtils.getDoubleOrFail("mean", attrs);
		exp = new Exponential(1.0/mean, RandomSource.getEngine());	
		proposedLogLikelihood = computeProposedLikelihood();
		this.stateAccepted();
	}
	
	public ExponentialPrior(DoubleParameter param, double mean) {
		addParameter(param);
		this.param = param;
		this.mean = mean;
		exp = new Exponential(1.0/mean, RandomSource.getEngine());
		proposedLogLikelihood = computeProposedLikelihood();
		this.stateAccepted();
	}
	
	@Override
	public Double computeProposedLikelihood() {
		return Math.log( exp.pdf(param.getValue()) );
	}

	@Override
	public String getLogHeader() {
		return "ExpPrior[ " + param.getName() + " ]";
	}

}
