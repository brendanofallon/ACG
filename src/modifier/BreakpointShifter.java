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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import math.RandomSource;

import arg.ARG;
import arg.ARGNode;
import arg.BiRange;
import arg.RecombNode;
import arg.SiteRange;
import arg.argIO.ARGParser;

import parameter.InvalidParameterValueException;

public class BreakpointShifter extends ARGModifier {

	int windowSize = 100;
	
	int windowSizeMin = 10;
	int windowSizeMax = 25000;
	
	public BreakpointShifter() {
		super(new HashMap<String, String>());
	}
	
	public BreakpointShifter(Map<String, String> attributes) {
		super(attributes);
	}
	
	private BreakpointShifter(double freq) {
		super(new HashMap<String, String>());		
		this.frequency = freq;
	}
		
	public BreakpointShifter copy() {
		BreakpointShifter copy = new BreakpointShifter(getFrequency());
		return copy;
	}

	@Override
	/**
	 * Modifies an ARG by selection a recombination node (with uniform probability among all), and shifting
	 * its internal breakpoint by a few bases in one direction or the other.  
	 */
	public Double modifyARG() throws InvalidParameterValueException,
			IllegalModificationException, ModificationImpossibleException {

		if (windowSize > arg.getSiteCount()) {
			windowSize = arg.getSiteCount();
		}
		
		List<RecombNode> rNodes = arg.getRecombNodes();
		if (rNodes.size()==0) {
			throw new ModificationImpossibleException("No recombination nodes to modify");
		}
		
		RecombNode node = rNodes.get( RandomSource.getNextIntFromTo(0, rNodes.size()-1));
		
		int breakpoint = node.getInteriorBP();
		
		int newBreakpoint = breakpoint + (RandomSource.getNextIntFromTo(-windowSize/2, windowSize/2));
		
		if (newBreakpoint < 1) {
			newBreakpoint = -newBreakpoint +1;
		}
		
		if (newBreakpoint >= (arg.getSiteCount()-1)) {
			int dif = newBreakpoint - (arg.getSiteCount()-1);
			newBreakpoint = (arg.getSiteCount()-1)-dif;
		}
		
		if (newBreakpoint < 1 || newBreakpoint >= (arg.getSiteCount()-1)) {
			throw new ModificationImpossibleException("Invalid new bp location");
		}
	
		
		BiRange newRange = new BiRange(0, newBreakpoint, arg.getSiteCount());

		node.proposeRecombRange(newRange);
		
		//Don't bother recalculating range info from here if no sites go through this node
		if (node.getActiveRanges()!= null && node.getActiveRanges().size()>0)
			propogateRangeProposals(node);
		
		if (getCallsSinceReset() > 100 && getTotalCalls() % 100 == 0) {
			changeTuning();
		}
		
		return 1.0;
	}
	
	private void changeTuning() {
		if (getRecentAcceptanceRatio() < lowRatio && windowSize > windowSizeMin) { //We're not accepting many states, so shrink the window
			windowSize *= 0.9;
			//System.out.println("Ratio too small (" + getRecentAcceptances() + "), shrinking window to : " + windowSize);
		}
		if (getRecentAcceptanceRatio() > highRatio && windowSize < windowSizeMax) { //We're accepting too many, grow the window
			windowSize *= 1.1;
			//System.out.println("Ratio too big (" + getRecentAcceptances() + "), growing window to : " + windowSize);
		}
	}

}
