package newgui.gui.widgets.fileBlocks;

import java.io.File;
import java.io.IOException;

import newgui.datafile.XMLDataFile;
import newgui.gui.filepanel.FileTree;

/**
 * A type of Block that displays a list of files in a directory
 * @author brendan
 *
 */
public class DirectoryBlock extends AbstractBlock {

	private FileTree fTree; //Panel that actually displays file tree
	private File baseFile;  //Directory whose files we're displaying
	static final String fileSep = System.getProperty("file.separator");
	
	/**
	 * Create a new directory block associated with the given file. The file must be a 
	 * directory.
	 * @param file
	 */
	public DirectoryBlock(BlocksManager manager, File file) {
		super(manager, file.getName());
		baseFile = file;
		if (! file.exists())
			throw new IllegalArgumentException("File " + file.getAbsolutePath() + " does not exist");
		if (! file.isDirectory())
			throw new IllegalArgumentException("File " + file.getAbsolutePath() + " is not a directory");
		
		fTree = new FileTree(baseFile);
		setMainComponent(fTree);
	}
	
	/**
	 * Obtain a reference to the FileTree object that actually displays the files
	 * in this block
	 * @return
	 */
	public FileTree getFileTree() {
		return fTree;
	}
	
	@Override
	public void deleteContents() {
		//PERMANENTLY DELETE ALL FILES IN THIS DIRECTORY!
		File[] contents = baseFile.listFiles();
		for(int i=0; i<contents.length; i++) {
			contents[i].delete();
		}
		baseFile.delete();
	}
	
	public void renameTo(String newLabel) {
		File destFile = new File(baseFile.getParentFile() + fileSep + newLabel);
		baseFile.renameTo(destFile);
		fTree.setRootDir(destFile);
		super.renameTo(newLabel);
	}
	
	/**
	 * Get the directory that this block is displaying
	 * @return
	 */
	public File getRootDirectory() {
		return baseFile;
	}
	
	/**
	 * Add a new file to this directory block
	 * @param file
	 * @throws IOException 
	 */
//	public void addDataFile(XMLDataFile file, String name) throws IOException {
//		if (!name.endsWith(".xml"))
//			name = name + ".xml";
//		String fullPath = baseFile + fileSep + name;
//		file.saveToFile(new File(fullPath));
//	//	fireDirectoryChangeEvent(baseFile);
//	}

}
