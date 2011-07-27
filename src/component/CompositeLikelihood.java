package component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A type of likelihood component that depends on multiple other likelihood components
 * @author brendan
 *
 */
public class CompositeLikelihood extends LikelihoodComponent {

	List<LikelihoodComponent> comps = new ArrayList<LikelihoodComponent>(5);
	
	public CompositeLikelihood(Map<String, String> attrs) {
		super(attrs);
	}
	
	public void addComponent(LikelihoodComponent comp) {
		if (!comps.contains(comp))
			comps.add(comp);
	}
	
	public boolean removeComponent(LikelihoodComponent comp) {
		return comps.remove(comp);
	}
	
	
	/**
	 * Accept the proposed state by setting the current likelihood value to be the
	 * proposed value. We also notify all sub components of the state acceptance
	 */
	public void stateAccepted() {
		currentLogLikelihood = proposedLogLikelihood;
		for(LikelihoodComponent comp : comps) {
			comp.stateAccepted();
		}
	}
	
	/**
	 * Reject the proposed state. We notify all subcomponents that the state was rejected
	 */
	public void stateRejected() {
		for(LikelihoodComponent comp : comps) {
			comp.stateRejected();
		}
	}
	
	/**
	 * Set the proposedLogLikelihood value to be the sum of all the likelihoods
	 * on which this component depends. 
	 */
	@Override
	public Double computeProposedLikelihood() {
		Double sum = 0.0; 
		for(LikelihoodComponent comp : comps) {
			sum += comp.getProposedLogLikelihood();
		}
		return sum;
	}
	
	public String toString() {
		StringBuilder str = new StringBuilder();
		str.append("Composite likelihood with " + comps.size() + " components : \n");
		for(LikelihoodComponent lc : comps) {
			str.append("\t" + lc +"\n");
		}
		return str.toString();
	}
	
	
	public String getLogHeader() {
		StringBuilder strB = new StringBuilder();
		for(LikelihoodComponent comp : comps) {
			strB.append(comp.getLogHeader());
		}
		return strB.toString();
	}

}
