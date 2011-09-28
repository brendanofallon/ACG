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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import arg.ARG;
import arg.ARGNode;
import arg.BiRange;
import arg.CoalNode;
import arg.RecombNode;
import parameter.InvalidParameterValueException;

import math.RandomSource;

public class TrivialAddRemove extends ARGModifier {

	boolean verbose = false;
	
	double jacobian = 2.6;
	
	public TrivialAddRemove() {
		this(new HashMap<String, String>());
	}
	
	public TrivialAddRemove(Map<String, String> attributes) {
		super(attributes);
		if (! attributes.containsKey("frequency"))
			frequency = 3.0;
	}
	
	
	@Override
	protected Double modifyARG() throws InvalidParameterValueException,
			IllegalModificationException, ModificationImpossibleException {
		
		double hastingsRatio;
		
		if (RandomSource.getNextUniform() < 0.5) {
			hastingsRatio = addBranch(arg);
		}
		else {
			hastingsRatio = removeBranch(arg);
		}
		
		return hastingsRatio;	
		
	}
	
	/**
	 * Select a branch from all branches (not just those with a recomb node at the base), and insert 
	 * a trivial recombination into it
	 * @param arg
	 * @return
	 */
	private double addBranch(ARG arg) {
		//Select a branch at random from all branches....
		List<Branch> branches = collectAllBranches();
		
		Branch b = branches.get(RandomSource.getNextIntFromTo(0, branches.size()-1));
		
		RecombNode rNode = new RecombNode(arg);
		CoalNode cNode = new CoalNode(arg);
		
		rNode.proposeParent(0, cNode);
		rNode.proposeParent(1, cNode);
		int breakpoint = RandomSource.getNextIntFromTo(1, arg.getSiteCount()-1);
		BiRange range = new BiRange(0, breakpoint, arg.getSiteCount());
		rNode.proposeRecombRange(range);
		
		cNode.proposeOffspring(0, rNode);
		cNode.proposeOffspring(1, rNode);
		
		double branchLength = b.parent.getHeight() - b.child.getHeight();
		
		double rNodeHeight = b.child.getHeight() + 0.5*RandomSource.getNextUniform()*branchLength;
		double cNodeHeight = b.child.getHeight() + (0.5+0.5*RandomSource.getNextUniform())*branchLength;
		
		rNode.proposeHeight(rNodeHeight);
		cNode.proposeHeight(cNodeHeight);
		
		rNode.proposeOffspring(0, b.child);
		int pIndex = whichParent(b.child, b.parent);
		int cIndex = whichChild(b.child, b.parent);
		
		b.child.proposeParent( pIndex, rNode);
		b.parent.proposeOffspring(cIndex, cNode);
		cNode.proposeParent(b.parent);
		rNode.proposeOffspring(0, b.child);
		
		arg.proposeNodeAdd(rNode);
		arg.proposeNodeAdd(cNode);
		
		propogateRangeProposals(rNode);
		
		if (verbose)
			System.out.println("Adding branch between " + b.child + " and " + b.parent);
		
		double probThisMove = 2.0/branchLength * 2.0/branchLength / (double)branches.size();
		double probReverseMove = 1.0 / countTrivialRecombs();
		return jacobian * probReverseMove / probThisMove;
	}
	
	private double removeBranch(ARG arg) throws ModificationImpossibleException {
		List<RecombNode> removeables = collectTrivialRecombs();
		if (removeables.size()==0)
			throw new ModificationImpossibleException("No trivial recombinations to remove");
		
		RecombNode rToRemove = removeables.get( RandomSource.getNextIntFromTo(0, removeables.size()-1));
		CoalNode cToRemove = (CoalNode)rToRemove.getParent(0);
		
		ARGNode newChild = rToRemove.getOffspring(0);
		ARGNode newParent = cToRemove.getParent(0);
		
		int pIndex = whichParent(newChild, rToRemove);
		int cIndex = whichChild(cToRemove, newParent);
		
		newChild.proposeParent(pIndex, newParent);
		newParent.proposeOffspring(cIndex, newChild);
		
		arg.proposeNodeRemove(rToRemove);
		arg.proposeNodeRemove(cToRemove);
		
		propogateRangeProposals(newChild);
		
		double probThisMove = 1.0 / removeables.size();
		double branchLength = newParent.getHeight() - newChild.getHeight();
		double probReverseMove = 1.0/ (collectAllBranches().size()+1.0) * 2.0/branchLength * 2.0/branchLength;
		

		if (verbose)
			System.out.println("Removing branch involving " + rToRemove + " and " + cToRemove);
		
		return 1.0 / jacobian * probReverseMove / probThisMove;
	}



	/**
	 * Collects all recomb nodes where both parents are the same coal node, and hence are at the base
	 * of a 'trivial recombination'
	 * @return
	 */
	private List<RecombNode> collectTrivialRecombs() {
		List<RecombNode> nodes = new ArrayList<RecombNode>();
		for(RecombNode node : arg.getRecombNodes()) {
			if (node.getParent(0) == node.getParent(1)) {
				nodes.add(node);
			}
		}
		return nodes;
	}



	private int countTrivialRecombs() {
		int count = 0;
		for(RecombNode node : arg.getRecombNodes()) {
			if (node.getParent(0) == node.getParent(1)) {
				count++;
			}
		}
		return count;
	}


	private List<Branch> collectAllBranches() {
		List<Branch> branches = new ArrayList<Branch>();
		
		for(ARGNode node : arg.getAllNodes()) {
			if (node.getParent(0)==null) //No branch for the parent
				continue; 
			
			Branch b = new Branch();
			b.child = node;
			b.parent = node.getParent(0);
			branches.add(b);
			
			if (node.getNumParents()==2) {
				Branch b2 = new Branch();
				b2.child = node;
				b2.parent = node.getParent(1);
				branches.add(b2);
			}
		}
		
		
		return branches;
	}
	
	/**
	 * Just a container for things into which we may add some trivial recombinations
	 * @author brendano
	 *
	 */
	class Branch {
		
		ARGNode parent;
		ARGNode child;
	}
	
	
//	public static void main(String[] args) {
//		RandomSource.initialize( 5876 );
//		
//		Map<String, String> attrs = new HashMap<String, String>();
//		attrs.put("tips", "4");
//		attrs.put("sites", "1000");
//		ARG arg = new ARG(attrs);
//		
//		ARGModifier tar = new TrivialAddRemove();
//		arg.addModifier(tar);
//		
//		ARGParser writer = new ARGParser();
//		
//		for(int i=0; i<100; i++) {
//			try {
//				if (i%5==0)
//					writer.writeARG(arg, new File("tar_test_" + i + ".xml"));
//				tar.modify();
//				arg.acceptValue();
//			} catch (InvalidParameterValueException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (IllegalModificationException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (ModificationImpossibleException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		
//		}
//	}

}
