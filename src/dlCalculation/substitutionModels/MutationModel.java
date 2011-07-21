package dlCalculation.substitutionModels;

import java.util.ArrayList;
import java.util.List;

import modifier.ModificationImpossibleException;
import modifier.Modifier;

import parameter.CompoundParameter;
import parameter.InvalidParameterValueException;
import parameter.Parameter;
import parameter.ParameterListener;

/**
 * Base class of things that can calculate a transition matrix for a number of discrete states
 * At some point this will probably be extended to handle things like variable evolutionary rates
 * over time
 * 
 * @author brendan
 *
 */
public abstract class MutationModel<Void> extends CompoundParameter<Void> {

	protected double[][] matrix;

	protected List<ParameterListener> listeners = new ArrayList<ParameterListener>();
	
	protected BaseFrequencies stationaries = null;
	
	public MutationModel(int numStates, BaseFrequencies stationaries) {
		matrix = new double[numStates][numStates];
		this.stationaries = stationaries;
	}
	
	public abstract void setBranchLength(double t);
	
	/**
	 * Write the transition matrix into the matrix provided
	 * @param t
	 * @param matrix
	 */
	public abstract void setMatrix(double t, double[][] matrix);

	/**
	 * Write the transition matrix into the matrix provided, in single precision
	 * @param t
	 * @param matrix
	 */
	public abstract void setMatrix(double t, float[][] matrix);
	
	public abstract double[] getStationaries();

	
	/**
	 * Get the stationary probability for the given state
	 * @param stateIndex
	 * @return
	 */
	public double getStationaryForState(int stateIndex) {
		return stationaries.getStationaries()[stateIndex];
	}	
	
	public double[][] getMatrix() {
		return matrix;
	}
	
	public void emit() {
		for(int i=0; i<matrix.length; i++) {
			double rowSum = 0;
			for(int j=0; j<matrix.length; j++) {
				rowSum += matrix[i][j];
				System.out.print(matrix[i][j] + "\t");
			}
			System.out.println("  | " + rowSum);
		}
	}

}
