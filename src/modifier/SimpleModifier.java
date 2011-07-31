package modifier;

import java.util.HashMap;
import java.util.Map;

import math.RandomSource;

import parameter.DoubleParameter;
import parameter.InvalidParameterValueException;
import parameter.Parameter;

/**
 * A uniform sliding window modifier for a one-dimensional parameter. Window width is tuned by default. 
 * @author brendano
 *
 */
public class SimpleModifier extends AbstractModifier<DoubleParameter> {

	DoubleParameter param = null;
	
	double windowMin = 0.001;
	double windowWidth = 0.5;
	double windowMax = 1000.0;
	
	public SimpleModifier(Map<String, String> attributes) {
		super(attributes);
	}
	
	public SimpleModifier() {
		this(new HashMap<String, String>());
	}
	
	private SimpleModifier(DoubleParameter param, double windowMin, double windowWidth, double windowMax, double lowRatio, double highRatio, double frequency) {
		super(param, lowRatio, highRatio, frequency);
		this.windowMin = windowMin;
		this.windowWidth = windowWidth;
		this.windowMax = windowMax;
	}
	
	public Double modify() throws InvalidParameterValueException, IllegalModificationException, ModificationImpossibleException {
		if (! param.isProposeable()) {
			throw new IllegalModificationException("Parameter " + param + " is already proposed, cannot modify");	 
		}
		
		double r = RandomSource.getNextUniform();
		Double val = param.getValue();
		Double newVal = val + windowWidth*(r-0.5);
		
		if (newVal < param.getLowerBound()) {
			double dif = param.getLowerBound() - newVal;
			newVal = param.getLowerBound()+dif;
		}
		
		if (newVal > param.getUpperBound()) {
			double dif = newVal - param.getUpperBound();
			newVal = param.getUpperBound()-dif;
		}
		
		if (newVal < param.getLowerBound()) {
			throw new ModificationImpossibleException("Param value " + newVal + " is less than lower bound " + param.getLowerBound());
		}
		
		if (newVal > param.getUpperBound()) {
			throw new ModificationImpossibleException("Param value " + newVal + " is greater than upper bound " + param.getUpperBound());
		}


		param.proposeValue(newVal);
		
		tallyCall();
		if (getCallsSinceReset() > 100 & getTotalCalls() % 50 == 0) {
			changeTuning();
		}
		
		return 1.0;
	}

	private void changeTuning() {
		if (getRecentAcceptanceRatio() < lowRatio && windowWidth > windowMin) { //We're not accepting many states, so shrink the window
			windowWidth *= 0.9;
			//System.out.println("Kappa ratio too small (" + getRecentAcceptances() + "), shrinking window to : " + windowWidth);
		}
		if (getRecentAcceptanceRatio() > highRatio && windowWidth < windowMax) { //We're accepting too manu, grow the window
			windowWidth *= 1.1;
			//System.out.println("Kappa ratio too big (" + getRecentAcceptances() + "), growing window to : " + windowWidth);
		}
	}

	public void setParameter(DoubleParameter param) {
		this.param = param;
	}



}
