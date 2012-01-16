package newgui.gui.display.jobDisplay;

import java.awt.BorderLayout;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import jobqueue.ACGJob;
import jobqueue.JobQueue;
import jobqueue.QueueManager;
import newgui.gui.display.*;
/**
 * A display that shows the current job queue, with completed, running, and scheduled jobs
 * @author brendano
 *
 */
public class JobQueueDisplay extends Display {

	private JobQueue queue;
	
	public JobQueueDisplay() {
		queue = QueueManager.getCurrentQueue();
		setTitle("Job Manager");
		initComponents();
	}

	private void initComponents() {
		setLayout(new BorderLayout());
		
		JPanel topPanel = new JPanel();
		topPanel.setOpaque(false);
		
		JPanel centerPanel = new JPanel();
		centerPanel.setOpaque(false);
		centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
		
		for(ACGJob job : queue.getJobs()) {
			JobView jView = new JobView(job);
			centerPanel.add(jView);
		}
		
		this.add(topPanel, BorderLayout.NORTH);
		this.add(centerPanel, BorderLayout.CENTER);
	}
}
