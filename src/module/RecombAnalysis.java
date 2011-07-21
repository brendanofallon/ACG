package module;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import logging.BreakpointDensity;
import logging.BreakpointLocation;
import logging.LastARGLogger;
import logging.RootHeightDensity;
import math.RandomSource;
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
import parameter.DoubleParameter;
import priors.ExponentialPrior;
import sequence.Alignment;
import sequence.DataMatrix;
import tools.ApplicationHeader;
import arg.ARG;
import coalescent.CoalescentLikelihood;
import coalescent.ConstantPopSize;
import coalescent.ConstantRecombination;
import dlCalculation.DataLikelihood;
import dlCalculation.substitutionModels.BaseFrequencies;
import dlCalculation.substitutionModels.F84Matrix;
import dlCalculation.substitutionModels.MutationModel;

public class RecombAnalysis extends NoRecombAnalysis {

	public RecombAnalysis(String inputFilename, int runLength, int frequency) {
		super(inputFilename, runLength, frequency);
	}

	
	protected void initializeComponents() {

		System.out.println("Running file : " + filename);
		File file = new File(filename); //input file
		File summaryFile = new File(fileStem + "_summary.txt");
		PrintStream summaryStream = System.out;
		try {
			summaryStream = new PrintStream(new FileOutputStream(summaryFile));
		} catch (FileNotFoundException e) {
			System.out.println("Could not open output stream " + summaryFile + " for summary file, defaulting to System.out");
		}
		

		Alignment alignment = new Alignment(file);

		Map<String, String> argAttrs = new HashMap<String, String>();
		ARG arg = new ARG(argAttrs, alignment);
		arg.setFrequency(20.0);
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
		kappa.addModifier(new SimpleModifier());
		parameters.add(kappa);
		
		BaseFrequencies freqs = new BaseFrequencies(new double[]{0.25, 0.25, 0.25, 0.25});
		freqs.addModifier(new DirichletModifier());
		parameters.add(freqs);

		MutationModel mutationModel = new F84Matrix(freqs , kappa);
		
		Map<String, String> dlAttrs = new HashMap<String, String>();

		DataLikelihood dl = new DataLikelihood(dlAttrs, mutationModel, null, arg);

		likelihoods.add(dl);
	
		
		ConstantPopSize popSize = new ConstantPopSize(0.01);
		popSize.setFrequency(0.1);
		popSize.addModifier(new ScaleModifier());
		popSize.acceptValue();
		
		ConstantRecombination rec = new ConstantRecombination(0.02);
		rec.setFrequency(0.1);
		rec.addModifier(new ScaleModifier());

		ExponentialPrior rhoPrior = new ExponentialPrior(rec, 100);
		likelihoods.add(rhoPrior);
				
		CoalescentLikelihood coalLikelihood = new CoalescentLikelihood(popSize, rec, arg);
		likelihoods.add(coalLikelihood);
		
		parameters.add(popSize);
		parameters.add(rec);
		
		listeners.add(new LastARGLogger(arg, fileStem + "_lastARG.xml"));
		BreakpointDensity bpDensity = new BreakpointDensity(arg, sampleFrequency, 500, summaryStream);
		bpDensity.setWriteTempData(true, 100000, fileStem + "breakpoint_density_tmp.txt");
		bpDensity.setBurnin(runLength / 10);
		listeners.add(bpDensity);
		
		BreakpointLocation bpLocation =  new BreakpointLocation(arg, sampleFrequency, summaryStream);
		bpLocation.setWriteTempData(true, 100000, fileStem + "breakpoint_location_tmp.txt");
		bpLocation.setBurnin(runLength / 10);
		listeners.add(bpLocation);
		
		RootHeightDensity rhDensity = new RootHeightDensity(arg, sampleFrequency, 500, summaryStream);
		rhDensity.setWriteTempData(true, 100000, fileStem + "treeHeight_density_tmp.txt");
		rhDensity.setBurnin(runLength / 10);
		listeners.add(rhDensity);
	}
	
	
	public void run() {
		runMCMC(runLength, fileStem);
		
		System.out.println("Recomb. analysis on file " + filename + " has completed.\n\n");
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
			runLength = 10000000;
		
		Integer sampleFrequency = argOps.getIntegerOp("frequency");
		if (sampleFrequency == null)
			sampleFrequency = 5000;
		
		System.out.println("\n" + ApplicationHeader.getHeader() );
		System.out.println("\n\n Running input files with run length : " + runLength + " and sample frequency : " + sampleFrequency);
		
		for(String arg : args) {
			String filename = arg;
			if (arg.endsWith("fas") || arg.endsWith("phy") || arg.endsWith("nex")) {
				RecombAnalysis anal = new RecombAnalysis(filename, runLength, sampleFrequency);
				anal.run();	
			}
		}
		
	}
}
