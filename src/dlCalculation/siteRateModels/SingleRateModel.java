package dlCalculation.siteRateModels;

import java.util.HashMap;
import java.util.Map;

import modifier.Modifier;
import parameter.InvalidParameterValueException;
import parameter.Parameter;
import parameter.ParameterListener;

/**
 * Only one rate category in this simplest of site rate models
 * @author brendan
 *
 */
public class SingleRateModel extends AbstractSiteRateModel {
	
	public SingleRateModel(Map<String, String> attrs, double rate) {
		super(attrs, 1);
		proposedValue.rates[0] = rate;
		proposedValue.probabilities[0] = 1.0;
		activeValue = proposedValue;
	}

	public String getName() {
		return "Single rate model";
	}

	public String getLogHeader() {
		return "site.rate";
	}

	public String getLogString() {
		return "" + activeValue.rates[0];
	}

	@Override
	protected void proposeNewValue(Parameter<?> source) {
		throw new IllegalArgumentException("Nothing can be proposed to SingleRate model");
	}

}
