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


package tools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import arg.ARGNode;
import arg.CoalNode;

/**
 * A simple utility class to represent multifurcating, but not recombining, trees. Nodes
 * may have more than two offspring, but only one parent.
 * Right now this is just used by ConsensusTreeBuilder 
 * @author brendan
 *
 */
public class Tree {

	Node root = null;
	
	public Tree() {
	}
	
	public Tree(CoalNode cRoot) {
		cloneFromARG(cRoot);
	}
	
	/**
	 * Returns the number of nodes in the tree with zero offspring
	 * @return
	 */
	public int getTipCount() {
		int count = 0;
		Stack<Node> stack = new Stack<Node>();
		stack.push(root);
		
		while(! stack.isEmpty()) {
			Node n = stack.pop();
			if (n.getNumOffspring()==0)
				count++;
			for(int i=0; i<n.getNumOffspring(); i++) {
				stack.push(n.getOffspring(i));
			}
		}
		
		
		return count;
	}
	
	public Node getRoot() {
		return root;
	}
	
	public void setRoot(Node root) {
		this.root = root;
	}
	
	public Node createNode(String label) {
		return new Node(label);
	}
	
	/**
	 * Return a list of all nodes in this tree. 
	 * @return
	 */
	public List<Node> getAllNodes() {
		List<Node> nodes = new ArrayList<Node>();
		Stack<Node> stack = new Stack<Node>();
		stack.push(root);
		
		while(! stack.isEmpty()) {
			Node n = stack.pop();
			nodes.add(n);
			for(int i=0; i<n.getNumOffspring(); i++) {
				stack.push(n.getOffspring(i));
			}
		}
		
		
		return nodes;
	}
	
	public Node createNode() {
		return createNode("");
	}
	
	/**
	 * Right now this is the only way to build one of these trees :  make an ARG-type
	 * tree and then clone it into this guy. 
	 * @param root
	 */
	public void cloneFromARG(ARGNode coalRoot) {
		root = new Node(coalRoot.getLabel());
		root.setHeight(coalRoot.getHeight());
		for(int i=0; i<coalRoot.getNumOffspring(); i++) {
			Node child = cloneSubtree(coalRoot.getOffspring(i));
			root.addOffspring(child);
			child.setParent(root);
		}
	}
	
	private Node cloneSubtree(ARGNode coalParent) {
		Node child = new Node(coalParent.getLabel());
		child.setHeight(coalParent.getHeight());
		if (coalParent.getNumOffspring()>0) {
			for(int i=0; i<coalParent.getNumOffspring(); i++) {
				ARGNode offspring = coalParent.getOffspring(i);
				Node kid = cloneSubtree(offspring);
				child.addOffspring(kid);
				kid.setParent(child);
			}
		}
		return child;
	}
	
	/**
	 * Return a newick representation of this tree
	 * @return
	 */
	public String getNewick() {
		return getNewick(true);
	}
	
	/**
	 * Return a newick representation of this tree
	 * @return
	 */
	public String getNewick(boolean includeAnnotations) {
		StringBuffer str = new StringBuffer("(");
		for (int i=0; i<root.getNumOffspring()-1; i++) {
			String cStr = getNewickSubtree( root.getOffspring(i), includeAnnotations, 1.0) + ", ";
			str.append(cStr);
		}
		
		String cStr = getNewickSubtree( root.getOffspring(root.getNumOffspring()-1), includeAnnotations, 1.0);
		str.append(cStr);

		str.append(");");
		return str.toString();		
	}
	
	private static String getNewickSubtree(Node n, boolean includeAnnotations, double scaleFactor) {
		String branchLengthStr  = "" + ((n.getParent().getHeight() - n.getHeight())*scaleFactor);
				
		if (n.getNumOffspring()==0) {
			if (n.getLabel() != null) {
				return new String(n.getLabel() + ":" + branchLengthStr );
			}
			else {
				return new String("ind?:" + branchLengthStr );
			}
		}
		
		if (n.getNumOffspring()==1) {
			System.err.println("Huh? This node has only one child! Newick representation may not be correct. Also, tree may be screwed up.");
			return "ERROR"; //Code will never be reached, obviously, but apparently it's an error NOT to have this here
		}
		else {
			StringBuffer str = new StringBuffer("(");
						
			for (int i=0; i<n.getNumOffspring()-1; i++) {
				String cStr = getNewickSubtree( n.getOffspring(i), includeAnnotations, scaleFactor) + ", ";
				str.append(cStr);
			}
			
			String cStr = getNewickSubtree( n.getOffspring(n.getNumOffspring()-1), includeAnnotations, scaleFactor);
			str.append(cStr);

			String annotations = "";
			if (includeAnnotations)
				annotations = "[&" + n.getAnnotationString() + "]";
			
			str.append(")" + ":" + annotations +  branchLengthStr);
			return str.toString();
		}	
	}

	/**
	 * A single node in this simple tree. Must have exactly one parent (unless its the root),
	 * but can have any number of offspring
	 * @author brendan
	 *
	 */
	public class Node {
		double height = Double.NaN;
		String label;
		Map<String, String> annos = new HashMap<String, String>();
		List<Node> offspring = new ArrayList<Node>();
		Node parent = null;
		
		public Node(String label) {
			this.label = label;
		}
		
		public Node() {
			this.label = "";
		}
		
		public void addAnnotation(String key, String value) {
			annos.put(key, value);
		}
		
		public String getAnnotation(String key) {
			return annos.get(key);
		}
		
		public void setLabel(String lab) {
			this.label = lab;
		}
		
		public String getLabel() {
			return label;
		}
		
		public double getHeight() {
			return height;
		}
		
		public void setHeight(double h) {
			this.height = h;
		}
		
		public void setParent(Node parent) {
			this.parent = parent;
		}
		
		public void removeAnnotation(String key) {
			annos.remove(key);
		}
		
		public List<Node> getOffspring() {
			return offspring;
		}

		public int getNumOffspring() {
			return offspring.size();
		}
		
		public Node getOffspring(int which) {
			return offspring.get(which);
		}
		
		public void addOffspring(Node child) {
			offspring.add(child);
		}
		
		public Node getParent() {
			return parent;
		}
		
		/**
		 * A a single string representing the set of annotations for this node
		 * @return
		 */
		public String getAnnotationString() {
			StringBuilder strB = new StringBuilder();
			List<String> keys = new ArrayList<String>();
			for(String key : annos.keySet()) {
				keys.add(key);
			}
			for(int i=0; i<keys.size()-1; i++) {
				String key = keys.get(i);
				strB.append(key + "=" + annos.get(key) + ", ");
			}
			String key = keys.get(keys.size()-1);
			strB.append(key + "=" + annos.get(key));
			return strB.toString();
		}
		
	}
}
