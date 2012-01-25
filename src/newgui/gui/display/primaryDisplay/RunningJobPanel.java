package newgui.gui.display.primaryDisplay;

import java.awt.BorderLayout;

import gui.document.ACGDocument;
import gui.figure.series.XYSeries;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import newgui.UIConstants;
import newgui.gui.display.jobDisplay.JobView;
import newgui.gui.widgets.sideTabPane.SideTabPane;

import jobqueue.ExecutingChain;
import jobqueue.JobQueue;
import jobqueue.QueueManager;
import logging.MemoryStateLogger;

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
		memLogger = new MemoryStateLogger();
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
		
		
		//Sneak in a new listener that will store data that we can quickly write to displays
		chain.addListener(memLogger);
		XYSeries dlSeries = memLogger.getSeries( memLogger.getSeriesNames().get(0) );
		seriesPanel.addSeries(memLogger.getSeriesNames().get(0), dlSeries);
		seriesPanel.setMemoryLogger(memLogger);
		
		//The job is actuallu run by submitting it to a global "JobQueue" that manages all running jobs
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
		
		
		sidePane.addTab("Parameters & Likelihoods", icon, seriesPanel);
		
		
		ImageIcon icon2 = UIConstants.getIcon("gui/icons/scaledBlueArrow.png");
		JLabel label2 = new JLabel("Component for tab 2");
		sidePane.addTab("Tab number dos", icon2, label2);
		
		add(sidePane, BorderLayout.CENTER);
	}
	
	MemoryStateLogger memLogger; //Listens to chains and logs parameter values / likelihoods
	SeriesFigurePanel seriesPanel = new SeriesFigurePanel();
	SideTabPane sidePane;
	JobView jobView;
}


