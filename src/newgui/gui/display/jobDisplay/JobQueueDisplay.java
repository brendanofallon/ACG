package newgui.gui.display.jobDisplay;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;


import jobqueue.ACGJob;
import jobqueue.JobQueue;
import jobqueue.QueueListener;
import jobqueue.QueueManager;
import newgui.UIConstants;
import newgui.gui.display.*;
/**
 * A display that shows the current job queue, with completed, running, and scheduled jobs
 * @author brendano
 *
 */
public class JobQueueDisplay extends Display implements QueueListener {

	private JobQueue queue;
	
	//Stores all jobviews currently displayed in the center panel 
	private List<JobQueueItem> jobViews = new ArrayList<JobQueueItem>();
	
	public JobQueueDisplay() {
		queue = QueueManager.getCurrentQueue();
		setTitle("Job Manager");
		queue.addListener(this);
		initComponents();
	}

	private void initComponents() {
		setLayout(new BorderLayout());
		setOpaque(false);
		JPanel topPanel = new JPanel();
		JLabel topLabel = new JLabel("All scheduled jobs : ");
		topLabel.setFont(UIConstants.sansFontBold.deriveFont(14f));
		topPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
		topPanel.add(topLabel);
		topPanel.setOpaque(false);
		
		centerPanel = new JPanel();
		centerPanel.setOpaque(false);
		centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
		
		layoutJobViews();
		
		this.add(topPanel, BorderLayout.NORTH);
		JScrollPane scrollPane = new JScrollPane(centerPanel);
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		scrollPane.setViewportBorder(BorderFactory.createEmptyBorder());
		scrollPane.setOpaque(false);
		scrollPane.getViewport().setOpaque(false);
		this.add(scrollPane, BorderLayout.CENTER);
	}

	/**
	 * Removes all jobViews and re-adds them based on the given state of the queue
	 */
	private void layoutJobViews() {
		centerPanel.removeAll();
		
		for(ACGJob job : queue.getJobs()) {
			JobQueueItem jView = viewForJob(job);
			if (jView == null) {
				jView = new JobQueueItem(null, job);
				jView.setBorder(BorderFactory.createEmptyBorder(5, 35, 5, 35));
				jobViews.add(jView);
			}
			centerPanel.add(jView);
		}
		
		if (queue.getJobs().size() == 0) {
			JLabel lab = new JLabel("No jobs in queue.");
			lab.setFont(UIConstants.sansFont.deriveFont(14f));
			centerPanel.add(lab);
			centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 0, 20));
		}
		
		centerPanel.add(Box.createVerticalGlue());
		centerPanel.revalidate();
		centerPanel.repaint();
	}
	
	/**
	 * Returns the JObView associated with the given job, or null if there
	 * is no such view
	 * @param job
	 * @return
	 */
	private JobQueueItem viewForJob(ACGJob job) {
		for(JobQueueItem view : jobViews) {
			ACGJob viewJob = view.getJob();
			if (viewJob == job)
				return view;
		}
		return null;
	}
	
	@Override
	public void queueChanged(JobQueue queue) {
		layoutJobViews();
	}
	
	JPanel centerPanel;
}
