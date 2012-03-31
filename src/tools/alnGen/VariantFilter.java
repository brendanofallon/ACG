package tools.alnGen;

/**
 * An object that can examine a Variant and determine if it 'passes' or not
 * Implementations include depth filters and quality filters, which rule out
 * variants that are likely to be false positives
 * @author brendan
 *
 */
public interface VariantFilter {

	public boolean variantPasses(Variant var);
	
}
