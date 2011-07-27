package priors;

import parameter.Parameter;

/**
 * No longer user, ignore this for now
 *  
 * @author brendan
 *
 */
public abstract class Prior1D extends AbstractPrior {

	Double lowerBound = Double.NEGATIVE_INFINITY;
	Double upperBound = Double.POSITIVE_INFINITY;
	
	public Prior1D(Parameter<Double> param) {
		super(new HashMap<String, String>(), param);
	}

	public void setLowerBound(Double bound) {
		lowerBound = bound;
	}
	
	public void setUpperBound(Double bound) {
		upperBound = bound;
	}
	
	public void setBounds(Double lower, Double upper) {
		lowerBound = lower;
		upperBound = upper;
	}
	
	public Double getLowerBound() {
		return lowerBound;
	}
	
	public Double getUpperBound() {
		return upperBound;
	}
	

}
