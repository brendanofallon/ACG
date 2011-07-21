package modifier;

import java.util.HashMap;
import java.util.Map;

import math.RandomSource;
import parameter.InvalidParameterValueException;
import arg.ARG;
import coalescent.PiecewiseLinearFunction;
import coalescent.PiecewiseLinearPopSize;

public class LinearFunctionAddRemove extends AbstractModifier<PiecewiseLinearPopSize> {

	ARG arg;
	
	boolean verbose = false;
	
	double jacobian = 5.5;
	
	public LinearFunctionAddRemove() {
		this(new HashMap<String, String>(), null);
	}
	
	public LinearFunctionAddRemove(ARG arg) {
		this(new HashMap<String, String>(), arg);
	}
	
	public LinearFunctionAddRemove(Map<String, String> attributes, ARG arg) {
		super(attributes);
		this.arg = arg;
	}

	@Override
	public Double modify() throws InvalidParameterValueException,
			IllegalModificationException, ModificationImpossibleException {
		
		PiecewiseLinearFunction newValue = param.getValue().getCopy();
		double hr = 1.0;
		
		if (RandomSource.getNextUniform() < 0.5) {
			hr = addPoint(newValue);
		}
		else {
			if (newValue.changePoints == 0)
				return 1.0;
			
			hr = removePoint(newValue);
		}
		

		param.proposeValue(newValue);
		return hr;
	}

	private double removePoint(PiecewiseLinearFunction func) throws ModificationImpossibleException {
		int which = RandomSource.getNextIntFromTo(1, func.getChangePointCount());
		func.removePoint(which);
		
		double probThisMove = 1.0/(func.getChangePointCount()+1);
		double probReverseMove = 1.0/arg.getMaxHeight();
		return jacobian*probReverseMove / probThisMove;
	}

	private double addPoint(PiecewiseLinearFunction func) {
		double newX = RandomSource.getNextUniform() * arg.getMaxHeight();
		func.addMidPoint(newX);
		
		double probThisMove = 1.0/arg.getMaxHeight();
		double probReverseMove = 1.0/func.getChangePointCount(); 
		return jacobian*probReverseMove / probThisMove;
	}


	
}
