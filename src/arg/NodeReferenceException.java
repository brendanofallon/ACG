package arg;

/**
 * A type of exception thrown when a violation of node reference structure is detected - for instance, if 
 * a node's parent doesn't have the node as an offspring. 
 * @author brendan
 *
 */
public class NodeReferenceException extends Exception {

	ARGNode perp = null;
	
	public NodeReferenceException(ARGNode node, String message) {
		super(message);
		perp = node;
	}
	
	public ARGNode getNode() {
		return perp;
	}
}
