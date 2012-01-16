package jobqueue;

public interface ACGJob {

	/**
	 * Return a title / label for this job
	 * @return
	 */
	public String getJobTitle();
	
	/**
	 * Begin running the current job
	 */
	public void beginJob();
	
	/**
	 * Obtain the JobState object that describes this job's state 
	 * @return
	 */
	public JobState getJobState();
	
	/**
	 * Return the maximum number of steps this job will run
	 * @return
	 */
	public int getTotalRunLength();
	
	/**
	 * Return the current mcmc step this job in on (will be approximate)
	 */
	public int getCurrentStep();
	
	/**
	 * Add new listener to this job
	 * @param listener
	 */
	public void addListener(JobListener listener);
	
	/**
	 * Remove the given listener from the  
	 * @param listener
	 */
	public void removeListener(JobListener listener);
	
	
}
