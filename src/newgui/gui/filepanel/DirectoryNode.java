package newgui.gui.filepanel;

import java.io.File;

import javax.swing.tree.DefaultMutableTreeNode;

public class DirectoryNode extends DefaultMutableTreeNode {

	private File directory;
	
	public DirectoryNode(File dir) {
		super(dir);
		if (! dir.isDirectory())
			throw new IllegalArgumentException("File " + dir.getAbsolutePath() + " is not a directory");
		this.directory = dir;
	}
	
	public void setDirectory(File dir) {
		if (! dir.isDirectory())
			throw new IllegalArgumentException("File " + dir.getAbsolutePath() + " is not a directory");
		this.directory = dir;
	}
	
	/**
	 * Obtain list of files contained in this directory
	 * @return
	 */
	public File[] getFiles() {
		File[] files = directory.listFiles();
		return files;
	}
}
