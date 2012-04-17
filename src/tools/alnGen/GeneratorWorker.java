package tools.alnGen;

import javax.swing.SwingWorker;

/**
 * A class that can handle generating a sequence in the background - this is
 * useful since we can do lots of these in parallel, speeding up performance
 * when dealing with big vcf's, like from 1000 Genomes.
 * @author brendan
 *
 */
public class GeneratorWorker extends SwingWorker {

	final SampleReader reader;
	final ProtoSequence seq;
	final String contig;
	final int startPos;
	final int endPos;
	private int errorCount = 0;
	private int maxErrors = 10;
	
	public GeneratorWorker(SampleReader reader, ProtoSequence protoSeq, String contig, int startPos, int endPos) {
		this.reader = reader;
		this.seq = protoSeq;
		this.contig = contig;
		this.startPos = startPos;
		this.endPos = endPos;
	}
	
	@Override
	protected Object doInBackground() throws Exception {
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
		return null;
	}

	public ProtoSequence getProtoSequence() {
		return seq;
	}
}
