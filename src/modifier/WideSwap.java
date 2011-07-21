package modifier;

import java.io.File;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import arg.ARG;
import arg.ARGNode;
import arg.CoalNode;
import arg.Newick;
import arg.RecombNode;
import arg.TreeUtils;
import arg.argIO.ARGParser;

import math.RandomSource;

import parameter.InvalidParameterValueException;


/**
 * This modifier takes a node and moves it to an entirely new branch at exactly the same height. It's like a less local
 * version of 'subtree swap', which makes fairly timid rearrangements. It also doesn't change any node heights, and never
 * affects the root node. 
 * @author brendano
 *
 */
public class WideSwap extends ARGModifier {

	int maxTries = 100;
	
	boolean verbose = false;
	
	public WideSwap(Map<String, String> attributes) {
		super(attributes);
		setUsePropagationShortcut(true);
	}

	public WideSwap() {
		this(new HashMap<String, String>());
		//Default frequency is 1
	}
	
	private WideSwap(double frequency) {
		this();
		this.frequency = frequency;
	}
	
	public WideSwap copy() {
		return new WideSwap( getFrequency() );
	}
	
	@Override 
	protected Double modifyARG() throws InvalidParameterValueException, IllegalModificationException, ModificationImpossibleException {
		
		List<ARGNode> internalNodes = arg.getInternalNodes();
		
		int nodeNum = RandomSource.getNextIntFromTo(0, internalNodes.size()-1);
		ARGNode node = internalNodes.get(nodeNum);
		

		if (internalNodes.size()==1) //Must be two tips, no recomb nodes
			throw new ModificationImpossibleException("No internal nodes to modify");
		
		//Pick a node that is not the root
		while(node.getParent(0) == null) {
			node = internalNodes.get(RandomSource.getNextIntFromTo(0, internalNodes.size()-1) );
		}

		double hr;
		if (node instanceof CoalNode) {
			hr = swapCoalNode( (CoalNode)node);
		}
		else {
			hr = swapRecombNode( (RecombNode)node);
		}
		
		return hr;
	}

	protected double swapRecombNode(RecombNode node) throws ModificationImpossibleException {
		List<ARGNode> branches = arg.getBranchesCrossingTime(node.getHeight());
		ARGNode newChild = branches.get(RandomSource.getNextIntFromTo(0, branches.size()-1)); //This will be new child of coal node
		ARGNode newParent = findDisplaceableParent(newChild, node.getHeight()); //New parent of node
		
		if (newChild == node.getParent(0) || newChild == node.getParent(1)) {
			throw new ModificationImpossibleException("Picked same branch for wide coal swap");
		}
		
		int oldParentIndex = RandomSource.getNextIntFromTo(0, 1);
		ARGNode oldParent = node.getParent(oldParentIndex);
		ARGNode oldChild = node.getOffspring(0);
		int pIndex = whichChild(node, oldParent);
		
		
		//Connect old child to old parent
		oldChild.proposeParent(whichParent(oldChild, node), oldParent);
		oldParent.proposeOffspring(pIndex, oldChild);
		
		
		//Insert the new node between newChild and newParent
		int cIndex = whichParent(newChild, newParent);
		int cpIndex = whichChild(newChild, newParent);
		node.proposeParent(oldParentIndex, newParent);
		newParent.proposeOffspring(cpIndex, node);
		newChild.proposeParent(cIndex, node);
		node.proposeOffspring(0, newChild);
		
		propogateRangeProposals(oldChild);
		propogateRangeProposals(node);
		
		if (verbose) 
			System.out.println("Swapping recomb node " + node + " to branch connecting parent " + newParent + " to offspring " + newChild);
		
		return 1.0;
	}

	protected double swapCoalNode(CoalNode node) throws ModificationImpossibleException, IllegalModificationException {

		List<ARGNode> branches = arg.getBranchesCrossingTime(node.getHeight());
		ARGNode newChild = branches.get(RandomSource.getNextIntFromTo(0, branches.size()-1)); //This will be new child of coal node
		ARGNode newParent = findDisplaceableParent(newChild, node.getHeight()); //New parent of node
		int oldChildIndex = RandomSource.getNextIntFromTo(0, 1);
		ARGNode oldChild = node.getOffspring(oldChildIndex); //Child that will be attached to node's parent
		ARGNode oldParent = node.getParent();
		
		if (newChild == node.getOffspring(0) || newChild == node.getOffspring(1)) {
			throw new ModificationImpossibleException("Picked same branch for wide coal swap");
		}
		
		//Attach the old child we are leaving to the old parent
		int pIndex = whichChild(node, oldParent);
		oldChild.proposeParent(whichParent(oldChild, node), oldParent);
		oldParent.proposeOffspring(pIndex, oldChild);
		
		//Insert the new node between newChild and newParent
		int cIndex = whichParent(newChild, newParent);
		int cpIndex = whichChild(newChild, newParent);
		node.proposeParent(newParent);
		newParent.proposeOffspring(cpIndex, node);
		newChild.proposeParent(cIndex, node);
		node.proposeOffspring(oldChildIndex, newChild);
		
		propogateRangeProposals(oldChild);
		propogateRangeProposals(node);
		

		if (verbose) 
			System.out.println("Swapping coal node  " + node + " to branch connecting parent " + newParent + " to offspring " + newChild);
		return 1.0;	
	}

	
	
}
