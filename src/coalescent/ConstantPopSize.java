package coalescent;

import java.util.Map;

import modifier.Modifier;
import modifier.ScaleModifier;
import modifier.SimpleModifier;
import java.lang.Double;

import parameter.DoubleParameter;

public class ConstantPopSize extends DoubleParameter implements DemographicParameter  {

	public ConstantPopSize(Double size) {
		super(size, "pop.size", "Population size", 0, Double.MAX_VALUE);
		proposedValue = size;
		lowerBound = 0.0;
	}
	
	public ConstantPopSize(Map<String, String> attrs) {
		super(attrs);
	}
	
	public ConstantPopSize(Map<String, String> attrs, Modifier<?> mod) {
		this(attrs);
		
		addModifier(mod);
	}
	

	public String getName() {
		return "popSize";
	}
	
	public String getLogHeader() {
		return "popSize";
	}


	/**
	 * Returns the integral of the COALESCENT RATE, not the population size, over time. 
	 */
	public double getIntegral(double t0, double t1) {
		double size = getValue();
		return (t1-t0)/size;
	}


	public double getPopSize(double t) {
		return getValue();
	}

	

}
