package jobqueue;

/**
 * These things listen for changes in a job queue - for instance new jobs being submitted or jobs being removed
 * (changes to the individual jobs themselves can be listened to via a JobListener)
 * @author brendano
 *
 */
public interface QueueListener {

	/**
	 * Called when a new job has been submitted, a job has been removed, or the job order
	 * has changed (which is not currently implemented)
	 * @param queue
	 */
	public void queueChanged(JobQueue queue);
	
}
