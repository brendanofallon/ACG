package jobqueue;

public interface ACGJob {

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
