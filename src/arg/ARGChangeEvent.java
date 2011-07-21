package arg;

/**
 * A class that summarizes the type of alterations to the arg that occurred
 * @author brendan
 *
 */
public class ARGChangeEvent {
	
	enum ChangeType { NodeAdded, NodeRemoved, StructureChanged, HeightChanged, BreakpointChanged }
	
	ChangeType type;
	ARGNode[] nodesChanged;
	
	public ARGChangeEvent(ChangeType type, ARGNode nodeChanged) {
		this(type, new ARGNode[]{nodeChanged});
	}
	
	public ARGChangeEvent(ChangeType type, ARGNode[] nodesChanged) {
		this.type = type;
		this.nodesChanged = nodesChanged;
	}
	
	public ChangeType getType() {
		return type;
	}
	
	public ARGNode[] getNodesChanged() {
		return nodesChanged;
	}
}
