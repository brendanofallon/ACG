package testing;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import logging.BreakpointDensity;
import logging.HistogramCollector;
import logging.RootHeightDensity;
import logging.StateLogger;
import math.RandomSource;
import mcmc.MCMC;
import mcmc.MCMCListener;
import modifier.BreakpointShifter;
import modifier.BreakpointSwapper;
import modifier.LinearFunctionAddRemove;
import modifier.LinearFunctionMover;
import modifier.Modifier;
import modifier.NodeHeightModifier;
import modifier.RecombAddRemove;
import modifier.RootHeightModifier;
import modifier.ScaleModifier;
import modifier.SubtreeSwap;
import modifier.TrivialAddRemove;
import modifier.WideSwap;

import org.junit.Test;

import coalescent.CoalescentLikelihood;
import coalescent.ConstantPopSize;
import coalescent.ConstantRecombination;
import coalescent.PiecewiseLinearPopSize;
import coalescent.PopSizeLogger;
import coalescent.PopSizePrior;

import arg.ARG;
import arg.ARGNode;
import arg.TreeUtils;
import arg.argIO.ARGParser;

public class CoalescentLikelihoodTest {

	private void runMCMC(List<Object> likelihoods, List<Object> parameters) {
		runMCMC(likelihoods, parameters, null);
	}
	
	private void runMCMC(List<Object> likelihoods, List<Object> parameters, List<MCMCListener> listeners) {
		
		Map<String, String> loggerProps = new HashMap<String, String>();
		loggerProps.put("echoToScreen", "true");
		loggerProps.put("frequency", "1000");
		loggerProps.put("logFile", "coal_test.log");
		StateLogger logger = new StateLogger(loggerProps);

		MCMC mc = new MCMC(parameters, likelihoods);
		logger.setMCMC(mc);
		mc.addListener(logger);
		
		if (listeners != null) {
			for(MCMCListener l : listeners) 
				mc.addListener(l);
		}
		
		mc.run(10000000);
	}
	
	/**
	 * MCMC over Pr{G | N, rho }
	 */
	public void testARGPrior() {
		RandomSource.initialize( );

		int tips = 10;
		double genN = 100; 
		double genRho = 1.0;
		int sites = 10000;
				
		ARG arg = TreeUtils.generateRandomARG(tips, genN, genRho, sites); 
		
//		ARGParser parser = new ARGParser();
//		try {
//			parser.writeARG(arg, new File("test_arg_4.xml"));
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
		
		System.out.println("Initial arg has : " + arg.getRecombNodes().size() + " breakpoints and root height : " + arg.getMaxHeight() );
		
		Modifier<ARG> swapModifier = new SubtreeSwap();
		Modifier<ARG> heightModifier = new NodeHeightModifier();
		Modifier<ARG> bpShifter = new BreakpointShifter();
		Modifier<ARG> bpSwapper = new BreakpointSwapper();
		Modifier<ARG> addRemove = new RecombAddRemove();
		Modifier<ARG> rootHeight = new RootHeightModifier();
		//Modifier<ARG> trivialRec = new TrivialAddRemove();
		arg.addModifier(swapModifier);
		arg.addModifier(new WideSwap());
		arg.addModifier(heightModifier);
		//arg.addModifier(bpShifter);
		arg.addModifier(bpSwapper);
		arg.addModifier(rootHeight);
		arg.addModifier(addRemove);
		//arg.addModifier(trivialRec);
		
		List<Object> likelihoods = new ArrayList<Object>();
		List<Object> parameters = new ArrayList<Object>();
		
		double N = genN;
		double rho = 2.0 /(2*N);
		
		ConstantPopSize popSize = new ConstantPopSize(N);
		
		ConstantRecombination rec = new ConstantRecombination(rho);
				
		CoalescentLikelihood coalLikelihood = new CoalescentLikelihood(popSize, rec, arg);
		coalLikelihood.addParameter(arg);
		parameters.add(arg);
		likelihoods.add(coalLikelihood);
				
		HistogramCollector bpHist = new HistogramCollector(arg, "num.breakpoints", 500, 0, 35, 35);
		bpHist.setBurnin(1000);
		List<MCMCListener> listeners = new ArrayList<MCMCListener>();
		listeners.add(bpHist);
		
		HistogramCollector rootHeightHist = new HistogramCollector(arg, "root.height", 500, 0, 500, 100);
		rootHeightHist.setBurnin(1000);
		listeners.add(rootHeightHist);
		
		//listeners.add(new BreakpointDensity(arg, 1000, 100+));
		//listeners.add(new RootHeightDensity(arg, 1000, 100));
		
		//HistogramCollector condRootHeight = new RootHeightCollector(arg, 5, 25, 0, 500, 100);
		//listeners.add(condRootHeight);
		
//		Map<String, String> treeLogProps = new HashMap<String, String>();
//		treeLogProps.put("filename", "arg_test");
//		treeLogProps.put("frequency", "10000");

		
		runMCMC(likelihoods, parameters, listeners);
		
		assertTrue( true );
		System.out.println("Coalescent Prior test 2 completed");
	}
	
	
	
	/**
	 * MCMC over Pr{ N, rho | G }
	 * @throws IOException 
	 */
	public void testCoalPrior() throws IOException  {
		RandomSource.initialize();
		
		ARGParser parser = new ARGParser();
		List<ARGNode> nodes = null;
		
		int tips = 2;
		double N = 100;
		double rho = 1;
		int sites = 1000;
		//nodes = TreeUtils.generateRandomARG(tips, N, rho, sites);
		//nodes = parser.readARG(new File("/Users/brendano/workspace/ACG_exp/growth_test/N500_r001_8.xml")); //new ARG(1000, nodes);

		Map<String, String> argAttrs = new HashMap<String, String>();
		argAttrs.put("file", "/Users/brendano/workspace/ACG_exp/growth_test/N500_bneck_1.xml");
		ARG arg = new ARG(argAttrs);


		List<Object> likelihoods = new ArrayList<Object>();
		List<Object> parameters = new ArrayList<Object>();
		List<MCMCListener> listeners = new ArrayList<MCMCListener>();
		
		
//		ConstantPopSize popSize = new ConstantPopSize(1.0);
//		popSize.addModifier(new ScaleModifier());
		
		PiecewiseLinearPopSize popSize = new PiecewiseLinearPopSize();
		popSize.addModifier(new LinearFunctionMover(arg));
		popSize.addModifier(new LinearFunctionAddRemove(arg));
		PopSizeLogger sizeLogger = new PopSizeLogger(10000, 1000, arg, popSize);
		listeners.add( sizeLogger );
		PopSizePrior popSizePrior = new PopSizePrior(popSize);
		likelihoods.add(popSizePrior);
		
		ConstantRecombination rec = new ConstantRecombination(1.0);
		rec.addModifier(new ScaleModifier());
		
		CoalescentLikelihood coalLikelihood = new CoalescentLikelihood(popSize, rec, arg);
		parameters.add(popSize);
		parameters.add(rec);
		likelihoods.add(coalLikelihood);
		
		
		HistogramCollector rhoHist = new HistogramCollector(rec, "rho", 50, 0, 0.025, 50);
		listeners.add(rhoHist);
		
		HistogramCollector popSizeHist = new HistogramCollector(popSize, "pop.size", 50, 0, 500, 50);
		listeners.add(popSizeHist);
		
		runMCMC(likelihoods, parameters, listeners);
		
		
		assertTrue( true );
		System.out.println("Coalescent Prior test 1 completed");
	}


        public static void main(String[] args) {
            CoalescentLikelihoodTest test = new CoalescentLikelihoodTest();
            test.testARGPrior();
        }
	
}
