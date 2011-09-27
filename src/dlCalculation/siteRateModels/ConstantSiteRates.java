package dlCalculation.siteRateModels;

import java.util.HashMap;
import java.util.Map;

import modifier.ModificationImpossibleException;
import modifier.Modifier;
import parameter.InvalidParameterValueException;
import parameter.Parameter;
import parameter.ParameterListener;
import xml.XMLUtils;

/**
 * A site rate model where the rates and probabilities never change
 * @author brendan
 *
 */
public class ConstantSiteRates extends AbstractSiteRateModel {

	public static final String XML_RATE = "rate";
	
	public ConstantSiteRates(Map<String, String> attrs) {
		super(attrs, 1);
		Double rate = XMLUtils.getOptionalDouble(XML_RATE, attrs);
		if (rate == null) {
			rate = 1.0;
		}
		initialize(new double[]{rate}, new double[]{1.0});
	}
	
	/**
	 * Construct a constant site rate model with a single class which has rate 1.0
	 */
	public ConstantSiteRates() {
		this(new HashMap<String, String>(), new double[]{1.0}, new double[]{1.0});
	}
	
	public ConstantSiteRates(Map<String, String> attrs, double[] rates, double[] probabilities) {
		super(attrs, rates.length);
		initialize(rates, probabilities);
	}
	
	private void initialize(double[] rates, double[] probabilities) {
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
