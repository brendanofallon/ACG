package tools;

import java.io.File;
import java.io.IOException;

import parameter.InvalidParameterValueException;

import math.Histogram;
import math.RandomSource;
import modifier.IllegalModificationException;
import modifier.ModificationImpossibleException;
import modifier.SubtreeSwap;
import arg.ARG;
import arg.TreeUtils;
import arg.argIO.ARGParser;

/**
 * A class to perform backward-time simulation for args
 * @author brendano
 *
 */
public class ARGSimulator {

	public static void generateHistograms() {
		double N = 1;
		double rho = 2.0 / (2.0);
		int trials = 100000;
		int tips = 10;

		Histogram rootHeightHistoT = new Histogram(0, 10, 100);		
		Histogram bpHisto = new Histogram(0, 35, 35);
		
		for(int i=0; i<trials; i++) {
			ARG arg = TreeUtils.generateRandomARG(tips,	 N, rho, 1000);
			int bpCount = arg.getRecombNodes().size();
			bpHisto.addValue((double)bpCount);

			rootHeightHistoT.addValue(arg.getMaxHeight());
		}
		
		
		System.out.println("Number of breakpoints");
		System.out.println(bpHisto);		
		
		System.out.println("Root height overall");
		System.out.println(rootHeightHistoT);
		System.out.println("Root height mean: " + rootHeightHistoT.getMean());
	}
	
	public static void main(String[] args) {
		RandomSource.initialize();
		
		ARGSimulator.generateHistograms();
		
//		ARGParser writer = new ARGParser();
//		
//		ARG arg = new ARG(1000, TreeUtils.generateRandomARG(4, 1000, 0.75, 1000) );
//		while(arg.getRecombNodes().size() != 3) {
//			 arg = new ARG(1000, TreeUtils.generateRandomARG(4, 1000, 0.75, 1000) );
//		}
//		
//		
//		try {
//			writer.writeARG(arg, new File("annoARGafter.xml"));
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		

	}
}
