package tools.alnGen;

/**
 * A single variant obtained from a line in a vcf file and associated with a single sample
 * @author brendan
 *
 */
public class Variant {

	final String contig;
	final int pos;
	final String ref;
	final String alt0;
	final String alt1;
	final Double quality;
	final Integer depth;
	final boolean phased;
	
	public Variant(String contig, int pos, String ref, String alt0, String alt1, double quality, int depth, boolean phased) {
		this.contig = contig;
		this.pos = pos;
		this.ref = ref;
		this.alt0 = alt0;
		this.alt1 = alt1;
		this.quality = quality;
		this.depth = depth;
		this.phased = phased;
	}
	
	public String getContig() {
		return contig;
	}
	
	public int getPos() {
		return pos;
	}
	
	public String getRef() {
		return ref;
	}
	
	public String getAlt0() {
		return alt0;
	}
	
	public String getAlt1() {
		return alt1;
	}
	
	public double getQuality() {
		return quality;
	}
	
	public int getDepth() {
		return depth;
	}
	
	/**
	 * True if this variant has been phased
	 * @return
	 */
	public boolean phased() {
		return phased;
	}
	
	public String toString() {
		return contig + "\t" + pos + "\t" + ref + "\t" + alt0 + "," + alt1 + "\t" + quality + "\t" + depth;
	}
}
