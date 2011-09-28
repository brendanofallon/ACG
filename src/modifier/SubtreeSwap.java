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


package modifier;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import arg.CoalNode;
import arg.RecombNode;
import arg.ARGNode;
import math.RandomSource;

import parameter.InvalidParameterValueException;

/**
 * A class that modifies trees by selecting an internal node that is not immediately above two tips, then
 * moving a branch from the child closest to it (whose distToParent is smallest) so that it is a descendant
 * of the other side, as in :
 * ((a:2, b:2):1, (c:1, d:1):2) ->  (a:3, (b:2, (c:1, d:1):1):1)
 * 
 * This modifier does not change any coalescence times, 
 * 
 * @author brendan
 *
 */
public class SubtreeSwap extends WideSwap {
	
	final int maxTries = 100;
	
	final boolean verbose = false;
	
	final Double hr = 1.0;
	
	public SubtreeSwap(Map<String, String> attributes) {
		super(attributes);
		modStr = "Subtree swapper";
		if (!attributes.containsKey("frequency")) {
			frequency = 2.0;
		}
		setUsePropagationShortcut(true);
	}

	public SubtreeSwap() {
		this(new HashMap<String, String>());
		modStr = "Subtree swapper";
	}

	
	/**
	 * Swap the closest parent ABOVE this recomb node to the other branch
	 * @param node Recomb node above which lineage will be swapped
	 * @return The hastings ratio of this term
	 * @throws ModificationImpossibleException
	 */
	protected double swapRecombNode(RecombNode node) throws ModificationImpossibleException {	
		ARGNode higherNode;
		ARGNode lowerNode; //This is the one that gets moved
		
		if (node.getParent(0) == node.getParent(1)) {
			throw new ModificationImpossibleException("Cannot swap on trivial recomb");
		}
		
		if (node.getParent(0).getHeight() > node.getParent(1).getHeight()) {
			higherNode = node.getParent(0);
			lowerNode = node.getParent(1);
		}
		else {
			higherNode = node.getParent(1);
			lowerNode = node.getParent(0);
		}
		
		ARGNode newParent = lowerNode.getParent(0); 
		
		int lpIndex = whichParent(node, lowerNode); //Index of lower node from node, so node.getParent(lpIndex)==lowerNode
		int lcIndex = whichChild(lowerNode, newParent);
		int lkIndex = whichParent(lowerNode, newParent);
		int hpIndex = whichParent(node, higherNode);
		int hcIndex = whichChild(node, higherNode);
		
		newParent.proposeOffspring(lcIndex, node);
		node.proposeParent(lpIndex, newParent);
			
		node.proposeParent(hpIndex, lowerNode);
		lowerNode.proposeParent(lkIndex, higherNode);
		higherNode.proposeOffspring(hcIndex, lowerNode);

		if (verbose)
			System.out.println("Swapping node-above-recomb node :  " + lowerNode + "new parent: " + lowerNode.getParent(lkIndex));

		propogateRangeProposals(node);
		return hr;
	}

	/**
	 * Swap a the highest descendant below the given coal node from one descendant branch to the other.
	 *  This is not possible if we're above two tips. 
	 * @param node
	 * @return
	 * @throws ModificationImpossibleException
	 * @throws IllegalModificationException
	 */
	protected double swapCoalNode(CoalNode node) throws ModificationImpossibleException, IllegalModificationException {
		//If we've selected a node that is above two tips, or that where both offspring are the same (recombination) node,
		//then we cannot use this operator. 
		if ( (node.getLeftOffspring().getNumOffspring()==0) && (node.getRightOffspring().getNumOffspring()==0) ) {
			throw new ModificationImpossibleException("Cannot perform swap on node " + node);			
		}
		
		//If we're above a trivial coalescent, no changes are made but we still accept the state
		if (node.getLeftOffspring() == node.getRightOffspring()) {
			throw new ModificationImpossibleException("Cannot swap on trivial recomb");
		}
		
		
		ARGNode higherChild;
		ARGNode lowerChild;
		if (node.getLeftOffspring().getHeight() > node.getRightOffspring().getHeight()) {
			higherChild = node.getLeftOffspring();
			lowerChild = node.getRightOffspring();
		}
		else {
			higherChild = node.getRightOffspring();
			lowerChild = node.getLeftOffspring();
		}
		
		if (higherChild instanceof CoalNode) {
			swapCoalAboveARGNode(node, (CoalNode)higherChild, lowerChild);
			return hr;
		}
		if (higherChild instanceof RecombNode) {
			swapRecombAboveARGNode(node, (RecombNode)higherChild, lowerChild);
			return hr;
		}
		
		throw new IllegalArgumentException("Error swapping coal node");
	}

	/**
	 * Performs a swap modification for when the higher child node is a recombination node, and the lower node can be any node.
	 * Interestingly, this does not change the parents of the 'moving' recomb. node, it really just reassigns its offspring. 
	 * @param parent Parent of recomb node to move. 
	 * @param movingNode Recomb node to swap
	 * @param constNode Other descendant of parent node. 
	 * @throws ModificationImpossibleException 
	 */
	private void swapRecombAboveARGNode(CoalNode parent,
			RecombNode movingNode, ARGNode constNode) throws ModificationImpossibleException {
		
		ARGNode grandKid = movingNode.getOffspring(0); //Grandchild will be attached to parent node
		
		grandKid.proposeParent( whichParent(grandKid, movingNode), parent);
		int firstKid = RandomSource.getNextIntFromTo(0, 1);
		int secondKid = 1-firstKid;
		parent.proposeOffspring(firstKid, grandKid);
		parent.proposeOffspring(secondKid, movingNode);
		
		constNode.proposeParent( whichParent(constNode, parent), movingNode);
		movingNode.proposeOffspring(0, constNode);
	
		if (verbose)
			System.out.println("Swapping recomb-above-arg node :   " + movingNode + " to be parent of " + constNode);
		
		//Range info must be updated for BOTH parents of moving node not just the focal node that we
		//originally selected to change, so propagate the changes from here
		propogateRangeProposals(movingNode);
	}

	/**
	 * movingKid and constKid are assumed to be immediate descendants of parent, with movingKid having a greater height than
	 * constKid. Moving kid is then swapped to the other branch, and becomes constKid's parent. MovingKid must be a CoalNode,
	 * but constKid can be any type of node. 
	 * @param node
	 * @param movingKid
	 * @param constKid
	 * @throws IllegalModificationException 
	 * @throws ModificationImpossibleException 
	 */
	private void swapCoalAboveARGNode(CoalNode parent, CoalNode movingKid,
			ARGNode constKid) throws IllegalModificationException, ModificationImpossibleException {
				
		ARGNode movingGrandKid;
		ARGNode constGrandKid;
		
		//Select a grandkid to be the child of the moving kid, both have equal probability
		if (RandomSource.getNextUniform() < 0.5) {
			movingGrandKid = movingKid.getLeftOffspring();
			constGrandKid = movingKid.getRightOffspring();
		}
		else {
			movingGrandKid = movingKid.getRightOffspring();
			constGrandKid = movingKid.getLeftOffspring();			
		}
		
		//If constKid is a recombination node, one of its parents may be movingKid. In this case we must assign the grandKids
		//non-randomly
		if (movingKid.getOffspring(0)==constKid) {
			constGrandKid = movingKid.getOffspring(1);
			movingGrandKid = movingKid.getOffspring(0);
		}
		if (movingKid.getOffspring(1)==constKid) {
			constGrandKid = movingKid.getOffspring(0);
			movingGrandKid = movingKid.getOffspring(1);
		}
		
		//ConstGrandKid gets attached to node by the branch that previously attached constGrandKid to
		//to movingKid
		constGrandKid.proposeParent( whichParent(constGrandKid, movingKid), parent);
		int firstKid = RandomSource.getNextIntFromTo(0, 1);
		int secondKid = 1-firstKid;
		parent.proposeOffspring(firstKid, constGrandKid);
		parent.proposeOffspring(secondKid, movingKid);
		
		//If constKid's parent#0 is parent, which is always true if constKid is a CoalNode or a TipNode (since they only have one parent),
		//and may be true if constKid is a recombNode, then constKid's new parent #0 is movingKid
		constKid.proposeParent( whichParent(constKid, parent), movingKid);
		firstKid = RandomSource.getNextIntFromTo(0, 1);
		secondKid = 1-firstKid;
		movingKid.proposeOffspring(firstKid, movingGrandKid);
		movingKid.proposeOffspring(secondKid, constKid);
		
		if (verbose)
			System.out.println("Swapping coal-above-arg :   " + movingKid + " to be parent of " + constKid);
		
		propogateRangeProposals(movingKid);
	}


		
}
