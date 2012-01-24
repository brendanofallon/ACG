package newgui.gui.filepanel;

import java.io.File;

import javax.swing.tree.DefaultMutableTreeNode;


/**
 * A single node in a file tree, basically just a normal tree node with a File attached.
 * DirectoryNode is a subclass of this that handles directories
 * @author brendan
 *
 */
public class FileNode extends DefaultMutableTreeNode {

	private File file;
	
	public FileNode(File file) {
		this.file = file;
	}
	
	public void setFile(File newFile) {
		this.file = newFile;
	}
	
	public File getFile() {
		return file;
	}
	
	public Object getUserObject() {
		return file;
	}

	public String toString() {
		return file.getName().replace(".xml", "");
	}
}
