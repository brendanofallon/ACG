package priors;

import java.util.Map;

import math.RandomSource;
import cern.jet.random.Exponential;
import cern.jet.random.Gamma;

import component.LikelihoodComponent;

import parameter.DoubleParameter;
import xml.XMLUtils;

/**
 * A gamma-distributed prior with user-defined mean and stdev
 * @author brendano
 *
 */
public class GammaPrior extends LikelihoodComponent {

	Gamma gamma;
	DoubleParameter param;
	
	public GammaPrior(Map<String, String> attrs, DoubleParameter param) {
		addParameter(param);
		this.param = param;
		double mean = XMLUtils.getDoubleOrFail("mean", attrs);
		double stdev = XMLUtils.getDoubleOrFail("stdev", attrs);
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

//	public static void main(String[] args) {
//		RandomSource.initialize();
//		
//		double mean = 5;
//		double stdev = 2;
//		double var = stdev*stdev;
//		
//		Gamma gamma = new Gamma(mean*mean/var , var/mean, null);
//		
//		for(double x=0; x<10; x+=0.1) {
//			System.out.println(x + "\t" + gamma.pdf(x));
//		}
//		
//	}
}
