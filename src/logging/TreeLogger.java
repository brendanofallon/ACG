package logging;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import arg.ARG;
import arg.CoalNode;
import arg.TreeUtils;
import arg.argIO.ARGParser;
import arg.argIO.ARGWriter;


import mcmc.MCMC;
import mcmc.MCMCListener;

/**
 * TreeLoggers listen for MCMC state acceptances and periodically emit trees to a file. 
 * @author brendan
 *
 */
public class TreeLogger implements MCMCListener {

	MCMC chain = null;
	ARG tree = null;
	ARGWriter argWriter;
	String name;
	
	int logFrequency = 1000;
	
	//If true, for every tree we log we store a count of its topology (stored as the newick string not
	//including branch lengths. We the frequency of the topologies to system.out when the chain is done. 
	boolean tabulateTopologyFrequencies = false;
	Map<String, Integer> topoMap;
	
	public TreeLogger(Map<String, String> attrs, ARG tree) throws IOException {
	
		this.tree = tree;
		
		String filename = attrs.get("filename");
		if (filename == null) {
			throw new IllegalArgumentException("A 'filename' attribute must be provided to the TreeLogger");
		}
		
		this.name = filename;
		argWriter = new ARGParser();
		
		String freq = attrs.get("frequency");
		if (freq != null) {
			try {
				logFrequency = Integer.parseInt(freq);
			}
			catch (NumberFormatException nfe) {
				throw new IllegalArgumentException("Could not parse log frequency for tree logger, got : " + freq);
			}
		}
		
		String countTopos = attrs.get("count.topos");
		if (countTopos != null) {
			tabulateTopologyFrequencies = Boolean.parseBoolean(countTopos);
		}
		
		if (tabulateTopologyFrequencies) {
			topoMap = new HashMap<String, Integer>();
		}
		
		System.out.println("TreeLogger is writing trees every " + logFrequency + " states to file : " + filename);
	}

	public void setChain(MCMC chain) {
		this.chain = chain;
	}
	
	
	@Override
	public void newState(int state) {
		if (state>0 && state%logFrequency==0) {
			if (tree.getRecombNodes().size()>20)
				logNewTree(state);
		}
	}

	private void logNewTree(int state) {
		
		File outFile = new File(name + "_" + state + ".xml");
		try {
			argWriter.writeARG(tree, outFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
//		CoalNode root = TreeUtils.createMarginalTree(tree, 0); 
//		String newick = TreeUtils.getNewick(root);
//		try {
//			writer.write(chain.getStatesProposed() + "\t" + newick + "\n");
//			writer.flush();
//		} catch (IOException e) {
//			System.out.println("Could not write tree to tree logger, reason : " + e.getMessage());
//		}

		//			if (topoMap != null) {
		//				String topo = tree.getNewick(false);
		//				Integer currentCount = topoMap.get(topo);
		//				if (currentCount ==  null) {
		//					topoMap.put(topo, 1);
		//				}
		//				else {
		//					topoMap.put(topo, currentCount+1);
		//				}
		//			}

	}
	
	

	@Override
	public void chainIsFinished() {
		//Nothing to do
		
//		try {
//			writer.write("\n");
//			writer.close();
//		} catch (IOException e) {
//			System.out.println("There was an error closing the tree logging writer : ");
//			System.out.println(e + "\n");
//		}
		
		//Topology logging stuff turned off until we figure out how to deal with ARGs
//		if (topoMap != null) {
//			System.out.println("Counts of observed topologies ");
//			for(String topo : topoMap.keySet() ) {
//				System.out.println(topo + " : " + topoMap.get(topo));
//			}
//			System.out.println("Total number of topologies : " + topoMap.size());
//		}
//		
//		int n = tree.getNumTips();
//		
//		Double totalCount = (factorial(2*n-3)/(Math.pow(2, n-2)*factorial(n-2) ));
//		System.out.println("Number of possible topologies : " + totalCount);
		
		
	}
	
	private Long factorial(int x) {
		if (x<=1) 
			return 1L;
		else {
			Long prod = 1L;
			for(int i=2; i<=x; i++) {
				prod *= i;
			}
			return prod;
		}
	}

	@Override
	public void setMCMC(MCMC chain) {
		this.chain = chain;
	}


}
