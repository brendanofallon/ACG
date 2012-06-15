package newgui.datafile;

import java.io.File;
import java.io.IOException;

import newgui.alignment.FastaImporter;
import newgui.alignment.FileParseException;
import newgui.alignment.UnrecognizedBaseException;
import newgui.datafile.resultsfile.HistogramElementReader;
import newgui.datafile.resultsfile.ResultsFile;
import newgui.gui.display.resultsDisplay.AbstractLoggerConverter;


/**
 * A class containing some static utilities allowing for creation of XMLDataFiles from
 * raw data
 * @author brendan
 *
 */
public class DataFileFactory {

	/**
	 * Attempts to create an appropriate XMLDataFile subclass given the file provided
	 * @param file
	 * @return
	 * @throws IOException 
	 * @throws FileParseException 
	 */
	public static XMLDataFile createDataFile(File file) throws IOException, FileParseException {
		if (file == null)
			return null;
		
		if (! file.exists())
			throw new IOException("File does not exist");
		
		if (file.getName().endsWith(".fasta") || file.getName().endsWith(".fas") || file.getName().endsWith(".fa")) {
			try {
				return new AlignmentFile(FastaImporter.getAlignment(file));
			} catch (UnrecognizedBaseException e) {
				e.printStackTrace();
				throw new FileParseException("Error reading file : " + file + "\n" + e.getMessage(), file);
			}
		}
		
		if (file.getName().endsWith(".xml")) {
			XMLDataFile testFile = new XMLDataFile(file);
			if (testFile.containsElementByName(AlignmentFile.XMLConverter.ALIGNMENT)) {
				return new AlignmentFile(file);
			}
			
			if (testFile.containsElementByName(AnalysisDataFile.MODEL)) {
				return new AnalysisDataFile(file);
			}
			
			if (testFile.containsElementByName(AbstractLoggerConverter.LOGGER_ELEMENT)) {
				return new ResultsFile(file);
			}
		}
		
		throw new FileParseException("Could not guess file type", file);
	}
}
