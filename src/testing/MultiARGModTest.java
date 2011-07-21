package testing;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import parameter.InvalidParameterValueException;

import math.RandomSource;
import modifier.BreakpointShifter;
import modifier.BreakpointSwapper;
import modifier.IllegalModificationException;
import modifier.ModificationImpossibleException;
import modifier.Modifier;
import modifier.NodeHeightModifier;
import modifier.RecombAddRemove;
import modifier.SubtreeSwap;
import arg.ARG;
import arg.ARGNode;
import arg.argIO.ARGParseException;
import arg.argIO.ARGParser;

public class MultiARGModTest {

	
	/**
	 * Modify the given ARG a few thousand times with the modifier and see if any exceptions get thrown
	 * @param arg
	 * @param mod
	 * @throws InvalidParameterValueException
	 * @throws IllegalModificationException
	 */
	private void runARG(ARG arg, List<Modifier<ARG>> modList) throws InvalidParameterValueException, IllegalModificationException {
		ARGParser writer = new ARGParser();
		int gens = 100000;
		for(int i=0; i<gens; i++) {
			if (i%1000==0) 
				System.out.println("Running : " + (int)Math.round(100*(double)i/(double)gens) + "% complete rNodes: " + arg.getRecombNodes().size() + " height: " + arg.getMaxHeight());
			try {
				modList.get( RandomSource.getNextIntFromTo(0, modList.size()-1)).modify();
			}
			catch (ModificationImpossibleException mfe) {
				i--;
				continue;
			}
			arg.acceptValue();
			if (i%10000==0) {
				try {
					writer.writeARG(arg, new File("multimod_" + i + ".xml"));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}		
	}
	
	
	@Test
	public void testMultiMod()  {
		RandomSource rng = new RandomSource();
		
		ARGParser parser = new ARGParser();
		List<ARGNode> nodes = null;
		
		/***** Perform a very test to make sure no exceptions are thrown during rearragement *****/
		try {
			nodes = parser.readARGNodes(new File("test/testSwap_large.xml") );
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (ARGParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		ARG arg = new ARG(1000, nodes);
		
//		try {
//			parser.writeARG(arg, new File("nomod_testarg.xml"));
//		} catch (IOException e1) {
//			e1.printStackTrace();
//		}
		
		Modifier<ARG> swapModifier = new SubtreeSwap();
		Modifier<ARG> heightModifier = new NodeHeightModifier();
		Modifier<ARG> bpShifter = new BreakpointShifter();
		Modifier<ARG> bpSwapper = new BreakpointSwapper();
		Modifier<ARG> addRemove = new RecombAddRemove();
		arg.addModifier(swapModifier);
		arg.addModifier(heightModifier);
		//arg.addModifier(bpShifter);
		//arg.addModifier(bpSwapper);
		arg.addModifier(addRemove);
		

		List<Modifier<ARG>> modList = new ArrayList<Modifier<ARG>>();
		modList.add(swapModifier);
		modList.add(heightModifier);
		//modList.add(bpShifter);
		//modList.add(bpSwapper);
		modList.add(addRemove);
		
		try {
			runARG(arg, modList);
		} catch (InvalidParameterValueException e) {
			e.printStackTrace();
			assertTrue( false );
		} catch (IllegalModificationException e) {
			e.printStackTrace();
			assertTrue(false);
		}
		
		assertTrue( true );
		System.out.println("Multi-modifier test 1 completed");
	}
	
}
