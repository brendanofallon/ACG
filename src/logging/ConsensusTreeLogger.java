package logging;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import parameter.AbstractParameter;
import arg.ARG;
import arg.CoalNode;
import arg.TreeUtils;

import mcmc.MCMC;

import tools.ConsensusTreeBuilder;
import tools.Tree;
import tools.ConsensusTreeBuilder.TreeItem;
import tools.Tree.Node;
import xml.XMLUtils;

/**
 * This logger uses a ConsensusTreeBuilder to tabulate the clades for marginal trees
 * ancestral to a given site, and then builds consensus trees out of em. 
 * @author brendan
 *
 */
public class ConsensusTreeLogger extends PropertyLogger {

	public static final String XML_SITE = "site";
	Integer site;
	ARG arg;
	int count = 0;
	private ConsensusTreeBuilder builder = new ConsensusTreeBuilder();
	
	public ConsensusTreeLogger(Map<String, String> attrs, ARG arg) {
		super(attrs);
		this.arg = arg;
		site = XMLUtils.getIntegerOrFail(XML_SITE, attrs);
	}

	/**
	 * Sample one marginal tree from the ARG, and add it to the consensus builder
	 */
	private void tabulateNewTree() {
		CoalNode coalRoot = TreeUtils.createMarginalTree(arg, site);
		Tree tree = new Tree(coalRoot);
		builder.addTree(tree.getRoot());
	}

	@Override
	public void addValue(int stateNumber) {
		if (stateNumber > burnin && stateNumber % collectionFrequency==0) {
			tabulateNewTree();
		}
	}
	
	@Override
	public String getSummaryString() {
		Tree tree = new Tree();
		count++;
		builder.buildMajorityCladeList();
		builder.mergeClades(tree);
		List<Node> nodes = tree.getAllNodes();
		for(Node node : nodes) {
			node.removeAnnotation("tips");
			node.removeAnnotation("height");
		}
		
		return tree.getNewick();
	}
	



	/**
	 * Critical for MC3 runs, when the chain switches we must listen to another arg / chain
	 */
	public void setMCMC(MCMC chain) {
		ARG newARG = findARG(chain);
		if (newARG == null) {
			throw new IllegalArgumentException("Cannot listen to a chain without an arg parameter");
		}
		this.arg = newARG;
	}
	
	private ARG findARG(MCMC mc) {
		for(AbstractParameter<?> par : mc.getParameters()) {
			if (par instanceof ARG)
				return (ARG)par;
		}
		return null;
	}
	
}
