package priors;

import java.util.Map;

import component.LikelihoodComponent;

import parameter.Parameter;

/**
 * Conceptually, priors are not really distinct from any other likelihood component.
 * This class is mostly just a marker for things that we intend to treat as 'priors'
 * @author brendan
 *
 */
public abstract class AbstractPrior extends LikelihoodComponent implements Prior {

	
	public AbstractPrior(Map<String, String> attrs, Parameter param) {
		super(attrs);
		addParameter(param);
		
		proposedLogLikelihood = computeProposedLikelihood();
		this.stateAccepted();
	}
	
	
	
	
}
