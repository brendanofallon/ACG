/********************************************************************
*
* 	Copyright 2011 Brendan O'Fallon
*
*   Licensed under the Apache License, Version 2.0 (the "License");
*   you may not use this file except in compliance with the License.
*   You may obtain a copy of the License at
*
*       http://www.apache.org/licenses/LICENSE-2.0
*
*   Unless required by applicable law or agreed to in writing, software
*   distributed under the License is distributed on an "AS IS" BASIS,
*   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*   See the License for the specific language governing permissions and
*   limitations under the License.
*
***********************************************************************/


package mcmc.mc3;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import coalescent.CoalescentLikelihood;
import coalescent.ConstantPopSize;
import coalescent.ConstantRecombination;

import logging.StateLogger;
import logging.StringUtils;
import math.RandomSource;
import mcmc.MCMC;
import mcmc.MCMCListener;
import modifier.AbstractModifier;
import modifier.BreakpointShifter;
import modifier.BreakpointSwapper;
import modifier.DirichletModifier;
import modifier.Modifier;
import modifier.NodeHeightModifier;
import modifier.RecombAddRemove;
import modifier.RootHeightModifier;
import modifier.ScaleModifier;
import modifier.SimpleModifier;
import modifier.SubtreeSwap;
import modifier.WideSwap;
import arg.ARG;

import parameter.AbstractParameter;
import parameter.DoubleParameter;
import priors.ExponentialPrior;
import sequence.BasicSequenceAlignment;
import sequence.DataMatrix;
import xml.XMLLoader;
import xml.XMLUtils;

import component.LikelihoodComponent;
import dlCalculation.DataLikelihood;
import dlCalculation.substitutionModels.BaseFrequencies;
import dlCalculation.substitutionModels.F84Matrix;
import dlCalculation.substitutionModels.MutationModel;


/**
 * A class to implement Metropolis-coupled MCMC runs,
 * @author brendano
 *
 */
public class MC3 {
	
	public static final String XML_THREADS = "threads";
	public static final String XML_CHAINS = "chains";
	public static final String XML_SWAPSTEPS = "swap.steps";
	
	int numThreads = 4;
	ThreadPoolExecutor threadPool = null;
	
	int swapSteps = 400;
	
	int runLength = -1;
	
	//Number of swaps to attempt every swapSteps MCMC steps
	int swapsPerSwapStep = 2;
	
	ChainHeats chainHeats = null;
	RunnableChain[] chains = null;
	List<MCMC> protoChains = new ArrayList<MCMC>(); //Stores chains while they're being added
	
	private boolean paused = false;
	private boolean abort = false;
	
	public MC3(Map<String, String> attrs, MCMC chain, ChainHeats heatModel) {
		this(attrs, chain, heatModel, null);
	}
	
	
	public MC3(Map<String, String> attrs, MCMC chain, ChainHeats heatModel, List<Object> listeners) {
		threadPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(numThreads);
		this.numThreads = XMLUtils.getIntegerOrFail(XML_THREADS, attrs);
		this.chainHeats = heatModel;
		int numChains = XMLUtils.getIntegerOrFail(XML_CHAINS, attrs);
		this.runLength = XMLUtils.getIntegerOrFail(MCMC.XML_RUNLENGTH, attrs);
		Integer swapStepOp = XMLUtils.getOptionalInteger(XML_SWAPSTEPS, attrs);
		if (swapStepOp != null)
			this.swapSteps = swapStepOp;
		
		//Label of the XML node defining the chain, we need this because we want to create multiple
		//instances of the chain, and we need to know which one
		String mcLabel = chain.getAttribute(XMLLoader.NODE_ID);
		
		addChain(chain);
		
		XMLLoader loader = XMLLoader.getPrimaryLoader();
		if (loader == null) {
			throw new IllegalArgumentException("Could not find primary XML loader to instantiate additional chains");
		}

		//loader.setVerbose(false); //Suppress output when we create additional objects
		//Reverse-lookup all node IDs for the given listeners so we can add them back to the
		//object map after creating the new chains
		Map<String, Object> listenerIDMap = loader.findObjectLabelMapping(listeners);
		
		
		//Construct all the listeners and add them to chain 0
		if (listeners != null) {
			MCMC chain0 = protoChains.get(0);
			for(Object listenObj : listeners) {
				try {
					MCMCListener listener = (MCMCListener)listenObj;
					listener.setMCMC(chain0);
					chain0.addListener(listener);
				}
				catch (ClassCastException cce) {
					System.out.println("Warning : Object  " + listenObj + " is not an MCMC listener, ignoring it.");
				}
			}
		}
		
		for(int i=0; i<numChains-1; i++) {
			loader.clearObjectMap();
			
			try {
				MCMC newChain = (MCMC) loader.getObjectForLabel(mcLabel);
				newChain.setUseTimers(false);
				addChain(newChain);
			} catch (Exception ex) {
				throw new IllegalArgumentException("Error loading additional chains from MC3 : \n" + ex);
			}
		}
		
		//Kind of clumsy, but there are a few things we must add BACK into the map after we've cleared it. Namely, 
		//anything having to do with chain heating and all of the MCMCListeners that were added, so when 
		//we grab listeners/ loggers subsequently we don't create new ones...
		List<AbstractParameter<?>> chainHeatParams = heatModel.getGlobalParameters();
		for(AbstractParameter<?> param : chainHeatParams) {
			loader.addToObjectMap(param.getAttribute(XMLLoader.NODE_ID), param);
			for(int i=0; i<param.getModifierCount(); i++) {
				AbstractModifier<?> mod = (AbstractModifier<?>) param.getModifier(i);
				loader.addToObjectMap(mod.getAttribute(XMLLoader.NODE_ID) , mod);
			}
		}
		//Add all listener objects back to loader's object map so they aren't re-created with 
		//subsequent calls to getObjectForLabel...
		for(String key: listenerIDMap.keySet()) {
			loader.addToObjectMap(key, listenerIDMap.get(key));
		}
		

		
		//Run unless the attributes say not to
		Boolean runNow = XMLUtils.getOptionalBoolean(MCMC.XML_RUNNOW, attrs);
		if (runNow == null || runNow) {
			run();
		}
		
	}
	
	


	public MC3(int threads) {
		this.numThreads = threads;
		threadPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(numThreads);
	}
	
	public int getRunLength() {
		return runLength;
	}
	
	public void addChain(MCMC chain) {
		if (chains != null) {
			throw new IllegalArgumentException("MC3 has already been initialized, can no longer add more chains");
		}
		
		protoChains.add(chain);
	}
	
	/**
	 * Add a new listener to the cold chain. 
	 * @param l
	 */
	public synchronized void addListener(MCMCListener l) {
		getColdChain().addListener(l);
	}
	
	/**
	 * Pause / unpause this chain in a thread-safe way
	 * @param paused
	 */
	public void setPaused(boolean pause) {
		this.paused = pause;
	}
	
	/**
	 * Abort all chains and exit
	 */
	public void abort() {
		this.abort = true;
	}
	
	/**
	 * Get the non-heated MCMC chain
	 * @return
	 */
	public MCMC getColdChain() {
		if (chains != null)
			return chains[0].chain;
		else {
			if (protoChains.size() > 0)
				return protoChains.get(0);
			else
				return null;
		}
	}
	
	public MCMC getChain(int which) {
		return chains[which].chain;
	}
	
	public int getChainCount() {
		if (chains != null)
			return chains.length;
		else 
			return protoChains.size();
	}
	
	
	
	/**
	 * Run using the current values for swap steps and chain heats
	 * @param steps
	 */
	public void run() {
		run(runLength, swapSteps, chainHeats);
	}
	
	public void run(int steps, int swapSteps, ChainHeats cHeats) {
		if (cHeats.getHeatCount() != protoChains.size()) {
			throw new IllegalStateException("Same number of chains and heats is required");
		}
		
		this.chainHeats = cHeats;
		
		if (swapsPerSwapStep > protoChains.size()/4) {
			swapsPerSwapStep = Math.max(1, protoChains.size()/4);
		}
		
		convertChains(swapSteps);

		
		int[] swapAttempts = new int[chains.length];
		int[] swaps = new int[chains.length];
		
		int cycleNumber = 0;
		
		int stateNumber = 0;
		int attemptedSwaps = 0;
		int actualSwaps = 0;
		
		while((!abort) && stateNumber < steps) {
			cycleNumber++;
			
			Future<?>[] res = new Future<?>[chains.length];
			
			//Run all chains for another 'swapSteps' MCMC steps
			for(int i=0; i<chains.length; i++) {
				if (chains[i].isRunning()) {
					System.out.println("Yikes! A chain is running but we're submitting it again!");
					System.exit(0);
				}
				
				res[i] = threadPool.submit(chains[i]);
			}
			
			//Wait until all threads have completed
			for(int i=0; i<chains.length; i++) {
				try {
					res[i].get(); //Blocks until thread i is done
				} catch (InterruptedException e) {
					e.printStackTrace();
					System.exit(0);
				} catch (ExecutionException e) {
					e.printStackTrace();
					System.exit(0);
				}
			}
			
			while(paused) {
				try {
					Thread.sleep(500); //Wake up every 0.5 seconds to see if we're unpaused
				} catch (InterruptedException e) {
					
				} 
			}
			
			//Try a swap of chains
			if (chains.length > 1) {
				//Occasionally emit some info so we can see whats going on
//				if (cycleNumber%10==0) {
//					System.out.print("Swap rates  : ");
//					for(int i=0; i<chains.length; i++) {
//						System.out.print(StringUtils.format( (double)swaps[i]/(double)swapAttempts[i], 4) + "\t");
//					}
//					System.out.println();
//					
//					System.out.print("Temperatures: ");
//					for(int i=0; i<chains.length; i++) {
//						System.out.print(StringUtils.format(chainHeats.getHeat(i), 3) + "\t");
//					}
					
//					for(int i=0; i<chains.length; i++) {
//						System.out.print(StringUtils.format(chains[i].chain.getTotalLogLikelihood()) + "\t");
//					}
//					System.out.println();
//				}
				
				//Clear every so often so we can see how this changes over time
				if (attemptedSwaps%500<swapsPerSwapStep) {
					for(int k=0; k<chains.length; k++) {
						swapAttempts[k]=0;
						swaps[k] = 0;
					}
				}
				
				for(int l=0; l<swapsPerSwapStep; l++) {
					attemptedSwaps++;
					boolean swapped = attemptSingleSwap(swapAttempts, swaps);
					if (swapped)
						actualSwaps++;
				}
				
			}
			
			stateNumber = chains[0].chain.getStatesProposed();
		}
		
		
		//Nicely shut down the thread pool
		threadPool.shutdown();
		try {
			threadPool.awaitTermination(10, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
		}
		
		//Required for writing of summary information
		chains[0].chain.fireChainDone();
		
		//Scan chains to see if any are using timers, if so emit their timing summary
		for(int i=0; i<chains.length; i++) {
			if (chains[i].chain.getUseTimers()) {
				System.out.println("Timing summary :");
				System.out.println(chains[i].chain.getTimerString());
			}
		}
		
		System.out.println("Modifier summary (for one chain) :");
		chains[0].chain.emitModifierRatios();
		
		System.out.println("Attempted swaps  : " + attemptedSwaps);
		System.out.println("Successful swaps : " + actualSwaps);
		System.out.println("Swap probability : " + StringUtils.format( (double)actualSwaps/(double)attemptedSwaps, 3));
	}
	
	/**
	 * Attempt to swap the chains at positions i and j in the chains array, and set the heats of the 
	 * swapped chains (if indeed they were swapped) to be what chainHeats.getHeat(i) and chainsHeats.getHeats(j)
	 * return. 
	 * 
	 * @param swapAttempts Array describing number of attempted swaps at index i
	 * @param swaps Array describing successful swaps at index i
	 * @return True if the swap was accepted
	 */
	private boolean attemptSingleSwap(int[] swapAttempts, int[] swaps) {
		//Pick two chains to attempt to swap
		int i = RandomSource.getNextIntFromTo(0, chains.length-1);
		int j = RandomSource.getNextIntFromTo(0, chains.length-1);
		while (i==j) {
			j = RandomSource.getNextIntFromTo(0, chains.length-1);
		}

		swapAttempts[i]++;
		swapAttempts[j]++;

		double currentLogLike = chainHeats.getHeat(i)*chains[i].chain.getTotalLogLikelihood() + chainHeats.getHeat(j)*chains[j].chain.getTotalLogLikelihood();
		double swapLogLike = chainHeats.getHeat(j)*chains[i].chain.getTotalLogLikelihood() + chainHeats.getHeat(i)*chains[j].chain.getTotalLogLikelihood();

		Double test =  swapLogLike - currentLogLike;

		Double r = Math.log( RandomSource.getNextUniform() );

		boolean swap = r <= test;

		if (swap) {
			swaps[i]++;
			swaps[j]++;
			//System.out.println("Swapping " + i + " (" + chains[i].chain.getTotalLogLikelihood() + ") and " + j + " ( " + chains[j].chain.getTotalLogLikelihood() +")" );

			RunnableChain tmp = chains[i];
			chains[i] = chains[j];
			chains[j] = tmp;

			chains[i].chain.setHeat(chainHeats.getHeat(i));
			chains[j].chain.setHeat(chainHeats.getHeat(j));

			if (i==0) {
				//Swap listeners from chain j back to i
				chains[i].chain.getListeners().clear();
				List<MCMCListener> listeners = chains[j].chain.getListeners();
				for(MCMCListener l : listeners) {
					chains[i].chain.addListener(l);
				}	
				chains[j].chain.getListeners().clear();
			}

			if (j==0) {
				//Swap listeners from chain i back to j
				chains[j].chain.getListeners().clear();
				List<MCMCListener> listeners = chains[i].chain.getListeners();
				for(MCMCListener listener : listeners) {
					chains[j].chain.addListener(listener);
				}
				chains[i].chain.getListeners().clear();
			}

			//Tell chain heats that swap was accepted
			if (i==0 || j==0)
				chainHeats.tallyAcceptance();

		}
		else {
			//If swap was rejected, tell chain heats so it can modify heating strategy, if necessary
			if (i==0 || j==0)
				chainHeats.tallyRejection();
		}
		
		return swap;
	}
	
	/**
	 * Create the private array of RunnableChains, so we can dispatch chains easily to the thread pool
	 * @param stepsToAdvance
	 */
	private void convertChains(int stepsToAdvance) {
		chains = new RunnableChain[protoChains.size()];
		int index = 0;
		for(MCMC mc : protoChains) {
			chains[index] = new RunnableChain(mc, stepsToAdvance);
			index++;
		}
	}
	

	/**
	 * A wrapper for an mcmc chain so that it can dispatched to a thread pool
	 * @author brendano
	 *
	 */
	class RunnableChain implements Runnable {

		MCMC chain = null;
		final int steps;
		boolean isRunning = false;
		
		public RunnableChain(MCMC chain, int stepsToRun) {
			this.steps = stepsToRun;
			this.chain = chain;
		}
		
		@Override
		public void run() {
			if (isRunning()) {
				System.err.println("Yikes! we're running but run is being called again!");
				System.exit(1);
			}
			setRunning(true);
			chain.advance(steps);
			setRunning(false);
		}

		private synchronized void setRunning(boolean running) {
			isRunning = running;
		}

		public synchronized boolean isRunning() {
			return isRunning;
		}
		
	}

	/**
	 * Returns the number of threads used in the threadpool
	 * @return
	 */
	public int getThreadCount() {
		return numThreads;
	}
	
//	public static MCMC makeChain() {
//		//String homeDir = "/home/brendan/workspace/ACG/";
//		String homeDir = "/Users/brendano/workspace/ACG_exp/";
//
//		File file = new File(homeDir + "test/25tips_N250_mu5e5_L50K_36.fas");
//
//		Alignment alignment = new Alignment(file);
//		DataMatrix data = new DataMatrix(alignment);
//
//		Map<String, String> argAttrs = new HashMap<String, String>();
//		ARG arg = new ARG(argAttrs, alignment, data);
//        arg.setFrequency(10.0);
//
//		List<AbstractParameter<?>> params = new ArrayList<AbstractParameter<?>>();
//
//		DoubleParameter kappa = new DoubleParameter(2.0, "kappa", "kappa", 0, 1000);
//		kappa.addModifier(new SimpleModifier());
//		kappa.setFrequency(0.1);
//
//		params.add(kappa);
//		
//		BaseFrequencies freqs = new BaseFrequencies(new double[]{0.25, 0.25, 0.25, 0.25});
//		freqs.addModifier(new DirichletModifier());
//		freqs.setFrequency(0.1);
//		params.add(freqs);
//
//		MutationModel mutationModel = new F84Matrix(freqs , kappa);
//		
//		Map<String, String> dlAttrs = new HashMap<String, String>();
//		DataLikelihood dl = new DataLikelihood(dlAttrs, mutationModel, arg, data);
//
//		
//		Modifier<ARG> swapModifier = new SubtreeSwap();
//		Modifier<ARG> heightModifier = new NodeHeightModifier();
//		Modifier<ARG> rootHeightMod = new RootHeightModifier();
//		Modifier<ARG> bpShifter = new BreakpointShifter();
//		Modifier<ARG> bpAddRemove = new RecombAddRemove();
//		Modifier<ARG> bpSwapper = new BreakpointSwapper();
//		Modifier<ARG> wideSwapper = new WideSwap();
//	
//		arg.addModifier(swapModifier);
//		arg.addModifier(heightModifier);
//		arg.addModifier(rootHeightMod);
//		arg.addModifier(wideSwapper);
//
//		arg.addModifier(bpShifter);
//		arg.addModifier(bpAddRemove);
//		arg.addModifier(bpSwapper);
//		
//		List<LikelihoodComponent> likelihoods = new ArrayList<LikelihoodComponent>();
//		likelihoods.add(dl);
//		
//		ConstantPopSize popSize = new ConstantPopSize(0.01);
//		popSize.addModifier(new ScaleModifier());
//		popSize.acceptValue();
//		
//		ConstantRecombination rec = new ConstantRecombination(10.0);
//		rec.addModifier(new ScaleModifier());
//		
//		ExponentialPrior rhoPrior = new ExponentialPrior(rec, 1000);
//		
//		CoalescentLikelihood coalLikelihood = new CoalescentLikelihood(popSize, rec, arg);
//		likelihoods.add(coalLikelihood);
//		likelihoods.add(rhoPrior);
//		
//		
//		params.add(popSize);
//		params.add(rec);
//		params.add(arg);
//		
//		List<MCMCListener> listeners = new ArrayList<MCMCListener>();
//		
//		MCMC mc = new MCMC(new HashMap<String, String>(), params, likelihoods, listeners);
//		
//		return mc;
//	}

//	public static MCMC makeChainNot() {
//		List<LikelihoodComponent> likelihoods = new ArrayList<LikelihoodComponent>();
//		List<AbstractParameter<?>> params = new ArrayList<AbstractParameter<?>>();
//		
//		DoubleParameter par = new DoubleParameter(1.0, "Param0", "Param0", -50, 50);
//		
//		par.addModifier(new SimpleModifier());
//		
//		GaussianLikelihood gauss = new GaussianLikelihood(par);
//		
//		params.add(par);
//		likelihoods.add(gauss);
//		
//		List<MCMCListener> listeners = new ArrayList<MCMCListener>();
//		
//		MCMC mc = new MCMC(new HashMap<String, String>(), params, likelihoods, listeners);
//		
//		return mc;
//	}

//	public static void main(String[] args) {
//		
//		RandomSource.initialize( );
//
//		MC3 mc3 = new MC3();
//	
//		MCMC mc0 = makeChain();
//		
//		Map<String, String> loggerProps = new HashMap<String, String>();
//		loggerProps.put("echoToScreen", "true");
//		loggerProps.put("frequency", "2000");
//		loggerProps.put("logFile", "mc3_test.log");
//		StateLogger logger = new StateLogger(loggerProps);
//		
//		mc0.addListener(logger);
//		logger.setMCMC(mc0);
//		
//		MCMC mc1 = makeChain();
//		MCMC mc2 = makeChain();
//		
//		mc3.addChain(mc0);
////		mc3.addChain(mc1);
////		mc3.addChain(mc2);
//		
//		double[] heats = new double[]{1.0}; //, 0.975, 0.95};
//		mc3.run(1000000, 250, heats);
//	}
}
