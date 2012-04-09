package tools.alnGen;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Reads a vcf-formatted file and allows creation of SampleReaders which read the variants
 * associated with particular samples from this vcf file
 * @author brendan
 *
 */
public class VCFReader {

	Map<String, Integer> sampleColumnMap = null; //Maps sample names to column indices
	Map<String, Integer> formatMap = null;
	File sourceFile = null; //File from which we're reading data

	
	public VCFReader(File sourceFile) throws IOException {
		this.sourceFile = sourceFile;
		readSampleNames();
		readFormatMap();
	}

	/**
	 * Generate list of samples in this file and the columns which they map to
	 * @throws IOException 
	 */
	private void readSampleNames() throws IOException {
		sampleColumnMap = new HashMap<String, Integer>();
		BufferedReader reader = new BufferedReader(new FileReader(sourceFile));
		String line = reader.readLine();
		while (line != null && (!line.startsWith("#CHROM"))) {
			line = reader.readLine();
		}
		if (line == null) {
			throw new IOException("Could not read data from file, no column header line found");
		}
		
		String[] toks = line.split("\t");
		//First 8 columns are info, remaining columns are samples
		for(int i=8; i<toks.length; i++) {
			String sampleName = toks[i];
			Integer column = i;
			sampleColumnMap.put(sampleName, column);
		}
	}
	
	/**
	 * Generates a map that associated particular 'format' items found in the FORMAT column 
	 * (such as GT, AD, AF, etc.) with their order in a sample's format column entry
	 * @throws IOException
	 */
	private void readFormatMap() throws IOException {
		formatMap = new HashMap<String, Integer>();
		BufferedReader reader = new BufferedReader(new FileReader(sourceFile));
		String line = reader.readLine();
		while (line != null && (line.startsWith("#"))) {
			line = reader.readLine();
		}
		if (line == null) {
			throw new IOException("Could not read data from file, no column header line found");
		}
		
		String[] toks = line.split("\t");
		if (toks.length < 8)
			throw new IllegalArgumentException("No FORMAT column found, only " + toks.length + " columns");
		String formatStr = toks[8];
		String[] formatToks = formatStr.split(":");
		for(int i=0; i<formatToks.length; i++) {
			formatMap.put(formatToks[i], i);
		}
	}
	
	public File getSourceFile() {
		return sourceFile;
	}
	
	/**
	 * Obtain the position in the format string that is associated with the given format id, 
	 * for instance, we might look up "GT" (for genotype), and find that it is at position 0
	 * in the colon-separated format list GT:AD:AF:PL
	 * @param formatID
	 * @return
	 */
	public Integer getFormatCol(String formatID) {
		return formatMap.get(formatID);
	}
	
	/**
	 * Obtain a SampleReader object that will read variants associated with the given sample name
	 * from this VCF file
	 * @param sampleName Sample to read variants for
	 * @param phase Whether to read first or second allele, must be zero or one
	 * @return
	 */
	public SampleReader getReaderForSample(String sampleName, int phase) {
		//Do we recognize the sample name?
		if (!sampleColumnMap.containsKey(sampleName)) {
			throw new IllegalArgumentException("No sample with name " + sampleName + " found in VCF file");
		}
		try {
			return new SampleReader(this, sampleName, phase);
		} catch (IOException e) {
			e.printStackTrace();
			throw new IllegalArgumentException("Error reading data from vcf file " + getSourceFile().getAbsolutePath());
		}
	}

	public Integer getColumnForSample(String sampleName) {
		return sampleColumnMap.get(sampleName);
	}
	
	
	public static void main(String[] args) {
//		File hht15 = new File("/media/MORE_DATA/detect_fp/rawvcfs/HHT15_all_variants.vcf");
//		File hht16 = new File("/media/MORE_DATA/detect_fp/rawvcfs/HHT16_all_variants.vcf");
//		File hht3 = new File("/media/MORE_DATA/detect_fp/rawvcfs/HHT3_all_variants.vcf");
//		File hht4 = new File("/media/MORE_DATA/detect_fp/rawvcfs/HHT4_all_variants.vcf");
		File genomesVCF = new File("/home/brendan/1000genomes/ALL.chr10.phase1_release_v3.20101123.snps_indels_svs.genotypes.vcf");
		File reference = new File("/home/brendan/resources/human_g1k_v37.fasta");
				
		Integer contig = 10;
		int startPos = 25000000;
		int endPos =   25050000;
		
		AlignmentGenerator alnGen = new AlignmentGenerator(reference);
		VCFReader vcfReader;
		try {
			vcfReader = new VCFReader(genomesVCF);
			
			SampleReader varReader = vcfReader.getReaderForSample("HG00097", 0);
			alnGen.addSampleReader(varReader);
			
			varReader = vcfReader.getReaderForSample("HG00097", 1);
			alnGen.addSampleReader(varReader);
			
			varReader = vcfReader.getReaderForSample("HG00099", 0);
			alnGen.addSampleReader(varReader);
			
			varReader = vcfReader.getReaderForSample("HG00099", 1);
			alnGen.addSampleReader(varReader);
			
			varReader = vcfReader.getReaderForSample("HG01075", 0);
			alnGen.addSampleReader(varReader);
			
			varReader = vcfReader.getReaderForSample("HG01075", 1);
			alnGen.addSampleReader(varReader);
			
			varReader = vcfReader.getReaderForSample("HG01171", 0);
			alnGen.addSampleReader(varReader);
			
			varReader = vcfReader.getReaderForSample("HG01171", 1);
			alnGen.addSampleReader(varReader);
			
			varReader = vcfReader.getReaderForSample("HG01389", 0);
			alnGen.addSampleReader(varReader);
			
			varReader = vcfReader.getReaderForSample("HG01389", 1);
			alnGen.addSampleReader(varReader);
			
			varReader = vcfReader.getReaderForSample("HG00100", 0);
			alnGen.addSampleReader(varReader);
			
			varReader = vcfReader.getReaderForSample("HG00100", 1);
			alnGen.addSampleReader(varReader);
			
			varReader = vcfReader.getReaderForSample("NA12873", 0);
			alnGen.addSampleReader(varReader);
			
			varReader = vcfReader.getReaderForSample("NA18566", 0);
			alnGen.addSampleReader(varReader);
			
			varReader = vcfReader.getReaderForSample("NA18566", 1);
			alnGen.addSampleReader(varReader);
			
			varReader = vcfReader.getReaderForSample("HG01108", 0);
			alnGen.addSampleReader(varReader);
			
			varReader = vcfReader.getReaderForSample("HG01108", 1);
			alnGen.addSampleReader(varReader);
			
			varReader = vcfReader.getReaderForSample("NA18532", 0);
			alnGen.addSampleReader(varReader);
			
			varReader = vcfReader.getReaderForSample("NA18532", 1);
			alnGen.addSampleReader(varReader);
			
			List<ProtoSequence> seqs = alnGen.getAlignment("" + contig, startPos, endPos);
			
			BufferedWriter writer = new BufferedWriter(new FileWriter("vcfreader_out.fasta"));
			for(ProtoSequence seq : seqs) {
				writer.write(seq.toString() + "\n");
			}
			writer.close();
			System.out.println("Wrote " + seqs.size() + " sequences of length " + (endPos - startPos));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
}
