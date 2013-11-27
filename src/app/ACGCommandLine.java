package app;

import java.io.File;

import jobqueue.ExecutingChain;

import document.ACGDocument;

import newgui.datafile.AnalysisDataFile;

/**
 * Class for command-line access to ACG. 
 * @author brendanofallon
 *
 */
public class ACGCommandLine {

	public static void main(String[] args) {
		
		if (args.length == 0) {
			System.out.println("Please supply the name of an analysis settings file to execute.");
			return;
		}
		
		File inputFile = new File(args[0]);
		if (! inputFile.exists()) {
			System.err.println("Input file " + inputFile.getAbsolutePath() + " does not exist");
			System.exit(1);
		}
		
		
		
		ACGDocument doc = null;
		Exception ex = null;
		try {
			doc = new ACGDocument(inputFile);
		}
		catch (Exception e) {
			ex = e;
			//Not catastrophic, we'll try to create the document a different way below
		}

		//If user tries to execute a saved 'analysis data file' then we can't directly create
		//an ACGDocument from it (since the file will contain more than just a model)
		//So if we get an error here try to create an AnalysisDataFile
		if (doc == null) {
			try {
			AnalysisDataFile dataFile = new AnalysisDataFile(inputFile);
			doc = dataFile.getACGDocument();
			}
			catch (Exception e) {
				ex = e;
				//Errors handled downstream
			}
		}
		
		if (doc == null) {
			System.err.println("Could not create a analysis model from input file");
			if (ex != null)
				System.err.println(ex.getLocalizedMessage());
			
			//No document to execute, abort. 
			System.exit(1);
		}
		
		
		ExecutingChain chain = null;
		try {
			chain = new ExecutingChain(doc);
		} catch (Exception e) {
			System.err.println("Error reading input document: " + e.getLocalizedMessage());
			System.exit(1);
		}
		
		System.out.println("Now running chain...");
		chain.beginJob();
		System.out.println("Chain completed.");
		System.exit(0);
	}
}
