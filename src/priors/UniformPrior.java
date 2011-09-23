package priors;

import java.util.HashMap;
import java.util.Map;

import parameter.DoubleParameter;
import parameter.Parameter;
import gui.inputPanels.Configurator.InputConfigException;

/**
 * A prior with the same density between two bounds, which may be positive and negative infinity.
 * This actually will work with multiple Double parameters if they are all added to the param list. 
 * @author brendan
 *
 */
public class UniformPrior extends AbstractPrior {

	Double logDensity; //Stores the log of the density, generally log (1.0 / (upperBound - lowerBound));
	
	//Whether we know the current density. 
	boolean densityKnown = false;
	double lowerBound = Double.NEGATIVE_INFINITY;
	double upperBound = Double.POSITIVE_INFINITY;
	
	DoubleParameter param;
	
	/**
	 * Construct a new UniformPrior with no boundaries (lowerBound = negative infinity, upperBound = +infinity)
	 * @param param
	 */
	public UniformPrior(DoubleParameter param) {
		this(new HashMap<String, String>(), param);
		this.param = param;
		logDensity = 1.0;
		initialize();
	}
	
	public UniformPrior(Map<String, String> attrs, DoubleParameter param) {
		super(attrs, param);
		this.param = param;
		String lowerStr = attrs.get(DoubleParameter.XML_LOWERBOUND);
		if (lowerStr != null) {
			try {
				this.lowerBound = Double.parseDouble(lowerStr);
			}
			catch (NumberFormatException nfe) {
				throw new IllegalArgumentException("Could not read value for lower uniform prior bound : " + lowerStr);
			}
		}
		
		String upperStr = attrs.get(DoubleParameter.XML_UPPERBOUND);
		if (upperStr != null) {
			try {
				this.upperBound = Double.parseDouble(upperStr);
			}
			catch (NumberFormatException nfe) {
				throw new IllegalArgumentException("Could not read value for upper uniform prior bound : " + upperStr);
			}
		}
		

		initialize();
	}

	private void initialize() {
		lowerBound = Math.max(param.getLowerBound(), lowerBound);
		upperBound = Math.min(param.getUpperBound(), upperBound);
		if (upperBound < lowerBound)
			throw new IllegalArgumentException("Cannot construct a uniform prior with upper bound < lower bound");
		if (Double.isInfinite(lowerBound) || Double.isInfinite(upperBound)) {
			logDensity = 1.0;
		}
		else {
			if (upperBound > lowerBound )
				logDensity = Math.log( 1.0/( upperBound - lowerBound ));
			if (upperBound == lowerBound) {
				logDensity = 1.0;
			}
		}
		
		proposedLogLikelihood = computeProposedLikelihood();
		this.stateAccepted();
	}
	
	@Override
	public Double computeProposedLikelihood() {
		Double val = param.getValue();
		if ( val>=lowerBound && val<upperBound) {
			return logDensity;
		}
		
		return Double.NEGATIVE_INFINITY;
	}


	@Override
	public String getLogHeader() {
		return "UniformPrior[" + param.getName() + "]";
	}

}
