package arg;

import arg.ARGChangeEvent.ChangeType;

import testing.Timer;

import dlCalculation.computeCore.ComputeCore;

/**
 * An ARG node which represents a coalescence of two lineages and at which we may calculate a data likelihood
 * @author brendan
 *
 */
public class CoalNode extends ARGNode {

	NodeState currentState; 
	NodeState proposedState; 	
	NodeState activeState; //Points to the current state used for dl calculation - generally 'current state', but gets bumped to proposed state when we propose a change
	
	//True if this node has had its partials, etc, initialized
	//It may be false in situations where we want a tree, but no data (i.e. calculating the stationary distribution)
	boolean hasData = false;
	
	ComputeCore computeCore;
		
	public CoalNode(ARG owner) {
		super(owner);
		currentState = new NodeState();
		proposedState = new NodeState();
		activeState = new NodeState(); //Yes, this needs its own object - we swap around the references within, not the state itself

        currentState.coalescingSites = owner.getCoalRangeList();
        currentState.descendingSites = owner.getSiteRangeList();
                
        proposedState.coalescingSites = owner.getCoalRangeList();
        proposedState.descendingSites = owner.getSiteRangeList();
	}
	
	/**
	 * Set the compute core associ
	 * @param core
	 */
	public void setComputeCore(ComputeCore core) {
		this.computeCore = core;
	}

	
	public void proposeHeight(double height) {
		proposedState.height = height;
		activeState.height = proposedState.height;
		
		if (parentARG != null) {
			parentARG.queueChangeEvent(new ARGChangeEvent(ChangeType.HeightChanged, this));
		}
		
		if (computeCore != null) {
			for(int i=0; i<activeState.coalescingSites.rangeCount(); i++) {
				computeCore.proposeNode(activeState.coalescingSites.getRefID(i), activeState.height);
			}
			
			parentARG.setDLRecalc();
		}
	}
	
	public boolean isRangeProposed() {
		return proposedState.descendingSites != currentState.descendingSites;
	}
	
	public void computeProposedRanges() {
		computeProposedRanges(false);
	}
	
	/**
	 * Update 'range' information, including which sites coalesce at this node and which sites descend from this
	 * node to the tips. This also notifies the compute core of any changes to the range information associated
	 * with this node. Lots of crazy gymnastics and, likely, opportunities for performance improvements below
	 */
	public void computeProposedRanges(boolean force) { 
		
		//Right now we don't attempt any of this if there's not a compute core active
		if (computeCore == null) {
			return;
		}	

		
		proposedState.descendingSites.clear();
		proposedState.coalescingSites.clear();
		
		//If neither child has any range info we know right away what the ranges are (they're both empty) 
		if ( activeState.leftChild.getActiveRanges().size()==0	&& activeState.rightChild.getActiveRanges().size()==0) {
			activeState.coalescingSites = proposedState.coalescingSites;
			activeState.descendingSites = proposedState.descendingSites;
			return;
		}
				
		SiteRange leftFilter = null; 
		SiteRange rightFilter = null; 
		
        //If we have a child who is a recomb node but we're not above a trivial coalescent, only some sites come to us along 
        //one branch, so we 'filter' the active ranges from that branch by the sites that go to us (represented as a SiteRange)
        if (activeState.leftChild != activeState.rightChild) {

        	if (activeState.leftChild.getNumParents() == 2) {
        		leftFilter = ((RecombNode)activeState.leftChild).getRangeForParent(this);
        	}

        	if (activeState.rightChild.getNumParents() == 2) {
        		rightFilter = ((RecombNode)activeState.rightChild).getRangeForParent(this);
        	}

        	if (rightFilter == null || rightFilter.intersects(activeState.rightChild.getActiveRanges())) {

        		if (leftFilter == null || leftFilter.intersects(activeState.leftChild.getActiveRanges())) {
        			SiteRangeMerger.processListsWithFilters(activeState.rightChild.getActiveRanges(), rightFilter,
        													activeState.leftChild.getActiveRanges(), leftFilter,
        													proposedState.descendingSites, proposedState.coalescingSites,
                                                            currentState.coalescingSites, computeCore);
        		} else {
        			//No sites from left, but some from the right
        			if (rightFilter == null)
        				  activeState.rightChild.getActiveRanges().copy(proposedState.descendingSites);
        			else if (rightFilter.intersects(activeState.rightChild.getActiveRanges()))
        				  activeState.rightChild.getActiveRanges().copyFilter(rightFilter.getMin(), rightFilter.getMax(), proposedState.descendingSites);
        		}

        	} else {
        		//No sites from the right side, but maybe some from left
        		if (leftFilter == null) {
        			activeState.leftChild.getActiveRanges().copy(proposedState.descendingSites);
        		}
        		else if (leftFilter.intersects(activeState.leftChild.getActiveRanges())) {
        			activeState.leftChild.getActiveRanges().copyFilter(leftFilter.getMin(), leftFilter.getMax(), proposedState.descendingSites);
        		}

        	}
        } else {
        	//Recombination was a trivial recomb, there are no coalesceing sites here and our descending sites
        	//are identical to that of our kid
        	 activeState.leftChild.getActiveRanges().copy(proposedState.descendingSites);
        }

        activeState.coalescingSites = proposedState.coalescingSites;
        activeState.descendingSites = proposedState.descendingSites;

        Timer.startTimer("Core-propose");

        CoalRangeList cSites = activeState.coalescingSites;
        if (cSites.size()>0) {
        	parentARG.setDLRecalc();
        }

        for(int i=0; i<cSites.size(); i++) {
        	computeCore.proposeRange(cSites.getRefID(i), 
        			activeState.height, 
        			cSites.getRangeBegin(i), 
        			cSites.getRangeEnd(i),
        			cSites.getLChild(i), 
        			cSites.getRChild(i));
        }


        Timer.stopTimer("Core-propose");
	}


	public SiteRangeList getActiveRanges() {
		return activeState.descendingSites;
	}
	
	/**
	 * Returns true if this node refers to a compute node with the given
	 * compute id
	 * @param id
	 * @return
	 */
	public boolean containsComputeID(int id) {
		for(int i=0; i<activeState.coalescingSites.size(); i++) {
			if (activeState.coalescingSites.getRefID(i)==id) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Returns true if the given site coalesces at this node
	 * @param site
	 * @return
	 */
	public boolean siteCoalesces(int site) {
		if (activeState.coalescingSites == null)
			return false;
		
		return activeState.coalescingSites.contains(site);
		
//		for(SiteRange r : activeState.coalescingSites) {
//			if (r.contains(site))
//				return true;
//		}
//		return false;
	}
	
//	public void initializeStates(int stateCount, int patternCount) {
//		hasData = true;
//		this.stateCount = stateCount;
//
//	}
	
	/**
	 * Invokes the compute core to calculate partials for all site ranges associated with this node
	 */
//	public void computeProposedDL() {
//		if (computeCore == null) {
//			System.err.println("Core has not been initialized for " + this);
//		}
//		for(SiteRange coalSites : activeState.coalescingSites) {
//			computeCore.computePartials(coalSites.getRefNodeID());
//		}
//		
//	}
	
	public boolean isActiveProposed() {
		return  (activeState.parent != currentState.parent) 
				|| (activeState.leftChild != currentState.leftChild) 
				|| (activeState.rightChild != currentState.rightChild)
				|| (computeCore != null && activeState.coalescingSites != currentState.coalescingSites);
	}
	
	
	public ARGNode getParent() {
		return getParent(0);
	}
	
	public ARGNode getOffspring(int which) {
		if (which==0) 
			return getLeftOffspring();
		if (which==1) 
			return getRightOffspring();
		throw new IllegalArgumentException("Cannot access node #" + which + " from ARGNode " + getLabel());
	}
	
	
	/**
	 * Returns true if initializeStates (or initializeTipStates) has been called, which 
	 * signals that we can calculate DL on this node
	 * @return
	 */
	public boolean hasData() {
		return hasData;
	}

	
	/**
	 * Directly set the 'current' left child. Should only be used for tree initialization purposes. 
	 * @param kid
	 */
	public void setLeftOffspring(ARGNode kid) {
		currentState.leftChild = kid;
		activeState.leftChild = currentState.leftChild;
	}
	

	/**
	 * Directly set the 'current' right child. Should only be used for tree initialization purposes. 
	 * @param kid
	 */
	public void setRightOffspring(ARGNode kid) {
		currentState.rightChild = kid;
		activeState.rightChild = currentState.rightChild;
	}

	/**
	 * Return the 'active' left child
	 * @return
	 */
	public ARGNode getLeftOffspring() {
		return activeState.leftChild;
	}
	
	/**
	 * Return the active right child
	 * @return
	 */
	public ARGNode getRightOffspring() {
		return activeState.rightChild;
	}
	
	public int getNumOffspring() {
		return 2;
	}

	private void proposeRightChild(ARGNode rightChild) {
		proposedState.rightChild = rightChild;
		activeState.rightChild = rightChild;
	}
	
	private void proposeLeftChild(ARGNode leftChild) {
		proposedState.leftChild = leftChild;
		activeState.leftChild = proposedState.leftChild;
	}
	
	public void proposeParent(ARGNode parent) {
		proposedState.parent = parent;
		activeState.parent = proposedState.parent;
		if (parentARG != null) {
			parentARG.queueChangeEvent(new ARGChangeEvent(ChangeType.StructureChanged, this));
		}
	}

	@Override
	public void proposeParent(int parentIndex, ARGNode parent) {
		if (parentIndex >0) {
			throw new IllegalArgumentException("Cannot propose parent #" + parentIndex + " for coal node " + getLabel());
		}
		proposeParent(parent);
	}


	@Override
	public void proposeOffspring(int offspringIndex, ARGNode child) {
		if (offspringIndex==0) {
			proposeLeftChild(child);
			if (parentARG != null) {
				parentARG.queueChangeEvent(new ARGChangeEvent(ChangeType.StructureChanged, this));
			}
			return;
		}
		if (offspringIndex==1) {
			proposeRightChild(child);
			if (parentARG != null) {
				parentARG.queueChangeEvent(new ARGChangeEvent(ChangeType.StructureChanged, this));
			}
			return;
		}
		
		throw new IllegalArgumentException("Cannot propose offspring #" + offspringIndex + " for coal node " + getLabel());	
	}

	
	/**
	 * Convert the proposed state into the current state by swapping the current state and proposed state
	 */
	public void acceptProposal() {
		
		//Swap current and proposed range info, for both ancestral and compute ranges
		if (activeState.descendingSites == proposedState.descendingSites) {
			SiteRangeList tmp = proposedState.descendingSites;
			proposedState.descendingSites = currentState.descendingSites;
			currentState.descendingSites = tmp;
		}
		
		
		if (activeState.coalescingSites == proposedState.coalescingSites) {
			//We must tell the core to release the compute nodes we're no longer going to use. Our current
			//strategy attempts to reuse as many compute nodes as possible when we propose new ranges, which means
			//that if the current state has j ranges, and the proposed state has i ranges, the first i of j will have
			//the same compute node id's as the current state, and the remaining j-i will have new ids. So if i>j there's 
			//nothing to release. But if the new state has fewer ranges we must release the ones we're not using
			//remember they're been swapped
			for(int i=proposedState.coalescingSites.size(); i<currentState.coalescingSites.size(); i++) {
				//System.out.println("Releasing node " + currentState.coalescingSites.getRefID(i) + " from existing coal node");
				computeCore.releaseNode(currentState.coalescingSites.getRefID(i));
			}
			
			CoalRangeList tmp = proposedState.coalescingSites;
			proposedState.coalescingSites = currentState.coalescingSites;
			currentState.coalescingSites = tmp;
		}
		
		currentState.parent = activeState.parent;
		currentState.height = activeState.height;
		currentState.leftChild = activeState.leftChild;
		currentState.rightChild = activeState.rightChild;
	}
	
	/**
	 * Reject the recent proposal, we set all activeState references to currentState
	 */
	public void rejectProposal() {	
		activeState.parent = currentState.parent;
		activeState.leftChild = currentState.leftChild;
		activeState.rightChild = currentState.rightChild;
		activeState.height = currentState.height;
		
		if (activeState.coalescingSites == proposedState.coalescingSites) {
			for(int i=currentState.coalescingSites.size(); i<proposedState.coalescingSites.size(); i++) {
				//System.out.println("Releasing node " + proposedState.coalescingSites.getRefID(i) + " from existing coal node");
				computeCore.releaseNode(proposedState.coalescingSites.getRefID(i));
			}
		}
		
		activeState.descendingSites = currentState.descendingSites;
		activeState.coalescingSites = currentState.coalescingSites;
	}
	
	/**
	 * Obtain a list of SiteRanges describing the sites which coalesce at this node
	 * @return
	 */
	public CoalRangeList getCoalescingSites() {
		return activeState.coalescingSites;
	}
	
	public  SiteRangeList getCurrentRanges() {
		return currentState.descendingSites;
	}
	
	public SiteRangeList getProposedRanges() {
		return proposedState.descendingSites;
	}
	
	
	/**
	 * Return the proposed state left offspring, this is useful for debugging purposes
	 * @return
	 */
	public ARGNode getProposedLeftOffspring() {
		return proposedState.leftChild;
	}
	
	/**
	 * Return the proposed state right offspring, this is useful for debugging purposes
	 * @return
	 */
	public ARGNode getProposedRightOffspring() {
		return proposedState.rightChild;
	}
	

	public void verifyComputeNodes() {
		if (computeCore != null) {
			CoalRangeList cSites = activeState.coalescingSites;
			for(int i=0; i<cSites.size(); i++) {
				int nodeNumber = activeState.coalescingSites.getRefID(i);
				if (computeCore.getLeftChildForNode(nodeNumber) != cSites.getLChild(i))
					throw new IllegalStateException("Left child does not match for arg node " + this + " range # " + i);
				if (computeCore.getRightChildForNode(nodeNumber) != cSites.getRChild(i))
					throw new IllegalStateException("Right child does not match for arg node " + this + " range # " + i);
				if ((float)computeCore.getHeightForNode(nodeNumber) != (float)activeState.height)
					throw new IllegalStateException("Height does not match arg node " + this + " range # " + i);
				if (computeCore.getStartSiteForNode(nodeNumber) != cSites.getRangeBegin(i))
					throw new IllegalStateException("Start site does not match arg node " + this + " range # " + i);
				if (computeCore.getEndSiteForNode(nodeNumber) != cSites.getRangeEnd(i))
					throw new IllegalStateException("End site does not match arg node " + this + " range # " + i);
			}
		}
	}

	
	@Override
	public int getNumParents() {
		return 1;
	}

	@Override
	public ARGNode getParent(int which) {
		if (which ==0)
			return activeState.parent;
		
		throw new IllegalArgumentException("Cannot access parent>0 for Coal node " + getLabel());
	}


	@Override
	public double getHeight() {
		return activeState.height;
	}


	@Override
	public ARGNode getParentForSite(int site) {
		return activeState.parent;
	}
	
	
	class NodeState {
		
		//The distance of this node above the zero reference point
		double height;
		
		ARGNode parent = null;
		ARGNode leftChild = null;
		ARGNode rightChild = null;
		
		//Keeps track of which sites descend from this node all the way to (observed) tips
		SiteRangeList descendingSites = null;
		
		//List of site ranges which coalesce at this node
		CoalRangeList coalescingSites = null;
	}



}
