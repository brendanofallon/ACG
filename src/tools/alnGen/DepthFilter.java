package tools.alnGen;

public class DepthFilter implements VariantFilter {

	final int minDepth;
	
	public DepthFilter(int minDepth) {
		this.minDepth = minDepth;
	}
	
	@Override
	public boolean variantPasses(Variant var) {
		return var.getDepth() >= minDepth;
	}

}
