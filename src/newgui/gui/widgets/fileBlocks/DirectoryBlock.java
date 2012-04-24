package newgui.gui.widgets.fileBlocks;

import java.io.File;
import java.io.IOException;

import newgui.datafile.XMLDataFile;
import newgui.gui.filepanel.AnalysisFilesManager;
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
	public DirectoryBlock(File file) {
		super(file.getName());
		baseFile = file;
		if (! file.exists())
			throw new IllegalArgumentException("File " + file.getAbsolutePath() + " does not exist");
		if (! file.isDirectory())
			throw new IllegalArgumentException("File " + file.getAbsolutePath() + " is not a directory");
		
		fTree = new FileTree(baseFile);
		setMainComponent(fTree);
	}
	
	/**
	 * Add a new file to this directory block
	 * @param file
	 * @throws IOException 
	 */
	public void addDataFile(XMLDataFile file, String name) throws IOException {
		if (!name.endsWith(".xml"))
			name = name + ".xml";
		String fullPath = baseFile + fileSep + name;
		file.saveToFile(new File(fullPath));
		fireDirectoryChangeEvent(baseFile);
	}

}
