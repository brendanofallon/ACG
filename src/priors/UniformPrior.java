package priors;

import java.util.Map;

import parameter.Parameter;

/**
 * A prior with the same density between two bounds, which may be positive and negative infinity.
 * This actually will work with multiple Double parameters if they are all added to the param list. 
 * @author brendan
 *
 */
public class UniformPrior extends Prior1D {

	Double logDensity; //Stores the log of the density, generally log (1.0 / (upperBound - lowerBound));
	
	//Whether we know the current density. 
	boolean densityKnown = false;
	
	
	/**
	 * Construct a new UniformPrior with no boundaries (lowerBound = negative infinity, upperBound = +infinity)
	 * @param param
	 */
	public UniformPrior(Parameter<Double> param) {
		super(param);
		logDensity = 1.0;
	}
	
	/**
	 * Construct a new uniform prior with the given bounds. We permit lower=upper. 
	 * @param param
	 * @param lowerBound
	 * @param upperBound
	 */
	public UniformPrior(Parameter<Double> param, Double lowerBound, Double upperBound) {
		super(param);
		this.lowerBound = lowerBound;
		this.upperBound = upperBound;
		if (upperBound < lowerBound)
			throw new IllegalArgumentException("Cannot construct a uniform prior with upper bound < lower bound");
		if (upperBound > lowerBound )
			logDensity = Math.log( 1.0/( upperBound - lowerBound ));
		if (upperBound == lowerBound) {
			logDensity = 1.0;
		}
	}
	
	public UniformPrior(Map<String, String> attrs, Parameter<Double> param) {
		super(param);
		Double lower = this.lowerBound;
		Double upper = this.upperBound;
		String lowerStr = attrs.get("lowerBound");
		if (lowerStr != null) {
			try {
				this.lowerBound = Double.parseDouble(lowerStr);
			}
			catch (NumberFormatException nfe) {
				System.out.println("Could not read value for lower uniform prior bound : " + lowerStr);
				System.exit(0);
			}
		}
		
		String upperStr = attrs.get("uppperBound");
		if (lowerStr != null) {
			try {
				this.upperBound = Double.parseDouble(upperStr);
			}
			catch (NumberFormatException nfe) {
				System.out.println("Could not read value for upper uniform prior bound : " + upperStr);
				System.exit(0);
			}
		}
		
		if (upperBound < lowerBound)
			throw new IllegalArgumentException("Cannot construct a uniform prior with upper bound < lower bound");
		if (upperBound > lowerBound )
			logDensity = Math.log( 1.0/( upperBound - lowerBound ));
		if (upperBound == lowerBound) {
			logDensity = 1.0;
		}
	}


	@Override
	public void parameterChanged(Parameter source) {
		densityKnown = false;
	}

	@Override
	public Double computeProposedLikelihood() {
		Double logL = 0.0;
		for(Parameter<?> param : parameters) {
			if ( ((Double)param.getValue())>=lowerBound && (Double)param.getValue()<upperBound) {
				logL += logDensity;
			}
			else {
				logL = Double.NEGATIVE_INFINITY;
			}
		}
		return logL;
	}

	@Override
	public Double getCurrentLogLikelihood() {
		return currentLogLikelihood;
	}
	
	/**
	 * Obtain the current likelihood value of this component. 
	 * @return
	 */
	public Double getProposedLogLikelihood() {
//		Parameter param = parameters.get(0);
//		if ( ((Double)param.getValue())>=lowerBound && (Double)param.getValue()<upperBound) {
//			return 1.0;
//		}
//		else {
//			return Double.NEGATIVE_INFINITY;
//		}
		if (!densityKnown)
			computeProposedLikelihood();
		return proposedLogLikelihood;
	}

	@Override
	public String getLogHeader() {
		return "Uniform prior";
	}

}
