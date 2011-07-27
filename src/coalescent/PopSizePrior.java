package coalescent;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
public class PopSizePrior extends LikelihoodComponent {
	
	double smoothness = 1.0;

	Gamma gamDist;
	Poisson cpPrior;
	
	double mean = 2000;
	double stdev = 2000;
	
	double changePointMean = 3.0; //Mean of poisson distribution describing expected number of change points
	
	
	PiecewiseLinearPopSize popSize;
	
	public PopSizePrior(PiecewiseLinearPopSize popSize) {
		this.popSize = popSize;
		addParameter(popSize);
		double variance = stdev*stdev;
		
		gamDist = new Gamma(mean*mean/variance, variance/mean, null); //Has mean 1.0, 
		cpPrior = new Poisson(changePointMean, null);
		
//		for(double x=0; x<10000; x+=200) {
//			System.out.println(x + "\t" + gamDist.pdf(x));
//		}
//		
//		System.exit(0);
	}
	
	@Override
	public Double computeProposedLikelihood() {
		
		//First find the mean value
		double sum = calculateArea();

		//return Math.log( cpPrior.pdf( popSize.getChangePointCount()) );
		
		return Math.log(cpPrior.pdf( popSize.getChangePointCount()));
	}

	private double calculateArea() {
		PiecewiseLinearFunction func = popSize.getFunction();
		double[] yVals = func.yVals;
		
		double sum = 1;
		for(int i=0; i<=func.changePoints; i++) {
			sum *= gamDist.pdf(yVals[i]);
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

		
		return currentLogLikelihood + "\t" + calculateArea();// + strB.toString();
	}
	
//	public static void main(String[] args) {
//		RandomSource.initialize();
//		PiecewiseLinearPopSize popsize = new PiecewiseLinearPopSize();
//		ARG arg = new ARG(new HashMap<String, String>(), new Newick("(one:1.0, two:1.0);"));
//		popsize.addModifier(new LinearFunctionMover(arg));
//		popsize.addModifier(new LinearFunctionAddRemove(arg));
//		
//		
//		PopSizePrior prior = new PopSizePrior(popsize);
//		
//		
//		List<Object> likes = new ArrayList<Object>();
//		likes.add(prior);
//		
//		List<Object> params = new ArrayList<Object>();
//		params.add(popsize);
//		
//		List<Object> listeners = new ArrayList<Object>();
//		
//		PrintStream cplog;
//		try {
//			cplog= new PrintStream(new FileOutputStream("cpLog.txt"));
//
//			StateLogger slogger = new StateLogger(cplog);
//			slogger.addStream(System.out);
//			listeners.add( slogger );
//		} catch (FileNotFoundException e) {
//
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		//listeners.add(new PopSizeLogger(100, 100, arg, popsize));
//
//		
//		MCMC mc = new MCMC(params, likes, listeners);
//		
//		mc.run(1000000);
//		
//		
//		System.out.println("Final pop size function :\n " + popsize.getFunction());
//	}
}
