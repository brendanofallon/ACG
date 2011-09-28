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


package testing;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import math.RandomSource;
import modifier.IllegalModificationException;
import modifier.ModificationImpossibleException;
import modifier.Modifier;
import modifier.SubtreeSwap;

import org.junit.Test;

import parameter.InvalidParameterValueException;

import arg.ARG;
import arg.ARGNode;
import arg.CoalNode;
import arg.TreeUtils;
import arg.argIO.ARGParseException;
import arg.argIO.ARGParser;

public class SubtreeSwapTest {

	/**
	 * Modify the given ARG a few thousand times with the modifier and see if any exceptions get thrown
	 * @param arg
	 * @param mod
	 * @throws InvalidParameterValueException
	 * @throws IllegalModificationException
	 */
	private void runARG(ARG arg, Modifier<ARG> mod) throws InvalidParameterValueException, IllegalModificationException {
		for(int i=0; i<10000; i++) {
			try {
				mod.modify();
			}
			catch (ModificationImpossibleException mfe) {
				i--;
				continue;
			}
			arg.acceptValue();
		}		
	}
	
	private void runARGCountTopos(ARG arg, Modifier<ARG> mod) throws InvalidParameterValueException, IllegalModificationException {
		Map<String, Integer> topoMapA = new HashMap<String, Integer>(); //Counts marginal tree topologies at site 0
		Map<String, Integer> topoMapB = new HashMap<String, Integer>(); //Counts margina tree topologies at site 1000
		for(int i=0; i<100000; i++) {
			try {
				mod.modify();	
			}
			catch (ModificationImpossibleException mfe) {
				i--;
				continue;
			}
			arg.acceptValue();
			String topoA = TreeUtils.getNewick(TreeUtils.createMarginalTree(arg, 0), false) ;
			String topoB = TreeUtils.getNewick(TreeUtils.createMarginalTree(arg, 999), false) ;
			
			Integer currentCount = topoMapA.get(topoA);
			if (currentCount ==  null) {
				topoMapA.put(topoA, 1);
			}
			else {
				topoMapA.put(topoA, currentCount+1);
			}
			
			currentCount = topoMapB.get(topoB);
			if (currentCount ==  null) {
				topoMapB.put(topoB, 1);
			}
			else {
				topoMapB.put(topoB, currentCount+1);
			}
		}	
		
		System.out.println("Counts of observed topologies ");
		for(String topo : topoMapA.keySet() ) {
			System.out.println(topo + " : " + topoMapA.get(topo) + "\t" + topoMapB.get(topo));
		}
		System.out.println("Total number of topologies at site 0 " + topoMapA.size() + "  at site 1000: " + topoMapB.size());
	}
	
	
	@Test
	public void testSmallTree() {
		RandomSource.initialize();
		
		ARGParser parser = new ARGParser();
		List<ARGNode> nodes = null;
		
		/***** Perform a very basic test to make sure no exceptions are thrown during rearragement *****/
		try {
			nodes = parser.readARGNodes(new File("test/testSwap_small.xml") );
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (ARGParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		ARG arg = new ARG(1000, nodes);
		Modifier<ARG> mod = new SubtreeSwap();
		arg.addModifier(mod);
		
		try {
			runARGCountTopos(arg, mod);
		} catch (InvalidParameterValueException e) {
			e.printStackTrace();
			assertTrue( false );
		} catch (IllegalModificationException e) {
			e.printStackTrace();
			assertTrue( false );
		}

		System.out.println("SubtreeSwap test 1 (very small ARG) completed");
		
		
		/************* Another test with a slightly larger ARG ****************************/
		try {
			nodes = parser.readARGNodes(new File("test/testSwap_med.xml") );
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (ARGParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		arg = new ARG(1000, nodes);
		mod = new SubtreeSwap();
		arg.addModifier(mod);
		try {
			runARG(arg, mod);
		} catch (InvalidParameterValueException e) {
			e.printStackTrace();
			assertTrue( false );
		} catch (IllegalModificationException e) {
			e.printStackTrace();
			assertTrue( false );
		}
		
		System.out.println("SubtreeSwap test 2 (small ARG) completed");
		assertTrue( true );
		
		
		
		/************* Another test with a larger ARG, 50 tips and 7 breakpoints *************************/
		try {
			nodes = parser.readARGNodes(new File("test/testSwap_large.xml") );
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (ARGParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		arg = new ARG(1000, nodes);
		mod = new SubtreeSwap();
		arg.addModifier(mod);
		try {
			runARG(arg, mod);
		} catch (InvalidParameterValueException e) {
			e.printStackTrace();
			assertTrue( false );
		} catch (IllegalModificationException e) {
			e.printStackTrace();
			assertTrue( false );
		}

		System.out.println("SubtreeSwap test 3 (large ARG) completed");
		assertTrue( true );
	}
	
	
	public static void main(String[] args) {
		SubtreeSwapTest test = new SubtreeSwapTest();
		test.testSmallTree();
	}
}
