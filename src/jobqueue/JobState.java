package jobqueue;

/**
 * Small class to hold some information about the state of a job
 * @author brendan
 *
 */
public class JobState {

	enum State {NOT_STARTED, RUNNING, PAUSED, ERROR, COMPLETED};
	
	private State state = State.NOT_STARTED;
	
	public State getState() {
		return state;
	}
	
	public void setState(State newState) {
		this.state = newState;
	}
}
