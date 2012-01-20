package newgui.gui.display.primaryDisplay;

import java.awt.BorderLayout;

import gui.document.ACGDocument;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import newgui.UIConstants;
import newgui.gui.display.jobDisplay.JobView;
import newgui.gui.widgets.sideTabPane.SideTabPane;

import jobqueue.ExecutingChain;
import jobqueue.JobQueue;
import jobqueue.QueueManager;

/**
 * A panel that shows various characteristics of an executing run. 
 * @author brendano
 *
 */
public class RunningJobPanel extends JPanel {

	private PrimaryDisplay displayParent;
	private ExecutingChain chain = null;
	
	public RunningJobPanel(PrimaryDisplay parentDisplay) {
		this.displayParent = parentDisplay;
		initComponents();
	}
	
	public void runJob(ACGDocument doc) {
		if (chain != null)
			throw new IllegalStateException("Only one chain per job panel");
		String jobTitle = displayParent.getTitle().replace(".xml", ""); 
		try {
			chain = new ExecutingChain(doc);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		chain.setJobTitle( jobTitle + "-analysis" );
		JobQueue currentQueue = QueueManager.getCurrentQueue();
		currentQueue.addJob(chain);
		jobView = new JobView(chain);
		add(jobView, BorderLayout.NORTH);
		revalidate();
		repaint();
	}
	
	private void initComponents() {
		setLayout(new BorderLayout());
		
		
		
		sidePane = new SideTabPane();
		ImageIcon icon = UIConstants.getIcon("gui/icons/openFile.png");
		JLabel label1 = new JLabel("Component for tab 1");
		
		sidePane.addTab("Tab number 1", icon, label1);
		
		ImageIcon icon2 = UIConstants.getIcon("gui/icons/scaledBlueArrow.png");
		JLabel label2 = new JLabel("Component for tab 2");
		sidePane.addTab("Tab number dos", icon2, label2);
		
		add(sidePane, BorderLayout.CENTER);
	}
	
	SideTabPane sidePane;
	JobView jobView;
}


