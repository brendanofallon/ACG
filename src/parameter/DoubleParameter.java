package parameter;

import java.util.HashMap;
import java.util.Map;

import xml.XMLLoader;
import xml.XMLUtils;

import logging.StringUtils;
import modifier.ModificationImpossibleException;
import modifier.Modifier;

/**
 * A simple parameter that encapsulates a single Double value. 
 * @author brendan
 *
 */
public class DoubleParameter extends AbstractParameter<Double> {

	protected Double lowerBound = Double.NEGATIVE_INFINITY;
	protected Double upperBound = Double.POSITIVE_INFINITY;
	
	protected String name;
	protected String logHeader;
	
	public DoubleParameter(double initValue, String logHeader, String name, double lowerBound, double upperBound) {
		super(new HashMap<String, String>());
		this.name = name;
		this.logHeader = logHeader;
		setLowerBound(lowerBound);
		setUpperBound(upperBound);
		try {
			proposeValue(initValue);
		} catch (ModificationImpossibleException e) {
			throw new IllegalArgumentException("Invalid initial value for parameter " + name + " : " + initValue);
		}
		this.acceptValue();
	}
	
	public DoubleParameter(Map<String, String> attrs) {
		this(attrs, null);
	}
	
	public DoubleParameter(Map<String, String> attrs, Modifier<DoubleParameter> mod) {
		super(attrs);
		
		Double lBound = XMLUtils.getOptionalDouble("lowerBound", attrs);
		if (lBound != null)
			lowerBound = lBound;
		
		Double uBound = XMLUtils.getOptionalDouble("upperBound", attrs);
		if (uBound != null)
			upperBound = uBound;
		
		name = XMLUtils.getOptionalString("name", attrs);
		if (name == null)
			name = attrs.get(XMLLoader.NODE_ID);
		
		Double val = XMLUtils.getDoubleOrFail("value", attrs);
		try {
			proposeValue(val);
			acceptValue();
		} catch (ModificationImpossibleException e) {
			throw new IllegalArgumentException("Invalid initial value for parameter : " + name + ", got value: " + val);
		}
		
		logHeader = attrs.get("logHeader");
		if (logHeader == null)
			logHeader = name;
		
		if (mod != null)
			addModifier(mod);
	}

	
	public String getName() {
		return name;
	}
	
	public String getLogHeader() {
		return logHeader;
	}

	public Double getLowerBound() {
		return lowerBound;
	}

	public void setLowerBound(Double lowerBound) {
		this.lowerBound = lowerBound;
	}

	public Double getUpperBound() {
		return upperBound;
	}

	public void setUpperBound(Double upperBound) {
		this.upperBound = upperBound;
	}
	
	public String getLogString() {
		if (currentValue != null)
			return StringUtils.format(currentValue, 6);
		else
			return "null";
	}
	
	public String toString() {
		return getName() + " value=" + currentValue;
	}


}
