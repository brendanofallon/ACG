package coalescent;

import java.util.Map;

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
		this.baseSize = baseSize;
		addParameter(baseSize);
		this.growthRate = growthRate;
		addParameter(growthRate);
	}
	
	@Override
	public double getIntegral(double t0, double t1) {
		// TODO Auto-generated method stub
		return 0;
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
		
	}

}
