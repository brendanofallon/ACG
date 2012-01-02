package newgui.alignment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * A class that reads in a fasta-formatted input file and converts it to an
 * alignment
 * @author brendan
 *
 */
public class FastaImporter {

	public static Alignment getAlignment(File inputFasta) throws FileParseException, IOException, UnrecognizedBaseException {
		Alignment aln = new BasicAlignment();
		BufferedReader file = new BufferedReader(new FileReader(inputFasta));
		int lineNumber = 0;

		String line = file.readLine();
		lineNumber++;

		while(line!=null && (! line.startsWith(">"))) {
			lineNumber++;
			line = file.readLine();
		}

		if (line == null)  {
			throw new FileParseException("Error parsing fasta file, could not find beginning of data.", inputFasta);
		}

		Sequence seq;
		while(line != null) {
			//System.out.println("Trying to read line " + lineNumber + " : " + line);
			line = line.trim();
			while(line!=null && line.trim().length()==0) {
				line = file.readLine();
				lineNumber++;
			}
			if (line == null)
				seq = null;
			else {
				if (line.charAt(0) != '>')  {
					throw new FileParseException("Error parsing fasta file, could not parse a sequence name from line #" + lineNumber, inputFasta);
				}

				String label = line.substring(1);
				StringBuffer sq = new StringBuffer();
				line = file.readLine();
				lineNumber++;
				if (line!=null)
					line = line.trim();
				while(line != null && line.length()>0 && line.charAt(0)!='>') {
					sq.append(line);
					line = file.readLine();
					lineNumber++;

					if (line!=null)
						line = line.trim();
				}

				seq = new SimpleSequence(label, sq.toString().toUpperCase());
				aln.addSequence(seq);

			}//else line was not null
		}//while reading new sequences
		return aln;
	}

}
