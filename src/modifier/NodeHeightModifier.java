package modifier;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import arg.ARG;
import arg.ARGNode;
import arg.argIO.ARGParser;
import arg.argIO.ARGWriter;

import math.RandomSource;
import parameter.InvalidParameterValueException;
import tools.ARGMarginalTreeWriter;

/**
 * Another very simple modifier that moves one node up or down in sliding-window fashion. Tunable in theory, but
 * for many cases it seems that having the node be able to slide up and down essentially in the entire range available (from the
 * closest offspring to the closest parent) yields fine acceptance ratios. 
 * @author brendan
 *
 */
public class NodeHeightModifier extends ARGModifier {

	//double windowSize = 0.1;
	
	final boolean verbose = false;
	
	final Double hr = 1.0;
	
	public NodeHeightModifier(Map<String, String> attributes) {
		super(attributes);
		modStr = "Node height";
		if (! attributes.containsKey("frequency"))
			frequency = 10.0;
	}
	
	public NodeHeightModifier() {
		this(new HashMap<String, String>());
	}
	
	private NodeHeightModifier(double frequency) {
		super(new HashMap<String, String>());
		this.frequency = frequency;
	}
	
	public Double modifyARG() throws InvalidParameterValueException, IllegalModificationException, ModificationImpossibleException {
		
		//Some args (with two tips and no recombinations) may have no non-root internal nodes, in this case abort
		if (arg.getInternalNodeCount()==1) {
			throw new ModificationImpossibleException("No internal nodes to modify");
		}
		
		
		List<ARGNode> internalNodes = arg.getInternalNodes();
		int nodeNum = RandomSource.getNextIntFromTo(0, internalNodes.size()-1);
		ARGNode node = internalNodes.get(nodeNum);
		
		//Select a non-root internal node 'at random'		
		while( node.getParent(0)==null ) {
			nodeNum = RandomSource.getNextIntFromTo(0, internalNodes.size()-1);
			node = internalNodes.get(nodeNum);
		}
		
		double minHeight = node.getNumOffspring() == 1
				? node.getOffspring(0).getHeight() 
				: Math.max(node.getOffspring(0).getHeight(), node.getOffspring(1).getHeight());
				
		double maxHeight = node.getNumParents() == 1 
				? node.getParent(0).getHeight() 
				: Math.min( node.getParent(0).getHeight(), node.getParent(1).getHeight() );
		
		double windowSize = maxHeight - minHeight; 
		//Randomly select a new height between min and max with equal probability everywhere
		double newHeight =  minHeight + windowSize*RandomSource.getNextUniform(); 
		
		if (verbose)
			System.out.println("Changing height of " + node + " to : " + newHeight);
		//System.out.println("Modifying node height of node: " + node);
		//System.out.println("New height: " + newHeight);
		
		node.proposeHeight(newHeight);
				
		return hr; //Sliding window, right?  
	}
	
}
