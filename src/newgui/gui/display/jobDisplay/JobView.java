package newgui.gui.display.jobDisplay;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.Timer;

import newgui.UIConstants;
import newgui.gui.widgets.BorderlessButton;


import jobqueue.ACGJob;
import jobqueue.JobListener;
import jobqueue.JobState;
import jobqueue.JobState.State;

/**
 * A graphical view of a single ACGJob. These are typically displayed in a list in the JobQueueDisplay. 
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
		statusUpdated(job); //Forces initial configuration of timer and some components
	}

	private void initComponents() {
		this.setLayout(new BorderLayout());
		this.setBorder(BorderFactory.createEmptyBorder(10, 20, 0, 10));
		JPanel topPanel = new JPanel();
		topPanel.setOpaque(false);
		topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));
		JLabel titleLabel = new JLabel("<html> <b> " + job.getJobTitle() + " </b> </html>");
		topPanel.add(titleLabel);
		this.add(topPanel, BorderLayout.NORTH);
		
		JPanel centerPanel = new JPanel();
		centerPanel.setOpaque(false);
		centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
		centerPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));
		
		JPanel statusPanel = new JPanel();
		statusPanel.setOpaque(false);
		statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.X_AXIS));
		statusLabel = new JLabel("<html> Job status : <em> In queue </em> </html>");
		BorderlessButton startButton = new BorderlessButton(UIConstants.startButton);
		startButton.setPreferredSize(new Dimension(24, 22));
		startButton.setXDif(-2);
		startButton.setYDif(-1);
		BorderlessButton pauseButton = new BorderlessButton(UIConstants.pauseButton);
		pauseButton.setPreferredSize(new Dimension(24, 22));
		pauseButton.setXDif(-1);
		pauseButton.setYDif(-1);
		statusPanel.add(statusLabel);
		statusPanel.add(Box.createHorizontalGlue());
		statusPanel.add(startButton);
		statusPanel.add(pauseButton);
		
		statusPanel.add(Box.createHorizontalStrut(25));
		statusPanel.setMaximumSize(new Dimension(1000, 26));
		
		
		centerPanel.add(statusPanel);
		updateStatusLabel();
		
		progressBar = new JProgressBar(0, job.getTotalRunLength());
		centerPanel.add(progressBar);
		this.add(centerPanel, BorderLayout.CENTER);
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		int currentStep = job.getCurrentStep();
		progressBar.setValue(currentStep);
		progressBar.repaint();
		updateStatusLabel();
	}

	/**
	 * Get a user-friendly string describing job status
	 * @return
	 */
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

	/**
	 * Change text in status label to reflect current job status
	 */
	private void updateStatusLabel() {
		statusLabel.setText("<html> Job status : <em> " + getStatusString() + " </em> </html>");
		statusLabel.revalidate();
		statusLabel.repaint();
	}
	
	@Override
	public void statusUpdated(ACGJob job) {
		System.out.println("Status updated to : " + job.getJobState().getState() + " step: " + job.getCurrentStep() + " state: " + job.getJobState().getState());
		updateStatusLabel();
		
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
	
	/**
	 * Obtain the job that this view is displaying
	 * @return
	 */
	public ACGJob getJob() {
		return job;
	}
	
	private State currentState = State.NOT_STARTED;
	private State previousState = State.NOT_STARTED;
	private Timer timer;
	private JLabel statusLabel;
	private JProgressBar progressBar;
	
}
