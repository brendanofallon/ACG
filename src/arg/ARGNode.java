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

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Base class of all nodes in an ARG. These things have 0-2 children and 0-2 parent nodes, 
 * depending on their type. They also contain a reference to their owner ARG
 * 
 * Current subclasses are TipNode (no children, 1 parent), CoalNode (2 children 1 parent) and
 * RecombNode (1 child, two parents)
 * 
 *  
 * @author brendan
 *
 */
public abstract class ARGNode implements Comparable<ARGNode> {

	//All nodes have a unique, immutable number
	//private static int nodeCount = 0;	
	private final int myNumber;
	
	protected String label = "?";
		
	final ARG parentARG;
	
	//Some traversal algorithms require setting a visited flag to mark when a node has been 
	//reached. These can be cleared for all nodes by invoking arg.clearVisitedFlags
	protected boolean visited = false;
	
	//A bunch annotations, so we can set, check, and write properties for nodes
	protected Map<String, String> annotations = new HashMap<String, String>();
	
	public ARGNode(ARG owner) {
		myNumber = owner.nextNodeNumber();
		label = String.valueOf(myNumber);
		parentARG = owner;
	}
	
	/**
	 * Gets the arg to which this node belongs. ARGChangeEvents are dispatched to the given arg
	 * @param arg
	 */
	public ARG getARG() {
		return parentARG;
	}
	
	
	/**
	 * Obtain the label associated with this node, by default this is the node number
	 * @return
	 */
	public String getLabel() {
		return label;
	}
	
	/**
	 * Set an arbitrary label for this node
	 * @param label
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	/**
	 * Get a unique number associated with this node
	 * @return
	 */
	public long getNumber() {
		return myNumber;
	}
	
	/**
	 * Set the visited flag for this node to the given value
	 */
	public void setVisited(boolean vis) {
		visited = vis;
	}
	
	/**
	 * Returns the state of the visited flag
	 * @return
	 */
	public boolean isVisited() {
		return visited;
	}
	
	/**
	 * Compares two ARGNodes based on height
	 */
	public int compareTo(ARGNode c) {
		return c.getHeight() < getHeight() ? -1 : 1;
	}
	
	/**
	 * True if range information for this node has been altered since the 
	 * last call to accept or reject
	 * @return
	 */
	public abstract boolean isRangeProposed();
	
	/**
	 * Compute site range information for this node, including the ranges of sites which
	 * descend from this node, and in the case of coalescent nodes all sites that coalesce
	 * at the node. 
	 */
	public abstract int computeProposedRanges();

	/**
	 * Compute proposed range info. If force is true then do not use shortcutting. 
	 * @param force
	 */
	public abstract int computeProposedRanges(boolean force);
	
	/**
	 * Return the parent associated with the given site.
	 * @return
	 */
	public abstract ARGNode getParentForSite(int site);
	
	/**
	 * Return the height of the given node above an arbitrary (but fixed) reference point
	 * @return
	 */
	public abstract double getHeight();
	
	/**
	 * Return the number of immediate offspring descending from this node. CoalNodes always have 2, RecombNodes have 1, TipNodes have 0
	 * @return
	 */
	public abstract int getNumOffspring();
	
	/**
	 * Returns true if this node's references seem to make sense. This only checks the 'activeState' of
	 * this node, and thus may yield spurious results if used mid-modification
	 * @return
	 */
	public void checkReferences() throws NodeReferenceException {
		for(int i=0; i<getNumOffspring(); i++) {
			if (getOffspring(i)==null) {
				throw new NodeReferenceException(this, "Child #" + i + " of node " + getLabel() + " is null");
			}
			if (! getOffspring(i).hasThisParent(this)) {
				StringBuilder message = new StringBuilder();
				message.append("Child #" + i + "  (" + getOffspring(i) + ")  of node " + getLabel() + " does not agree on parent \n");
				message.append("Parents are : \n");
				for(int j=0; j<getOffspring(i).getNumParents(); j++) {
					message.append( this.getOffspring(i).getParent(j) + "\n");
				}
				throw new NodeReferenceException(this, message.toString());
			}
		}
		
		for(int i=0; i<getNumParents(); i++) {
			if (getParent(i)==null) {
				throw new NodeReferenceException(this, "Parent #" + i + " of node " + getLabel() + " is null");
			}
			if (! getParent(i).hasThisChild(this)) {
				throw new NodeReferenceException(this, "Parent #" + i + "  (" + getParent(i) + ") of node " + getLabel() + " does not think " + this + " is a child");
			}
		}
	}
	
	/**
	 * The activeState offspring at the given index. 
	 * @param which
	 * @return
	 */
	public abstract ARGNode getOffspring(int which);
	
	/**
	 * The number of parents of this node, 1 for Coal or Tip nodes, 2 for RecombNodes
	 * @return
	 */
	public abstract int getNumParents();
	
	/**
	 * Obtain a reference to the activeState parent at the given index
	 * @param which
	 * @return
	 */
	public abstract ARGNode getParent(int which);
	
	/**
	 * Propose a new parent for this node
	 * @param parentIndex
	 * @param parent
	 */
	public abstract void proposeParent(int parentIndex, ARGNode parent);
	
	/**
	 * Propose that the node that descends from this node at offspringIndex be the given child
	 * @param offspringIndex
	 * @param child
	 */
	public abstract void proposeOffspring(int offspringIndex, ARGNode child);
	
	/**
	 * Propose a new value for the height of this node
	 * @param height
	 */
	public abstract void proposeHeight(double height);
	
	/**
	 * Return a list of SiteRanges describing which sites descend from this node all the way to the tips
	 * @return
	 */
	public abstract SiteRangeList getActiveRanges();
	
	public abstract SiteRangeList getCurrentRanges();
	
	public abstract SiteRangeList getProposedRanges();
	
	/**
	 * Accept the all proposed changes. This sets all currentState references to their activeState equivalents.
	 */
	public abstract void acceptProposal();
	
	/**
	 * Rejects all proposed changes and sets all activeState references back to the currentState refs. 
	 */
	public abstract void rejectProposal();	
	
	/**
	 * Returns true if we have proposed some change, but not subsequently called accept or rejectProposal(). 
	 * @return
	 */
	public abstract boolean isActiveProposed();

	public String toString() {
		NumberFormat formatter = new DecimalFormat("0.0#");
		if (getHeight()>100)
			formatter = new DecimalFormat("0.0");
		if (getHeight()<0.1)
			formatter = new DecimalFormat("0.0####");
		
		return "" + ((this instanceof CoalNode) ?  "Coal: " : "Tip: ")  + getLabel() + " height: " + formatter.format(getHeight());
	}

	
	/**
	 * Returns true if this node's activeState has the supplied node as a child
	 * @param child
	 * @return
	 */
	public boolean hasThisChild(ARGNode child) {
		for(int i=0; i<this.getNumOffspring(); i++) {
			if (this.getOffspring(i)==child)
				return true;
		}
		return false;
	}
	
	
	/**
	 * Returns true if this nodes activeState has the supplied node as a parent
	 * @param parent
	 * @return
	 */
	public boolean hasThisParent(ARGNode parent) {
		for(int i=0; i<this.getNumParents(); i++) {
			if (this.getParent(i)==parent) {
				return true;
			}
		}
		return false;
	}

	
	
	/**************************** Annotation related stuff *************************************/
	
	/**
	 * Add a new key=value annotation to this node
	 * @param key
	 * @param value
	 * @return True if an annotation with the given key already exists
	 */
	public boolean addAnnotation(String key, String value) {
		boolean retVal = annotations.containsKey(key);
		annotations.put(key, value);
		return retVal;
	}
	
	/**
	 * Returns true if this node has the given annotation
	 * @param key
	 * @return
	 */
	public boolean hasAnnotation(String key) {
		return annotations.containsKey(key);
	}
	
	/**
	 * Remove all annotations from this node
	 */
	public void clearAnnotations() {
		annotations.clear();
	}
	
	/**
	 * Get the annotation associated with the given key
	 * @param key
	 * @return
	 */
	public String getAnnotation(String key) {
		return annotations.get(key);
	}
	
	/**
	 * Return all keys used for annotations as a Set
	 * @return
	 */
	public Set<String> getAnnotationKeys() {
		return annotations.keySet();
	}

}
