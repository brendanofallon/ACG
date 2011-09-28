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
import java.util.Map;

import arg.ARGNode;
import arg.CoalNode;

import math.RandomSource;

import parameter.InvalidParameterValueException;

/**
 * A simple modifier that changes the distance the root node is above its two descendants. 
 * @author brendan
 *
 */
public class RootHeightModifier extends ARGModifier {

	double windowSize = 1.0;

	
	double maxHeight = 30000.0;
	
	final boolean verbose = false;
	
	public RootHeightModifier(Map<String, String> attributes) {
		super(attributes);
		setHighRatio(0.50);
		setLowRatio(0.30);
		if (!attributes.containsKey("frequency")) {
			frequency = 2.0;
		}
	}
	
	public RootHeightModifier() {
		this(new HashMap<String, String>());
	}
	
	private RootHeightModifier(double freq) {
		this();
		this.frequency = freq;
	}
	
	public RootHeightModifier copy() {
		RootHeightModifier copy = new RootHeightModifier(getFrequency());
		return copy;
	}

	@Override
	public Double modifyARG() throws InvalidParameterValueException,
			IllegalModificationException, ModificationImpossibleException {
		
		CoalNode root = arg.getMaxHeightNode();
		double rootHeight = root.getHeight();
		ARGNode leftChild = root.getLeftOffspring();
		ARGNode rightChild = root.getRightOffspring();
	
		if (root.getHeight() > maxHeight) {
			throw new IllegalModificationException("Root height is already greater than max height (root height = " + root.getHeight() + ".");
		}
		
		double minHeight = Math.max(leftChild.getHeight(), rightChild.getHeight() );

		double newHeight = rootHeight + (RandomSource.getNextUniform()-0.5)*windowSize;

		//Reflect over lower boundary
		//Seems like this should create an upward bias in node height, but it doesn't appear to
		if (newHeight < minHeight) {
			newHeight = 2.0*minHeight - newHeight; // = min + (min-newHeight)
		}
			
		if (verbose)
			System.out.println("Proposing root height: " + newHeight);
		
		root.proposeHeight(newHeight);		
		
		if (getCallsSinceReset() > 100 & getTotalCalls() % 50 == 0) {
			changeTuning();
		}

		return 1.0;
	}
	
	
	private void changeTuning() {
		if (getRecentAcceptanceRatio() < lowRatio) { //We're not accepting many states, so shrink the window
			windowSize *= 0.9;
			//System.out.println("Ratio too small (" + getRecentAcceptances() + "), shrinking window to : " + windowSize);
		}
		if (getRecentAcceptanceRatio() > highRatio) { //We're accepting too many, grow the window
			windowSize *= 1.1;
			//System.out.println("Ratio too big (" + getRecentAcceptances() + "), growing window to : " + windowSize);
		}
	}

	
}
