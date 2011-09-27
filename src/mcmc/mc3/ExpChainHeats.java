package mcmc.mc3;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import logging.StringUtils;
import modifier.AbstractModifier;
import modifier.ModificationImpossibleException;
import modifier.ScaleModifier;
import parameter.AbstractParameter;
import parameter.DoubleParameter;
import parameter.Parameter;
import parameter.ParameterListener;
import xml.XMLUtils;

/**
 * A strategy of heating multiple MCMC chains for an MC3 analysis. Chain i has temperature Exp[-lambda * i], where lambda is some small value.
 * Currently, we have lambda be determined by a DoubleParameter, and we've enabled some tuning stuff, so lambda can be adjusted to 
 * make sure the chains are always being mixed by the 'right' amount. This is experimental, but seems to work for now.  
 * @author brendano
 *
 */
public class ExpChainHeats implements ChainHeats, ParameterListener {

	double[] heats;
	
	//Experimental adaptive-heating scheme stuff below
	ScaleModifier lambdaMod = null;
	DoubleParameter lambdaParam;
	
	//Number of calls between parameter tunings
	private int tuneFrequency = 200;

	public ExpChainHeats(Map<String, String> attrs, DoubleParameter lambda) {
		int numberOfChains = XMLUtils.getIntegerOrFail(ChainHeats.XML_CHAINNUMBER, attrs);
		this.lambdaParam = lambda;

		if (lambda.getModifierCount()>0) {
			System.out.println("Creating exponential heating model with adaptive tuning");
			setUseAdaptiveHeating(true);
		}
		else {
			System.out.println("Creating exponential heating model without tuning");
		}
		
		heats = new double[numberOfChains];
		lambdaParam.addListener(this);
		recalcHeats();
	}
	
	public ExpChainHeats(int numberOfChains, double lambda) {
		this(numberOfChains, new DoubleParameter(lambda, "chains.lambda", "chains.lambda", 1e-8, 0.5));
	}
	
	public ExpChainHeats(int numberOfChains, DoubleParameter lambda) {
		heats = new double[numberOfChains];
		this.lambdaParam = lambda;
		lambdaParam.addListener(this);
		recalcHeats();
	}
	
	@Override
	public double getHeat(int chainNumber) {
		return heats[chainNumber];
	}

	public void setUseAdaptiveHeating(boolean useIt) {
		if (useIt && lambdaMod == null) {
			lambdaMod = new ScaleModifier();
			lambdaMod.setResetFrequency(2000);
			lambdaParam.addModifier(lambdaMod);
		}
		if (lambdaMod != null && (!useIt)) {
			lambdaMod = null;
			lambdaParam.removeModifier(lambdaMod);
		}
	}
	
	@Override
	public void parameterChanged(Parameter<?> source)
			throws ModificationImpossibleException {
		if (source==lambdaParam) {
			recalcHeats();
		}
		else {
			throw new IllegalArgumentException("Unexpected parameter fired change to ExpChain heats, param was: " + source);
		}
	}

	@Override
	public int getHeatCount() {
		return heats.length;
	}
	
	/**
	 * Recalculate all chain heats based on the current value of lambda. Heat if chain i is Exp[ -lambda * i ]
	 */
	private void recalcHeats() {
		System.out.println("Recalculating heats with lambda = " + lambdaParam.getValue() );
		for(int i=0; i<heats.length; i++) {
			heats[i] = Math.exp(-lambdaParam.getValue()*i);
		}
	}


	@Override
	public void tallyRejection() {
		if (lambdaMod != null) {
			//System.out.println("Heat swap accepted, total accepts: " + lambdaMod.getTotalAcceptances() + " recent accepts: " + lambdaMod.getRecentAcceptanceRatio());
			if (lambdaMod.getCallsSinceReset() > 100 && lambdaMod.getTotalCalls() % tuneFrequency==0) {
				changeTuning();
			}
			
			lambdaMod.tallyCall();
			lambdaMod.tallyRejection(); //Actually a no-op, we just track total calls and number of acceptances
		}
	}

	@Override
	public void tallyAcceptance() {
		if (lambdaMod != null) {
			if (lambdaMod.getCallsSinceReset() > 100 && lambdaMod.getTotalCalls() % tuneFrequency==0) {
				changeTuning();
			}
			
			lambdaMod.tallyCall();
			lambdaMod.tallyAcceptance();
		}
	}
	
	/**
	 * Adjust lambda parameter to get appropriate level of mixing of chains
	 */
	private void changeTuning() {
		System.out.println("Changing tuning, calls since reset: " + lambdaMod.getCallsSinceReset() + " total calls: " + lambdaMod.getTotalCalls());
		if (lambdaMod.getRecentAcceptanceRatio() < 0.3 && lambdaParam.getValue()>lambdaParam.getLowerBound()) {
			try {
				lambdaParam.proposeValue( lambdaParam.getValue()*0.9 );
				System.out.println("Too few acceptances ( " + StringUtils.format(lambdaMod.getRecentAcceptanceRatio(), 4) + " ), reducing lambda to : " + lambdaParam.getValue());
			} catch (ModificationImpossibleException e) {
				//Should never happen
			}
			lambdaParam.acceptValue();
		}
		
		if (lambdaMod.getRecentAcceptanceRatio() > 0.6 && lambdaParam.getValue()<lambdaParam.getUpperBound()) {
			try {
				lambdaParam.proposeValue( lambdaParam.getValue()*1.1 );
				System.out.println("Too many acceptances ( " + StringUtils.format(lambdaMod.getRecentAcceptanceRatio(), 4) + " ), increasing lambda to : " + lambdaParam.getValue());
			} catch (ModificationImpossibleException e) {
				//Should never happen
			}
			lambdaParam.acceptValue();
		}
	}

	@Override
	public List<AbstractParameter<?>> getGlobalParameters() {
		List<AbstractParameter<?>> params = new ArrayList<AbstractParameter<?>>();
		params.add(lambdaParam);
		return params;
	}

}
