package newgui.alignment;

import java.io.File;

/**
 * These are thrown when we have a problem parsing / importing a file
 * @author brendan
 *
 */
public class FileParseException extends Exception {

	private File offendingFile;
	
	public FileParseException(String message, File badFile) {
		super(message);
		this.offendingFile = badFile;
	}
	
	public File getFile() {
		return offendingFile;
	}
}
