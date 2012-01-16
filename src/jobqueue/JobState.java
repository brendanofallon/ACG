package jobqueue;

/**
 * Small class to hold some information about the state of an ACGJob
 * @author brendan
 *
 */
public class JobState {

	public enum State {NOT_STARTED, RUNNING, PAUSED, ERROR, COMPLETED};
	
	private State state = State.NOT_STARTED;
	private int currentStep = 0;
	private int runLength = -1;
	private final ACGJob job;
	
	public JobState(ACGJob job) {
		this.job = job;
	}
	
	public State getState() {
		return state;
	}
	
	public void setState(State newState) {
		this.state = newState;
	}
	
	/**
	 * Set the current mcmc step of the job
	 * @param step
	 */
	public void setStep(int step) {
		this.currentStep = step;
	}
	
	
	public int getCurrentStep() {
		return currentStep;
	}
	
	/**
	 * Return the maximum number of mcmc steps this job will run
	 */
	public int getTotalRunLength() {
		return runLength;
	}
	
}
