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

import math.RandomSource;

import arg.ARGNode;
import arg.RecombNode;
import arg.SiteRange;

import parameter.InvalidParameterValueException;


/**
 * A simple modifier that changes which sites 'go which way' from a single recombination node.
 * If parent #0 is associated with 0..100, and parent #1 is 100..end, then after modification
 * parent #1 is associated with 0..100, and parent #0 is from 100..end.
 * This only throws a ModificationImpossibleException if there are no RecombNodes to change. 
 * @author brendan
 *
 */
public class BreakpointSwapper extends ARGModifier {

	
	public BreakpointSwapper() {
		this(new HashMap<String, String>());
	}
	public BreakpointSwapper(Map<String, String> attributes) {
		super(attributes);
		if (!attributes.containsKey("frequency")) {
			frequency = 0.5;
		}
	}
	
	private BreakpointSwapper(double freq) {
		super(new HashMap<String, String>());		
		this.frequency = freq;
	}
	
	public BreakpointSwapper copy() {
		BreakpointSwapper copy = new BreakpointSwapper(getFrequency());
		return copy;
	}

	@Override
	public Double modifyARG() throws InvalidParameterValueException,
			IllegalModificationException, ModificationImpossibleException {

		
		List<RecombNode> recombNodes = arg.getRecombNodes();
		if (recombNodes.size()==0) {
			throw new ModificationImpossibleException("No recomb. nodes to modify");
		}
		
		RecombNode rNode = recombNodes.get( RandomSource.getNextIntFromTo(0, recombNodes.size()-1));
		//Swap parent 0 and 1
		ARGNode tmp = rNode.getParent(0);
		rNode.proposeParent(0, rNode.getParent(1));
		rNode.proposeParent(1, tmp);
		

		//Don't bother recalculating range info from here if no sites go through this node
		if (rNode.getActiveRanges() != null && rNode.getActiveRanges().size()>0)
			propogateRangeProposals(rNode);
		
		return 1.0;
	}

}
