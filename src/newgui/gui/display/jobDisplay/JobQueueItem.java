package newgui.gui.display.jobDisplay;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.Timer;

import jobqueue.ACGJob;
import jobqueue.JobListener;
import jobqueue.JobState;
import jobqueue.JobState.State;
import newgui.UIConstants;
import newgui.gui.display.primaryDisplay.RunningJobPanel;
import newgui.gui.widgets.BorderlessButton;
import newgui.gui.widgets.ToolbarPanel;

/**
 * Single item in job queue display
 * 
 * @author brendanofallon
 *
 */
public class JobQueueItem extends ToolbarPanel implements JobListener, ActionListener {
	
	private ACGJob job;
	private RunningJobPanel jobPanel;
	
	public JobQueueItem(RunningJobPanel jobPanel, ACGJob job) {
		this.job = job;
		this.jobPanel = jobPanel;
		job.addListener(this);
		initComponents();
		timer = new Timer(500, this);
		
		//Since mcmc's will execute in another thread, they can't reliably fire events to their listeners
		// (at least, it seems like they should be able to, but threads appear to stall...), so instead
		//we start a timer that constantly monitors their state..
		timer.start();
		statusUpdated(job); //Forces initial configuration of timer and some components
	}

	private void initComponents() {
		this.setLayout(new BorderLayout());
		this.setBorder(BorderFactory.createEmptyBorder(10, 20, 0, 10));
		this.setOpaque(false);
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
		startButton.setToolTipText("Resume running this job");
		startButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				resumeJob();
			}
		});

		BorderlessButton pauseButton = new BorderlessButton(UIConstants.pauseButton);
		pauseButton.setToolTipText("Pause this job");
		pauseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				pauseJob();
			}
		});
		
		BorderlessButton stopButton = new BorderlessButton(UIConstants.stopButton);
		stopButton.setToolTipText("Abort this job");
		stopButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				killJob();
			}
		});
		
		BorderlessButton saveResultsButton = new BorderlessButton(UIConstants.saveGrayButton);
		saveResultsButton.setToolTipText("Save results");
		saveResultsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				saveResults();
			}
		});
		
		statusPanel.add(statusLabel);
		statusPanel.add(Box.createHorizontalGlue());
//		if (jobPanel != null)
//			statusPanel.add(saveResultsButton);
		statusPanel.add(startButton);
		statusPanel.add(Box.createHorizontalStrut(4));
		statusPanel.add(pauseButton);
		statusPanel.add(Box.createHorizontalStrut(4));
		statusPanel.add(stopButton);
		
		statusPanel.add(Box.createHorizontalStrut(25));
		statusPanel.setMaximumSize(new Dimension(1000, 34));
		
		
		centerPanel.add(statusPanel);
		updateStatusLabel();
		
		progressBar = new JProgressBar(0, job.getTotalRunLength());
		progressBar.setStringPainted(true);
		centerPanel.add(progressBar);
		this.add(centerPanel, BorderLayout.CENTER);
		
		JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.X_AXIS));
		bottomPanel.setOpaque(false);
		bottomPanel.add(Box.createRigidArea(new Dimension(10, 10)));
		
		this.setMaximumSize(new Dimension(32167, 200));
		this.add(bottomPanel, BorderLayout.SOUTH);
		
	}
	
	/**
	 * Called when user clicks the save results button. Causes the RunningJobPanel to create and save
	 * a ResultsFile encapsulating the results of this run
	 */
	protected void saveResults() {
		jobPanel.saveResults();
	}

	protected void pauseJob() {
		if (job.getJobState().getState() == State.RUNNING) {
			job.pause();
		}
	}

	protected void resumeJob() {
		if (job.getJobState().getState() == State.PAUSED) {
			job.resume();
		}
	}

	protected void killJob() {
		if (currentState != JobState.State.COMPLETED) {
			int n = JOptionPane.showConfirmDialog(this, "Abort this job?");
			if (n == JOptionPane.OK_OPTION) {
				job.abort();	
			}
		}
	}

	@Override
	/**
	 * This gets called every time the timer fires. We update the status label and progress bar
	 * here
	 */
	public void actionPerformed(ActionEvent arg0) {
		int currentStep = job.getCurrentStep();
		progressBar.setValue(currentStep);
		progressBar.setString("" + currentStep);
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
		currentState = job.getJobState().getState();
		
		updateStatusLabel();
		
		if ( (!timer.isRunning()) && currentState == State.RUNNING) {
			timer.start();
		}
		
		if (currentState == State.COMPLETED) {
			actionPerformed(null); //updates status label & progress bar
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
	private Timer timer;
	private JLabel statusLabel;
	private JProgressBar progressBar;

}
