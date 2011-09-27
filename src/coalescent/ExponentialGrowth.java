package coalescent;

import java.util.Map;

import modifier.ModificationImpossibleException;

import parameter.CompoundParameter;
import parameter.DoubleParameter;
import parameter.Parameter;

/**
 * A demographic model of exponential growth over time
 * @author brendan
 *
 */
public class ExponentialGrowth extends CompoundParameter<Void> implements DemographicParameter {

	DoubleParameter baseSize;
	DoubleParameter growthRate;
	
	public ExponentialGrowth(Map<String, String> attrs, DoubleParameter baseSize, DoubleParameter growthRate) {	
		super(attrs);
		this.baseSize = baseSize;
		addParameter(baseSize);
		this.growthRate = growthRate;
		addParameter(growthRate);
		baseSize.acceptValue();
		growthRate.acceptValue();
	}
	
	@Override
	public double getIntegral(double t0, double t1) {
		double r = growthRate.getValue();
		if (Math.abs(r) < 1e-10) {
			return (t1-t0)/baseSize.getValue();
		}
		else 
			return (Math.exp(r*t1)-Math.exp(r*t0))/(baseSize.getValue()*r);
	}

	@Override
	public double getPopSize(double t) {
		return baseSize.getValue()*Math.exp(-growthRate.getValue()*t);
	}
	
	@Override
	public String getName() {
		return "exp.growth";
	}

	@Override
	protected void proposeNewValue(Parameter<?> source) {
		try {
			fireParameterChange();
		} catch (ModificationImpossibleException e) {
			if (growthRate.isProposed())
				growthRate.revertValue();
			if (baseSize.isProposed())
				baseSize.revertValue();
		}
	}

}
