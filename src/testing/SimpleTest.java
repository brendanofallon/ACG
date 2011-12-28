package testing;

import java.util.HashMap;

import component.GaussianLikelihood;
import component.LikelihoodComponent;

import parameter.DoubleParameter;
import logging.StateLogger;
import math.RandomSource;
import mcmc.MCMC;
import modifier.SimpleModifier;

/**
 * A very basic "test" mostly for illustrative purposes. This creates an MCMC chain with
 * just a single LikelihoodComponent (a GaussianLikelihood), a single Parameter (a DoubleParameter),
 * and a single modifier (a SimpleModifier), and runs the chain for a few steps. 
 * @author brendan
 *
 */
public class SimpleTest {

	public static void main(String[] args) {
		
		//This always needs to happen before random numbers can be generated
		RandomSource.initialize(); 
		
		DoubleParameter param = new DoubleParameter(1.0,			//Initial value
													"Parameter",	//Title of column for state logger
													"Parameter",	//Generic name for this parameter
													-100.0,			//Lower bound for param
													100.0);			//Upper bound for param
		
		
		//A modifier that will propose new values for the Parameter. This one just adds 
		//or subtracts a small amount from the current Parameter value
		SimpleModifier mod = new SimpleModifier(new HashMap<String, String>()); 
		param.addModifier(mod);
		
		//A likelihood component that computes a likelihood given the value of the parameter
		LikelihoodComponent likelihood = new GaussianLikelihood(param);
		
		
		//Periodically records values of parameter and emits them to System.out or a file
		StateLogger logger = new StateLogger();
		logger.addStream(System.out); //Just emit values to system.out
		
		MCMC chain = new MCMC();
		chain.addComponent(likelihood);
		chain.addParameter(param);
		chain.addListener(logger);
		
		chain.run(10000);
		
		
	}
}
