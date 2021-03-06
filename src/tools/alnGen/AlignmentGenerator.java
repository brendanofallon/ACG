package tools.alnGen;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import newgui.ErrorWindow;
import tools.alnGen.FastaIndex.IndexNotFoundException;
import tools.alnGen.FastaReader2.EndOfContigException;

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
	 * @throws IndexNotFoundException 
	 */
	public List<ProtoSequence> getAlignment(String contig, int startPos, int endPos) throws IOException, ContigNotFoundException, IndexNotFoundException {
		List<ProtoSequence> seqs = new ArrayList<ProtoSequence>();

		FastaReader2 refReader = new FastaReader2(referenceFile);
		StringBuilder refSeq = new StringBuilder();
		refReader.advanceToContig(contig);
		
		try {
			refReader.advanceToPosition(startPos-1);
			for(int i=startPos; i<endPos; i++) {
				char base = refReader.nextBase();
				refSeq.append(base);
			}
		} catch (EndOfContigException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

		
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
		}

		return seqs;
	}
	

	/**
	 * Obtain a list of proto-sequences that contain all sequence into for the requested region - but perform
	 * the operation in parallel for speedier results. This blocks until all sequences have been generated.
	 * @param contig
	 * @param startPos
	 * @param endPos
	 * @return
	 * @throws IOException
	 * @throws ContigNotFoundException
	 * @throws IndexNotFoundException 
	 */
	public List<ProtoSequence> getAlignmentParallel(String contig, int startPos, int endPos) throws IOException, ContigNotFoundException, IndexNotFoundException {
		int cpuCount = Runtime.getRuntime().availableProcessors();
		int threads = 1;
		if (cpuCount > 3)
			threads = 2;
		if (cpuCount > 5)
			threads = 4;
		ThreadPoolExecutor threadPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(threads);
		
		FastaReader2 refReader = new FastaReader2(referenceFile);
		StringBuilder refSeq = new StringBuilder();
		refReader.advanceToContig(contig);
		
		try {
			refReader.advanceToPosition(startPos-1);
			for(int i=startPos; i<endPos; i++) {
				char base = refReader.nextBase();
				refSeq.append(base);
			}
		} catch (EndOfContigException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		List<GeneratorWorker> workers = new ArrayList<GeneratorWorker>();
		
		//Create a GeneratorWorker for each sample reader and submit it to the thread pool
		for(SampleReader reader : sampleReaders) {
			ProtoSequence seq = new ProtoSequence(refSeq.toString(), startPos);
			seq.setSampleName(reader.getSampleName() + "_" + reader.getPhase());
			GeneratorWorker worker = new GeneratorWorker(reader, seq, contig, startPos, endPos);
			workers.add(worker);
			threadPool.submit(worker);
			
		}

		threadPool.shutdown(); //No new tasks will be submitted,
		try {
			threadPool.awaitTermination(1, TimeUnit.DAYS);
		} catch (InterruptedException e) {
			e.printStackTrace();
			ErrorWindow.showErrorWindow(e, "Alignment generation interrupted");
		} //Wait until all tasks have completed

		//Make a list of all protosequences we're going to return and grab the sequence 
		//from the worker and add it to the list
		List<ProtoSequence> seqs = new ArrayList<ProtoSequence>();
		for(GeneratorWorker worker : workers) {
			seqs.add(worker.getProtoSequence());
		}
		return seqs;
	}
	
	
}
