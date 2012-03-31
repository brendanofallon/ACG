package newgui.gui.filepanel;

import gui.ErrorWindow;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import newgui.datafile.AlignmentFile;
import newgui.datafile.resultsfile.ResultsFile;
import newgui.gui.ViewerWindow;
import sequence.Alignment;

public class ResultsFilesManager {

	//Directory containing input files to track
	private File rootDirectory;
	private static final String fileSep = System.getProperty("file.separator");
	private static final ResultsFilesManager manager = new ResultsFilesManager();
	
	/**
	 * Get the (single) InputFilesManager
	 * @return
	 */
	public static ResultsFilesManager getManager() {
		return manager;
	}
	
	private ResultsFilesManager() {
		rootDirectory = new File("resultsfiles/");
		if (rootDirectory.exists() && rootDirectory.isDirectory()) {
			//we're ok, nothing to do, I guess
		}
		else {
			throw new IllegalArgumentException("InputFiles root directory " + rootDirectory.getAbsolutePath() + " is not a directory");
		}
	}
	
	/**
	 * Returns a File object associated with the root directory of the input files storage dir 
	 * @return
	 */
	public File getRootDirectory() {
		return rootDirectory;
	}

	/**
	 * Create a directory at the given parentPath, relative to the rootDirectory. parentPath must specify
	 * the path to the directory in which the new directory is to be made. For instance, to make a new dir
	 * called "subProject" in the folder "myProject", parentPath should be "myProject" and dirName should 
	 * be "subProject" 
	 * @param parentPath
	 * @param dirName
	 */
	public void createDirectory(String parentPath, String dirName) {
		if (parentPath.startsWith("/"))
			throw new IllegalArgumentException("Parent path cannot be absolute - it's always a subdirectory of the root directory");
		File newDir = new File(rootDirectory + fileSep + parentPath + fileSep + dirName);
		if (newDir.exists()) {
			throw new IllegalArgumentException("Directory " + newDir.getAbsolutePath() + " already exists");
		}
		else {
			newDir.mkdir();
			fireDirectoryChange(new File(parentPath));
		}
	}
	
	/**
	 * Removes the directory at the given path. Exceptions are thrown if the file does
	 * not exist, is not a directory, or if the directory is not empty
	 * @param path
	 */
	public void removeDirectory(String path) {
		File dir = new File(rootDirectory + fileSep + path);
		if (! dir.exists()) {
			throw new IllegalArgumentException("Directory " + path  + " does not exist");
		}
		if (! dir.isDirectory()) {
			throw new IllegalArgumentException("File at " + path + " is not a directory");
		}
		File[] files = dir.listFiles();
		if (files.length == 0) {
			dir.delete();
			fireDirectoryChange(rootDirectory);
		}
		else {
			throw new IllegalArgumentException("Only empty directories can be deleted");
		}
	}
	
	public void addListener(DirectoryListener l) {
		listeners.add(l);
	}
	
	public void removeListener(DirectoryListener l) {
		listeners.remove(l);
	}
	
	private void fireDirectoryChange(File rootChange) {
		for(DirectoryListener l : listeners) {
			l.filesChanged( rootChange );
		}
	}
	
	private List<DirectoryListener> listeners = new ArrayList<DirectoryListener>();
	
	/**
	 * Open a dialog prompting the user to enter a name for the results file, 
	 * and if accepted save the alignment with the name specified
	 * @param aln
	 * @param suggestedName
	 * @return
	 */
	public boolean saveResults(ResultsFile file, String suggestedName) {
		String name = (String)JOptionPane.showInputDialog(ViewerWindow.getViewer(), 
				"Choose a name for these results:",
				"Save Results",
				JOptionPane.PLAIN_MESSAGE,
				null, 
				null,
				suggestedName);
		
		if (name == null) {
			return false;
		}

		if (! name.endsWith(".xml")) {
			name = name + ".xml";
		}
	
		try {
			file.saveToFile(new File(getRootDirectory().getAbsolutePath() + fileSep + name));
			fireDirectoryChange(getRootDirectory());
			return true;
		} catch (IOException e) {
			ErrorWindow.showErrorWindow(e, "Error saving results file");
			e.printStackTrace();
			return false;
		}
		
	}
}
