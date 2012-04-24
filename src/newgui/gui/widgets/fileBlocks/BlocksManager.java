package newgui.gui.widgets.fileBlocks;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import newgui.gui.filepanel.AnalysisFilesManager;
import newgui.gui.filepanel.FileTree;

/**
 * A class to manage a collection of AbstractBlocks, which are basically 
 * prettified jtrees in a resizable panels. 
 * @author brendan
 *
 */
public class BlocksManager {

	private File rootDir = null;
	
	protected List<AbstractBlock> blocks = new ArrayList<AbstractBlock>();
	static final String fileSep = System.getProperty("file.separator");
	
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
	 * Create a new block within the root directory with the given name
	 * @param name
	 */
	public void createBlock(String name) {
		File newBlock = new File(rootDir.getAbsolutePath() + fileSep + name);
		FileTree analysisTree = new FileTree( newBlock);
		AbstractBlock block = new AbstractBlock(name);
		block.setMainComponent(analysisTree);
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
