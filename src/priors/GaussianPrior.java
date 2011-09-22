package priors;

import java.util.Map;

import component.LikelihoodComponent;

import math.RandomSource;
import parameter.DoubleParameter;
import xml.XMLUtils;
import cern.jet.random.Gamma;
import cern.jet.random.Normal;

/**
 * A Gaussian (Normal) prior on a DoubleParameter, specified by mean and standard deviation
 * @author brendano
 *
 */
public class GaussianPrior extends LikelihoodComponent {

	public static final String XML_MEAN = "mean";
	public static final String XML_STDEV = "stdev";
	
	Normal gaussian;
	DoubleParameter param;
	
	public GaussianPrior(Map<String, String> attrs, DoubleParameter param) {
		super(attrs);
		addParameter(param);
		this.param = param;
		double mean = XMLUtils.getDoubleOrFail( XML_MEAN, attrs);
		double stdev = XMLUtils.getDoubleOrFail(XML_STDEV, attrs);
			
		gaussian = new Normal(mean , stdev, RandomSource.getEngine());
		
		proposedLogLikelihood = computeProposedLikelihood();
		this.stateAccepted();
	}
	

	
	@Override
	public Double computeProposedLikelihood() {
		return Math.log( gaussian.pdf(param.getValue()));
	}

	@Override
	public String getLogHeader() {
		return "GaussianPrior[" + param.getName() + "]";
	}

}
