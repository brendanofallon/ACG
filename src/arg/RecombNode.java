package arg;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import arg.ARGChangeEvent.ChangeType;

public class RecombNode extends ARGNode {

	RecombNodeState currentState;
	RecombNodeState proposedState;
	RecombNodeState activeState;
	
	//Used for getRangeForParent, so we're not constantly allocating new siteranges
	private SiteRange range0 = new SiteRange(0, 1);
	private SiteRange range1 = new SiteRange(0, 1);
	
	public RecombNode(ARG owner) {
		super(owner);
		currentState = new RecombNodeState();
		proposedState = new RecombNodeState();
		activeState = new RecombNodeState();

        currentState.siteRanges = owner.getSiteRangeList();
        proposedState.siteRanges = owner.getSiteRangeList();
	}
	
	
	@Override
	public int getNumOffspring() {
		return 1;
	}
	
	public boolean isRangeProposed() {
		return proposedState.siteRanges != currentState.siteRanges
				|| proposedState.recombRange != currentState.recombRange;
	}

	@Override
	public ARGNode getOffspring(int which) {
		if (which==0)
			return activeState.child;
		
		throw new IllegalArgumentException("Cannot access child #" + which + " of recomb node " + getLabel());
	}

	@Override
	public int getNumParents() {
		return 2;
	}

	@Override
	public ARGNode getParent(int which) {
		if (which==0)
			return activeState.parentZero;
		if (which==1)
			return activeState.parentOne;
		throw new IllegalArgumentException("Cannot index parent # " + which + " of a RecombNode");
	}

	
	public  SiteRangeList getCurrentRanges() {
		return currentState.siteRanges;
	}
	
	public SiteRangeList getProposedRanges() {
		return proposedState.siteRanges;
	}
	
	@Override
	public double getHeight() {
		return activeState.height;
	}


	@Override
	public void proposeHeight(double height) {
		proposedState.height = height;
		activeState.height = proposedState.height;
		if (parentARG != null) {
			parentARG.queueChangeEvent(new ARGChangeEvent(ChangeType.HeightChanged, this));
		}
		
		//I don't think we need to tell anyone that this has changed, interestingly. No coalescences / heights 
		//will change at all as a result of this shift
	}

	
	public void computeProposedRanges() {
		computeProposedRanges(false);
	}
	
	/**
	 * Site ranges for RecombNodes are identical to whatever its descendant site ranges are
	 */
	public void computeProposedRanges(boolean force) {

		//Skip all this if there's no compute core
		if (parentARG != null  && ! parentARG.hasComputeCore()  ) {
			return;
		}
		
		proposedState.siteRanges.clear();

		if (activeState.child.getActiveRanges().size()==0) {		
			activeState.siteRanges = proposedState.siteRanges;
			return;
		}

		
		//If child is a coal node, our active ranges are whatever its active ranges are
		if (activeState.child.getNumParents()==1) {
			if (activeState.child.getActiveRanges() != null) {
				activeState.child.getActiveRanges().copy(proposedState.siteRanges);
			}
		}
		else { //If child is a recomb node, only the sites that go to us matter, so our active ranges 
				//are the intersection of the child ranges and the sites that go to us
			SiteRange filter = ((RecombNode)activeState.child).getRangeForParent(this);
			if (activeState.child.getActiveRanges() != null) {
				activeState.child.getActiveRanges().copyFilter(filter.getMin(), filter.getMax(), proposedState.siteRanges);
			}
		}
		
		activeState.siteRanges = proposedState.siteRanges;
	}
	
	@Override
	public void proposeParent(int whichParent, ARGNode parent) {
		if (whichParent > 1) {
			throw new IllegalArgumentException("Cannot propose parent #" + whichParent + " for a recomb. node");
		}
		if (whichParent==0) {
			proposedState.parentZero = parent;
			activeState.parentZero = proposedState.parentZero;
		}
		if (whichParent == 1) {
			proposedState.parentOne = parent;
			activeState.parentOne = proposedState.parentOne;
		}
		if (parentARG != null) {
			parentARG.queueChangeEvent(new ARGChangeEvent(ChangeType.StructureChanged, this));
		}
	}
	
	@Override
	public void proposeOffspring(int offspringIndex, ARGNode child) {
		if (offspringIndex > 0) {
			throw new IllegalArgumentException("Cant propose offspring #" + offspringIndex + " for recomb node " + getLabel());
		}
		
		proposedState.child = child;
		activeState.child = proposedState.child;
		if (parentARG != null) {
			parentARG.queueChangeEvent(new ARGChangeEvent(ChangeType.StructureChanged, this));
		}
	}

	@Override
	public void acceptProposal() {
		if (activeState.siteRanges == proposedState.siteRanges) {
			SiteRangeList tmp = proposedState.siteRanges;
			proposedState.siteRanges = currentState.siteRanges;
			currentState.siteRanges = tmp;
		}
		
		currentState.height = activeState.height;
		currentState.parentOne = activeState.parentOne;
		currentState.parentZero = activeState.parentZero;
		currentState.child = activeState.child;
		currentState.recombRange = activeState.recombRange;
	}

	public void proposeRecombRange(BiRange newRange) {
		proposedState.recombRange = newRange;
		activeState.recombRange = proposedState.recombRange;
		if (parentARG != null) {
			parentARG.queueChangeEvent(new ARGChangeEvent(ChangeType.BreakpointChanged, this));
		}
		
		range0.setMin(activeState.recombRange.getMin());
		range0.setMax(activeState.recombRange.getBreakpoint()); 
		range1.setMin(activeState.recombRange.getBreakpoint());
		range1.setMax(activeState.recombRange.getMax());
	}
	
	@Override
	public void rejectProposal() {
		activeState.height = currentState.height;
		activeState.parentOne = currentState.parentOne;
		activeState.parentZero = currentState.parentZero;
		activeState.child = currentState.child;
		activeState.recombRange = currentState.recombRange;
		activeState.siteRanges = currentState.siteRanges;
		
		if (activeState.recombRange != null) {
			range0.setMin(activeState.recombRange.getMin());
			range0.setMax(activeState.recombRange.getBreakpoint()); 
			range1.setMin(activeState.recombRange.getBreakpoint());
			range1.setMax(activeState.recombRange.getMax());
		}
	}

	@Override
	public ARGNode getParentForSite(int site) {
		return activeState.recombRange.indexRangeForSite(site) == 0 ? activeState.parentZero : activeState.parentOne;
	}
	
	/**
	 * Obtain a SiteRange that describes the group of sites that head from this node toward the given parent
	 * @param parent
	 * @return
	 */
	public SiteRange getRangeForParent(ARGNode parent) {
//		if (! this.hasThisParent(parent)) {
//			throw new IllegalArgumentException("Parent " + parent + " is not a parent of this node, " + this);
//		}
		if (parent == activeState.parentZero) {
			return range0; //new SiteRange(activeState.recombRange.getMin(), activeState.recombRange.getBreakpoint());
		}
		else {
			return range1; //new SiteRange(activeState.recombRange.getBreakpoint(), activeState.recombRange.getMax());
		}
	}
	
	@Override
	public SiteRangeList getActiveRanges() {
		return activeState.siteRanges;
	}
	
	/**
	 * Obtain a BiRange object describing this node's current recombination breakpoint
	 * @return
	 */
	public BiRange getRecombRange() {
		return activeState.recombRange;
	}
	
	public String toString() {
		NumberFormat formatter = new DecimalFormat("0.0#");
		if (getHeight()>100)
			formatter = new DecimalFormat("0.0");
		if (getHeight()<0.1)
			formatter = new DecimalFormat("0.0####");
		
		return "Recomb. " + getLabel() + " height: " + formatter.format(getHeight()) + " range: " + activeState.recombRange;
	}

	
	/**
	 * Returns the breakpoint that is in the interior of the sequence... if parentZeroRange is from 0..10, we return 10. 
	 * But if the range is from 50..(end of sequence) we return 50. This is useful if we want to collect a list of 
	 * all interior breakpoints, which define a set of marginal trees.
	 * @return
	 */
	public int getInteriorBP() {
		return activeState.recombRange.getBreakpoint();
	}

	
	@Override
	/**
	 * Return true if anything has been proposed for this node and there has not been a subsequent call
	 * to accept() or reject(). 
	 */
	public boolean isActiveProposed() {
		return (currentState.height != activeState.height) || 
				(currentState.child != activeState.child) ||
				(currentState.parentOne != activeState.parentOne) ||
				(currentState.parentZero != activeState.parentZero) || 
				(currentState.recombRange != activeState.recombRange);
	}
	
	/**
	 * Summarizes the mutable state of this node, which consists of a height, two parents, and one child.
	 * 
	 * @author brendan
	 *
	 */
	class RecombNodeState {
		
		//The distance of this node above the zero reference point
		double height;
		
		//Sites in this range came from parentZero 
		BiRange recombRange;
		
		ARGNode parentZero = null;
		ARGNode parentOne = null;
		ARGNode child = null;
				
		//Keeps track of which sites descend from this node all the way to (observed) tips
		SiteRangeList siteRanges = null;
	}


}
