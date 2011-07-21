package tools;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import arg.ARG;
import arg.ARGNode;
import arg.CoalNode;
import arg.RecombNode;
import arg.TipNode;
import arg.TreeUtils;
import arg.argIO.ARGParseException;
import arg.argIO.ARGParser;
import arg.argIO.ARGWriter;

/**
 * Writes the given arg as a series of newick-formatted trees preceded by a short string
 * describing to which range of sites the trees correspond.
 *  
 * @author brendan
 *
 */
public class ARGMarginalTreeWriter implements ARGWriter {

	@Override
	public void writeARG(ARG arg, File file) throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(file));
		
		Integer[] breakpoints = arg.collectBreakPoints();
		
		int thisBP = 0;
		for(int i=0; i<breakpoints.length; i++) {
			if (breakpoints[i] > thisBP) {
				CoalNode root = TreeUtils.createMarginalTree(arg, thisBP);
				String newick = TreeUtils.getNewick(root);
				System.out.println(thisBP + " - " + breakpoints[i] + " : " + newick );
				writer.write(thisBP + " - " + breakpoints[i] + " : " + newick + "\n");
			}
			thisBP = breakpoints[i];
		}
		
		//There's no breakpoint associated with the last region, so just do it manually here
		int lastRegion = arg.getSiteCount()-1;
		CoalNode root = TreeUtils.createMarginalTree(arg, lastRegion);
		String newick = TreeUtils.getNewick(root);
		System.out.println(thisBP + " - " + lastRegion + " : " + newick);
		writer.write(thisBP + " - " + lastRegion + " : " + newick + "\n");
		writer.close();
	}

	/**
	 * Creates a bifurcating tree of CoalNodes that reflect the marginal tree structure of the given ARG at the site provided.
	 * This is the same tree-cloning algorithm as in treesimj. 
	 * @param arg
	 * @param site
	 * @return
	 */
//	private CoalNode getMarginalTree(ARG arg, int site) {
//		List<ARGNode> treeNodes = new ArrayList<ARGNode>();
//		for(TipNode tip : arg.getTips()) {
//			addNodesFromTip(tip, site, treeNodes);
//		}
//		
//		
//		//find and return root
//		double maxHeight = 0;
//		ARGNode rootNode = treeNodes.get(0);
//		for(ARGNode node : treeNodes) {
//			if (node.getHeight() > maxHeight) {
//				maxHeight = node.getHeight();
//				rootNode = node;
//			}
//		}
//		if (rootNode.getParent(0)!=null) {
//			throw new IllegalArgumentException("Bad ARG structure: deepest node does not have a null parent?");
//		}
//
//		//Root may have only one offspring.. advance it toward tips while this is true
//		rootNode = moveRootTipward(rootNode);
//		
//		//Above procedure makes a tree, but it may have some nodes with only one descendant. We need to strip these out to
//		//make sure we have a nice bifurcating tree with only CoalNodes 
//		stripNodesWithOneOffspring(rootNode);
//		
//		
//		return (CoalNode)rootNode;
//	}

	/**
	 * The marginal tree creation procedure may create a tree with a root that only has one offspring. This function
	 * finds the node that's the MRCA of everyone and makes that the root (by advancing the root tipward while it
	 * has only one offspring). 
	 * @param root
	 */
//	private ARGNode moveRootTipward(ARGNode root) {
//		while( hasOneOffspring(root)) {
//			root = getSingleOffspring(root);
//			root.proposeParent(0, null);
//		}
//		return root;
//	}
	
	
	/**
	 * Recursive function that modifies tree structure by removing interior nodes that have
	 * exactly one offspring, and connecting their children to the parents
	 * @param parent
	 */
//	private static void stripNodesWithOneOffspring(ARGNode parent) {
//		if (parent.getNumOffspring()==0)
//			return;
//		
//		if ( hasOneOffspring(parent) ) {
//			//this is an error, we must resolve these before we get here
//		}
//		
//		ARGNode kid0 = parent.getOffspring(0);
//		ARGNode kid1 = parent.getOffspring(1);
//		
//		if ( hasOneOffspring(kid0)) {
//			ARGNode grandKid = getSingleOffspring(kid0);
//			while (hasOneOffspring(grandKid)) {
//				grandKid = getSingleOffspring(grandKid);
//			}
//			//System.out.println("Connecting parent " + parent + " to descendant " + grandKid);
//			grandKid.proposeParent(0, parent);
//			parent.proposeOffspring(0, grandKid);
//		}
//		
//		if ( hasOneOffspring(kid1)) {
//			ARGNode grandKid = getSingleOffspring(kid1);
//			while (hasOneOffspring(grandKid)) {
//				grandKid = getSingleOffspring(grandKid);
//			}
//			//System.out.println("Connecting parent " + parent + " to descendant " + grandKid);
//			grandKid.proposeParent(0, parent);
//			parent.proposeOffspring(1, grandKid);
//		}
//		
//		
//		if (parent.getNumOffspring()==2) {
//			stripNodesWithOneOffspring( parent.getOffspring(0));
//			stripNodesWithOneOffspring( parent.getOffspring(1));
//		}
//		
//	}

//	private static ARGNode getSingleOffspring(ARGNode node) {
//		if (node instanceof RecombNode) 
//			return node.getOffspring(0);
//		if (node instanceof CoalNode) {
//			if (node.getOffspring(0)==null)
//				return node.getOffspring(1);
//			else
//				return node.getOffspring(0);
//		}
//		
//		//we should never get here
//		throw new IllegalArgumentException("Cannot find single offspring for node that is not a Recomb or Coal node");
//	}
	
	
//	private static boolean hasOneOffspring(ARGNode node) {
//		if (node instanceof RecombNode)
//			return true;
//		if (node instanceof CoalNode) {
//			int kids = 0;
//			if (((CoalNode) node).getLeftOffspring()==null)
//				kids++;
//			if (((CoalNode) node).getRightOffspring()==null)
//				kids++;
//			return kids==1;
//		}
//		
//		return false;
//	}
	
	/**
	 * Trace rootward from the given tip, duplicating the ancestral structure of the nodes found and adding them to treeNodes,
	 * until we reach a node whose parent is already in the list of treeNodes, in which we just attach to that node and then
	 * return.  If we do this for every tip in the an ARG, always using getParentForSite, we end up with a marginal tree. 
	 * @param tip
	 * @param treeNodes
	 */
//	private void addNodesFromTip(TipNode tip, int site, List<ARGNode> treeNodes) {
//		ARGNode kid = tip;
//		ARGNode parent = kid.getParentForSite(site); //Tips only have one parent
//		ARGNode treeKid = new TipNode();
//		
//		treeKid.setLabel(kid.getLabel());
//		treeKid.proposeHeight(kid.getHeight());
//		
//		ARGNode treeParent = findNodeByLabel(treeNodes, "" + parent.getNumber());
//		
//		while(treeParent == null && parent != null) {
//			if (parent instanceof CoalNode)
//				treeParent = new CoalNode();
//			else
//				treeParent = new RecombNode();
//			
//			treeParent.setLabel("" + parent.getNumber());
//			treeParent.proposeHeight(parent.getHeight());
//			treeKid.proposeParent(0, treeParent);
//			treeParent.proposeOffspring(0, treeKid);
//			
//			
//			kid = parent;
//			parent = kid.getParentForSite(site);
//			treeNodes.add(treeParent); // MUST come before we look for the new treeParent
//			
//			if (parent != null) {
//				treeKid = treeParent;
//				treeParent = findNodeByLabel(treeNodes, "" + parent.getNumber());
//			}
//		}
//		
//		if (parent != null) {
//			treeParent.proposeOffspring(1, treeKid);
//			treeKid.proposeParent(0, treeParent);
//		}
//		
//	}

//	private ARGNode findNodeByLabel(List<ARGNode> list, String label) {
//		for(ARGNode node : list) {
//			if (node.getLabel().equals(label)) {
//				return node;
//			}
//		}
//		return null;
//	}
	
	public static void main(String[] args) {
		ARGParser reader = new ARGParser();
		List<ARGNode> nodes;
		try {
			nodes = reader.readARGNodes(new File("testARG.xml") );
			ARG arg = new ARG(1000, nodes);
			
//			nodes = arg.getAllNodes();
//			System.out.println("Read " + nodes.size() + " from input file");
//			for(ARGNode node : nodes) {
//				System.out.println(node);
//			}
			
			ARGMarginalTreeWriter writer = new ARGMarginalTreeWriter();
			writer.writeARG(arg, new File("testARGoutput.trees"));
			
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ARGParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

}
