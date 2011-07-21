package dlCalculation.substitutionModels;

import modifier.ModificationImpossibleException;
import parameter.Parameter;



public class JC69Matrix extends MutationModel<Void> {

	double currentBranchLength = 0;
	
	
	public JC69Matrix(int numStates) {
		super(numStates, new BaseFrequencies(new double[]{0.25, 0.25, 0.25, 0.25}));		
	}

	/**
	 * Compute a new JC69 matrix based on the given branch length
	 */
	@Override 
	public void setBranchLength(double length) {
		if (length != currentBranchLength) {
			Double ex = Math.exp(-4.0*length);
			for(int i=0; i<matrix.length; i++) {
				for(int j=i; j<matrix.length; j++) {
					if (j==i)
						matrix[i][j] = 0.25 + 0.75*ex;
					else {
						matrix[i][j] = 0.25 - 0.25*ex;
						matrix[j][i] = 0.25 - 0.25*ex;
					}
				}
			}
			
			currentBranchLength = length;
		}
		
	}

	@Override
	public double getStationaryForState(int stateIndex) {
		return 0.25;
	}

	public void setStationaries(double[] stat) {
		throw new IllegalStateException("Can't set stationaries for JC69 model");
	}

	
	
	@Override
	public String getName() {
		return "JC69 mutation model";
	}

	public double[] getStationaries() {
		return stationaries.getStationaries();
	}
	
	

	@Override
	public void setMatrix(double t, double[][] matrix) {
		// TODO Auto-generated method stub
		
	}


	@Override
	protected void proposeNewValue(Parameter<?> source) {
		//Nothing to do here, nothing can be proposed for this matrix
	}

	@Override
	public void setMatrix(double t, float[][] matrix) {
		// TODO Auto-generated method stub
		
	}


}
