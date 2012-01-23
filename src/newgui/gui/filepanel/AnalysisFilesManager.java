package newgui.gui.filepanel;

import gui.ErrorWindow;
import gui.document.ACGDocument;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import newgui.datafile.AlignmentFile;
import newgui.datafile.AnalysisDataFile;
import newgui.datafile.XMLConversionError;
import sequence.Alignment;

public class AnalysisFilesManager {
	//Directory containing input files to track
	private File rootDirectory;
	private static final String fileSep = System.getProperty("file.separator");
	private static final AnalysisFilesManager manager = new AnalysisFilesManager();
	
	/**
	 * Get the (single) InputFilesManager
	 * @return
	 */
	public static AnalysisFilesManager getManager() {
		return manager;
	}
	
	private AnalysisFilesManager() {
		rootDirectory = new File("analysisfiles/");
		if (rootDirectory.exists() && rootDirectory.isDirectory()) {
			//we're ok, nothing to do, I guess
		}
		else {
			throw new IllegalArgumentException("Analysis files root directory " + rootDirectory.getAbsolutePath() + " is not a directory");
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
	 * Create a new AlignmentFile at the path specified by 'path' (relative to the rootDirectory, of course)
	 * @param aln
	 * @param filename
	 */
	public void addAlignment(Alignment aln, String path) {
		AlignmentFile alnFile = new AlignmentFile(aln);
		try {
			System.out.println("Attempting to save a file at path : " + rootDirectory + path);
			alnFile.saveToFile(new File(rootDirectory + fileSep + path));
			fireDirectoryChange(new File(path));
		} catch (IOException e) {
			ErrorWindow.showErrorWindow(e, "Could not import file " + path);
		}
	}
	

	public void addAnalysisFile(ACGDocument doc, String path) {
		AnalysisDataFile  dataFile = new AnalysisDataFile();
		try {
			dataFile.setACGDocument(doc);
			dataFile.saveToFile(new File(rootDirectory + fileSep + path));
			fireDirectoryChange(new File(path));
		} catch (XMLConversionError e) {
			ErrorWindow.showErrorWindow(e, "Could not save analysis " + path);
		} catch (IOException e) {
			ErrorWindow.showErrorWindow(e, "Could not save analysis " + path);
		}
		
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
}
