package sequence;

/**
 * An interface for things that can map one site index to another. These are useful for when we alter an alignment
 * prior to input (for instance, to remove gapped columns), and after an analysis we want to map sites
 * back to their original position.   
 * @author brendano
 *
 */
public interface SiteMap {

	/**
	 * Find the original position of a site that now has the given index
	 * @param site
	 * @return
	 */
	public int getOriginalSite(int site);
	
	
}
