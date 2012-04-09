package tools.alnGen;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A class that takes as input a reference file and a few SampleReaders, and constructs full Sequences
 * and alignments from the data. This is so we can take a reference and a vcf file 
 * @author brendan
 *
 */
public class AlignmentGenerator {

	protected File referenceFile;
	List<SampleReader> sampleReaders = new ArrayList<SampleReader>();
	private final int maxErrors = 10; //Number of variant-reading errors to tolerate
	
	public AlignmentGenerator(File referenceFile) {
		this.referenceFile = referenceFile;
	}
	
	/**
	 * Associate an additional SampleReader that will provide a list of variants with this 
	 * object. Each SampleReader will create an additional sequence when getAlignment is called
	 * @param reader
	 */
	public void addSampleReader(SampleReader reader) {
		this.sampleReaders.add(reader);
	}
	
	/**
	 * Obtain a list of proto-sequences that contain full sequence information for the given region
	 * for each SampleReader
	 * @param contig
	 * @param startPos
	 * @param endPos
	 * @return
	 * @throws IOException
	 */
	public List<ProtoSequence> getAlignment(String contig, int startPos, int endPos) throws IOException, ContigNotFoundException {
		List<ProtoSequence> seqs = new ArrayList<ProtoSequence>();

		FastaReader refReader = new FastaReader(referenceFile);
		StringBuilder refSeq = new StringBuilder();

		Integer intContig = FastaReader.getIntegerTrack(contig);

		for(int i=startPos; i<endPos; i++) {
			char base = refReader.getBaseAt(intContig, i);
			refSeq.append(base);
		}

		System.out.println(">reference");
		System.out.println(refSeq.toString());
		
		int errorCount = 0;
		
		for(SampleReader reader : sampleReaders) {
			ProtoSequence seq = new ProtoSequence(refSeq.toString(), startPos);
			seq.setSampleName(reader.getSampleName() + "_" + reader.getPhase());
			reader.advanceTo(contig, startPos);
			Variant var = reader.getVariant();
			while(errorCount < maxErrors && var.getContig().equals("" + contig) && var.getPos() < endPos) {
				System.out.println(var);
				seq.applyVariant(var, reader.getPhase());
				reader.advance();
				var = reader.getVariant();
				//Skip over errors (null variants), but only until max errors is reached
				while (errorCount < maxErrors && var == null) {
						errorCount++;
						var = reader.getVariant();
				}
				
			}
			seqs.add(seq);
			
			//System.out.println(seq.toString());
		}

		return seqs;
	}
	
}
