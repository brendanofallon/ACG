package tools.alnGen;

/**
 * Not too surprisingly, these are thrown when we can't find the requested contig (or chromosome)
 * in a VCF or fasta reference file
 * @author brendano
 *
 */
public class ContigNotFoundException extends Exception {

	public ContigNotFoundException(String message) {
		super(message);
	}
}
