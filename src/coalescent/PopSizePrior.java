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


package coalescent;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import priors.AbstractPrior;

import logging.StateLogger;
import math.RandomSource;
import mcmc.MCMC;
import modifier.LinearFunctionAddRemove;
import modifier.LinearFunctionMover;
import cern.jet.random.Gamma;
import cern.jet.random.Poisson;
import arg.ARG;
import arg.Newick;
import component.LikelihoodComponent;

/**
 * An experimental prior for fluctuating population size models
 * @author brendano
 *
 */
public class PopSizePrior extends AbstractPrior {
	
	double smoothness = 1.0;

	Gamma gamDist;
	Poisson cpPrior;
	
	double mean = 1000;
	double stdev = 750;
	
	double changePointMean = 3.0; //Mean of poisson distribution describing expected number of change points
	
	PiecewiseLinearPopSize popSize = null;

	public PopSizePrior(Map<String, String> attrs, PiecewiseLinearPopSize popSize) {
		super(attrs, popSize);

		this.popSize = popSize;
		double variance = stdev*stdev;
		
		
		gamDist = new Gamma(mean*mean/variance, variance/mean, null); //Has mean 1.0, 
		cpPrior = new Poisson(changePointMean, null);
		
		for(double x=0; x<mean*3; x+=(mean/200.0)) {
			System.out.println(x + "\t" + gamDist.pdf(x));
		}
//		try {
//			System.in.read();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		proposedLogLikelihood = computeProposedLikelihood();
		this.stateAccepted();
	}
	
	public PopSizePrior(PiecewiseLinearPopSize popSize) {
		this(new HashMap<String, String>(), popSize);
	}
	
	@Override
	public Double computeProposedLikelihood() {
		if (popSize == null)
			return Double.NaN;
		
		//First find the mean value
		double sum = calculateLogArea();

		return sum;
		
		//return Math.log(cpPrior.pdf( popSize.getChangePointCount()));
	} 

	private double calculateLogArea() {
		PiecewiseLinearFunction func = popSize.getFunction();
		double[] yVals = func.yVals;
		
		double sum = 0;
		for(int i=0; i<=func.changePoints; i++) {
			sum += Math.log( gamDist.pdf(yVals[i]) );
		}
		
		return sum;
	}
	
	@Override
	public String getLogHeader() {

		PiecewiseLinearFunction func = popSize.getFunction();
		StringBuilder strB = new StringBuilder();
		for(int i=0; i<=func.changePoints; i++)
			strB.append("\t popSize" + i);
		for(int i=0; i<=func.changePoints; i++)
			strB.append("\t popTime" + i);
		
		return "pop.size.prior\t sum ";// + strB.toString();
	}

	public String getLogString() {
		PiecewiseLinearFunction func = popSize.getFunction();
		StringBuilder strB = new StringBuilder();
		for(int i=0; i<=func.changePoints; i++)
			strB.append("\t " + func.yVals[i]);
		for(int i=0; i<=func.changePoints; i++)
			strB.append("\t " + func.xVals[i]);

		
		return currentLogLikelihood + "\t" + calculateLogArea();// + strB.toString();
	}
	
	public static void main(String[] args) {
		RandomSource.initialize();
		
		
		Map<String, String> argAttrs = new HashMap<String, String>();
		argAttrs.put("filename", "test/bneck/20tips_growth_mu2e5_bneck_L20K_2_#1.xml");
		ARG arg = new ARG(argAttrs);
		
		PiecewiseLinearPopSize popsize = new PiecewiseLinearPopSize(arg);
		popsize.addModifier(new LinearFunctionMover(arg));
		
		RecombinationParameter recParam = new ConstantRecombination(100.0);
		CoalescentLikelihood coalLike = new CoalescentLikelihood(popsize, recParam, arg);
		
		//popsize.addModifier(new LinearFunctionAddRemove(arg));
		
		
		PopSizePrior prior = new PopSizePrior(popsize);
		
		
		List<Object> likes = new ArrayList<Object>();
		likes.add(coalLike);
		likes.add(prior);
		
		List<Object> params = new ArrayList<Object>();
		params.add(popsize);
		
		List<Object> listeners = new ArrayList<Object>();
		
		String filename = "cplog.log";
		Map<String, String> logAttrs = new HashMap<String, String>();
		logAttrs.put("filename", filename);
		StateLogger slogger = new StateLogger(logAttrs);
		slogger.addStream(System.out);
		listeners.add( slogger );
		
		listeners.add(new PopSizeLogger(100000, 100, arg, popsize));

		Map<String, String> mcAttrs = new HashMap<String, String>();
		mcAttrs.put("length", "5000000");		
		MCMC mc = new MCMC(mcAttrs, params, likes, listeners);

		mc.run();
		
		
		System.out.println("Final pop size function :\n " + popsize.getFunction());
	}
}
