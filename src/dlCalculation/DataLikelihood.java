package dlCalculation;

import gnu.trove.list.array.TIntArrayList;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import parameter.InvalidParameterValueException;
import parameter.Parameter;
import mcmc.MCMC;

import arg.CoalNode;
import arg.SiteRange;
import arg.ARG;
import arg.ARGNode;
import arg.SiteRangeList;
import arg.TipNode;
import arg.TreeUtils;
import arg.argIO.ARGParser;

import sequence.DNAUtils;
import sequence.DataMatrix;
import testing.Timer;

import component.LikelihoodComponent;
import dlCalculation.computeCore.CompressionCore2;
import dlCalculation.computeCore.ComputeCore;
import dlCalculation.computeCore.MultiRateCore;
import dlCalculation.siteRateModels.ConstantSiteRates;
import dlCalculation.siteRateModels.GammaSiteRates;
import dlCalculation.siteRateModels.SiteRateModel;
import dlCalculation.substitutionModels.F84Matrix;
import dlCalculation.substitutionModels.MutationModel;

/**
 * Given a mutation model and a tree, this calculates the probability of observing the data given the tree.
 * 
 * @author brendan
 *
 */
public class DataLikelihood extends LikelihoodComponent {

	ARG arg;
	DataMatrix dataMatrix;
	MCMC chain = null;
	
	//Number of times calculateProposedLikelihood is called
	double calls = 0;
	
	//Field for writing the 'verification log', which emits trees and their likelihoods to a 
	//stream, so they can be verified with external tools
	private final boolean writeVerificationLog = false;
	private final boolean writeARGs = false;
	//Call to start log on
	private final int verificationStart = 0;
	//Frequency with which we write verification log stuff, if at all
	private final int verificationFrequency = 10;
	private BufferedWriter vLog;
	
	//Debugging, used to write comments into the verification file
	String modStr = "";
	String prevMod = "";
	
	MutationModel mutationModel = null;
	
	ComputeCore computeCore;

		
	//Set to true when a parameter has changed, and false when we call accept/reject
	//Let us know if there's an active modification
	private boolean modified = true;
	
	//Model of how evolutionary rate changes over sites
	SiteRateModel siteRateModel = null;
	
	//private SortedSiteRangeList rootRanges = null; 
	
	private SortedSiteRangeList currentRootRanges = null;
	private SortedSiteRangeList proposedRootRanges = null;
	private SortedSiteRangeList activeRootRanges;
	
	public DataLikelihood() {
		this(new HashMap<String, String>());
	}
	
	public DataLikelihood(Map<String, String> attributes) {	
		super(attributes);
		if (writeVerificationLog) {
			File logFile = new File("treeDLlog.txt");
			try {
				vLog = new BufferedWriter(new FileWriter(logFile));
			} catch (IOException e) {
				System.out.println("Could not create file for tree dl log writing");
				System.exit(1);
			}	
		}
	}
	
	/**
	 * Create a new data likelihood calculator with a siteratemodel with one class with a rate of 1
	 * @param attributes
	 * @param mutationModel
	 * @param tree
	 * @param data
	 */
	public DataLikelihood(Map<String, String> attributes, MutationModel mutationModel, ARG tree) {
		this(attributes, mutationModel, null, tree);
	}
	
	/**
	 * XML-approved constructor. 
	 * @param attributes
	 * @param tree
	 * @param data
	 */
	public DataLikelihood(Map<String, String> attributes, MutationModel mutationModel, SiteRateModel siteRateModel, ARG tree) {
		this(attributes);
		
		this.mutationModel = mutationModel;
		addParameter(mutationModel);
		
		this.siteRateModel = siteRateModel;
		if (siteRateModel == null) {
			//If no explicit model is supplied we assume there's a single rate model with rate = 1.0
			this.siteRateModel = new ConstantSiteRates(new double[]{1.0}, new double[]{1.0});
		}
		else {
			addParameter(siteRateModel);
		}
		

		
		setDataMatrix(tree.getDataMatrix());

		setTree(tree);

		//Initialize all SiteRange information
		List<ARGNode> nodes = arg.getInternalNodes();
		Collections.sort(nodes, arg.getNodeHeightComparator());
		
		//Traverse all coal nodes in ascending height order and cause them to tell the
		//computeCore to calculate data likelihoods for all ranges associated with the coal node
		for(int i=0; i<nodes.size(); i++) {
			nodes.get(i).computeProposedRanges(true);
		}
		
		proposedLogLikelihood = computeProposedLikelihood();
		
		stateAccepted();
		tree.acceptValue();
	}
	
	public void setChain(MCMC chain) {
		this.chain = chain;
	}
	
	/**
	 * For debugging : This sets a string that can be emitted with a tree when trees are written to a verification
	 * file, which can be useful when trying to figure out what happened when a tree has a bad DL
	 * @param mod
	 */
	public void setModStr(String mod) {
		prevMod = modStr;
		modStr = mod;
	}
	
	/**
	 * Forces a full recomputation of the likelihood next time by setting all DL known flags to false
	 * and recollecting all subtree patterns. 
	 */
	public void forceRecomputeLikelihood() {
		recalculateLikelihood = true;
		((MultiRateCore)computeCore).proposeAll();
	}
	
	/**
	 * Obtain the data matrix this DL is using
	 * @return
	 */
	public DataMatrix getDataMatrix() {
		return dataMatrix;
	}
	
	/**
	 * Sets the given tree to be the tree we use to calculate the data likelihood.
	 * @param newTree
	 */
	public void setTree(ARG newTree) {
		if (this.arg != null)
			removeParameter(arg);
		
		this.arg = newTree;
		currentRootRanges = new SortedSiteRangeList(arg.getSiteCount());
		proposedRootRanges = new SortedSiteRangeList(arg.getSiteCount());
		activeRootRanges = proposedRootRanges;
		
		computeCore = new MultiRateCore(arg.getTips().size(), mutationModel, newTree.getDataMatrix(), siteRateModel);
		//computeCore = new FloatCore(arg.getTips().size(), mutationModel, dataMatrix, siteRateModel);
		
		arg.setComputeCore(computeCore);
		addParameter(arg);
	}

	public void setDataMatrix(DataMatrix dm) {
		this.dataMatrix = dm;
	}
	
	public void setStationaries(double[] stat) {
		throw new IllegalArgumentException("You must now set stationaries on the compute core");
	}


	@Override
	public Double computeProposedLikelihood() {
		calls++;
		
		if (! arg.getRequiredDLRecalc()) {
			return currentLogLikelihood;
		}
		
		double logProb = 0;
		
		if (chain != null && writeVerificationLog && writeARGs && chain.getCurrentState() >= verificationStart && chain.getCurrentState() % verificationFrequency == 0) {
			ARGParser writer = new ARGParser();
			File argFile = new File("verify_arg_" + chain.getCurrentState() + ".xml");
			try {
				writer.writeARG(arg, argFile);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		


		Timer.startTimer("DL");
				
		List<CoalNode> cNodes = arg.getDLCoalNodes(); //We only need the nodes that have coalescing sites
		Collections.sort(cNodes, arg.getNodeHeightComparator());
		
		//Find root partials.. nodes are already sorted, so traverse from back (root) toward tips
		//collecting sites that coalesce, until we finally have them all.
		
		boolean complete = false;
		activeRootRanges = proposedRootRanges;
		proposedRootRanges.clear();
		for(int i = cNodes.size()-1; i>=0 && (!complete); i--) {
			SiteRangeList coalRanges = cNodes.get(i).getCoalescingSites();
			for(int j=0; j<coalRanges.size(); j++) {
				complete = proposedRootRanges.add( coalRanges.getRangeBegin(j), coalRanges.getRangeEnd(j), coalRanges.getRefID(j) );

				if (complete) {
					break;
				}
			}				
		}

		//Now we tell the compute core which nodes are the roots
		computeCore.clearRootRanges();
		for(SiteRange rootRange : proposedRootRanges.getRanges()) {
			computeCore.setRootRangeNode( rootRange.getRefNodeID() );
		}
		
		//Traverse all coal nodes in ascending height order, put their ids into the
		//a list, and tell the compute core to compute partials for them. The
		//core is smart enough to know which ones need updating
		TIntArrayList computeRefs = getComputeNodeIDs( cNodes );		
		computeCore.computePartialsList( computeRefs );

		
		//Finally, tell compute core to compute root DL across the given ranges
		logProb = computeCore.computeRootLogDL();
		

		Timer.stopTimer("DL");
		
		if (chain != null && writeVerificationLog && chain.getCurrentState() > verificationStart && chain.getCurrentState() % verificationFrequency == 0) {
			writeVerificationLogLine(logProb);
		}
		
		return logProb;
	}
	
	
	/**
	 * Writes an addition line to the verification log, which is can be used by 
	 * outside tools (phylip) to double-check the calculated likelihood
	 */
	public void writeVerificationLogLine(double logDL) {
		double kappa = ((F84Matrix)mutationModel).getKappa();
		double[] stats = mutationModel.getStationaries();
		double aFreq = stats[DNAUtils.A];
		double cFreq = stats[DNAUtils.C];
		double gFreq = stats[DNAUtils.G];
		double tFreq = stats[DNAUtils.T];
		String freqStr = " a=" + aFreq + " c=" + cFreq + " g=" + gFreq + " t=" + tFreq;
		
		DecimalFormat formatter = new DecimalFormat("##.#######");
		String rateStr = null;
		double alpha = ((GammaSiteRates)siteRateModel).getAlpha();
		StringBuilder rateBldr = new StringBuilder(" rates=" + siteRateModel.getCategoryCount() + " alph=" + alpha);
		for(int i=0; i<siteRateModel.getCategoryCount(); i++) {
			rateBldr.append(" rate" + i + "=" + formatter.format(siteRateModel.getRateForCategory(i)) + " prob" + i + "=" + siteRateModel.getProbForCategory(i));
		}
		rateStr = rateBldr.toString();
	
		
		if (vLog != null && calls > 0) {
			try {
				double recomputedDL = 0;
				
				//for(int category = 0; category<siteRateModel.getCategoryCount(); category++) {
				//	String rateStr = " catry=" + category + " rate=" + siteRateModel.getRateForCategory(category) + " ";
					if (arg.getRecombNodes().size()==0) {
						String newick = TreeUtils.getNewick( TreeUtils.createMarginalTree(arg, 0)); //Creates marginal newick at site 0
						double dl = computeCore.computeRootLogDLForRange(0, arg.getSiteCount());
						recomputedDL += dl;
						vLog.write(dl + "\t[ state=" + chain.getCurrentState() + rateStr + freqStr + " kappa=" + kappa + " start=0 end=" + arg.getSiteCount() + " ]\t" + newick + "\n");
						vLog.flush();

						//System.out.println("Verification range " + 0 + " .. " +  dataMatrix.getTotalColumnCount() + " : " + dl);
						return;
					}

					Integer[] internalBPs = arg.collectBreakPoints();
					Integer[] breakpoints = new Integer[ internalBPs.length + 1];
					breakpoints[0] = 0;
					for(int i=1; i<breakpoints.length; i++)
						breakpoints[i] = internalBPs[i-1];
					Arrays.sort(breakpoints);


					for(int i=0; i<breakpoints.length-1; i++) {
						if (breakpoints[i] == breakpoints[i+1]) //Skip redundant breakpoints
							continue;
						String newick = TreeUtils.getNewick( TreeUtils.createMarginalTree(arg, breakpoints[i])); //Creates marginal newick at site 0
						double dl = computeCore.computeRootLogDLForRange(breakpoints[i], breakpoints[i+1]);
						vLog.write(dl + "\t[ state=" + chain.getCurrentState() + rateStr + freqStr + " kappa=" + kappa + " start=" + breakpoints[i] + " end=" + breakpoints[i+1] + " ]\t" + newick + "\n");
						recomputedDL += dl;

						//System.out.println("Verification range " + breakpoints[i] + " .. " +  breakpoints[i+1] + " : " + dl);
						vLog.flush();
					}

					int lastSite = arg.getSiteCount();
					String newick = TreeUtils.getNewick( TreeUtils.createMarginalTree(arg, lastSite-1)); //Creates marginal newick at site 0
					double dl = computeCore.computeRootLogDLForRange(breakpoints[ breakpoints.length-1], lastSite);
					vLog.write(dl + "\t[ state=" + chain.getCurrentState() + rateStr + freqStr + " kappa=" + kappa + " start=" + breakpoints[ breakpoints.length-1] + " end=" + lastSite + " ]\t" + newick + "\n");
					vLog.flush();

					//System.out.println("Verification range " + breakpoints[ breakpoints.length-1] + " .. " +  lastSite + " : " + dl);
					recomputedDL += dl;
					//System.out.println("State: " + MCMC.getCurrentState() + " total DL: " + recomputedDL);
				//}

				if (Math.abs(recomputedDL - logDL) > 1e-6) {
					throw new IllegalStateException("Uh-oh, verification DLs did not add up to proposed DL. \n Verification DL sum : " + recomputedDL + " prop DL : " + logDL);

				}
			} catch (IOException e) {
				System.out.println("Error writing tree dl log line..");
				System.exit(1);
			}
			
		}
	}
	
	
	/**
	 * Overridden here so we can call accept on the computeCore
	 */
	public void stateAccepted() {
		if (modified) { //It's important that we don't call accept on the compute core if we haven't modified anything
			currentLogLikelihood = proposedLogLikelihood;
			computeCore.accept();
			
			//Another periodically occurring validity check...propose all nodes and recompute the full DL to make
			//sure it matches the DL we just accepted
			if (calls < 10000 || calls % 10000 == 0) {
				List<CoalNode> cNodes = arg.getDLCoalNodes(); //We only need the nodes that have coalescing sites
				//Propose all nodes
				TIntArrayList computeRefs = getComputeNodeIDs( cNodes );
				for(int i=0; i<computeRefs.size(); i++) {
					computeCore.proposeNode(computeRefs.get(i), computeCore.getHeightForNode( computeRefs.get(i)));
				}
				
				double logProb = computeProposedLikelihood();
				
				//And see if it matches the likelihood we just accepted...
				if (logProb != currentLogLikelihood) {
					throw new IllegalStateException("Likelihood verification did not match, accepted likelihood=" + currentLogLikelihood + " recomputed: " + logProb);
				}
			}
			
			modified = false;
			
			//Stuff below not actually used now...
			if (activeRootRanges == proposedRootRanges) {
				SortedSiteRangeList tmp = proposedRootRanges;
				proposedRootRanges = currentRootRanges;
				currentRootRanges = tmp;
				activeRootRanges = currentRootRanges;
			}
		}
	}
	
	/**
	 * Reject the proposed state, and set the proposed likelihood equal to the current likelihood (does this matter?) 
	 */

	public void stateRejected() {
		if (modified) { //Don't call reject on the compute core if we haven't modified
			proposedLogLikelihood = currentLogLikelihood;
			computeCore.reject();
			modified = false;
			
			activeRootRanges = currentRootRanges;
		}
	}

	
	
	/**
	 * Returns a list of all of the compute node ids used by the 
	 * given list of nodes. 
	 * @return
	 */
	private TIntArrayList getComputeNodeIDs(List<CoalNode> cNodes) {
		TIntArrayList allIDs = new TIntArrayList(2*cNodes.size());
		for(CoalNode cNode : cNodes) {
			if (cNode.getCoalescingSites() != null) {
				for(int i=0; i<cNode.getCoalescingSites().size(); i++) {
					allIDs.add(cNode.getCoalescingSites().getRefID(i));
				}
			}
		}
		
		return allIDs;
	}

	

	
	public void parameterChanged(Parameter<?> source) {
		recalculateLikelihood = true;
		modified = true;
		
		//Forces recalculation of all partials, but does not change structure / cause
		//recollecting of patterns
		//TODO: Seems weird that we will now re-collect all dl nodes, sort them, etc. on the
		//next call to getProposedLikelihood(), when they haven't changed. Happens very rarely though, compared to ARG changes
		if (source == mutationModel || source == siteRateModel) {
			//System.out.println("Updating all matrices...");
			computeCore.setUpdateAllMatrices();
			arg.setDLRecalc();
		}
	}
	
	
	public ARG getTree() {
		return arg;
	}


	@Override
	public String getLogHeader() {
		//return "treeLikelihood";
		return "treeLikelihood";
	}
	
	public String getLogString() {
		//return "" + currentLogLikelihood;
		return "" + currentLogLikelihood;
	}


}
