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


package testing;

import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import logging.BreakpointDensity;
import logging.BreakpointLocation;
import logging.LastARGLogger;
import logging.RootHeightDensity;
import logging.StateLogger;
import math.RandomSource;
import mcmc.MCMC;
import mcmc.MCMCListener;
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
import modifier.TrivialAddRemove;
import modifier.WideSwap;
import parameter.DoubleParameter;
import parameter.Parameter;
import priors.ExponentialPrior;
import sequence.BasicSequenceAlignment;
import sequence.DataMatrix;
import arg.ARG;
import arg.Newick;
import arg.TreeUtils;
import coalescent.CoalescentLikelihood;
import coalescent.ConstantPopSize;
import coalescent.ConstantRecombination;
import dlCalculation.DataLikelihood;
import dlCalculation.siteRateModels.ConstantSiteRates;
import dlCalculation.siteRateModels.GammaSiteRates;
import dlCalculation.substitutionModels.BaseFrequencies;
import dlCalculation.substitutionModels.F84Matrix;
import dlCalculation.substitutionModels.MutationModel;
import dlCalculation.substitutionModels.TN93Matrix;

public class DataLikelihoodTest {
	
	/**
	 * MCMC over the prior arg space with N=1 and rho = 0.5
	 * @param arg
	 * @param listeners
	 */
	private void runMCMC(ARG arg, DataLikelihood dl, List<Object> likelihoods, List<Parameter<?>> params, List<MCMCListener> listeners) {
		List<Object> parameters = new ArrayList<Object>();
		
		List<MCMCListener> mcListeners = new ArrayList<MCMCListener>();
		if (listeners != null)
			mcListeners.addAll(listeners);
		
		Map<String, String> loggerProps = new HashMap<String, String>();
		loggerProps.put("echoToScreen", "true");
		loggerProps.put("frequency", "10");
		loggerProps.put("logFile", "gammatest.log");
		StateLogger logger = new StateLogger(loggerProps);
		mcListeners.add(logger);
		
		ConstantPopSize popSize = new ConstantPopSize(0.01);
		popSize.addModifier(new ScaleModifier());
		popSize.acceptValue();
		
//		PiecewiseLinearPopSize popSize = new PiecewiseLinearPopSize();
//		popSize.addModifier(new LinearFunctionMover(arg));
//		mcListeners.add(new PopSizeLogger(100, 10, arg, popSize));
//		PopSizePrior popSizePrior = new PopSizePrior(popSize);
//		likelihoods.add(popSizePrior);
		
		
		ConstantRecombination rec = new ConstantRecombination(10.0);
		rec.addModifier(new ScaleModifier());
		
		ExponentialPrior rhoPrior = new ExponentialPrior(rec, 1);
		
		CoalescentLikelihood coalLikelihood = new CoalescentLikelihood(popSize, rec, arg);
		likelihoods.add(coalLikelihood);
		likelihoods.add(rhoPrior);
		
		
		parameters.add(popSize);
		parameters.add(rec);
		parameters.add(arg);
		if (params != null) {
			for(Parameter par : params)
				parameters.add(par);
		}
		
		Map<String, String> mcAttrs = new HashMap<String, String>();
		mcAttrs.put("length", "100");
		MCMC mc = new MCMC(mcAttrs, parameters, likelihoods);
		if (dl!= null)
			dl.setChain(mc);
		for(MCMCListener listener : mcListeners) {
			listener.setMCMC(mc);
			mc.addListener(listener);
		}
		
		
		mc.run( 5000 );
	}
	
	
	@Test public void testTest() {
		RandomSource.initialize( /*-1784465878 */ );
		
		//String homeDir = "/Users/brendano/workspace/ACG_exp/";
		//String homeDir = "/home/brendan/workspace/ACG/";
		String homeDir = "/Users/brendano/workspace/ACG/";

		//String homeDir = "/home/brendan/workspace/ACG_exp/";

		//File file = new File(homeDir + "24.04e6_L50K.fas");
		//File file = new File(homeDir + "test/test_arg_3tip_R1_L100.fas");
		//File file = new File(homeDir + "test/10tips_N100_L250_1bigrecomb.fas");
		File file = new File(homeDir + "test/10tips_t01_L1K.fas");
		//File file = new File(homeDir + "gapTest.fas");
		
		
		//Newick newick = new Newick("(one:1.0, two:1.0);");
		
		BasicSequenceAlignment alignment = new BasicSequenceAlignment(file);
        //alignment.removeGapsAndUnknowns();
		DataMatrix data = new DataMatrix(alignment);

		Map<String, String> argAttrs = new HashMap<String, String>();
		argAttrs.put("upgma", "false");
		//argAttrs.put("theta", "0.01");
		//argAttrs.put("file", "/Users/brendano/NetBeansProjects/ACG/startARG.xml");
		//argAttrs.put("file", homeDir + "someSimulatedARGs/6tips_N200_mu5e5_L1K_3.xml");
		//argAttrs.put("file", homeDir + "test/test_triv.xml");
		//argAttrs.put("file", homeDir + "23.32_startARG_bp100.xml");

		ARG arg = new ARG(argAttrs, alignment);
        arg.setFrequency(10.0);
		//ARG arg = new ARG(argAttrs, newick);
		
		

		List<Parameter<?>> params = new ArrayList<Parameter<?>>();

//		DoubleParameter alpha = new DoubleParameter(1.0, "siteRates.alpha", "siteRates.alpha", 0.01, 100.0);
//		alpha.addModifier(new SimpleModifier());
//		alpha.setFrequency(4);
//		GammaSiteRates siteRates = new GammaSiteRates(4, alpha);
//		params.add(siteRates);
//		params.add(alpha);
		
		ConstantSiteRates siteRates = new ConstantSiteRates();
		
		
		DoubleParameter kappa = new DoubleParameter(1.25, "kappa", "kappa", 0, 1000);
		kappa.addModifier(new ScaleModifier());
		kappa.setFrequency(0.2);
		params.add(kappa);
		
		
//		DoubleParameter kappa2 = new DoubleParameter(1.25, "kappa2", "kappa2", 0, 1000);
//		kappa2.addModifier(new ScaleModifier());
//		kappa2.setFrequency(0.2);
//		params.add(kappa2);
		
		
		
		BaseFrequencies freqs = new BaseFrequencies(new double[]{0.25, 0.25, 0.25, 0.25});
		freqs.addModifier(new DirichletModifier());
		freqs.setFrequency(0.1);
		params.add(freqs);

		MutationModel mutationModel = new F84Matrix(freqs , kappa);
		//MutationModel mutationModel = new TN93Matrix(freqs , kappa, kappa2);
		
		Map<String, String> dlAttrs = new HashMap<String, String>();

		
		DataLikelihood dl = new DataLikelihood(dlAttrs, mutationModel, siteRates, arg);

		
		Modifier<ARG> swapModifier = new SubtreeSwap();
		Modifier<ARG> heightModifier = new NodeHeightModifier();
		Modifier<ARG> rootHeightMod = new RootHeightModifier();
		Modifier<ARG> bpShifter = new BreakpointShifter();
		Modifier<ARG> bpAddRemove = new RecombAddRemove();
		Modifier<ARG> bpSwapper = new BreakpointSwapper();
		Modifier<ARG> wideSwapper = new WideSwap();
		Modifier<ARG> trivialAdd = new TrivialAddRemove();
	
		arg.addModifier(swapModifier);
		arg.addModifier(heightModifier);
		arg.addModifier(rootHeightMod);
		arg.addModifier(wideSwapper);

		arg.addModifier(bpShifter);
		arg.addModifier(bpAddRemove);
		arg.addModifier(bpSwapper);
		//arg.addModifier(trivialAdd); //This one is broken and sucks
		
		List<Object> likelihoods = new ArrayList<Object>();
		likelihoods.add(dl);
		
		List<MCMCListener> listeners = new ArrayList<MCMCListener>();
//		BreakpointDensity bpd = new BreakpointDensity(arg, 5000, 100, System.out);
//		bpd.setWriteTempData(true, 10000, "breakpoint_density_tmp.txt");
//		bpd.setBurnin(100000);
//		listeners.add(bpd);
//		
//		RootHeightDensity rhd = new RootHeightDensity(arg, 5000, 100, System.out);
//		rhd.setWriteTempData(true, 10000, "rootheight_tmp.txt");
//		rhd.setBurnin(100000);
//		listeners.add(rhd);
//		listeners.add(new LastARGLogger(arg, "lastARG.xml"));
//		try {
//			BreakpointLocation bpl = new BreakpointLocation(arg, 2500, new PrintStream(new FileOutputStream("bpLocation.csv")));
//			bpl.setWriteTempData(true, 10000, "breakpoint_location_tmp.txt");
//			bpl.setBurnin(100000);
//			listeners.add(bpl);
//		} catch (FileNotFoundException e) {
//			System.err.println("Could not add BreakpointLocation listener, cause: " + e);
//		}
		//listeners.add(new MarginalTreeLogger(arg, 10, "marginalTree_" + 10 + ".trees"));
		
		runMCMC(arg, dl, likelihoods, params, listeners);	
		
		System.out.println("\n Number of patterns : " + data.getNumberOfPatterns());
		System.out.println("Number of polymorphic sites : " + data.getPolymorphicSites().length);
	}
	
	/**
	 * Compute the data likelihoods for several trees / sequences, and see if the result matches
	 * values we've computed with PHYLIP. Unfortunately, phylip seems to fail if branch lengths are
	 * very short (less than about 1e-8), so we currently stay away from those cases. 
	 */
	public void testDataLikelihoods() {
		String testDir = "/home/brendan/workspace/ACG_exp/test/";
		
		/** First test is with equal base frequencies, a tt ratio of 1.0, a tree with two tips and sequences 
		 *  that are both ACTG. 
		 */
		double dl = calcF84Likelihood(new File(testDir + "test0.phy"), new File(testDir + "test0.tre"), 1.0, 0.25, 0.25, 0.25, 0.25);
		System.out.println("Calculated likelihood : " + dl);
		assertTrue(Math.abs(dl - (-5.55317)) < 5e-6);

		/**
		 * Two tips, equal base freqs, tt ratio is 1.0, but now sequences are polymorphic
		 */
		dl = calcF84Likelihood(new File(testDir + "test1.phy"), new File(testDir + "test1.tre"), 1.0, 0.25, 0.25, 0.25, 0.25);
		System.out.println("Calculated likelihood : " + dl);
		assertTrue(Math.abs(dl - (-34.56799)) < 5e-6);

		
		/**
		 * Unequal base frequencies
		 */
		dl = calcF84Likelihood(new File(testDir + "test1.phy"), new File(testDir + "test1.tre"), 1.0, 0.1, 0.2, 0.4, 0.3);
		System.out.println("Calculated likelihood : " + dl);
		assertTrue(Math.abs(dl - (-35.09652)) < 5e-6);
		
		/**
		 * Four tips, ten bases per sequences, unequal base frequencies
		 */
		dl = calcF84Likelihood(new File(testDir + "test2.phy"), new File(testDir + "test2.tre"), 1.0, 0.1, 0.2, 0.4, 0.3);
		System.out.println("Calculated likelihood : " + dl);
		assertTrue(Math.abs(dl - (-73.94220)) < 5e-6);
			
		/**
		 * Four tips, ten bases per sequences, unequal base frequencies, tt ratio of 5.0
		 */
		dl = calcF84Likelihood(new File(testDir + "test2.phy"), new File(testDir + "test2.tre"), 5.0, 0.1, 0.2, 0.4, 0.3);
		System.out.println("Calculated likelihood : " + dl);
		assertTrue(Math.abs(dl - (-76.30803)) < 5e-6);	
		
		/**
		 * A really big tree with 100 tips and sequence length of 5000....
		 */
		dl = calcF84Likelihood(new File(testDir + "test3.phy"), new File(testDir + "test3.tre"), 2.0, 0.1, 0.2, 0.4, 0.3);
		System.out.println("Calculated likelihood : " + dl);
		assertTrue(Math.abs(dl - (-95961.41764)) < 5e-6);
		
	}
	
	/**
	 * Calculate the likelihood of the given sequences on the given tree using the F84 model of evolution,
	 * with the given ttRatio and base frequencies
	 * @param sequences
	 * @param treeFile
	 * @return The data likelihood
	 */
	private static double calcF84Likelihood(File sequences, File treeFile, 
											double kappa, 
											double aFreq, 
											double cFreq, 
											double gFreq, 
											double tFreq) {
		BasicSequenceAlignment alignment = new BasicSequenceAlignment(sequences);
		DataMatrix data = new DataMatrix(alignment);
		
		System.out.println("Total number of patterns : " + data.getNumberOfPatterns());
		String newickTree = null;
		try {
			BufferedReader treeReader = new BufferedReader(new FileReader(treeFile));
			newickTree = treeReader.readLine();
		}
		catch (Exception ex) {
			System.err.println("Could not read tree : " + treeFile);
		}

		ARG tree = new ARG(new HashMap<String, String>(), new Newick(newickTree), alignment);
		
		DoubleParameter kapParam = new DoubleParameter(kappa, "kappa", "kappa", 0, 1000);
		BaseFrequencies freqs = new BaseFrequencies(new double[]{aFreq, cFreq, gFreq, tFreq});

		MutationModel mutationModel = new F84Matrix(freqs , kapParam);
		DataLikelihood treeComp = new DataLikelihood(new HashMap<String, String>(), mutationModel, null, tree);

		treeComp.computeProposedLikelihood();		
		return treeComp.getProposedLogLikelihood();
	}
	
	/**
	 * A Test to verify that SiteRange information is being calculated correctly
	 */
	public void testSiteRanges() {
		RandomSource.initialize();
		
		int trials = 1000;
		int tips = 25;
		
		System.out.println("Testing random trees with " + tips + " tips...");
		//First we try a bunch of random args
		for(int i=0; i<trials; i++) {
			ARG arg = TreeUtils.generateRandomARG(tips, 1000.0, 1.0, 1000);
			arg.initializeRanges();
		}
		
		System.out.println("Passed random tree test.");
		
		//Now we do some MCMC rearrangements
		double genN = 1; 
		double genRho = 0.5;
		int sites = 1000;
		
		
		ARG arg = TreeUtils.generateRandomARG(tips, genN, genRho, sites);
		
		
		Modifier<ARG> swapModifier = new SubtreeSwap();
		Modifier<ARG> heightModifier = new NodeHeightModifier();
		Modifier<ARG> bpShifter = new BreakpointShifter();
		Modifier<ARG> bpSwapper = new BreakpointSwapper();
		Modifier<ARG> addRemove = new RecombAddRemove();
		Modifier<ARG> rootHeight = new RootHeightModifier();
		Modifier<ARG> wideSwapper = new WideSwap();
		arg.addModifier(swapModifier);
		arg.addModifier(heightModifier);
		arg.addModifier(bpShifter);
		arg.addModifier(bpSwapper);
		arg.addModifier(rootHeight);
		arg.addModifier(addRemove);
		arg.addModifier(wideSwapper);
		
		System.out.println("Rearranging trees..");
		List<Object> likelihoods = new ArrayList<Object>();
		List<MCMCListener> listeners = new ArrayList<MCMCListener>();
		//listeners.add( new ErrorTreeWriter(arg));
		ConstantPopSize popSize = new ConstantPopSize(1.0);
		
		ConstantRecombination rec = new ConstantRecombination(0.5);
				
		CoalescentLikelihood coalLikelihood = new CoalescentLikelihood(popSize, rec, arg);
		coalLikelihood.addParameter(arg);
		likelihoods.add(coalLikelihood);
		
		
		runMCMC(arg, null, likelihoods, null, listeners);
		
		System.out.println("Passed site range verification test");
	}


	public static void main(String[] args) {
		DataLikelihoodTest dlTester = new DataLikelihoodTest();
		//dlTester.testDataLikelihoods();
		dlTester.testTest();	
	}
}
