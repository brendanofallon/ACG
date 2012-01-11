package jobqueue;

/**
 * Interface for things that listen for changes in job status
 * @author brendan
 *
 */
public interface JobListener {

	/**
	 * Fired when a job's status changes
	 * @param job
	 */
	public void statusUpdated(ACGJob job);
}
