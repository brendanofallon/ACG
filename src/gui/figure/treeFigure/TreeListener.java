package gui.figure.treeFigure;

/**
 * An interface for things that listen for tree events, like changes to orientation or structure 
 * @author brendan
 *
 */
public interface TreeListener {

	public enum ChangeType {ORIENTATION, NODES_ADDED, NODES_REMOVED, NODES_MOVED, COLLAPSED};
	
	public void treeChanged(ChangeType type);
	
}
