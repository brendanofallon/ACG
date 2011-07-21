package dlCalculation.siteRateModels;

import java.util.Map;

import modifier.ModificationImpossibleException;
import modifier.Modifier;
import parameter.InvalidParameterValueException;
import parameter.Parameter;
import parameter.ParameterListener;

/**
 * A site rate model where the rates and probabilities never change
 * @author brendan
 *
 */
public class ConstantSiteRates extends AbstractSiteRateModel {

	public ConstantSiteRates(Map<String, String> attrs) {
		this();
	}
	
	/**
	 * Construct a constant site rate model with a single class which has rate 1.0
	 */
	public ConstantSiteRates() {
		this(new double[]{1.0}, new double[]{1.0});
	}
	
	public ConstantSiteRates(double[] rates, double[] probabilities) {
		super(rates.length);
		SiteRates siteRates = new SiteRates();
		siteRates.rates = rates;
		siteRates.probabilities = probabilities;
		
		try {
			super.proposeValue(siteRates);
			acceptValue();
		} catch (ModificationImpossibleException e) {
			throw new IllegalArgumentException("Invalid initial values for ConstantSiteRate model");
		}
		
	}

	public void proposeValue(SiteRates proposal) {
		throw new IllegalArgumentException("Cannot propose new value for ConstantSiteRates model");
	}
	
	@Override
	public String getName() {
		return "constant.siteRates";
	}

	@Override
	protected void proposeNewValue(Parameter<?> source) {
		//This model doesn't depend on any parameters
		throw new IllegalArgumentException("Nothing to propose for the constant site rate model");
	}


}
