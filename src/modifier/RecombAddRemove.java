package modifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cern.jet.random.Exponential;

import arg.ARG;
import arg.ARGIntervals;
import arg.ARGNode;
import arg.BiRange;
import arg.CoalNode;
import arg.RecombNode;
import math.RandomSource;

import parameter.InvalidParameterValueException;

/**
 * Adds and removes recombination nodes to an ARG. New 'branches' consisting of a new recombination node and a new coalescent
 * node are added by selecting random heights (from exponential distributions), and then selecting random lineages at
 * those heights to insert into. Branches are removed by collecting all branches that are possible to remove (with a 
 * recomb node at base and coalescent node as parent), and picking one at random to remove. 
 * @author brendan
 *
 */
public class RecombAddRemove extends ARGModifier {

	//Probability that a node is added. Removal probability is 1-addProb. Setting this to anything other than 0.50 
	//will violate all principles of reversibility. 
	final double addProb = 0.50;
	
	final boolean verbose = false;
	
	final double jacobian = 2.0;
	
	final double rScaleFactor = 2.0;
	final double cScaleFactor = 2.0;
	private final double rDivisor = 1.0-Math.exp(-rScaleFactor);
	
	//Used to select new node heights
	Exponential rExpRng; //For the recomb node
	Exponential cExpRng; //for the new coal node
	
	
	public RecombAddRemove() {
		this(new HashMap<String, String>());	
	}
	
	public RecombAddRemove(Map<String, String> attributes) {
		super(attributes);
		rExpRng = new Exponential(1.0, RandomSource.getEngine());// We modify the mean later
		cExpRng = new Exponential(1.0, RandomSource.getEngine());// same here

		if (! attributes.containsKey("frequency"))
			frequency = 2.0;
	}
	
	private RecombAddRemove(double freq) {
		super(new HashMap<String, String>());
		this.frequency = freq;
	}
	

	@Override
	public Double modifyARG() throws InvalidParameterValueException,
			IllegalModificationException, ModificationImpossibleException {
				
		double hastingsRatio;
		
		if (RandomSource.getNextUniform() < addProb) {
			hastingsRatio = addBranch(arg);
		}
		else {
			hastingsRatio = removeBranch(arg);
		}
		
		return hastingsRatio;	
	}
	
	/**
	 * Adds a 'branch', consisting of one RecombNode and one CoalNode, to the ARG. 
	 * We select a random height to insert the new RecombNode and a random height at which
	 * to insert the additional coalNode it induces above it. The new coalnode may be above the current root. 
	 * 
	 * Hastings ratio (defined somewhat generally here) is 
	 *  (probability of reverse move) / (probability of this move)
	 *  
	 *  Prob of reverse move is prob that the 'Branch' created by this is removed by removeBranch. A bit tricky since
	 * some branches cannot be removed (if a branch has a recombNode for a parent, it cannot be removed), so we 
	 * count the number of removeableBranches to find out the chance that the branch is removed. 
	 *  
	 * Probability of this move is the probability that the two node heights we selected were selected, and that the 
	 * lineages we inserted into were selected. Node heights are taken directly from an exponential pdf, and the number
	 * of lineages that cross at the certain time is found by arg.getBranchesCrossingTime(height). 
	 *  
	 * 
	 * Code could probably be compressed (many cases are similar), but is in relatively readable form right now. 
	 * 
	 * @param arg
	 * @return
	 * @throws ModificationImpossibleException 
	 */
	private double addBranch(ARG arg) throws ModificationImpossibleException {
		RecombNode rNode = new RecombNode(arg);
		CoalNode cNode = new CoalNode(arg);
		
		double argHeight = arg.getMaxHeight();
		
		rExpRng.setState(1.0/(argHeight/rScaleFactor));
		cExpRng.setState(1.0/(argHeight/cScaleFactor));
		
		double rNodeHeight = rExpRng.nextDouble(); 
		while (rNodeHeight > argHeight)
			rNodeHeight = rExpRng.nextDouble();
		double cNodeHeight = rNodeHeight + cExpRng.nextDouble();
				
		List<ARGNode> rNodeBottoms = arg.getBranchesCrossingTime(rNodeHeight);
		List<ARGNode> cNodeBottoms = arg.getBranchesCrossingTime(cNodeHeight); 
		
		if (rNodeBottoms.size()==0) {
			throw new IllegalArgumentException("Could not find any branches crossing time: " + rNodeHeight);
		}
		if (cNodeBottoms.size()==0 && cNodeHeight<argHeight) {
			throw new IllegalArgumentException("Could not find any branches crossing time: " + cNodeHeight);
		}
		
		//The only child of the rNode we are inserting
		ARGNode rNodeChild = rNodeBottoms.get( RandomSource.getNextIntFromTo(0, rNodeBottoms.size()-1));
		
		ARGNode rDisplacedParent = findDisplaceableParent(rNodeChild, rNodeHeight); //Parent we are displacing from rNodeChild, if rNodeChild is a recombNode, could be either one or two depending on heights
		
		rNode.proposeHeight(rNodeHeight);
		cNode.proposeHeight(cNodeHeight);
		int breakpoint = RandomSource.getNextIntFromTo(1, arg.getSiteCount()-1);
		BiRange range = new BiRange(0, breakpoint, arg.getSiteCount());
		rNode.proposeRecombRange(range);
		
		//Used to assign indices to new parents, this randomizes which range of sites go to which parent
		int firstParent = RandomSource.getNextIntFromTo(0, 1);
		int secondParent = 1-firstParent;
		
		//Perform an early check to see if we're placing the new coal node above the root. If so
		//we can skip much of the rest. 
		if (cNodeHeight > argHeight) {
			int rpIndex = whichParent(rNodeChild, rDisplacedParent);
			int rcIndex = whichChild(rNodeChild, rDisplacedParent);

			rNodeChild.proposeParent(rpIndex, rNode);
			rNode.proposeOffspring(0, rNodeChild);
			rNode.proposeParent(firstParent, rDisplacedParent);
			rNode.proposeParent(secondParent, cNode);
			rDisplacedParent.proposeOffspring(rcIndex, rNode);
			
			CoalNode prevRoot = arg.getMaxHeightNode();
			int p0 = RandomSource.getNextIntFromTo(0, 1);
			cNode.proposeOffspring(p0, prevRoot);
			cNode.proposeOffspring(1-p0, rNode);
			cNode.proposeParent(0, null);
			prevRoot.proposeParent(cNode);
		} 
		else {

			ARGNode cNodeChild = cNodeBottoms.get( RandomSource.getNextIntFromTo(0, cNodeBottoms.size()-1));
			ARGNode cDisplacedParent = findDisplaceableParent(cNodeChild, cNodeHeight);

			if (rNodeChild == cNodeChild) {
				if (rDisplacedParent == cDisplacedParent) {
					//Easiest case, we're making a trivial recombination on a single branch
					int pIndex = whichParent(rNodeChild, rDisplacedParent);
					int cIndex = whichChild(cNodeChild, cDisplacedParent);
					
					rNodeChild.proposeParent(pIndex, rNode);
					rNode.proposeOffspring(0, rNodeChild);
					rNode.proposeParent(firstParent, cNode);
					rNode.proposeParent(secondParent, cNode);
					

					int p0 = RandomSource.getNextIntFromTo(0, 1);
					cNode.proposeOffspring(p0, rNode);
					cNode.proposeOffspring(1-p0, rNode);
					cNode.proposeParent(0, cDisplacedParent);
					cDisplacedParent.proposeOffspring(cIndex, cNode);
				}
				else {
					//Slightly more complicated case, inserted branch shares a single bottom, but has different parents
					int rcIndex = whichChild(rNodeChild, rDisplacedParent);
					int ccIndex = whichChild(cNodeChild, cDisplacedParent);
					
					rNodeChild.proposeParent(0, rNode);
					rNode.proposeOffspring(0, rNodeChild);
					rNode.proposeParent(firstParent, rDisplacedParent);
					rNode.proposeParent(secondParent, cNode);
					rDisplacedParent.proposeOffspring(rcIndex, rNode);

					rNodeChild.proposeParent(1, cNode);

					int p0 = RandomSource.getNextIntFromTo(0, 1);
					cNode.proposeOffspring(p0, rNodeChild);
					cNode.proposeOffspring(1-p0, rNode);
					cNode.proposeParent(0, cDisplacedParent);
					cDisplacedParent.proposeOffspring(ccIndex, cNode);
				}

			}
			else { //cNode and rNode do not share a child 

				//Not inserting above root and no shared children, but check to see if shared parent
				if (cDisplacedParent == rDisplacedParent) {
					int rpIndex = whichParent(rNodeChild, rDisplacedParent);
					int cpIndex = whichParent(cNodeChild, cDisplacedParent);

					rNodeChild.proposeParent(rpIndex, rNode);
					rNode.proposeOffspring(0, rNodeChild);
					rNode.proposeParent(firstParent, rDisplacedParent);
					rNode.proposeParent(secondParent, cNode);
					rDisplacedParent.proposeOffspring(0, rNode);

					cNodeChild.proposeParent(cpIndex, cNode);

					int p0 = RandomSource.getNextIntFromTo(0, 1);
					cNode.proposeOffspring(p0, cNodeChild);
					cNode.proposeOffspring(1-p0, rNode);
					cNode.proposeParent(cDisplacedParent);
					cDisplacedParent.proposeOffspring(1, cNode);
				}
				else {
					//No shared children and no shared parents and not inserting above root
					int rpIndex = whichParent(rNodeChild, rDisplacedParent);
					int cpIndex = whichParent(cNodeChild, cDisplacedParent);
					int rcIndex = whichChild(rNodeChild, rDisplacedParent);
					int ccIndex = whichChild(cNodeChild, cDisplacedParent);

					rNodeChild.proposeParent(rpIndex, rNode);
					rNode.proposeOffspring(0, rNodeChild);
					rNode.proposeParent(firstParent, rDisplacedParent);
					rNode.proposeParent(secondParent, cNode);
					rDisplacedParent.proposeOffspring(rcIndex, rNode);

					cNodeChild.proposeParent(cpIndex, cNode);

					int p0 = RandomSource.getNextIntFromTo(0, 1);
					cNode.proposeOffspring(p0, cNodeChild);
					cNode.proposeOffspring(1-p0, rNode);
					cNode.proposeParent(cDisplacedParent);
					cDisplacedParent.proposeOffspring(ccIndex, cNode);
				}

			}
		}
		
		if (verbose) {
			System.out.println("Adding branch : " + rNode + " --- " + cNode);
		}

		arg.proposeNodeAdd(rNode);
		arg.proposeNodeAdd(cNode);
		
		//Alert all nodes rootward of nodes added that their site range information may have changed
		propogateRangeProposals(rNode);
		//propogateRangeProposals(cNode); //Not actually necessary since cNode is a parent of rNode
		
		double probThisMove;
		probThisMove = rExpRng.pdf(rNodeHeight) * cExpRng.pdf(cNodeHeight-rNodeHeight) / rDivisor;
		
		double probReverseMove = 1.0/ (double)countRemoveableBranches();
		
		if (verbose) {
			System.out.println("Adding nodes: " + rNode + "\t\t" + cNode);
			System.out.println("Adding branch, prob reverse move: " + probReverseMove + " prob this move:" + probThisMove);
		}
		
		
		double lineagesFactor = 1.0/(rNodeBottoms.size());
		if (cNodeHeight < argHeight)
			lineagesFactor *= 1.0/(cNodeBottoms.size());
		
		
		probThisMove *= lineagesFactor;
		
		double hastingsRatio = probReverseMove / probThisMove;
		return jacobian*hastingsRatio;
	}

	/**
	 * Remove a branch from the ARG by collecting all 'removeable' Branches, and selecting one uniformly, and eliminating it.
	 * This may remove the root, in which case the next highest coal node is made the root. Hastings ratio is essentially
	 * the reverse of the add move. 
	 * 
	 * @param arg ARG to which to add the branch
	 * @return The hastings ratio of this move
	 * @throws ModificationImpossibleException If no branches can be removed
	 */
	private double removeBranch(ARG arg) throws ModificationImpossibleException {
		List<Branch> branches = collectRemoveableBranches();
	
		if (branches.size()==0) {
			//FIX 8/11/2011 Returning -inf here fixes a minor inconsistency in the ARG prior, with
			//a bit too much mass in the low end of the distribution of the number recombination breakpoints
			return Double.NEGATIVE_INFINITY;
			//throw new ModificationImpossibleException("No branches can be removed"); Old version, not as accurate
		}
		
		//Select a branch to remove at random from all branches that can be removed
		Branch branchToRemove = branches.get( RandomSource.getNextIntFromTo(0, branches.size()-1));
		RecombNode toRemove = branchToRemove.rNode; 
				
		CoalNode parentToRemove = branchToRemove.cNode;
		ARGNode parentNotRemoved; //The other parent which we will not remove, may be the same as parentToRemove
		
		//Find the other parent of the recomb node, the parent we're not removing
		if (toRemove.getParent(0)==parentToRemove)
			parentNotRemoved = toRemove.getParent(1);
		else
			parentNotRemoved = toRemove.getParent(0);
			
		boolean rootRemoved = false; //This gets set if we remove the root node, which we must know for hastings ratio calculation
		

		double recNodeHeight = toRemove.getHeight(); 
		double coalNodeHeight = parentToRemove.getHeight();
		
		ARGNode rangeUpdateNode = null; //Reference to additional node whose range info we will update after structural changes
		
		//A few special cases follow
		//1. If toRemove is at the base of a 'trivial coalescent' where both parents are the same
		//coal node, then a bit of special manuevering is required
		if (parentToRemove == parentNotRemoved) {
			ARGNode child = toRemove.getOffspring(0);
			ARGNode newParent = parentToRemove.getParent(0);
			
			//This would be a trivial coalescent above the MRCA, which should never happen 
			if (newParent == null)  
				throw new IllegalStateException("Trivial coalescent above MRCA, this should never happen");


			int pIndex = whichParent(child, toRemove);
			int cIndex = whichChild(parentToRemove, newParent);
			child.proposeParent(pIndex, newParent);
			newParent.proposeOffspring(cIndex, child);

			// No need to worry about range information here, since trivial coalescents never change it. Right?
		}
		else { //Not a trivial coalescent
			ARGNode newParent = parentToRemove.getParent();
			
			//2. parentToRemove may be the root, in which case a new root node is created, and a bit more
			//special manuevering is necessary
			if (newParent == null) {
				//Parent to remove must be the root. Its child that is not toRemove will be the new root. Right?
				int otherIndex = 1-whichChild(toRemove, parentToRemove);
				ARGNode newRoot = parentToRemove.getOffspring(otherIndex);
				rootRemoved = true;
				if (newRoot instanceof CoalNode) {
					newRoot.proposeParent(0, null);
				}
				else {
					throw new IllegalStateException("Other child of root is not a coal node??");
				}
			}
			else {
				//Remove parentToRemove and attach the non-toRemove child to parentToRemove's only parent
				int otherIndex = 1-whichChild(toRemove, parentToRemove);
				removeNode(parentToRemove, parentToRemove.getOffspring(otherIndex));
				rangeUpdateNode = parentToRemove.getOffspring(otherIndex);
			}

			removeNode(toRemove, parentNotRemoved);
		}
		
		if (verbose) {
			if (parentToRemove.getParent(0)==null)
				System.out.println("Removing branch : " + toRemove + " --- " + parentToRemove + " (the root)");
			else
				System.out.println("Removing branch : " + toRemove + " --- " + parentToRemove);
		}
				
		arg.proposeNodeRemove(toRemove);
		arg.proposeNodeRemove(parentToRemove);
		
		
		//Alert all nodes rootward of nodes removed that their site range information may have changed
		propogateRangeProposals(toRemove.getOffspring(0));
		propogateRangeProposals(parentNotRemoved);
		if (rangeUpdateNode != null)
			propogateRangeProposals(rangeUpdateNode);
		
		//Prob this move is the probability that we pick this branch to remove. This is the same as the probability that
		//we pick the given recomb node toRemove and it's parent parentToRemove
		double probThisMove = 1.0 / ((double)branches.size()); //This may be altered below
		
		//The probability that this move is reversed is the probability that we add a new branch at exactly the
		//spots where we removed it. This must take into account both the height of the nodes we removed as well
		//as the number of lineages that crossed those heights
		
		double newHeight = arg.getMaxHeight();
		rExpRng.setState(1.0/(newHeight/rScaleFactor));
		cExpRng.setState(1.0/(newHeight/cScaleFactor));
		
		double probReverseMove = rExpRng.pdf(recNodeHeight) * cExpRng.pdf(coalNodeHeight-recNodeHeight) / rDivisor;		

		double rLineageCount = arg.getBranchesCrossingTime(toRemove.getHeight()).size();
		double lineagesFactor = 1.0/rLineageCount;
		if (!rootRemoved)
			lineagesFactor *= 1.0/arg.getBranchesCrossingTime(parentToRemove.getHeight()).size();
			
		probReverseMove *= lineagesFactor;
				
		//Try seeing if this removal would create an "Extraneous node group", and if so disallow it. 
		double hastingsRatio = probReverseMove / probThisMove;
		return 1.0/jacobian*hastingsRatio;
	}


	
	/**
	 * Removes the given node, and attaches the provided parent to the offspring of the recomb. node
	 * @param node
	 * @param parent
	 */
	private static void removeNode(RecombNode node, ARGNode parent) {
		ARGNode child = node.getOffspring(0);
		int pIndex = whichParent(child, node);
		int cIndex = whichChild(node, parent);
		child.proposeParent(pIndex, parent);
		parent.proposeOffspring(cIndex, child);
		//System.out.println("\t Removing node " + node + " and attaching child " + child + " to " + parent);
	}
	
	/**
	 * Removes the given coal node and attaches the child node to the parent of the coal node
	 * @param node
	 * @param child
	 */
	private static void removeNode(CoalNode node, ARGNode child) {
		ARGNode newParent = node.getParent(0);
		int pIndex = whichParent(child, node);
		int cIndex = whichChild(node, newParent);
		child.proposeParent(pIndex, newParent);
		newParent.proposeOffspring(cIndex, child);
		//System.out.println("\t Removing node " + node + " and attaching " + child + " to " + newParent);
	}
	
	/**
	 * Create a list of all branches which are removeable
	 * @return
	 */
	private List<Branch> collectRemoveableBranches() {
		List<Branch> branches = new ArrayList<Branch>();
		List<RecombNode> recombNodes = arg.getRecombNodes();
		List<Double> badTimes = collectUnremoveableTimes();
		for(RecombNode rNode : recombNodes) {
			if (rNode.getParent(0) instanceof CoalNode) {
				Branch branch = new Branch(rNode, (CoalNode)rNode.getParent(0));
				if ( branchIsRemoveable(branch, badTimes) )
					branches.add(branch);
			}
			if (rNode.getParent(1) instanceof CoalNode) {
				Branch branch = new Branch(rNode, (CoalNode)rNode.getParent(1));
				if ( branchIsRemoveable(branch, badTimes) )
					branches.add(branch);
			}
		}
		return branches;
	}

	/**
	 * Creates a list of times (arg depths) at which the lineage count
	 * is only 2. Removing branches that cross such times is illegal. 
	 * @return
	 */
	private List<Double> collectUnremoveableTimes() {
		List<Double> times = new ArrayList<Double>();
		ARGIntervals intervals = arg.getIntervals();
		for(int i=0; i<intervals.getIntervalCount()-1; i++) {
			if (intervals.getLineageCount(i)<3) {
				double avTime = (intervals.getIntervalStartTime(i)+intervals.getIntervalEndTime(i))/2.0;
				times.add(avTime);
			}
		}
		
		return times;
	}

	
	private boolean intervalIsRemoveable(double startTime, double endTime, List<Double> badTimes) {
		for(Double badTime : badTimes) {
			if (startTime <= badTime && endTime > badTime)
				return false;
		}
		
		return true;
	}
	
    private int countRemoveableBranches() {
		int sum = 0;
		List<RecombNode> recombNodes = arg.getRecombNodes();
		List<Double> badTimes = collectUnremoveableTimes();
		for(RecombNode rNode : recombNodes) {
			if (rNode.getParent(0).getNumParents()==1) {
				//Branch branch = new Branch(rNode, (CoalNode)rNode.getParent(0));
				if ( intervalIsRemoveable(rNode.getHeight(), rNode.getParent(0).getHeight(), badTimes) )
					sum++;
			}
			if (rNode.getParent(1).getNumParents()==1) {
				//Branch branch = new Branch(rNode, (CoalNode)rNode.getParent(1));
				if ( intervalIsRemoveable(rNode.getHeight(), rNode.getParent(1).getHeight(), badTimes) )
					sum++;
			}
		}
		return sum;
	}

	/**
	 * Returns true if removing the branch connecting the given nodes will 
	 * NOT result in an internal interval with a single lineage
	 * @param rNode
	 * @param cNode
	 * @return
	 */
	private boolean branchIsRemoveable(Branch branch, List<Double> badTimes) {
		return intervalIsRemoveable(branch.rNode.getHeight(), branch.cNode.getHeight(), badTimes);
            //When there are many (like 500) nodes, we spend tons of time doing this (like 10-25% of total runtime)
            //TODO Optimization : If we knew at which times there existed intervals with only
            //two lineages, we wouldn't have to scan across all intervals to see if 
            //individual branches crossed the time. Alternatively, we could binary search
            //for the start and end of an interval, given a time, since times are sorted
            //Finally, we could pre-test to see if any lineages

//
//                int interval = 0;
//                while( intervals.getIntervalStartTime(interval) < startTime && interval < intervals.getIntervalCount()-1)
//                    interval++;
//
//                while( intervals.getIntervalEndTime(interval)<=endTime && interval < intervals.getIntervalCount()-1) {
//                    if (intervals.getLineageCount(interval)==2)
//                        return false;
//                    interval++;
//                }
//
//                return true;

		//Do any intervals between startTime and endTime have count of two (or less?)
		//if so we cant remove this branch
		//Its IMPERATIVE that we DONT'T INCLUDE THE FINAL INTERVAL here, since doing so means we'll NEVER 
		//remove the root (since removing it will create an 'interval' with less than two lineages)
//		ARGIntervals intervals = arg.getIntervals();
//		double startTime = branch.rNode.getHeight();
//		double endTime = branch.cNode.getHeight();
//		for(int i=0; i<intervals.getIntervalCount()-1; i++) {
//			if ( intervals.getIntervalStartTime(i)>=startTime) {
//				if (intervals.getIntervalEndTime(i)<=endTime) {
//					if (intervals.getLineageCount(i)<=2) //This interval is between start and end time
//						return false;					 //If there are two or less lineages in it we cant remove the branch
//				}
//				else {
//					break; //Past end time, no need to look further
//				}
//			}
//		}
//		return true;
	}
		

	/**
	 * Just a container for a recomb node and a coal node that define a branch. It's
	 * illegal to make one of these in which cNode is not a parent of rNode
	 * @author brendan
	 *
	 */
	class Branch {
		
		RecombNode rNode;
		CoalNode cNode;
		
		public Branch(RecombNode rNode, CoalNode cNode) {
			if ( rNode.getParent(0)!=cNode && rNode.getParent(1)!=cNode) {
				throw new IllegalArgumentException("CoalNode is not a parent of Recomb. Node");
			}
			
			this.rNode = rNode;
			this.cNode = cNode;
		}
	}

	

}
