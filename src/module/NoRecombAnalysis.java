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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import coalescent.CoalescentLikelihood;
import coalescent.ConstantPopSize;
import coalescent.ConstantRecombination;

import logging.BreakpointDensity;
import logging.LastARGLogger;
import logging.RootHeightDensity;
import math.RandomSource;
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
import modifier.WideSwap;
import parameter.DoubleParameter;
import parameter.Parameter;
import priors.ExponentialPrior;
import sequence.Alignment;
import sequence.DataMatrix;
import arg.ARG;
import dlCalculation.DataLikelihood;
import dlCalculation.substitutionModels.BaseFrequencies;
import dlCalculation.substitutionModels.F84Matrix;
import dlCalculation.substitutionModels.MutationModel;

/**
 * A class that runs a Bayesian genealogical analysis on a single locus with no recombination
 * @author brendano
 *
 */
public class NoRecombAnalysis extends AbstractModule {

	String filename;
	String fileStem;
	int runLength;

	
	public NoRecombAnalysis(String inputFilename, int runLength, int frequency) {
		if (RandomSource.getEngine() == null)
			RandomSource.initialize();
		this.filename = inputFilename;
		this.runLength = runLength;
		this.sampleFrequency = frequency;
		
		int lastPos = filename.lastIndexOf(".");
		String filesep = System.getProperty("file.separator");
		int firstPos = filename.lastIndexOf( filesep );
		if (firstPos < 0)
			firstPos = 0;
		else
			firstPos++;
		fileStem = filename.substring(firstPos, lastPos > 0 ? lastPos : filename.length() );
		
		try {
			initializeComponents();
		}
		catch (Exception ex) {
			System.err.println("Error initializing module with input file : " + inputFilename + "\n\t Cause : " + ex);
			ex.printStackTrace();
		}
	}

	protected void initializeComponents() {
		File file = new File(filename);

		Alignment alignment = new Alignment(file);

		Map<String, String> argAttrs = new HashMap<String, String>();
		ARG arg = new ARG(argAttrs, alignment);
		parameters.add(arg);
	
		
		Modifier<ARG> swapModifier = new SubtreeSwap();
		Modifier<ARG> heightModifier = new NodeHeightModifier();
		Modifier<ARG> rootHeightMod = new RootHeightModifier();
		Modifier<ARG> wideSwapper = new WideSwap();
	
		arg.addModifier(swapModifier);
		arg.addModifier(heightModifier);
		arg.addModifier(rootHeightMod);
		arg.addModifier(wideSwapper);
		
		
		
		DoubleParameter kappa = new DoubleParameter(2.0, "kappa", "kappa", 0, 1000);
		kappa.addModifier(new SimpleModifier());
		kappa.setFrequency(0.1);
		parameters.add(kappa);
		
		BaseFrequencies freqs = new BaseFrequencies(new double[]{0.25, 0.25, 0.25, 0.25});
		freqs.addModifier(new DirichletModifier());
		freqs.setFrequency(0.1);
		parameters.add(freqs);

		MutationModel mutationModel = new F84Matrix(freqs , kappa);
		
		Map<String, String> dlAttrs = new HashMap<String, String>();

		DataLikelihood dl = new DataLikelihood(dlAttrs, mutationModel, null, arg);

		likelihoods.add(dl);
	
		
		ConstantPopSize popSize = new ConstantPopSize(0.01);
		ConstantRecombination rec = new ConstantRecombination(0.0);
		popSize.addModifier(new ScaleModifier());
		popSize.acceptValue();
				
		CoalescentLikelihood coalLikelihood = new CoalescentLikelihood(popSize, rec, arg);
		likelihoods.add(coalLikelihood);
		
		parameters.add(popSize);
		
		listeners.add(new LastARGLogger(arg, fileStem + "lastARG.xml"));
	}

	public void run() {
		runMCMC(runLength, fileStem);
		
		System.out.println("No recomb analysis on file " + filename + " has completed.");
	}
	
	
	public static void main(String[] args) {
		
		String filename = "/Users/brendano/workspace/ACG_exp/test/no_recomb/50tips_N1K_mu1e5_L25K_2.fas";
		NoRecombAnalysis anal = new NoRecombAnalysis(filename, 2500000, 2000);
		anal.run();
	}
}
