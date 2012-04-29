package newgui.gui.widgets.fileBlocks;

import gui.ErrorWindow;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import newgui.datafile.XMLDataFile;
import newgui.gui.ViewerWindow;
import newgui.gui.filepanel.BlockChooser;
import newgui.gui.filepanel.DirectoryListener;
import newgui.gui.filepanel.FileTree;

/**
 * A class to manage a collection of AbstractBlocks, which are basically 
 * prettified jtrees in a resizable panels. 
 * @author brendan
 *
 */
public class BlocksManager {

	private File rootDir = null;
	public static final String blockStateChangedEvent = "blockStateChanged";
	protected List<AbstractBlock> blocks = new ArrayList<AbstractBlock>();
	static final String fileSep = System.getProperty("file.separator");
	protected List<PropertyChangeListener> listeners = new ArrayList<PropertyChangeListener>();
	protected List<DirectoryListener> dirListeners = new ArrayList<DirectoryListener>();
	
	
	public BlocksManager(File rootDirectory) {
		this.rootDir = rootDirectory;
		if (! rootDir.exists()) {
			throw new IllegalArgumentException("Root directory at path " + rootDir.getAbsolutePath() + " does not exist");
		}
		if (! rootDir.isDirectory()) {
			throw new IllegalArgumentException("Root directory at path " + rootDir.getAbsolutePath() + " exists but is not a directory");
		}
		initializeBlocks();
	}
	
	public List<String> getBlockNames() {
		List<String> names = new ArrayList<String>();
		for(AbstractBlock block : blocks) {
			names.add( block.getLabel() );
		}
		return names;
	}
	
	/**
	 * Show a dialog that allows the user to save the given file in one of
	 * the file blocks
	 * @param data
	 */
	public void showSaveDialog(XMLDataFile data, String suggestedName) {
		BlockChooser chooser = new BlockChooser(this, data, suggestedName);
		chooser.setVisible(true);
	}
	
	/**
	 * Immediately save the data in the given XML file in the block whose name
	 * matches the given block name, using the filename provided
	 * @param blockName
	 * @param filename
	 * @param data
	 */
	public void saveFile(String blockName, String filename, XMLDataFile data) {
		AbstractBlock block = getBlockByName(blockName);
		if (block == null) {
			JOptionPane.showMessageDialog(ViewerWindow.getViewer(), "No folder with name " + blockName);
			return;
		}
			
		if (! filename.endsWith(".xml")) {
			filename = filename + ".xml";
		}
		String destPath= rootDir.getAbsolutePath() + fileSep + blockName + fileSep + filename; 
		File destFile = new File(destPath);
		
		if (destFile.exists()) {
			Object[] options = {"Cancel",
					"No",
			"Overwrite " + filename};
			int n = JOptionPane.showOptionDialog(ViewerWindow.getViewer(),
					"Replace existing file " + filename + "?",
					"File already exists",
					JOptionPane.YES_NO_CANCEL_OPTION,
					JOptionPane.WARNING_MESSAGE,
					null,
					options,
					options[1]);
			if (n != 2)
				return;
		}
		
		System.out.println("Saving file to path: " + destPath);
		try {
			data.saveToFile(destFile);
			if (block instanceof DirectoryBlock) {
				DirectoryBlock db = (DirectoryBlock)block;
				fireDirectoryChangeEvent( db.getRootDirectory() );
			}
		} catch (IOException e) {
			e.printStackTrace();
			ErrorWindow.showErrorWindow(e, "Error saving file " + filename);
		}
	}
	
	/**
	 * Add a new listener that will be notified when the state of a block (open vs. closed)
	 * changes
	 * @param listener
	 */
	public void addBlockListener(PropertyChangeListener listener) {
		listeners.add(listener);
	}
	
	/**
	 * Remove the given listener from those objects that are informed of changes
	 * to block states (i.e. open vs. closed)
	 * @param listener
	 */
	public void removeBlockListener(PropertyChangeListener listener) {
		listeners.remove(listener);
	}
	
	/**
	 * Add a listener that will be notified when the contents of a block changes
	 * @param listener
	 */
	public void addDirectoryListener(DirectoryListener listener) {
		dirListeners.add(listener);
	}
	
	/**
	 * Remove the given listener from those objects that are informed of directory change events
	 * @param listener
	 */
	public void removeDirectoryListener(DirectoryListener listener) {
		dirListeners.remove(listener);
	}
	
	protected void fireBlockStateChangedEvent(AbstractBlock block) {
		PropertyChangeEvent evt = new PropertyChangeEvent(block, BlocksManager.blockStateChangedEvent, null, null);
		for(PropertyChangeListener listener: listeners) {
			listener.propertyChange(evt);
		}
	}
	
	protected void fireDirectoryChangeEvent(File dirChanged) {
		for(DirectoryListener listener : dirListeners) {
			listener.filesChanged(dirChanged);
		}
	}
	
	/**
	 * Create a new block within the root directory with the given name
	 * @param name
	 */
	public void createBlock(String name) {
		File newBlock = new File(rootDir.getAbsolutePath() + fileSep + name);
		//FileTree fileTree = new FileTree( newBlock );
		DirectoryBlock block = new DirectoryBlock(this, newBlock);
		addDirectoryListener(block.getFileTree());
		//block.setMainComponent(fileTree);
		blocks.add(block);
	}
	
	/**
	 * Obtain the block whose name is .equal to blockName
	 * @param blockName
	 * @return
	 */
	public AbstractBlock getBlockByName(String blockName) {
		for(AbstractBlock block : blocks) {
			if (block.getLabel().equals(blockName)) {
				return block;
			}
		}
		return null;
	}
	
	public int getBlockCount() {
		return blocks.size();
	}
	
	public AbstractBlock getBlockByNumber(int which) {
		return blocks.get(which);
	}
	
	public void removeBlock(AbstractBlock block) {
		// ?
		// Be sure to remove tree from listeners list
	}
	
	private void initializeBlocks() {
		//Scan root directory, create AbstractBlocks (or a subclass?)
		File[] rootFiles = rootDir.listFiles();
	
		for(int i=0; i<rootFiles.length; i++) {
			File dir = rootFiles[i];
			if (dir.isDirectory()) {
				createBlock(dir.getName());
			}
		}
	}
	
}
