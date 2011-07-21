package modifier;

import java.util.HashMap;
import java.util.Map;

import math.RandomSource;
import cern.jet.random.Gamma;

import parameter.InvalidParameterValueException;
import parameter.Parameter;
import sequence.DNAUtils;

/**
 * Modifies a param
 * @author brendan
 *
 */
public class DirichletModifier extends AbstractModifier<Parameter<double[]>> {

	Parameter<double[]> param;
	
	double minWindow = 1e-4;
	double maxWindow = 0.5;
	
	double windowSize = 0.1;
	
	//double scale = 2.0; //Inversely proportional to how much variance distributions will have, making this huge will 
	//make all value cluster very close to 0.25 
	
	public DirichletModifier(Map<String, String> attributes) {
		super(attributes);
		//TODO try to read a scale attribute from the list of attributes
		//gam = new Gamma(scale, 1.0, RandomSource.getEngine());
	}
	
	public DirichletModifier() {
		this(new HashMap<String, String>());
	}	
	
	public DirichletModifier copy() {
		DirichletModifier copy = new DirichletModifier();
		return copy;
	}

	@Override
	public Double modify() throws InvalidParameterValueException,
			IllegalModificationException, ModificationImpossibleException {
		double[] currentVal = param.getValue();
		
		double[] newval = new double[currentVal.length];
		
		System.arraycopy(currentVal, 0, newval, 0, currentVal.length);
		
		int which = RandomSource.getNextIntFromTo(0, 3);
		double howMuch = windowSize*RandomSource.getNextUniform();
		
		newval[which] = currentVal[which] + howMuch;
		
		double sum = 0;
		for(int i=0; i<newval.length; i++) {
			sum += newval[i];
		}
		
		for(int i=0; i<newval.length; i++) {
			newval[i] /= sum;
		}
		
//		int A = DNAUtils.A;
//		int C = DNAUtils.C;
//		int G = DNAUtils.G;
//		int T = DNAUtils.T;
		//System.out.println("Proposing new stats, from  : A=" + currentVal[A] + " " + currentVal[C] + " "+ currentVal[T] + " "+ currentVal[G] );
		//System.out.println("Proposing new stationaries : A=" + newval[A] + " " + newval[C] + " "+ newval[T] + " "+ newval[G] );
		
		param.proposeValue(newval);
		
//		totalCalls++;
//		callsSinceReset++;
//		if (totalCalls % resetFrequency==0) {
//			callsSinceReset = 1;
//			acceptsSinceReset = 0;
//		}
		tallyCall();
		
		//System.out.println("Old freqs " + currentVal[0] + "\t" + currentVal[1] + "\t" + currentVal[2] + "\t" + currentVal[3]);
		//System.out.println("New freqs " + newval[0] + "\t" + newval[1] + "\t" + newval[2] + "\t" + newval[3]);
		
		if (getCallsSinceReset() > 100 & getTotalCalls() % 50 == 0) {
			changeTuning();
		}
		return 1.0;
	}

	
	private void changeTuning() {
		if (getRecentAcceptanceRatio() < lowRatio && windowSize > minWindow) { //We're not accepting many states, so shrink the window
			windowSize *= 0.9;
			//System.out.println("Dirichlet window too big (" + getRecentAcceptances() + "), shrinking window to : " + windowSize);
		}
		//Hard upper bound makes it so that we never multiply by more than exp(2.0*0.5)=exp(1)=2.718
		if (getRecentAcceptanceRatio() > highRatio && windowSize < maxWindow) { //We're accepting too manu, grow the window
			windowSize *= 1.1;
			//System.out.println("Dirichlet window too small (" + getRecentAcceptances() + "), growing window to : " + windowSize);
		}
	}
	
	@Override
	public void setParameter(Parameter<double[]> param) {
		this.param = param;
	}


}
