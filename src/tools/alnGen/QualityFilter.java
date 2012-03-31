package tools.alnGen;

public class QualityFilter implements VariantFilter {

	final double minQuality;
	
	public QualityFilter(double minQuality) {
		this.minQuality = minQuality;
	}
	
	@Override
	public boolean variantPasses(Variant var) {
		return var.getQuality() >= minQuality;
	}

}
