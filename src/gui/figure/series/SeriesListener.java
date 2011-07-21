package figure.series;

/**
 * An interface for parties interested in when series in a series figure are changed
 * @author brendan
 *
 */
public interface SeriesListener {

	public void seriesRemoved(AbstractSeries removedSeries);
	
	public void seriesChanged(AbstractSeries changedSeries);
	
}
