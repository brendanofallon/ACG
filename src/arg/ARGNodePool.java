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

import java.util.Stack;

/**
 * Storage for allocated ARGNodes (Coal and Recomb, but not tips) so we're not constantly allocating new ones...
 * @author brendano
 *
 */
public class ARGNodePool {

	//Maximum size of either pool. 
	private int poolSize = 200;
	
	Stack<CoalNode> coalNodePool = new Stack<CoalNode>();
	
	Stack<RecombNode> recombNodePool = new Stack<RecombNode>();
	
	ARG arg;
	
	public ARGNodePool(ARG arg) {
		this.arg = arg;
	}
	
	public CoalNode getCoalNode() {
		if (! coalNodePool.isEmpty()) {
			CoalNode cNode = coalNodePool.pop();
			cNode.currentState.coalescingSites.clear();
			cNode.proposedState.coalescingSites.clear();

			cNode.currentState.descendingSites.clear();
			cNode.proposedState.descendingSites.clear();
			System.out.println("Returning coal node from pool");
			return cNode;
		}
		else {
			CoalNode cNode = new CoalNode(arg);
			System.out.println("Creating new coal node");
			return cNode;
		}
	}
	
	public RecombNode getRecombNode() {
		if (! recombNodePool.isEmpty()) {
			RecombNode rNode = recombNodePool.pop();
			rNode.currentState.siteRanges.clear();
			rNode.proposedState.siteRanges.clear();
			
			System.out.println("Returning recomb node from pool");
			return rNode;
		}
		else {
			RecombNode rNode = new RecombNode(arg);
			System.out.println("Creating new recomb node");
			return rNode;
		}
	}
	
	public void retireNode(ARGNode node) {
		if (node instanceof CoalNode) {
			if (coalNodePool.size() < poolSize) {
				coalNodePool.push((CoalNode)node);
				node.acceptProposal();
			}
			System.out.println("Retiring coal node, pool size: " + coalNodePool.size());
		}
		
		if (node instanceof RecombNode) {
			if (recombNodePool.size() < poolSize) {
				recombNodePool.push((RecombNode)node);
				node.acceptProposal();
			}
			
			System.out.println("Retiring recomb node, pool size: " + recombNodePool.size());			
		}
			
	}
	
	
}
