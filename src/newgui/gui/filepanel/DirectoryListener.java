package newgui.gui.filepanel;

import java.io.File;

/**
 * These guys listen for changes in directory structure, including addition and deletion of files 
 * @author brendano
 *
 */
public interface DirectoryListener {

	/**
	 * Called when something about the directory has changed - including moving of files and addition or deletion of files
	 * (but typically not changing of file contents)
	 * @param root
	 */
	public void filesChanged(File root);
	
}
