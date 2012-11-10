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


package module;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import component.LikelihoodComponent;

import parameter.AbstractParameter;
import parameter.DoubleParameter;
import priors.ExponentialPrior;
import sequence.BasicSequenceAlignment;
import sequence.DataMatrix;
import sequence.RemovedColumnsMap;
import testing.Timer;
import arg.ARG;
import arg.TipNode;
import coalescent.CoalescentLikelihood;
import coalescent.ConstantPopSize;
import coalescent.ConstantRecombination;
import dlCalculation.DataLikelihood;
import dlCalculation.siteRateModels.ConstantSiteRates;
import dlCalculation.substitutionModels.BaseFrequencies;
import dlCalculation.substitutionModels.F84Matrix;
import dlCalculation.substitutionModels.MutationModel;
import logging.BreakpointDensity;
import logging.BreakpointLocation;
import logging.LastARGLogger;
import logging.MPEARG;
import logging.MarginalTreeLogger;
import logging.PropertyLogger;
import logging.RootHeightDensity;
import logging.StateLogger;
import math.RandomSource;
import mcmc.MCMC;
import mcmc.MCMCListener;
import mcmc.mc3.ChainHeats;
import mcmc.mc3.ExpChainHeats;
import mcmc.mc3.MC3;
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

public class RecombMC3 {

	int chains;
	int threads;
	int swapFrequency;
	int sampleFrequency;
	int runLength;
	String filename;
	double lambda;
	String initARGfilename;
	
	boolean useLastARG = true; //If true, attempt to use a matching file with suffix _lastARG as starting ARG
	
	MC3 mc3 = null;

    private BasicSequenceAlignment aln = null;
	
	public RecombMC3(int chains, 
						int threads, 
						int swapFrequency,
						int sampleFrequency, 
						int runLength, 
						double lambda,
						String filename,
						String initARGfilename,
						String outputDir) {
		this.chains = chains;
		this.threads = threads;
		this.swapFrequency = swapFrequency;
		this.sampleFrequency = sampleFrequency;
		this.runLength = runLength;
		this.lambda = lambda;
		this.filename = filename;
		this.initARGfilename = initARGfilename;
		
		//Parse an appropriate file stem from the file name to be used for log files 
		int lastPos = filename.lastIndexOf(".");
		String filesep = System.getProperty("file.separator");
		int firstPos = filename.lastIndexOf( filesep );
		if (firstPos < 0)
			firstPos = 0;
		else
			firstPos++;
		String fileStem = filename.substring(firstPos, lastPos > 0 ? lastPos : filename.length() );
		
		String baseDirectory = outputDir;
		if (baseDirectory == null) {
			baseDirectory = System.getProperty("user.dir");
		}
		else {
			//There is a user-specified output directory, so make sure everything goes in there
			fileStem = baseDirectory + (baseDirectory.endsWith(filesep) ? "" : filesep) + fileStem;
			System.out.println("Base directory : " + baseDirectory + "\nFile stem : "+ fileStem);
		}
		
		String logFileName = fileStem + ".log";
		System.out.println("Log file name : " + logFileName);
		
		//Create the summary 'stream' to capture output written by various listeners
		File summaryFile = new File(fileStem + "_summary.txt");
		PrintStream summaryStream = System.out;
		try {
			summaryStream = new PrintStream(new FileOutputStream(summaryFile));
		} catch (FileNotFoundException e) {
			System.out.println("Could not open output stream " + summaryFile + " for summary file, defaulting to System.out");
		}
		
		
		
		

		//Temporarily make alignment to we can build the site map
		aln = new BasicSequenceAlignment(filename);
		//RemovedColumnsMap siteMap = new RemovedColumnsMap( aln.getSiteCount() );
		//List<Integer> removedCols = aln.removeGapsAndUnknowns();
		//System.out.println("Removed " + removedCols.size() + " gapped and unknown columns from alignment");
		//siteMap.setRemovedColumns(removedCols);
		System.out.println("NOT removing columns fmor alignment, using new gap-calculation method");
		
		
		if (initARGfilename == null && useLastARG) {
			this.initARGfilename = fileStem + "_mpe_arg.xml";
			File startARG = new File(this.initARGfilename);
			if (! startARG.exists()) {
				this.initARGfilename = null;
				System.out.println("Could not find initial arg in " + this.initARGfilename);
			}
			else {
				System.out.println("Initiating run from start arg in " + this.initARGfilename);
			}
		}
		
		MCMC chain0 = makeChain();
		ARG arg = findARG(chain0);
		DataLikelihood dl = findDL(chain0);
		
		//Emit of bit of summary information to std. out
		DataMatrix data = dl.getDataMatrix();
		System.out.println("Read in " + data.getSequenceCount() + " sequences with " + data.getTotalColumnCount() + " total sites, " + data.getPolymorphicSites().length + " polymorphic sites, and " + data.getNumberOfPatterns() + " data patterns");
		
		//Create the loggers to be used for the cold chain
		List<MCMCListener> listeners = new ArrayList<MCMCListener>();
		
		Map<String, String> loggerProps = new HashMap<String, String>();
		loggerProps.put("echoToScreen", "true");
		loggerProps.put("frequency", "" + sampleFrequency);
		loggerProps.put("filename", logFileName);
		listeners.add(new StateLogger(loggerProps));
		
		listeners.add(new LastARGLogger(arg, fileStem + "_lastARG.xml"));
		
		BreakpointDensity bpDensity = new BreakpointDensity(arg, sampleFrequency, 500, summaryStream);
		bpDensity.setWriteTempData(true, 100000, fileStem + "breakpoint_density_tmp.txt");
		bpDensity.setBurnin(runLength / 2);
		//bpDensity.setSiteMap(siteMap);
		bpDensity.setMCMC(chain0);
		listeners.add(bpDensity);
		
		BreakpointLocation bpLocation =  new BreakpointLocation(arg, sampleFrequency, summaryStream);
		bpLocation.setWriteTempData(true, 100000, fileStem + "breakpoint_location_tmp.txt");
		bpLocation.setBurnin(runLength / 2);
		//bpLocation.setSiteMap(siteMap);
		bpLocation.setMaxHeight(0.001);
		bpLocation.setMCMC(chain0);
		listeners.add(bpLocation);
		
		RootHeightDensity rhDensity = new RootHeightDensity(arg, sampleFrequency, 500, summaryStream);
		rhDensity.setWriteTempData(true, 100000, fileStem + "treeHeight_density_tmp.txt");
		rhDensity.setBurnin(runLength / 2);
		//rhDensity.setSiteMap(siteMap);
		rhDensity.setMCMC(chain0);
		listeners.add(rhDensity);	
		
		Map<String, String> mpeAttrs = new HashMap<String, String>();
		mpeAttrs.put(PropertyLogger.FILENAME, fileStem + "_mpe_arg.xml");
		mpeAttrs.put(PropertyLogger.FREQUENCY, "" + sampleFrequency);
		MPEARG mpeARG = new MPEARG(mpeAttrs);
		mpeARG.setMCMC(chain0);
		listeners.add(mpeARG);
		
		int maxSite = arg.getSiteCount();
		MarginalTreeLogger margLog0 = new MarginalTreeLogger(arg, 0, fileStem + "_site0.trees");
		margLog0.setMCMC(chain0);
		listeners.add(margLog0);
		
		MarginalTreeLogger margLog10 = new MarginalTreeLogger(arg, maxSite-1, fileStem + "_site" + maxSite + ".trees");
		margLog10.setMCMC(chain0);
		listeners.add(margLog10);
		
		int middleSite = maxSite / 2;
		MarginalTreeLogger margLog20 = new MarginalTreeLogger(arg, middleSite, fileStem + "_site" + middleSite + ".trees");
		margLog20.setMCMC(chain0);
		listeners.add(margLog20);
		
		
		for(MCMCListener l : listeners)
			chain0.addListener( l );
		
		mc3 = new MC3(threads);
		mc3.addChain(chain0);
		
		for(int i=1; i<chains; i++) {
			MCMC newChain = makeChain();
			newChain.setUseTimers(false); //Turn off timing for chains that aren't #0
			mc3.addChain( newChain );
		}
	}
	
	/**
	 * Return the arg parameter from the chain
	 * @param mc
	 * @return
	 */
	private ARG findARG(MCMC mc) {
		for(AbstractParameter<?> par : mc.getParameters()) {
			if (par instanceof ARG)
				return (ARG)par;
		}
		return null;
	}

	/**
	 * Try to find and return a LikelihoodComponent whose class 
	 * is DataLikelihood in the given chain
	 * @param mc
	 * @return
	 */
	private DataLikelihood findDL(MCMC mc) {
		for(LikelihoodComponent comp : mc.getComponents()) {
			if (comp instanceof DataLikelihood) {
				return (DataLikelihood)comp;
			}
		}
		return null;
	}
	
	/**
	 * Construct an mcmc chain 
	 * @return
	 */
	private MCMC makeChain() {
		List<AbstractParameter<?>> parameters = new ArrayList<AbstractParameter<?>>();
		List<LikelihoodComponent> likelihoods = new ArrayList<LikelihoodComponent>();

		Map<String, String> argAttrs = new HashMap<String, String>();
		//If an initial arg has been supplied tell the ARG to start with it
		if (initARGfilename != null)
			argAttrs.put("filename", initARGfilename);
		ARG arg = new ARG(argAttrs, aln);
		
		arg.setFrequency(30.0);
		parameters.add(arg);
		
		Modifier<ARG> swapModifier = new SubtreeSwap();
		Modifier<ARG> heightModifier = new NodeHeightModifier();
		Modifier<ARG> rootHeightMod = new RootHeightModifier();
		Modifier<ARG> wideSwapper = new WideSwap();
	
		Modifier<ARG> bpShifter = new BreakpointShifter();
		Modifier<ARG> bpSwapper = new BreakpointSwapper();
		Modifier<ARG> addRemove = new RecombAddRemove();
		
		arg.addModifier(swapModifier);
		arg.addModifier(heightModifier);
		arg.addModifier(rootHeightMod);
		arg.addModifier(wideSwapper);
		arg.addModifier(bpShifter);
		arg.addModifier(bpSwapper);
		arg.addModifier(addRemove);
		
		
		DoubleParameter kappa = new DoubleParameter(2.0, "kappa", "kappa", 0, 1000);
		kappa.addModifier(new SimpleModifier(new HashMap<String, String>()));
		parameters.add(kappa);
		
		BaseFrequencies freqs = new BaseFrequencies(new double[]{0.25, 0.25, 0.25, 0.25});
		freqs.addModifier(new DirichletModifier());
		parameters.add(freqs);

		MutationModel mutationModel = new F84Matrix(freqs , kappa);
		
		Map<String, String> dlAttrs = new HashMap<String, String>();

		ConstantSiteRates siteRates = new ConstantSiteRates();
		
		DataLikelihood dl = new DataLikelihood(dlAttrs, mutationModel, siteRates, arg);

		likelihoods.add(dl);
	
		
		ConstantPopSize popSize = new ConstantPopSize(0.001);
		popSize.setFrequency(0.05);
		popSize.addModifier(new ScaleModifier());
		popSize.acceptValue();
		
		ConstantRecombination rec = new ConstantRecombination(10.0);
		rec.setFrequency(0.05);
		rec.addModifier(new ScaleModifier());

		ExponentialPrior rhoPrior = new ExponentialPrior(rec, 25);
		likelihoods.add(rhoPrior);
				
		CoalescentLikelihood coalLikelihood = new CoalescentLikelihood(popSize, rec, arg);
		likelihoods.add(coalLikelihood);
		
		parameters.add(popSize);
		parameters.add(rec);
		
		HashMap<String, String> mcAttrs = new HashMap<String, String>();
		mcAttrs.put("length", "" + runLength); //Without this attr the run won't initialize
		MCMC chain = new MCMC(mcAttrs, parameters, likelihoods, null);
		dl.setChain(chain);
		return chain;
	}
	
	public void run() {
		System.out.println("Heating strategy : lambda = " + lambda);
		DoubleParameter lambdaParam = new DoubleParameter(lambda, "chains.lambda", "chains.lambda", 1e-8, 0.5);
		ExpChainHeats chainHeats = new ExpChainHeats(chains, lambdaParam);
		chainHeats.setUseAdaptiveHeating(true);
		
		try {
			mc3.run(runLength, swapFrequency, chainHeats);
			System.out.println("MC3 analysis has completed. \n\n");
			Timer.clearAllTimers(); //So successive timers will be independent. 
		}
		catch (RuntimeException rex) {
			System.err.println("\n\n **************************************** \n Encountered Fatal Exception while running file : " + filename + "\n" + rex + "\n");
		}
	}

	
	
	public static void main(String[] args) {
		ArgumentParser argOps = new ArgumentParser();
		argOps.parse(args);
		
		
		Integer seed = argOps.getIntegerOp("seed");
		if (seed == null) {
			RandomSource.initialize();
		}
		else {
			RandomSource.initialize(seed);
		}
		
		Integer runLength = argOps.getIntegerOp("length");
		if (runLength == null)
			runLength = 50000000;
		
		Integer sampleFrequency = argOps.getIntegerOp("frequency");
		if (sampleFrequency == null)
			sampleFrequency = 10000;
	
		
		Integer swapFrequency = argOps.getIntegerOp("swap");
		if (swapFrequency == null)
			swapFrequency = 400;
	

		Integer chains = argOps.getIntegerOp("chains");
		if (chains == null)
			chains = 4;


		Integer threads = argOps.getIntegerOp("threads");
		if (threads == null)
			threads = 4;
		
		Double lambda = argOps.getDoubleOp("lambda");
		if (lambda == null)
			lambda = 0.001;

		String startARG = argOps.getStringOp("start");
		
		String outputDir = argOps.getStringOp("dir");
		
		System.out.println("\n Running input files with run length : " + runLength + " and sample frequency : " + sampleFrequency);
		System.out.println("Chains: " + chains + "\t threads: " + threads + "\t chain swap frequency: " + swapFrequency + " lambda: " + lambda);
		if (startARG != null) {
			System.out.println("Using initial ARG found in : " + startARG);
		}
		
		for(String arg : args) {
			String filename = arg;
			if (arg.endsWith("fas") || arg.endsWith("phy") || arg.endsWith("nex")) {
				RecombMC3 mc3 = new RecombMC3(chains, threads, swapFrequency, sampleFrequency, runLength, lambda, filename, startARG, outputDir);
				mc3.run();	
			}
		}
		
	}
	
}
