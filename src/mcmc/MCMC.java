package mcmc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import arg.ARG;

import logging.StateLogger;
import logging.StringUtils;
import logging.TreeLogger;
import math.RandomSource;
import modifier.IllegalModificationException;
import modifier.ModificationImpossibleException;
import modifier.Modifier;

import component.LikelihoodComponent;
import dlCalculation.DataLikelihood;

import parameter.AbstractParameter;
import parameter.InvalidParameterValueException;
import parameter.Parameter;
import testing.Timer;
import xml.XMLUtils;

public class MCMC {

	//Keeps track of current state numbers
	int acceptedStates = 0;
	int totalStates = 0;
	
	//Parties to notify of MCMC state changes
	private List<MCMCListener> listeners = new ArrayList<MCMCListener>();
	
	//Parameters that are part of the model (all of them)
	private List<AbstractParameter<?>> parameters = new ArrayList<AbstractParameter<?>>();
	
	//Parameters that we can modify. These have a frequency > 0 and at least one installed modifier
	private List<AbstractParameter<?>> modParams = null; //Object is created in createModParams method
	
	private List<LikelihoodComponent> components = new ArrayList<LikelihoodComponent>();
	
	private List<AcceptRejectListener> arListeners = new ArrayList<AcceptRejectListener>();
	
	//Initially, we do a lot of (expensive) double-checking to ensure the validity of the likelihoods
	//calculated. The field below specifies the number of steps for which we have the 
	//these validity checkers on. 

	int carefulPeriod = 5000;
	
	private boolean paramFreqsKnown = false;
	private double paramFreqSum = 0;

	boolean verbose = false;
	
	boolean verifyRejection = true; //These get turned off after the 'careful period'
	boolean verifyLogCalc = false;
	
	//Never accept the proposed state - just modify, test, and discard
	final boolean neverAccept = false;
	
	MCDebug debugger = new MCDebug();
	
	Modifier<?> lastModifier = null;
	AbstractParameter<?> lastParam = null;
	
	//Keeps track of how long we spend using each modifier
	Map<Modifier<?>, Long> modTimes = new HashMap<Modifier<?>, Long>();
	
	Timer modTimer = new Timer(); //Used to time modifiers
	
	
	//Chain heating parameter, used for MC3
	private double heat = 1.0;
	
	private double[] currentLikelist = null; 
	private double[] propLikeList = null; 
	private double acceptedLogL = Double.NEGATIVE_INFINITY;
	
	//Just for debugging, its nice to have this accessible from several places
	private int currentState = 0;
	
	private boolean useTimers = true;
	
	//User-supplied value for run length
	private int userRunLength;
	
	public MCMC( ) {
		
	}

	public MCMC(List<Object> params, List<Object> comps) {
		this(new HashMap<String, String>(), params, comps, null);
	}
	
	public MCMC(Map<String, String> attrs, List<Object> params, List<Object> comps) {
		this(attrs, params, comps, null);
	}
	
	public MCMC(List<Object> params, List<Object> comps, List<Object> listeners) {
		this(new HashMap<String, String>(), params, comps, listeners);
	}
	
	public MCMC(HashMap<String, String> attrs, List<AbstractParameter<?>> params, List<LikelihoodComponent> comps, List<MCMCListener> listeners) {
		initialize(attrs, params, comps, listeners);
	}
	
	/**
	 * XML-approved constructor
	 * @param attrs
	 * @param components
	 * @param parameters
	 */
	public MCMC(Map<String, String> attrs, List<Object> params, List<Object> comps, List<Object> listeners) {
		List<AbstractParameter<?>> parList = new ArrayList<AbstractParameter<?>>();
		List<LikelihoodComponent> compList = new ArrayList<LikelihoodComponent>();
		List<MCMCListener> listList = new ArrayList<MCMCListener>();
		for(Object comp : comps) {
			try {
				compList.add( (LikelihoodComponent)comp);
			}
			catch (ClassCastException cce) {
				System.err.println("Could not cast object " + comp + " to likelihood component, aborting");
				System.exit(0);
			}
		}
		
		for(Object param : params) {
			try {
				parList.add( (AbstractParameter<?>)param );
			}
			catch (ClassCastException cce) {
				System.err.println("Could not cast object " + param + " to parameter, aborting");
				System.exit(0);				
			}
		}
		
		if (listeners != null) {
			for(Object listenObj : listeners) {
				try {
					listList.add( (MCMCListener)listenObj );
				}
				catch (ClassCastException cce) {
					System.out.println("Warning : Object  " + listenObj + " is not an MCMC listener, ignoring it.");
				}
			}
		}
		
		initialize(attrs, parList, compList, listList);
	}
	
	private void initialize(Map<String, String> attrs, List<AbstractParameter<?>> params, List<LikelihoodComponent> comps, List<MCMCListener> listeners) {
		if (comps.isEmpty()) {
			System.err.println("This model has no likelihood components, aborting run.");
			System.exit(0);
		}
		for(LikelihoodComponent comp : comps) {
			try {
				addComponent(comp);
			}
			catch (ClassCastException cce) {
				System.err.println("Could not cast object " + comp + " to likelihood component, aborting");
				System.exit(0);
			}
		}
		
		if (params.isEmpty()) {
			System.err.println("This model has zero active parameters, aborting run.");
			System.exit(0);
		}
		int modCount = 0;
		for(Object param : params) {
			try {
				addParameter( (AbstractParameter<?>)param);
				modCount += ((AbstractParameter<?>)param).getModifierCount();
			}
			catch (ClassCastException cce) {
				System.err.println("Could not cast object " + param + " to parameter, aborting");
				System.exit(0);				
			}
		}

		if (modCount==0) {
			System.err.println("No modifiers found for any parameter, cannot execute MCMC");
			System.exit(0);
		}
		
		String verboseStr = attrs.get("verbose");
		if (verboseStr != null) {
			verbose = Boolean.parseBoolean(verboseStr);
		}
		
		String carefulStr = attrs.get("careful");
		if (carefulStr != null) {
			try {
				Integer careful = Integer.parseInt(carefulStr);
				carefulPeriod = careful;
			}
			catch (NumberFormatException nfe) {
				System.err.println("Could not parse number from careful period string : " + carefulStr);
				System.exit(0);
			}
		}
		
		if (listeners != null) {
			for(Object listenObj : listeners) {
				try {
					MCMCListener listener = (MCMCListener)listenObj;
					listener.setMCMC(this);
					addListener(listener);
				}
				catch (ClassCastException cce) {
					System.out.println("Warning : Object  " + listenObj + " is not an MCMC listener, ignoring it.");
				}
			}
		}
		
		
		if (debugger != null) {
			debugger.setMCMC(this);
			addListener(debugger);
		}
		

		userRunLength = XMLUtils.getIntegerOrFail("length", attrs);
		
		boolean runNow = false;
		String runStr = attrs.get("run");
		if (runStr != null) {
			boolean run = Boolean.parseBoolean(runStr);
			runNow = run;
		}
		
				
		if (runNow) {
			run();		
		}
	}
	
	/**
	 * Turn on or off timing stuff for this markov chain. This should only be used before the first call to run();
	 * @param useEm
	 */
	public void setUseTimers(boolean useEm) {
		this.useTimers = useEm;
	}
	
	public boolean getUseTimers() {
		return useTimers;
	}
	
	/**
	 * The current state of chain
	 * @return
	 */
	public int getCurrentState() {
		return currentState;
	}
	
	/**
	 * Add a new parameter to the list of params to choose from. This checks to make
	 * sure the parameter is not already in the list. 
	 * @param param
	 */
	public void addParameter(AbstractParameter<?> param) {
		if (parameters.contains(param))
			throw new IllegalArgumentException("Param " + param + " is already in the list of parameters");
		parameters.add(param);
		if (param instanceof AcceptRejectListener) {
			arListeners.add((AcceptRejectListener)param);
		}
	}
	
	public boolean removeParameter(AbstractParameter<?> param) {
		return parameters.remove(param);
	}
	
	public boolean removeListener(MCMCListener listener) {
		return listeners.remove(listener);
	}
	
	/**
	 * Add a new component to the list of components to be included in the likelihood model. This
	 * checks to make sure the component is not already in here. 
	 * @param comp
	 */
	public void addComponent(LikelihoodComponent comp) {
		if (components.contains(comp))
			throw new IllegalArgumentException("Component " + comp + " is already in the list of parameters");
		components.add(comp);
		arListeners.add(comp);
		
	}
	
	public boolean removeComponent(LikelihoodComponent comp) {
		arListeners.remove(comp);
		return components.remove(comp);
	}
	
	/**
	 * Calculate the frequencies with which we should sample the various parameters. 
	 */
	private void createModParams() {
		modParams = new ArrayList<AbstractParameter<?>>();
		double sum = 0;
		for(AbstractParameter<?> param : parameters) {
			if (param.getSampleFrequency() > 0 && param.getModifierCount() > 0) {
				sum += param.getSampleFrequency();
				modParams.add(param);
			}
		}
		paramFreqSum = sum;
		paramFreqsKnown = true;
	}
	
	/**
	 * Selects a single parameter from the list 'modParams' with probability in
	 * proportional to its frequency field.
	 * 
	 * @return
	 */
	private AbstractParameter<?> pickParameter() {
		if (modParams == null) {
			createModParams();
		}
		
		double r = RandomSource.getNextUniform();
		AbstractParameter<?> param;
		int count = 0;
		while(r > modParams.get(count).getSampleFrequency()/paramFreqSum) {
			r -= modParams.get(count).getSampleFrequency()/paramFreqSum;
			count++;
		}
		
		param = modParams.get(count);
		
		return param; 
	}
	
	/**
	 * Set the heating of this chain
	 * @param heat
	 */
	public void setHeat(double heat) {
		this.heat = heat;
	}
	
	/**
	 * Run the chains for a number of steps equal to the userRunLength field
	 */
	public void run() {
		run(userRunLength);
	}
	
	
	/**
	 * Run the MCMC chain for the given number of states
	 * @param states
	 */
	public void run(int states) {

		try {
						
			advance(states);

			if (useTimers) {
				System.out.println( getTimerString() );
			}
			fireChainDone();		
			emitModifierRatios();

			System.out.println("\nRandom seed : " + RandomSource.getSeed() );
                        
		}
		catch (RuntimeException rex) {
			//Fancier error handling stuff would be nice here at some point
			System.out.flush();
			System.err.println("Runtime Exception encountered");
			System.err.println("MCMC state : " + getCurrentState());
			System.err.println("Last parameter : " + lastParam.getName());
			System.err.println("Last modifier : " + lastModifier);
			System.err.println("Random seed: " + RandomSource.getSeed());
			rex.printStackTrace();
			// could print stack trace to an error log file? rex.printStackTrace( errorLog )
		}
		catch (Exception ex) {
			System.out.flush();
			System.err.println("Exception encountered : ");
			System.err.println("Random seed: " + RandomSource.getSeed());
			ex.printStackTrace();		
		}

	}
	
	/**
	 * Return a string with summary information regarding the timers for this Markov chain. 
	 * @return
	 */
	public String getTimerString() {
		if (! useTimers) {
			return "Timing facility not in use for this Markov chain \n";
		}
		
		StringBuilder strB = new StringBuilder();
		
		Timer runLengthTimer = Timer.getTimer("RunLength");

		Timer patternTimer = Timer.getTimer("Core-propose");
		Timer dlTimer = Timer.getTimer("DL");
		Timer rangeTimer = Timer.getTimer("Range1");
		Timer rangeTimer2 = Timer.getTimer("Range2");
		Timer argModTimer = Timer.getTimer("ARGModifier");
		
		strB.append("\n\nTotal run time	 : " + runLengthTimer + "\n");
		strB.append("Core-propose time : " + patternTimer + "\t\t" + StringUtils.format(100.0*patternTimer.getTotalTimeMS()/runLengthTimer.getTotalTimeMS(), 3) + "% \n" );
		strB.append("DL time      : " + dlTimer + "\t\t" + StringUtils.format(100.0*dlTimer.getTotalTimeMS()/runLengthTimer.getTotalTimeMS(), 3) + "% \n");
		strB.append("Range time   : " + rangeTimer + "\t\t" + StringUtils.format(100.0*rangeTimer.getTotalTimeMS()/runLengthTimer.getTotalTimeMS(), 3) + "% \n");
		strB.append("Range time2   : " + rangeTimer2 + "\t\t" + StringUtils.format(100.0*rangeTimer2.getTotalTimeMS()/runLengthTimer.getTotalTimeMS(), 3) + "% \n");
		strB.append("ARG mod time : " + argModTimer + "\t\t" + StringUtils.format(100.0*argModTimer.getTotalTimeMS()/runLengthTimer.getTotalTimeMS(), 3) + "% \n");
		
		strB.append("\n\n Modifier times: \n");
		for(Modifier<?> mod : modTimes.keySet()) {
			double timePerCall = (double)modTimes.get(mod) / (double)mod.getCalls();
			strB.append(mod + "\t Total : " + modTimes.get(mod) + "\t\t" + StringUtils.format(timePerCall, 5) + "\n");
		}

		return strB.toString();
	}

	/**
	 * Performs actual running of the chain for the given number of states. 
	 * @param states
	 */
	public void advance(int states) {
		String modStr = "";
		
		if (currentLikelist == null)
			currentLikelist = new double[components.size()];
		if (propLikeList == null)
			propLikeList = new double[components.size()];
		
		
		if (getCurrentState()==0 && useTimers) {
			System.out.println("Initializing timers for Markov chain");
			Timer runLengthTimer = new Timer("RunLength");
			Timer patternTimer = new Timer("Core-propose");
			Timer dlTimer = new Timer("DL");
			Timer rangeTimer = new Timer("Range1");
			Timer rangeTimer2 = new Timer("Range2");
			Timer argModTimer = new Timer("ARGModifier");
		}
		
		Timer.startTimer("RunLength");
		
		int step = 0;
		Double currentL = 0.0;
		Double propL = 0.0;

		while(step < states) {		
			if (verbose) {
				System.out.println("\n\nBeginning calculations for state: " + acceptedStates + " / " + totalStates);
				System.out.println("Calculating current likelihood");
			}
			
			
			//1) Compute the current likelihood
			int i = 0;
			//if (verifyRejection) {
				currentL = 0.0;
				for(LikelihoodComponent comp : components) {
					double currentLike = comp.getCurrentLogLikelihood();
					currentL += currentLike;
					currentLikelist[i] = currentLike;
					i++;
				}

				//Test to make sure that after an accept step the current likelihood is equal to the likelihood that was proposed
				if ((!neverAccept) && acceptedStates>0 && acceptedLogL != currentL ) {
					System.out.flush();
					System.err.println("Error: Probability of previously accepted state is not the next state's current probability!");
					System.err.println("Current likelihood: " + currentL);
					System.err.println("Accepted likelihood: " + acceptedLogL);
					System.exit(0);
				}
			//}
			
			//Propose a new state by 
			//2) picking a parameter to change
			AbstractParameter<?> param = pickParameter();
			lastParam = param;
			
			//3) changing it and noting the hastings ratio
			Modifier<?> mod = param.pickModifier(); 
			lastModifier = mod;
			
			Double hastingsRatio = 1.0;
			
			if (verbose) {
				System.out.println("Modifying " + param.getName() + " with : "+ mod);
			}
			
			try {
				modTimer.clear();
				modTimer.start();
				hastingsRatio = mod.modify();
				modStr = mod.getModStr();
			} catch (InvalidParameterValueException e) {
				System.err.println("Caught invalid param value exception : " + e);
				System.exit(0);
			} catch (IllegalModificationException e) {
				System.err.println("Parameter " + param.getName() + " is not in valid state : \n " + e.getMessage());
				System.exit(0);
			} catch (ModificationImpossibleException e) {
				//The modifier could not perform its operation (probably tree operation on a small tree)
				//so don't log a new state and just continue
				continue;
			}
			
			
			// 4) Computing the proposed likelihood

			i = 0;
			propL = 0.0;
			for(LikelihoodComponent comp : components) {
				double lnL = comp.getProposedLogLikelihood();
				propL += lnL;
				propLikeList[i] = lnL;
				i++;
			}
		
	
			if (verbose)
				System.out.println("Current likelihood : " + currentL + " proposed likelihood: " + propL);
			
			
			//Verify likelihood calculations for all components
			if (verifyLogCalc) {
				verifyLogCalculation(propL, propLikeList);
			}
			
			//4.5) calculating the ratio of proposed / current * hastings term
			Double test =  heat*(propL - currentL) + Math.log(hastingsRatio);
	
			//5) Deciding to accept / reject
			Double r = Math.log( RandomSource.getNextUniform() );
			
			boolean accept = r <= test;
			
			if (verbose)
				System.out.println("hr: " + Math.log(hastingsRatio) + " test stat: " + test + " u: " + r);
			
			if (neverAccept) 
				accept = false;
			
			//6) notifying all components of the accept / rejection
			if (accept) {
				if (verbose) {
					System.out.println("Accepted, firing state accepted");
				}

				acceptedStates++;
				acceptedLogL = propL;
				currentL = propL;
				mod.tallyAcceptance();
				param.acceptValue();
				fireStateAccepted();
				
				if (verifyLogCalc) { //If we verified, then all params must have accept called
					for(Parameter<?> par : parameters) {
						par.acceptValue();
					}
				}
			}
			else { //State has been rejected
				if (verbose) {
					System.out.println("rejected, firing state rejected");
				}
				
				mod.tallyRejection();
				param.revertValue();
				fireStateRejected(); //Does order matter here? Most components ignore rejections...?  
				
				if (verifyLogCalc) { //If we verified, all params *except* the one we just modified should have accept called
					for(Parameter<?> par : parameters) {
						if (par != param) {
							par.acceptValue();
						}
					}
				}

				
				//Make sure we've restored the current likelihood after a rejection
				if (verifyRejection && (!neverAccept)) {
					double rejectedL = 0;
					if (verbose) 
						System.out.println("Verifying rejected state..");

					for(LikelihoodComponent comp : components) {
						rejectedL += comp.getProposedLogLikelihood();
					}
					
					if (rejectedL != currentL) {
						System.out.println("Yikes! Likelihood after rejection was not the same as current likelihood!");
						System.out.println("Current likelihood: " + currentL);
						System.out.println("Likelihood after rejection : " + rejectedL);
						System.exit(0);
					}
				}
			} //else state was rejected
			
			if (verbose) {
				System.out.println("done, state is: " + acceptedStates);
			}
			
			if (totalStates == carefulPeriod) {
				verifyRejection = false;
				verifyLogCalc = false;
				if (verbose)
					System.out.println("Turning off dl validation");
			}
			
			totalStates++;
			step++;
			modTimer.stop();
			Long time = modTimes.get(mod);
			if (time==null)
				modTimes.put(mod, modTimer.getTotalTimeMS());
			else
				modTimes.put(mod, time+modTimer.getTotalTimeMS());
			
			currentState = totalStates;
			fireNewState();
		}
		
		Timer.stopTimer("RunLength");
	}
	
	/**
	 * Write some summary information about the modifiers to system.out
	 */
	public void emitModifierRatios() {
		System.out.println("Modifier acceptance ratios: ");
		for(Parameter<?> p : parameters) {
			AbstractParameter<?> ap = (AbstractParameter<?>)p;
			System.out.println("\n" + ap.getName() + " total modifications: " + ap.getCalls() + " parameter acceptance ratio: " + ap.getAcceptanceRatio());
			for(int i=0; i<ap.getModifierCount(); i++) {
				System.out.println(ap.getModifier(i) + " total calls: " + ap.getModifier(i).getCalls() + " total accepts: " + ap.getModifier(i).getTotalAcceptances() + " ratio: " + StringUtils.format(ap.getModifier(i).getTotalAcceptanceRatio(), 4));
			}
		}
		System.out.println(" Total accepted states : " + acceptedStates);
		System.out.println(" Total proposed states : " + totalStates);
		System.out.println();
		
	}

	public List<LikelihoodComponent> getComponents() {
		return components;
	}
	
	public List<AbstractParameter<?>> getParameters() {
		return parameters;
	}
	
	/**
	 * Returns the sum of the log likelihood across all likelihood components
	 * @return
	 */
	public double getTotalLogLikelihood() {
		double sum = 0;
		for(LikelihoodComponent comp : components) {
			sum += comp.getProposedLogLikelihood();
		}
		
		return sum;
	}
	
	/**
	 * Calls forceRecomputeLikelihood and then computeProposedLikelihood on all components, and compares
	 * the result to the supplied likelihoods. They should be the same if everything is working. 
	 * @param propL The proposed likelihood of the full model, should be the sum of propLikeList
	 * @param propLikeList A list of the likelihoods for each component
	 */
	private void verifyLogCalculation(Double propL, double[] propLikeList) {
		double recalcL = 0.0;
		double[] verifyList = new double[propLikeList.length];
		
		int i=0;
		
		for(LikelihoodComponent comp : components) {
			comp.forceRecomputeLikelihood();
			double lnL = comp.getProposedLogLikelihood();
			verifyList[i] = lnL;
			i++;
			recalcL += lnL;
		}
		
		
		if ( Math.abs( (recalcL-propL)/propL) > 1e-8 || Double.isNaN(propL) || Double.isNaN(recalcL)) {
			System.out.flush();
			System.err.println("Likelihood was not correctly re-calculated!");
			System.err.println("Original : " + propL);
			System.err.println("After force recalc: " + recalcL);
			//System.err.println("Tree : \n" + ((TreeLikelihood)components.get(0)).getTree().getNewick() );
			
			for(i=0; i<verifyList.length; i++) {
				System.out.println(propLikeList[i] + "\t" + verifyList[i]);
			}
			System.exit(1);
		}		
	}
	
	/**
	 * Return the current state of the chain
	 * @return
	 */
	public int getStatesProposed() {
		return totalStates;
	}
	
	/**
	 * Return the current state of the chain
	 * @return
	 */
	public int getStatesAccepted() {
		return acceptedStates;
	}
	
	/**
	 * Notify all MCMC listeners that a the chain is in a new state
	 */
	protected void fireNewState() {
		for(MCMCListener l : getListeners()) {
			l.newState(getStatesProposed());
		}
	}
	
	/**
	 * Notify all likelihood components to accept the new state
	 */
	protected void fireStateAccepted() {
		for(AcceptRejectListener l : arListeners) {
			l.stateAccepted();
		}
	}
	
	/**
	 * Notify all likelihood components that the proposed state was rejected. 
	 */
	protected void fireStateRejected() {
		for(AcceptRejectListener l : arListeners) {
			l.stateRejected();
		}
	}

	/**
	 * Notify all listeners that the chain has finished 
	 */
	public void fireChainDone() {
		for(MCMCListener l : getListeners()) {
			l.chainIsFinished();
		}
	}
	
	/**
	 * Add additional listeners for mcmc events
	 * @param stateLogger
	 */
	public void addListener(MCMCListener listener) {
		getListeners().add(listener);
		listener.setMCMC(this);
	}

	/**
	 * Obtain a reference to the list of MCMC listeners this chain is firing events to
	 * @return
	 */
	public List<MCMCListener> getListeners() {
		return listeners;
	}

	/**
	 * Remove all listeners from this MCMC object
	 */
	public void clearListeners() {
		listeners.clear();
	}


	
		
}
		

