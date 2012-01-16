package newgui.gui.display.jobDisplay;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.Timer;


import jobqueue.ACGJob;
import jobqueue.JobListener;
import jobqueue.JobState;
import jobqueue.JobState.State;

/**
 * A view of a single ACGJob. These are typically displayed in a list in the JobQueueDisplay. 
 * Each jobview runs a javax.swing.Timer that periodically fires, causing this JobView to
 * update various UI elements, such as the progress bar and the status label
 * @author brendano
 *
 */
public class JobView extends JPanel implements JobListener, ActionListener {

	private ACGJob job;
	
	public JobView(ACGJob job) {
		this.job = job;
		job.addListener(this);
		initComponents();
		timer = new Timer(500, this);
	}

	private void initComponents() {
		this.setLayout(new BorderLayout());
		
		JPanel topPanel = new JPanel();
		topPanel.setOpaque(false);
		topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));
		JLabel titleLabel = new JLabel("<html> <b> " + job.getJobTitle() + " </b> </html>");
		topPanel.add(titleLabel);
		this.add(topPanel, BorderLayout.NORTH);
		
		JPanel centerPanel = new JPanel();
		centerPanel.setOpaque(false);
		centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
		centerPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
		
		JLabel statusLabel = new JLabel("Job status : <html> <i> " + getStatusString() + " </i> </html>");
		centerPanel.add(statusLabel);
		
		progressBar = new JProgressBar(0, job.getTotalRunLength());
		centerPanel.add(progressBar);
		this.add(centerPanel, BorderLayout.CENTER);
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		int currentStep = job.getCurrentStep();
		progressBar.setValue(currentStep);
		progressBar.repaint();
	}

	private String getStatusString() {
		if (currentState == JobState.State.COMPLETED) {
			return "Completed";
		}
		if (currentState == JobState.State.ERROR) {
			return "Error";
		}
		if (currentState == JobState.State.NOT_STARTED) {
			return "In queue";
		}
		if (currentState == JobState.State.RUNNING) {
			return "Running";
		}
		if (currentState == JobState.State.PAUSED) {
			return "Paused";
		}
		
		return "Unknown";
	}

	@Override
	public void statusUpdated(ACGJob job) {
		System.out.println("Status updated to : " + job.getJobState().getState() + " step: " + job.getCurrentStep());
		statusLabel.setText("Job status : <html> <i> " + getStatusString() + " </i> </html>");
		statusLabel.revalidate();
		statusLabel.repaint();
		if (job.getJobState().getState() != currentState) {
			previousState = currentState;
			currentState = job.getJobState().getState();
		}
		
		if ( (!timer.isRunning()) && currentState == State.RUNNING) {
			System.out.println("Starting timer...");
			timer.start();
		}
		
		if (currentState == State.COMPLETED) {
			timer.stop();
		}
		
	}
	
	private State currentState = State.NOT_STARTED;
	private State previousState = State.NOT_STARTED;
	private Timer timer;
	private JLabel statusLabel;
	private JProgressBar progressBar;

	
}
