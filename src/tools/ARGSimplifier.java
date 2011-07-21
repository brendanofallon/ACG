package tools;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import modifier.ARGModifier;

import arg.ARG;
import arg.ARGNode;
import arg.RecombNode;
import arg.argIO.ARGParseException;
import arg.argIO.ARGParser;

/**
 * Removes branches from an ARG that either are not ancestral to sites or are trivial
 * @author brendano
 *
 */
public class ARGSimplifier {

	
	public ARG cloneARG(ARG source) {
		//Fairly kludgy, write to a temporary file and read back in a new arg....
		int rnd = (int)(10000*Math.random());
		File tmpFile = new File(".argtmp" + rnd + ".xml");
		ARGParser parser = new ARGParser();
		ARG newARG = null;
		try {
			parser.writeARG(source, tmpFile);
			newARG = parser.readARG(tmpFile);
		} catch (IOException e) {
			System.out.println("There was an error cloning an ARG");
			e.printStackTrace();
		} catch (ARGParseException e) {
			System.out.println("There was an error cloning an ARG");
			e.printStackTrace();
		}
		
		tmpFile.delete();
		
		return newARG;
	}
	
	/**
	 * Returns a new ARG that is a 
	 * @param source
	 * @return
	 */
	public ARG cloneAndSimplify(ARG source) {
		ARG arg = cloneARG(source);
		
		int totalRemoved = 0;
		int passes = 0;
		int removed = 1;
		while (removed > 0) {
			removed = removeTrivialRecombs(arg);
			totalRemoved += removed;
			passes++;
			System.out.println("Pass " + passes + ", removed " + removed);
		}
		passes--;
		if (passes==1)
			System.out.println("Removed " + totalRemoved + " trivial recombinations in " + passes + " pass.");
		else
			System.out.println("Removed " + totalRemoved + " trivial recombinations in " + passes + " passes.");
		return arg;
	}
	
	/**
	 * Remove all 'simple' trivial recombinations from the given arg. Note that
	 * this may leave an ARG that still has some trivial recombinations, since removing the simplest
	 * trivial recombs may 'uncover' allow more complex ones to be detected. Calling this
	 * multiple times is recommended to remove all trivial recombs. 
	 * @param arg
	 * @return
	 */
	private static int removeTrivialRecombs(ARG arg) {
		List<RecombNode> rNodes = new ArrayList<RecombNode>();
		rNodes.addAll(arg.getRecombNodes());
		
		int count = 0;
		for(RecombNode rNode : rNodes) {
			if (rNode.getParent(0) == rNode.getParent(1)) {
				ARGNode topNode = rNode.getParent(0).getParent(0);
				int whichTop = ARGModifier.whichChild(rNode.getParent(0), topNode);
				ARGNode bottomNode = rNode.getOffspring(0);
				int whichBottom = ARGModifier.whichParent(bottomNode, rNode);
				
				bottomNode.proposeParent(whichBottom, topNode);
				topNode.proposeOffspring(whichTop, bottomNode);
				
				arg.proposeNodeRemove(rNode);
				arg.proposeNodeRemove(rNode.getParent(0));
				
				arg.acceptValue();
				count++;
			}
		}
		
		return count;
	}
	
	public static void main(String[] args) {
		ARGSimplifier simp = new ARGSimplifier();
		ARGParser parser = new ARGParser();
		try {
			//ARG arg = parser.readARG(new File("test/testARG.xml"));
			ARG arg = parser.readARG(new File("batch_test/N250_mu4e6_R01/N250_mu4e6_R01_1.xml"));
			
			parser.writeARG(arg, new File("cloned_arg.xml"));
			
			//ARG cloned = simp.cloneAndSimplify(arg);
			//parser.writeARG(cloned, new File("cloned_arg.xml"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ARGParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
}
