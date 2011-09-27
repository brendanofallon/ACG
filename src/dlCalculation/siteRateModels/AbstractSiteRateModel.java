package dlCalculation.siteRateModels;

import java.util.Map;

import parameter.CompoundParameter;

/**
 * Base class for objects that implement a discrete series of evolutionary rates over sites, such as the discretized-gamma-distributed
 * rate often employed. This is a CompoundParameter since we have a particular 'value' (a SiteRates object), but we (potentially) also 
 * depend on the state of other parameters - for instance the 'Alpha' parameter in the Gamma rates model
 * @author brendano
 *
 */
public abstract class AbstractSiteRateModel extends CompoundParameter<SiteRates> implements SiteRateModel {

	public AbstractSiteRateModel(Map<String, String> attrs, int categories) {
		super(attrs);
		currentValue = new SiteRates();
		currentValue.probabilities = new double[categories];
		currentValue.rates = new double[categories];
		
		proposedValue = new SiteRates();
		proposedValue.probabilities = new double[categories];
		proposedValue.rates = new double[categories];
		
		activeValue = proposedValue;
	}

	

	public int getCategoryCount() {
		return activeValue.probabilities.length;
	}

	public double getRateForCategory(int category) {
		return activeValue.rates[category];
	}

	public double getProbForCategory(int category) {
		return activeValue.probabilities[category];
	}

}
