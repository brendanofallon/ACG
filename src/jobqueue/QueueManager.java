package jobqueue;

/**
 * Provides a few static methods for dealing with JobQueues / running jobs
 * @author brendano
 *
 */
public class QueueManager {

	private static JobQueue currentQueue = null;
	
	/**
	 * Creates a new job queue. Someday we'll likely add the functionality to make different
	 * types of queues, but for right now this just makes the simple / basic one
	 * @return
	 */
	public static JobQueue createJobQueue() {
		return new JobQueue();
	}
	
	/**
	 * Returns the current jobqueue (making a new one if necessary)
	 * @return
	 */
	public static JobQueue getCurrentQueue() {
		if (currentQueue == null)
			currentQueue = createJobQueue();
		return currentQueue;
	}
	
}
