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


package arg;

import arg.ARGChangeEvent.ChangeType;

public class TipNode extends ARGNode {

	
	private int[] tipStates;
	boolean hasData = false;
	

	TipNodeState currentState; 
	TipNodeState proposedState; 	
	TipNodeState activeState; //Points to the current state used for dl calculation - generally 'current state', but gets bumped to proposed state when we propose a change
	
	//All sites always originate from tips
	private SiteRangeList range = new SiteRangeList();
		
	//Unique number used to identify this tip in the computeCore, if present
	private int computeID = -1;
	
	public TipNode(ARG owner) {
		super(owner);
		currentState = new TipNodeState();
		proposedState = new TipNodeState();
		activeState = new TipNodeState();
	}
	
	/**
	 * Set the index used by the computeCore to identify this tip. Changing this in any way besides 
	 * from within the computeCore is likely to destroy the world. 
	 * @param id
	 */
	public void setComputeID(int id) {
		this.computeID = id;
	}
	
	/**
	 * Obtain a reference to the array describing the state at the global site indices
	 * @return
	 */
	public int[] getTipStates() {
		return tipStates;
	}
	
	/**
	 * Returns false, range info is never changed for tip nodes.
	 */
	public boolean isRangeProposed() {
		return false;
	}
	
	/**
	 * Returns the id assigned by the computeCore to this node
	 * @return
	 */
	public int getComputeID() {
		return computeID;
	}
	
	public SiteRangeList getActiveRanges() {
		return range;
	}
	
	/**
	 * Initialize the SiteRange information for this Tip.
	 * Tips SiteRange info is not mutable, all Tips are always assumed to be 
	 * 'ancestral' to every site (although we may not *observed* a state for all sites...) 
	 * @param sites
	 */
	public void initializeRanges(int sites) {
		if (range == null)
			range = new SiteRangeList();
		range.clear();
		range.appendRange(0, sites, getComputeID());
	}
	
	
	public int getStateIndex(int index) {
		return tipStates[index];
	}
	
	public boolean getDLKnown() {
		return true;
	}
	
	public  SiteRangeList getCurrentRanges() {
		return range;
	}
	
	public SiteRangeList getProposedRanges() {
		return range;
	}
		

	public void proposeHeight(double height) {
		activeState.height = height;
		proposedState.height = activeState.height;
		if (parentARG != null) {
			parentARG.queueChangeEvent(new ARGChangeEvent(ChangeType.HeightChanged, this));
		}
	}
	
	/**
	 * This sets the state vector for this node, 
	 * @param probVec The new state
	 */
	public void addTipState(int[] stateVec) {
		hasData = stateVec != null;
		tipStates = stateVec;
	}
	

	
	public int getNumOffspring() {
		return 0;
	}
	
	public ARGNode getOffspring(int which) {
		throw new IllegalArgumentException("Cannot access child #" + which + " from a tree tip");
	}
	
	public void setLeftChild(ARGNode node) {
		throw new IllegalArgumentException("Cannot set child in a tree tip");
	}
	
	public void setRightChild(ARGNode node) {
		throw new IllegalArgumentException("Cannot set child in a tree tip");
	}
	


	@Override
	/**
	 * Return the height of this node, since we dont support tips having different times this should always be zero
	 */
	public double getHeight() {
		return activeState.height;
	}

	@Override
	/**
	 * Returns 1, since tip nodes always have just one parent
	 */
	public int getNumParents() {
		return 1;
	}

	@Override
	public ARGNode getParent(int which) {
		return activeState.parent;
	}

	@Override
	public void proposeParent(int parentIndex, ARGNode parent) {
		if (parentIndex>0) {
			throw new IllegalArgumentException("Cannot propose parent #" + parentIndex + " for tip node " + getLabel());
		}
		proposedState.parent = parent;
		activeState.parent = proposedState.parent;
		if (parentARG != null) {
			parentARG.queueChangeEvent(new ARGChangeEvent(ChangeType.StructureChanged, this));
		}
	}

	@Override
	public void proposeOffspring(int offspringIndex, ARGNode child) {
		throw new IllegalArgumentException("Cannot propose any offspring for a tip node.");
	}
	
	@Override
	/**
	 * The only mutable items in a tip node are the parent and (someday) the height, when we accept 
	 * we just need to set the current to active for those two items
	 */
	public void acceptProposal() {
		currentState.parent = activeState.parent;
		currentState.height = activeState.height;
	}

	@Override
	public void rejectProposal() {
		activeState.parent = currentState.parent;
		activeState.height = currentState.height;
	}

	@Override
	public boolean isActiveProposed() {
		return (currentState.height != activeState.height) || (currentState.parent != activeState.parent);
	}
	
	@Override
	public ARGNode getParentForSite(int site) {
		return activeState.parent;
	}
	
	@Override 
	public int computeProposedRanges() {
		//Nothing to do here, our range info never changes
		return 0;
	}
	
	public int computeProposedRanges(boolean force) {
		//Nothing to do here either
		return 0;
	}
	
	/**
	 * A class to store some of the mutable info for this node. We also maintain a mapping and state arrays here even though
	 * they're not actually mutable for tip nodes, since (right now) this makes some DL calculation code in CoalNode cleaner
	 * @author brendan
	 *
	 */
	class TipNodeState {
		
		//The distance of this node above the zero reference point
		double height;
		
		//Tip nodes always have exactly one parent, but it can be of any type
		ARGNode parent = null;
		
	}

	




	
}
