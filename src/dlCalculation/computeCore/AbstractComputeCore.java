package dlCalculation.computeCore;

import gnu.trove.list.array.TIntArrayList;

import java.util.List;

import dlCalculation.siteRateModels.SingleRateModel;
import dlCalculation.siteRateModels.SiteRateModel;

/**
 * A handy base class for compute cores
 * @author brendano
 *
 */
public abstract class AbstractComputeCore implements ComputeCore {

	//Tallies out numbers for ComputeNodes
	protected int nextNodeNumber = 0;
	
	//Counter for assigned tips, this is used to assign IDs to tip nodes
	protected int nextTipID = 0;
	
	//Model describing rates and probabilities over sites
	protected SiteRateModel siteRateModel;
	
	public final int rateCount;

	public AbstractComputeCore(double constSiteRate) {
		siteRateModel = new SingleRateModel(constSiteRate);
		rateCount = 1;
	}
	
	public AbstractComputeCore() {
		siteRateModel = new SingleRateModel(1.0);
		rateCount = 1;
	}
	
	public AbstractComputeCore(SiteRateModel siteRates) {
		this.siteRateModel = siteRates;
		rateCount = siteRates.getCategoryCount();
	}
	
	public void collectGarbage(int[] existingNodeIDs) {
		throw new IllegalStateException("This hasn't been implemented for this compute core yet");
	}
	
	public void setUpdateAllMatrices() {
		throw new IllegalStateException("This hasn't been implemented for this compute core yet");	
	}
	
	/**
	 * Release resources associated with the given nodeID
	 * @param nodeID
	 */
	public void releaseNode(int nodeID) {
		throw new IllegalStateException("This hasn't been implemented for this compute core yet");		
	}
	
	public void computePartials(TIntArrayList ids) {
		for(int i=0; i<ids.size(); i++) {
			computePartials(ids.get(i));
		}
	}

	public void computePartialsList(TIntArrayList ids) {
		for(int i=0; i<ids.size(); i++) {
			computePartials(ids.get(i));
		}
	}


	/**
	 * Obtain a unique new number for a compute node
	 * @return
	 */
	public int nextNumber() {
		nextNodeNumber++;
		if (nextNodeNumber == Integer.MAX_VALUE) {
			throw new IllegalStateException("Great. We ran out of numbers for nodes. We really made more than 2 billion of these?");
		}

		return nextNodeNumber;
	}
	
	/**
	 * Returns the next index for a tip (the 'tipID') used by this core to index tips, and advances the counter
	 * by 1. 
	 * @return
	 */
	public int nextTipID() {
		nextTipID++;
		return nextTipID-1;
	}
	
	
	
	
	
	/**
	 * Compute partial probabilities for 4 states, storing the result in 'result'
	 * @param matL
	 * @param matR
	 * @param leftProbs
	 * @param rightProbs
	 * @param result
	 */
	protected static void computeProbabilities(double[][] matL,  
											 double[][] matR, 
											 double[] leftProbs, 
											 double[] rightProbs, 
											 double[] result) {
		double[] vecL = matL[0];
		double[] vecR = matR[0];

		double l0 = leftProbs[0];
		double l1 = leftProbs[1];
		double l2 = leftProbs[2];
		double l3 = leftProbs[3];

		double r0 = rightProbs[0];
		double r1 = rightProbs[1];
		double r2 = rightProbs[2];
		double r3 = rightProbs[3];

		double probL = vecL[0]*l0  + vecL[1]*l1 + vecL[2]*l2 + vecL[3]*l3;
		double probR = vecR[0]*r0 + vecR[1]*r1+ vecR[2]*r2  + vecR[3]*r3;
		result[0] =  probL*probR;		

		vecL = matL[1];
		vecR = matR[1];
		probL = vecL[0]*l0  + vecL[1]*l1 + vecL[2]*l2 + vecL[3]*l3;
		probR = vecR[0]*r0 + vecR[1]*r1+ vecR[2]*r2  + vecR[3]*r3;
		result[1] =  probL*probR;

		vecL = matL[2];
		vecR = matR[2];
		probL = vecL[0]*l0  + vecL[1]*l1 + vecL[2]*l2 + vecL[3]*l3;
		probR = vecR[0]*r0 + vecR[1]*r1+ vecR[2]*r2  + vecR[3]*r3;
		result[2] =  probL*probR;

		vecL = matL[3];
		vecR = matR[3];
		probL = vecL[0]*l0  + vecL[1]*l1 + vecL[2]*l2 + vecL[3]*l3;
		probR = vecR[0]*r0 + vecR[1]*r1 + vecR[2]*r2  + vecR[3]*r3;
		result[3] =  probL*probR;
	}
	
	
	protected static void computeProbabilities(float[][] matL,  
			float[][] matR, 
			float[] leftProbs, 
			float[] rightProbs, 
			float[] result) {
		float[] vecL = matL[0];
		float[] vecR = matR[0];

		
		float l0 = leftProbs[0];
		float l1 = leftProbs[1];
		float l2 = leftProbs[2];
		float l3 = leftProbs[3];

		float r0 = rightProbs[0];
		float r1 = rightProbs[1];
		float r2 = rightProbs[2];
		float r3 = rightProbs[3];

		float probL = vecL[0]*l0  + vecL[1]*l1 + vecL[2]*l2 + vecL[3]*l3;
		float probR = vecR[0]*r0 + vecR[1]*r1+ vecR[2]*r2  + vecR[3]*r3;
		result[0] =  (probL*probR);		

		vecL = matL[1];
		vecR = matR[1];
		probL = vecL[0]*l0  + vecL[1]*l1 + vecL[2]*l2 + vecL[3]*l3;
		probR = vecR[0]*r0 + vecR[1]*r1+ vecR[2]*r2  + vecR[3]*r3;
		result[1] =   (probL*probR);

		vecL = matL[2];
		vecR = matR[2];
		probL = vecL[0]*l0  + vecL[1]*l1 + vecL[2]*l2 + vecL[3]*l3;
		probR = vecR[0]*r0 + vecR[1]*r1+ vecR[2]*r2  + vecR[3]*r3;
		result[2] =  (probL*probR);

		vecL = matL[3];
		vecR = matR[3];
		probL = vecL[0]*l0  + vecL[1]*l1 + vecL[2]*l2 + vecL[3]*l3;
		probR = vecR[0]*r0 + vecR[1]*r1 + vecR[2]*r2  + vecR[3]*r3;
		result[3] =  (probL*probR);
	}
	
	/**
	 * Compute partials for a single site when node is above one tip and one non-tip
	 * @param mat1
	 * @param mat2
	 * @param stateIndex
	 * @param probs
	 * @param result
	 */
	protected static void computeProbsForOneState(double[][] mat1,  double[][] mat2, int stateIndex, double[] probs, double[] result) {
		double p0 = probs[0];
		double p1 = probs[1];
		double p2 = probs[2];
		double p3 = probs[3];

		for(int i=0; i<result.length; i++) {
			double[] vec = mat2[i];
			double probR = vec[0]*p0 + vec[1]*p1 + vec[2]*p2 + vec[3]*p3;
			result[i] = mat1[i][stateIndex]*probR;		
		}
	}
	
	protected static void computeProbsForOneState(float[][] mat1,  float[][] mat2, int stateIndex, float[] probs, float[] result) {
		float p0 = probs[0];
		float p1 = probs[1];
		float p2 = probs[2];
		float p3 = probs[3];

		for(int i=0; i<result.length; i++) {
			float[] vec = mat2[i];
			float probR = vec[0]*p0 + vec[1]*p1 + vec[2]*p2 + vec[3]*p3;
			result[i] = mat1[i][stateIndex]*probR;
		}
	}
	
	/**
	 * Compute partials when a node is above one tip and one non-tip
	 * @param tipMat Transition matrix for branch toward tip
	 * @param nodeMat Transition matrix for branch toward node
	 * @param startSite
	 * @param endSite
	 * @param probStates
	 * @param tipStates
	 * @param resultStates
	 */
	protected static void computeProbsForState(double[][] tipMat, double[][] nodeMat, 
									 int startSite, int endSite,
									 int[] tipStates, 
									 double[][] probStates,	int probOffset,
									 double[][] resultStates) {
		for(int index = startSite; index<endSite; index++) {
			double[] probs = probStates[ index-probOffset ];
			double[] resultProbs = resultStates[ index-startSite ]; 

			computeProbsForOneState(tipMat, nodeMat, tipStates[index], probs, resultProbs);
		}
	}


	protected static void computeForTwoStates(double[][] mat1, double[][] mat2, 
			int startSite, int endSite,
			int[] tipStates1, 
			int[] tipStates2, 
			double[][] resultStates, int resultOffset ) {

		double[] m10 = mat1[0];
		double[] m11 = mat1[1];
		double[] m12 = mat1[2];
		double[] m13 = mat1[3];

		double[] m20 = mat2[0];
		double[] m21 = mat2[1];
		double[] m22 = mat2[2];
		double[] m23 = mat2[3];

		for(int site = startSite; site<endSite; site++) {
			double[] resultProbs = resultStates[ site - resultOffset ]; 

			int index1 = tipStates1[site];
			int index2 = tipStates2[site];

			resultProbs[0] = m10[ index1] * m20[index2];
			resultProbs[1] = m11[ index1] * m21[index2];
			resultProbs[2] = m12[ index1] * m22[index2];
			resultProbs[3] = m13[ index1] * m23[index2];

		}
	}
}
