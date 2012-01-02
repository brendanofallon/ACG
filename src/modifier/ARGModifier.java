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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import math.RandomSource;
import arg.ARG;
import arg.ARGNode;
import arg.CoalNode;
import parameter.InvalidParameterValueException;
import testing.Timer;

/**
 * Base class of modifiers that work on ARGs. This provides a few helper methods, and
 * wraps the modification procedure so that a bunch of code that appears in every 
 * modifier now just appears once here... 
 * 
 * @author brendan
 *
 */
public abstract class ARGModifier extends AbstractModifier<ARG> {
		
	//List of nodes whose range information must be updated. This is filled within the swap procedures
	//and then sorted and traversed after the rearrangement has taken place
	private List<ARGNode> nodesToUpdate = new ArrayList<ARGNode>(50);
	
	protected ARG arg = null;
	
	public ARGModifier(Map<String, String> attributes) {
		super(attributes);
	}

	
	/**
	 * Actually perform the modification
	 * @return
	 */
	protected abstract Double modifyARG() throws InvalidParameterValueException,
								IllegalModificationException, ModificationImpossibleException;
	
	public void setParameter(ARG arg) {
		this.arg = arg;
	}
	
	public final Double modify() throws InvalidParameterValueException,
					IllegalModificationException, ModificationImpossibleException {
		
	
		Timer.startTimer("ARGModifier");
		
		arg.clearVisitedFlags(); //Set all visited flags to false so we can re-flag nodes for site range update
		nodesToUpdate.clear(); //List will contain all nodes whose site range info must be recomputed
		
		//Perform actual modification
		double hastingsRatio = modifyARG();
		
		Timer.stopTimer("ARGModifier");
		
		Timer.startTimer("Range1");
		
		updateAllRangeProposals(); //Traverse 'nodesToUpdate' list and recompute site range information
		
		Timer.stopTimer("Range1");
		
		//Sets appropriate update flags in ARG based on the types of modifications we've made
		tallyCall();
		arg.proposeValue(null);
		arg.processEvents(); 

		return hastingsRatio;
	}
	
	
	/**
	 * When node structure is modified, we need to inform all nodes above it that the range
	 * information (which sites descend from and coalesce at certain nodes) must be recomputed.
	 * This traverses rootward in the tree from the given node and adds all no
	 * @param node
	 */
	protected void propogateRangeProposals(ARGNode node) {
		Stack<ARGNode> stack = new Stack<ARGNode>();
		stack.push(node);
		
		//Stack based traversal toward root of network, setting visited flags for each node 
		//we encounter to ensure that we don't visit nodes more than once
		while(! stack.isEmpty()) {
			node = stack.pop();
			if (! node.isVisited()) {
				node.setVisited(true);
				nodesToUpdate.add(node);
				if (node.getParent(0) != null) {
					stack.push(node.getParent(0));
					if (node.getNumParents()==2)
						stack.push(node.getParent(1));
				}
			}
			
		}
		
		
	}

	
	
	/**
	 * Update range information for all nodes we may have affected 
	 */
	protected void updateAllRangeProposals() {
		//Make sure we traverse nodes in ascending height order
		Collections.sort(nodesToUpdate, arg.getNodeHeightComparator());
		for(ARGNode aNode : nodesToUpdate) {
			aNode.computeProposedRanges();
		}
	}
	
	
	/**
	 * Returns the parent index of the given parent node from the kid, such that kid.getParent(index)== parent,
	 * or throws an exception if parent is not actually a parent of the kid node.  
	 */
	public static int whichParent(ARGNode kid, ARGNode parent) {
		if (kid.getParent(0)==parent) 
			return 0;
		if (kid.getParent(1)==parent)
			return 1;
		throw new IllegalArgumentException("Node " + parent + " is not a parent of node " + kid);
	}
	
	/**
	 * Returns the offspring index of kid from parent, such that parent.getOffspring(index)==kid
	 * @param kid
	 * @param parent
	 * @return
	 */
	public static int whichChild(ARGNode kid, ARGNode parent) {
		if (parent.getOffspring(0)==kid) {
			return 0;
		}
		if (parent.getOffspring(1)==kid) {
			return 1;
		}
		throw new IllegalArgumentException("Node " + kid + " is not a child of node " + parent);
	}
	
	/**
	 * Returns a parent of the given child whose height is greater than the given height. If both of child's parents
	 * are higher than height, select a random one to return
	 * @param child
	 * @param height
	 * @return
	 */
	protected ARGNode findDisplaceableParent(ARGNode child, double height) {
		if (child.getNumParents()==2) {
			if (child.getParent(0).getHeight()>height) {
				if (child.getParent(1).getHeight()>height) {
					//Both branches cross time, so select a random one to be displaced parent
					return child.getParent( RandomSource.getNextIntFromTo(0, 1));
				}
				else {
					//Parent 1 does not cross but 0 does, so 0 must be displaced parent
					return child.getParent(0);
				}
			}
			else {
				//Parent 0 does not cross, so one must have and it must be the displaced parent
				return child.getParent(1);
			}
		}
		else {
			//rNodeChild was not recombinant, therefore only had one parent
			return child.getParent(0);
		}

	}

	/**
	 * Inserts a node between another node and its parent, at distance distAboveChild above the new child.
	 * It is an error to specify distAboveChild that is greater than or equal to newChild.getDistToParent.
	 * 
	 * Kind of funky, since Node newChild is always set as the LEFT child of the nodeToInsert 
	 * @param nodeToInsert Node to insert between newChild and newChild.getParent
	 * @param newChild
	 * @param distAboveChild Distance above newChild that node is
	 * @throws IllegalModificationException 
	 */
	protected void insertNodeAbove(CoalNode nodeToInsert, CoalNode newChild) throws IllegalModificationException {
		if (nodeToInsert.getHeight() >= newChild.getParent(0).getHeight()) {
			throw new IllegalModificationException("Cannot insert node of height " + nodeToInsert.getHeight() + " below parent with height " + newChild.getParent(0).getHeight());
		}
		
		CoalNode parent = (CoalNode)newChild.getParent(0);
		
		nodeToInsert.proposeParent(parent);
		parent.proposeOffspring(0, nodeToInsert);
		
		newChild.proposeParent(nodeToInsert);
		nodeToInsert.proposeOffspring(0, newChild);
	}
	

	

}
