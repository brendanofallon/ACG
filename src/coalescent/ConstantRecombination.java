package coalescent;

import java.util.Map;

import modifier.Modifier;
import parameter.DoubleParameter;

/**
 * The simplest form of recombination, constant across all sites and for all time. 
 * @author brendan
 *
 */
public class ConstantRecombination extends DoubleParameter implements RecombinationParameter {

	public ConstantRecombination(double value) {
		super(value, "rec.rate", "Recombination rate", 0, Double.MAX_VALUE);
	}
	
	
	
	public ConstantRecombination(Map<String, String> attrs) {
		super(attrs);
		
	}
	
	public ConstantRecombination(Map<String, String> attrs, Modifier mod) {
		this(attrs);
		addModifier(mod);
	}
	


	
	@Override
	public double getInstantaneousRate(double t) {
		return getValue();
	}

	@Override
	public double getIntegral(double start, double end) {
		return getValue()*(end-start);
	}

	@Override
	public double getSiteProbability(int site) {
		return 1.0;
	}

}
