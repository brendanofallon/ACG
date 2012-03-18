package newgui.gui.display.jobDisplay;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import jobqueue.ACGJob;
import jobqueue.JobQueue;
import jobqueue.QueueListener;
import jobqueue.QueueManager;
import newgui.gui.display.*;
/**
 * A display that shows the current job queue, with completed, running, and scheduled jobs
 * @author brendano
 *
 */
public class JobQueueDisplay extends Display implements QueueListener {

	private JobQueue queue;
	
	//Stores all jobviews currently displayed in the center panel 
	private List<JobView> jobViews = new ArrayList<JobView>();
	
	public JobQueueDisplay() {
		queue = QueueManager.getCurrentQueue();
		setTitle("Job Manager");
		queue.addListener(this);
		initComponents();
	}

	private void initComponents() {
		setLayout(new BorderLayout());
		
		JPanel topPanel = new JPanel();
		topPanel.setOpaque(false);
		
		centerPanel = new JPanel();
		centerPanel.setOpaque(false);
		centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
		
		layoutJobViews();
		
		this.add(topPanel, BorderLayout.NORTH);
		this.add(centerPanel, BorderLayout.CENTER);
	}

	/**
	 * Removes all jobViews and re-adds them based on the given state of the queue
	 */
	private void layoutJobViews() {
		centerPanel.removeAll();
		
		for(ACGJob job : queue.getJobs()) {
			JobView jView = viewForJob(job);
			if (jView == null) {
				jView = new JobView(null, job);
				jobViews.add(jView);
			}
			centerPanel.add(jView);
		}
		
		centerPanel.revalidate();
		centerPanel.repaint();
	}
	
	/**
	 * Returns the JObView associated with the given job, or null if there
	 * is no such view
	 * @param job
	 * @return
	 */
	private JobView viewForJob(ACGJob job) {
		for(JobView view : jobViews) {
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
