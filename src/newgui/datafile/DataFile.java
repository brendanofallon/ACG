package newgui.datafile;


import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import javax.xml.transform.TransformerException;

import newgui.gui.display.Display;

/**
 * A persistent container for many different types of data displayable in the viewer and savable to a file.
 * Mostly, these are stored in an xml format with some meta-data (to be decided exactly what this is) 
 * common to all data files, and some type-specific info.   
 * Data files store things like alignments, results files, analyses, etc. 
 * @author brendan
 *
 */
public abstract class DataFile {

	protected File source = null;
	
	/**
	 * Return a Display - a component that can be used to view the data in this file
	 * @return
	 */
	public abstract Display getDisplay();
	
	public boolean hasSourceFile() {
		return source != null;
	}
	
	public String toString() {
		return source.getName();
	}
	
	
	/**
	 * Obtain the source File from which the data was obtained. May be null if the source has
	 * not been specified. 
	 * @return
	 */
	public File getSourceFile() {
		return source;
	}
	
	public Date getLastModified() {
		if (source == null) 
			return null;
		else
			return new Date(source.lastModified());
	}
	
	/**
	 * Get any notes that have been set for this file
	 * @return
	 */
	public abstract List<FileNote> getNotes();
	
	/**
	 * Add a new note to this file
	 * @param note
	 */
	public abstract void appendNote(FileNote note);
	
	/**
	 * Remove the given note from the file
	 * @param note
	 */
	public abstract void removeNote(FileNote note);
	
	/**
	 * Saves the given information to the source file, if it exists. If no source file 
	 * has been set, use saveToFile(File newsource) instead
	 * @throws IOException
	 * @throws  
	 */
	public abstract void save() throws IOException;
	
	public void saveToFile(File newSource) throws IOException {
		source = newSource;
		save();
	}
}
