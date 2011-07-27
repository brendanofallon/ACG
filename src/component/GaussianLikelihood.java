package component;

import java.util.HashMap;

import parameter.DoubleParameter;

public class GaussianLikelihood extends LikelihoodComponent {

	DoubleParameter par = null;
	
	double mean1 = 0;
	double mean2 = 8;
	double std1 = 0.2;
	double std2 = 0.2;
	
	
	public GaussianLikelihood(DoubleParameter par) {
		super(new HashMap<String, String>());
		this.par = par;
		addParameter(par);
	}
	
	@Override
	public Double computeProposedLikelihood() {
		return Math.log( Math.exp( -(mean1-par.getValue())*(mean1-par.getValue())/(2.0*std1)) + Math.exp( -(mean2-par.getValue())*(mean2-par.getValue())/(2.0*std2) ));
	}

	@Override
	public String getLogHeader() {
		return "GaussianLnL";
	}

}
