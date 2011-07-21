package dlCalculation.substitutionModels;

import java.util.HashMap;
import java.util.Map;

import parameter.DoubleParameter;
import parameter.Parameter;



public class K80Matrix extends F84Matrix {

	public K80Matrix(BaseFrequencies stationaries, DoubleParameter kappa) {
		super(stationaries, kappa);
		throw new IllegalArgumentException("Cannot explicitly set stationaries for K80 mutation model");
	}
	
	public K80Matrix(DoubleParameter kappa) {
		super(new BaseFrequencies(new double[]{0.25, 0.25, 0.25, 0.25}), kappa);
	}
	
	public K80Matrix(Map<String, String> attrs, DoubleParameter kappa) {
		super(attrs, kappa);
	}

	@Override
	public String getName() {
		return "K80 mutation model";
	}
	

	@Override
	protected void proposeNewValue(Parameter<?> source) {
		recalcIntermediates = true;
	}

}
