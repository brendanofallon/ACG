package modifier;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import math.RandomSource;
import parameter.DoubleParameter;
import parameter.InvalidParameterValueException;

public class ScaleModifier extends AbstractModifier<DoubleParameter> {
	
	DoubleParameter param = null;
	
	double scaleMin = 0.02;
	double scaleMax = 2.0;
	double scaleSize = 0.2;

	public ScaleModifier() {
		super(new HashMap<String, String>());
	}
	
	public ScaleModifier(Map<String, String> attributes) {
		super(attributes);
		
		modStr="Scale modifier for pop size";
	}
	
	private ScaleModifier(DoubleParameter param, double scaleMin, double scaleMax, double scaleSize, double lowRatio, double highRatio, double frequency) {
		super(param, lowRatio, highRatio, frequency);
		this.scaleMin = scaleMin;
		this.scaleMax = scaleMax;
		this.scaleSize = scaleSize;
	}
	
	public Double modify() throws InvalidParameterValueException, IllegalModificationException, ModificationImpossibleException {
		if (! param.isProposeable()) {
			throw new IllegalModificationException("Parameter " + param.getName() + " is not in a valid state, cannot modify");	 
		}
		
		
		double r = RandomSource.getNextUniform();
		Double val = param.getValue();
	
		
		Double multiplier = Math.exp( scaleSize * (r-0.5));
				
		Double newVal = val * multiplier; 
		
		//Reflecting boundaries?
//		if (newVal < param.getLowerBound()) {
//			double dif = param.getLowerBound() - newVal;
//			newVal = param.getLowerBound()+dif;
//		}
//		
//		if (newVal > param.getUpperBound()) {
//			double dif = newVal - param.getUpperBound();
//			newVal = param.getUpperBound()-dif;
//		}
		
		if (newVal <= param.getLowerBound()) {
			throw new IllegalModificationException("New value is less than lower bound (value: " + newVal + ", lower bound: " + param.getLowerBound());
		}
		
		if (newVal > param.getUpperBound()) {
			throw new IllegalModificationException("New value is greater than upper bound (value: " + newVal + ", lower bound: " + param.getUpperBound());
		}
		
		
//		if (param.getName().equals("kappa"))
//			System.out.println("Scaling kappa by : " + multiplier + " from " + val + " to " + newVal + "    " + getCallsSinceReset() + " / " + getTotalCalls() + " ratio: " + getRecentAcceptanceRatio() + " scale size: " + scaleSize);
		param.proposeValue(newVal);
		
		tallyCall();
		if (getCallsSinceReset() > 100 & getTotalCalls() % 50 == 0) {
//			if (param.getName().equals("kappa"))
//				System.out.println("Changing tuning of kappa, calls since reset: " + getCallsSinceReset());
			changeTuning();
		}
		return multiplier; 
	}

	private void changeTuning() {
		if (getRecentAcceptanceRatio() < lowRatio && scaleSize > scaleMin) { //We're not accepting many states, so shrink the window
			scaleSize *= 0.9;
//			if (param.getName().equals("kappa"))
//				System.out.println("Acceptances too big (" + getRecentAcceptanceRatio() + "), shrinking window to : " + scaleSize);
		}
		//Hard upper bound makes it so that we never multiply by more than exp(2.0*0.5)=exp(1)=2.718
		if (getRecentAcceptanceRatio() > highRatio && scaleSize < scaleMax) { //We're accepting too manu, grow the window
			scaleSize *= 1.1;
//			if (param.getName().equals("kappa"))
//				System.out.println("Acceptances too small (" + getRecentAcceptanceRatio() + "), growing window to : " + scaleSize);
		}
	}

	public void setParameter(DoubleParameter param) {
		this.param = param;
	}


}
