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


package tools;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import sequence.BasicSequenceAlignment;
import sequence.Sequence;

import math.RandomSource;

import arg.ARG;
import arg.ARGNode;
import arg.CoalNode;
import arg.RecombNode;
import arg.TreeUtils;
import arg.argIO.ARGParseException;
import arg.argIO.ARGParser;

/**
 * Packages a few small utilities that can be used from the command line
 * @author brendan
 *
 */
public class ARGUtils {

	
	public static String[] extractAllTrees(String filename) {
		ARGParser parser = new ARGParser();
		ARG arg = null;
		try {
			arg = parser.readARG(new File(filename));
		}
		catch (ARGParseException ex) {
			System.err.println("Error reading arg : " + ex);
			return new String[0];
		} catch (IOException e) {
			System.err.println("Error reading arg : " + e);
		}
		
		List<String> strs = new ArrayList<String>();
		Integer[] breakpoints = arg.collectBreakPoints();
		Arrays.sort(breakpoints);
		
		int thisBP = 0;
		for(int i=0; i<breakpoints.length; i++) {
			if (breakpoints[i] > thisBP) {
				CoalNode root = TreeUtils.createMarginalTree(arg, thisBP);
				String newick = TreeUtils.getNewick(root);
				int segLength = breakpoints[i] - thisBP;
				strs.add("[" + segLength + "] " + newick);
				//strs.add("" + thisBP + " - " + breakpoints[i] + " : " + newick);
			}
			thisBP = breakpoints[i];
		}
		
		//There's no breakpoint associated with the last region, so just do it manually here
		int lastRegion = arg.getSiteCount()-1;
		CoalNode root = TreeUtils.createMarginalTree(arg, lastRegion);
		String newick = TreeUtils.getNewick(root);
		strs.add("[" + (lastRegion-thisBP) + "] " + newick);
		//strs.add(thisBP + " - " + lastRegion + " : " + newick);
		
		String[] strArr = new String[strs.size()];
		int i=0;
		for(String str : strs) {
			strArr[i] = str;
			i++;
		}
		return strArr;
	}
	
	/**
	 * Read the file and emit the single tree at the given site. 
	 * @param filename
	 * @param site
	 * @return
	 */
	public static String extractTree(String filename, int site) {
		ARGParser parser = new ARGParser();
		try {
			ARG arg = parser.readARG(new File(filename));
			String newick = TreeUtils.getMarginalNewickTree(arg, site);
			return newick;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ARGParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Read the file and emit the single tree at the given site. 
	 * @param filename
	 * @param site
	 * @return
	 */
	public static String getScaledARG(String filename, double factor) {
		ARGParser parser = new ARGParser();
		try {
			ARG arg = parser.readARG(new File(filename));
			for(ARGNode node : arg.getAllNodes()) {
				node.proposeHeight(node.getHeight() * factor);
			}
			arg.acceptValue();
			return parser.argToXml(arg);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ARGParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public static String getScaledTree(String filename, double factor) {
		ARGParser parser = new ARGParser();
		try {
			ARG arg = parser.readARG(new File(filename));
			for(ARGNode node : arg.getAllNodes()) {
				node.proposeHeight(node.getHeight() * factor);
			}
			arg.acceptValue();
			return TreeUtils.getMarginalNewickTree(arg, 0);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ARGParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Emit tmrca of marginal trees across sequence
	 * @param filename
	 * @param bins
	 */
	public static void emitTMRCA(String filename, int bins) {
		
		ARGParser parser = new ARGParser();
		try {
			ARG arg = parser.readARG(new File(filename));
			
			int binStep = arg.getSiteCount() / bins;
			for(int site = 0; site < arg.getSiteCount(); site+= binStep) {
				CoalNode root = TreeUtils.createMarginalTree(arg, site);
				System.out.println(site + "\t" + root.getHeight());
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ARGParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public static String getHelpString() {
		StringBuilder str = new StringBuilder();
		str.append("ARGUtils v0.01 : Some utilities read extracting information from ARG text files \n");
		str.append(" Author: Brendan O'Fallon (brendano@u.washington.edu) \n");
		str.append("  For now, all ARGs must be in GraphML-like format \n");
		str.append("  usage \n");
		str.append(" --extract-trees [argfile.xml]         \n\t Emit all marginal trees in newick format \n");
		str.append(" --extract-tree  [argfile.xml] [site]  \n\t Emit marginal tree at given site \n");
		str.append(" --emit-bps 	 [argfile.xml]    \n\t Emit the locations of all recombination breakpoints \n");
		str.append(" --consense 	 [treesfile.trees]  \n\t Build consensus tree from tree log file \n");
		str.append(" --emit-tmrca    [argfile.xml] 		  \n\t Emit root heights across sequence length \n");
		str.append(" --scale [factor] [argfile.xml | treefile.tre] 		   \n\t Emit tree with branches scaled by factor \n");
		str.append(" --genarg [tips] [theta] [rho] [sites]	   \n\t Generate random ARG with given parameters \n");
		str.append(" --convert [inputFile.fas | inputFile.phy]  \n\t Convert fasta or phylip file to ACG alignment block \n");

		return str.toString();
	}
	
	public static void main(String[] args) {
		
		if (args.length == 0 || args[0].equals("-h") || args[0].startsWith("-help") || args[0].startsWith("--help")) {
			System.out.println( getHelpString() );
			return;
		}
		
		
		if (args[0].equals("--extract-tree")) {
			if (args.length != 3) {
				System.err.println("Please supply the name of the file and the site number to extract tree");
				return;
			}
			String filename = args[1];
			int site = Integer.parseInt(args[2]);
			String newick = extractTree(filename, site);
			System.out.println(newick);
			return;
		}
		
		
		if (args[0].equals("--extract-trees")) {
			if (args.length != 2) {
				System.err.println("Please supply the name of the file to extract trees");
				return;
			}
			String filename = args[1];
			String[] trees = extractAllTrees(filename);
			for(int i=0; i<trees.length; i++) {
				System.out.println(trees[i]);
			}
			return;
		}
		
		if (args[0].equals("--consense") || args[0].equals("--consensus")) {
			if (args.length < 2) {
				System.err.println("Please supply the name of the file to build consensus tree from");
				return;
			}
			int burnin = 0;
			if (args.length>=4 && args[2].equals("-b")) {
				try {
					burnin = Integer.parseInt(args[3]);
				}
				catch(NumberFormatException nfe) {
					System.out.println("Could not parse an integer for burnin, got :" + args[3]);
					System.exit(0);
				}
			}
			boolean writeAnnos = true;
			if (args.length==5 && args[4].equals("--no-annotations")) {
				writeAnnos = false;
			}
			
			String filename = args[1];
			try {
				TreeReader reader = new TreeReader(new File(filename));
				reader.setStripAnnotations(true);
				reader.setBurnin(burnin);
				ConsensusTreeBuilder builder = new ConsensusTreeBuilder();
				Tree tree = builder.buildConsensus(reader);
				String newick = tree.getNewick(writeAnnos);
				System.out.println(newick + "\n");
			} catch (IOException e) {
				System.out.println("Error building consensus tree : \n");
				e.printStackTrace();
			}
			
			return;
		}
		
		if (args[0].equals("--emit-tmrca")) {
			if (args.length < 2) {
				System.err.println("Please supply the name of the file to extract root heights");
				return;
			}
			int bins = 100;
			if (args.length==3) {
				try {
					bins = Integer.parseInt(args[2]);
				}
				catch (NumberFormatException nfe) {
					System.err.println("Could not parse an integer from : " + args[2]);
					return;
				}
			}
			String filename = args[1];
			emitTMRCA(filename, bins);
			return;
		}
		
		if (args[0].equals("--emit-bps")) {
			if (args.length < 2) {
				System.err.println("Please supply the name of the file to extract breakpionts");
				return;
			}
			
			String filename = args[1];
			ARGParser parser = new ARGParser();
			try {
				ARG arg = parser.readARG(new File(filename));
				List<RecombNode> rNodes = arg.getRecombNodes();
				System.out.println("Number of breakpoints : " + rNodes.size());
				System.out.println("Site \t Height");
				for(RecombNode node : rNodes) {
					System.out.println(node.getInteriorBP() + "\t" + node.getHeight());
				}
				
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ARGParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return;
		}
		
		
		if (args[0].equals("--scale") || args[0].equals("-s")) {
			if (args.length < 3) {
				System.err.println("Please supply the scale value and name of the file in which to scale branch lengths");
				return;
			}
			
			double scaleFactor = Double.parseDouble(args[1]);
			
			if (args[2].endsWith("xml")) {
				String xmlStr = getScaledARG(args[2], scaleFactor);
				System.out.println(xmlStr);
			}
			if (args[2].endsWith("tre")) {
				
				ARG arg = TreeUtils.buildARGFromNewick(new File(args[2]) );
				for(ARGNode node : arg.getAllNodes()) {
					node.proposeHeight(node.getHeight()*scaleFactor);
				}
				arg.acceptValue();
				String newick = TreeUtils.getMarginalNewickTree(arg, 0);
				System.out.println(newick);
			}

			
			return;
		}
		
		if (args[0].equals("--genarg")) {
			RandomSource.initializeSilently();
			
			if (args.length != 5) {
				System.out.println("--genarg requires four options [tips] [theta] [rho] [sites]");
				System.exit(0);
			}
			
			int tips = Integer.parseInt(args[1]);
			double theta = Double.parseDouble(args[2]);
			double rho = Double.parseDouble(args[3]);
			int sites = Integer.parseInt(args[4]);
			
			ARG arg = TreeUtils.generateRandomARG(tips, theta, rho, sites);
			String xmlstr = (new ARGParser()).argToXml(arg);
			System.out.println(xmlstr);
			return;
			
		}
		
		
		if (args[0].equals("--convert")) {
			if (args.length == 1) {
				System.out.println("Please enter the name of at least one file to convert");
				System.exit(0);
			}
			
			for(int i=1; i<args.length; i++) {
				BasicSequenceAlignment aln = new BasicSequenceAlignment(args[i]);
				
				System.out.println("<alignment" + (i) + " class=\"sequence.Alignment\">");
				System.out.println("\t<sequences" + i + " class=\"list\">");
				for(Sequence seq : aln.getSequences()) {
					String seqName = seq.getLabel();
					System.out.println("\t\t<" + seqName + " class=\"sequence.Sequence\">");
					System.out.println("\t\t\t" + seq.getSequence() );
					System.out.println("\t\t</" + seqName + ">");
				}
				System.out.println("\t</sequences" + i +">");
				System.out.println("</alignment" + i +">");
			}
			
			return;
		}
		
		System.err.println("Unknown option : " + args[0]);
		
	}
}
