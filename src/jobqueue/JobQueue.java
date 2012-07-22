package jobqueue;

import gui.ACGFrame;

import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingWorker;

import jobqueue.JobQueue.Mode;
import jobqueue.JobState.State;

/**
 * A list of ACGJobs that are executing or will be soon executed.
 * New jobs may be added by addJob(..) and will begin running as soon as other
 * jobs have completed
 * @author brendan
 *
 */
public class JobQueue implements JobListener {

	protected List<ACGJob> queue = new ArrayList<ACGJob>();
	
	/**
	 * Mode describes the current run policy. Run-at-will means that a job will
	 * be executed in the order they are added, with one job running at a time.
	 * pause-job will instantly pause the running job. 
	 * stop-after will continue running the current job until it completed, but will
	 * not run any more jobs thereafter 
	 * @author brendan
	 *
	 */
	public enum Mode {RUN_AT_WILL, STOP_AFTER};
	
	protected Mode currentMode = Mode.RUN_AT_WILL;
	protected ACGJob currentJob = null;
	
	public JobQueue() {
		
	}
	
	/**
	 * Get the total size of the queue, including jobs that have already completed
	 * @return
	 */
	public int getQueueSize() {
		return queue.size();
	}
	
	/**
	 * Obtain the which'th job in the queue
	 * @param which
	 * @return
	 */
	public ACGJob getJob(int which) {
		return queue.get(which);
	}
	
	/**
	 * Return a list of all jobs in the queue
	 * @return
	 */
	public List<ACGJob> getJobs() {
		return queue;
	}
	
	/**
	 * Get the currently executing job, if there is one. Otherwise returns null
	 * @return
	 */
	public ACGJob getCurrentJob() {
		return currentJob;
	}
	
	/**
	 * Add the given job to the queue
	 * @param job
	 */
	public void addJob(ACGJob job) {
	//	System.out.println("Adding job " + job.getJobTitle() + " to queue, state is " + job.getJobState().getState() );
		if (job.getJobState().getState() == State.RUNNING) {
			throw new IllegalArgumentException("Job is already running! This can't happen because it may lead to concurrent jobs running");
		}
		job.addListener(this);
		queue.add(job);
		handleQueueUpdate();
		fireQueueChangeEvent();
	}
	
	/**
	 * Returns true if this queue contains the given job
	 * @param job
	 * @return
	 */
	public boolean containsJob(ACGJob job) {
		return queue.contains(job);
	}

	/**
	 * Removes the given from from this queue
	 * @param job
	 */
	public void removeJob(ACGJob job) {
		job.removeListener(this);
		queue.remove(job);
		if (job == currentJob)
			currentJob = null;

		handleQueueUpdate();
		fireQueueChangeEvent();
	}
	
	/**
	 * Get the number of jobs that have completed in this queue
	 * @return
	 */
	public int getCompletedJobCount() {
		int count = 0;
		for(ACGJob job : queue) {
			if (job.getJobState().getState() == State.COMPLETED) {
				count++;
			}
		}
		return count;
	}
	
	/**
	 * Return the number of jobs in the queue whose state is NOT_STARTED
	 * @return
	 */
	public int getEligibleJobCount() {
		int count = 0;
		for(ACGJob job : queue) {
			if (job.getJobState().getState() == State.NOT_STARTED) {
				count++;
			}
		}
		return count;
		
	}
	
	
	/**
	 * Returns true if there is a currently running job
	 * @return
	 */
	public boolean isRunningJob() {
		return currentJob != null;
	}
	
	/**
	 * Set the current 'Mode' of this queue, which determines whether jobs are run as soon
	 * as previous jobs finish, or if we wait for user confirmation
	 * @param newMode
	 */
	public void setMode(Mode newMode) {
		if (currentMode == Mode.STOP_AFTER && newMode == Mode.RUN_AT_WILL) {
			currentMode = newMode;
			if (currentJob == null && hasEligibleJobs())
				handleQueueUpdate();
		}
		currentMode = newMode;
	}

	
	/**
	 * Returns true if there are any jobs in the queue whose state is NOT_STARTED
	 * @return
	 */
	public boolean hasEligibleJobs() {
		return findNextJob() != null;
	}
	
	@Override
	public void statusUpdated(ACGJob job) {
		JobState state = job.getJobState();
		//System.out.println("Status updated for job: " + job.getJobTitle() + " new status is: " + job.getJobState().getState());
		if (state.getState() == State.COMPLETED) {
			currentJob = null;
		}
		
		handleQueueUpdate();
	}
	
	public Mode getMode() {
		return currentMode;
	}
	
	/**
	 * Submit a new job to the queue if the mode is comptible 
	 */
	private void handleQueueUpdate() {
		//System.out.println("Handling queue update, current job is " + currentJob + " mode is: " + currentMode);
		if (currentJob == null && currentMode == Mode.RUN_AT_WILL) {
			ACGJob nextJob = findNextJob();
			if (nextJob != null) {
				submitJob(nextJob);
			}	
			else {
				System.out.println("nextJob is null, not submitting any job");
			}
		}
	}
	
	/**
	 * Run the given job in the background
	 * @param nextJob
	 */
	private void submitJob(final ACGJob nextJob) {
		JobRunner runner = new JobRunner(nextJob);
		currentJob =  nextJob;
		runner.execute();
	}


	/**
	 * Add a new listener for changes to this queue. This method ensures that each
	 * object is only added once! If list
	 * @param l
	 */
	public void addListener(QueueListener l) {
		if (!listeners.contains(l))
			listeners.add(l);	
	}
	
	/**
	 * Remove this listener from the queue
	 * @param l
	 */
	public void removeListener(QueueListener l) {
		listeners.remove(l);
	}
	
	
	/**
	 * Fire a change event to all listeners
	 */
	protected void fireQueueChangeEvent() {
		for(QueueListener l : listeners) {
			l.queueChanged(this);
		}
	}
	
	
	/**
	 * Find the job with the lowest index in the list whose 
	 * status is NOT_STARTED. Returns null if there is no such job
	 * @return
	 */
	private ACGJob findNextJob() {
		for(ACGJob job : queue) {
			if (job.getJobState().getState() == State.NOT_STARTED) {
				return job;
			}
		}
		return null;
	}
	
	/**
	 * Mini class  to handle running ACGJobs in background
	 * @author brendano
	 *
	 */
	class JobRunner extends SwingWorker {

		final ACGJob job;
		public JobRunner(ACGJob job) {
			this.job = job;
		}
		
		@Override
		protected Object doInBackground() throws Exception {
			job.beginJob();
			return job;
		}
		
	}


	
	
	private List<QueueListener> listeners = new ArrayList<QueueListener>();
}
