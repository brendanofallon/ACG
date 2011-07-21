package module;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import logging.StateLogger;
import mcmc.MCMC;
import mcmc.MCMCListener;

import parameter.AbstractParameter;
import parameter.Parameter;

import component.LikelihoodComponent;

/**
 * A 'module' is a just a way to encapsulate some frequently-used combinations of likelihoods and parameters
 * 
 * @author brendano
 *
 */
public abstract class AbstractModule {

	protected List<LikelihoodComponent> likelihoods = new ArrayList<LikelihoodComponent>();
	
	protected List<AbstractParameter<?>> parameters = new ArrayList<AbstractParameter<?>>();
	 
	protected List<MCMCListener> listeners = new ArrayList<MCMCListener>();
	
	int sampleFrequency;
	
	public abstract void run();
	
	protected MCMC makeChain(String header) {
		String baseDirectory = System.getProperty("user.dir");
		String logFileName = baseDirectory + System.getProperty("file.separator") + header + ".log";
		
		Map<String, String> loggerProps = new HashMap<String, String>();
		loggerProps.put("echoToScreen", "true");
		loggerProps.put("frequency", "" + sampleFrequency);
		loggerProps.put("logFile", logFileName);
		
		System.out.println("Writing log file to : " + logFileName);
		
		StateLogger logger = new StateLogger(loggerProps);
		listeners.add(logger);
		
		List<Object> paramObjs = new ArrayList<Object>();
		paramObjs.addAll(parameters);
		
		List<Object> likeObjs = new ArrayList<Object>();
		likeObjs.addAll(likelihoods);
		
		List<Object> listenObj = new ArrayList<Object>();
		listenObj.addAll(listeners);
		
		MCMC mc = new MCMC(new HashMap<String, String>(), paramObjs, likeObjs, listenObj);
		
		return mc;
	}
	
	/**
	 * Create and run an MCMC chain using the current information in parameters, likelihoods, etc. 
	 * @param steps
	 * @param header
	 */
	public void runMCMC(int steps, String header) {
		
		MCMC mc = makeChain(header);
		
		mc.run( steps );
	}
	
}
