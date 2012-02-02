package newgui.gui.display.primaryDisplay;

import java.awt.BorderLayout;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import gui.ErrorWindow;
import gui.document.ACGDocument;
import gui.figure.series.XYSeries;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import newgui.UIConstants;
import newgui.gui.display.jobDisplay.JobView;
import newgui.gui.display.primaryDisplay.loggerVizualizer.BPDensityViz;
import newgui.gui.display.primaryDisplay.loggerVizualizer.ConsensusTreeViz;
import newgui.gui.display.primaryDisplay.loggerVizualizer.TMRCAViz;
import newgui.gui.widgets.sideTabPane.SideTabPane;

import jobqueue.ExecutingChain;
import jobqueue.JobQueue;
import jobqueue.QueueManager;
import logging.BreakpointDensity;
import logging.ConsensusTreeLogger;
import logging.MemoryStateLogger;
import logging.PropertyLogger;
import logging.RootHeightDensity;

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

		
		//Must go first, otherwise objects in document will not be loaded / instantiated
		String jobTitle = displayParent.getTitle().replace(".xml", ""); 
		try {
			chain = new ExecutingChain(doc);
		} catch (Exception e) {
			ErrorWindow.showErrorWindow(e);
			e.printStackTrace();
		}
		
		//Grab all property loggers from document so we can create visualizers / tabs for them in the tab pane
		for(String label : doc.getLabelForClass(PropertyLogger.class)) {
			try {
				PropertyLogger logger = (PropertyLogger) doc.getObjectForLabel(label);
				propLoggers.add(logger);
				System.out.println("Adding logger : " + logger);
				
				if (logger instanceof BreakpointDensity) {
					BPDensityViz bpDensityViz = new BPDensityViz();
					bpDensityViz.initialize(logger);
					ImageIcon icon2 = UIConstants.getIcon("gui/icons/scaledBlueArrow.png");
					sidePane.addTab("Breakpoint Density", icon2, bpDensityViz);
					sidePane.revalidate();
				}
				
				if (logger instanceof RootHeightDensity) {
					TMRCAViz tmrcaViz = new TMRCAViz();
					tmrcaViz.initialize(logger);
					ImageIcon icon2 = UIConstants.getIcon("gui/icons/scaledBlueArrow.png");
					sidePane.addTab("TMRCA Density", icon2, tmrcaViz);
					sidePane.revalidate();
				}
				
				
				if (logger instanceof ConsensusTreeLogger) {
					ConsensusTreeViz treeViz = new ConsensusTreeViz();
					treeViz.initialize(logger);
					ImageIcon icon2 = UIConstants.getIcon("gui/icons/scaledBlueArrow.png");
					sidePane.addTab("Marginal tree", icon2, treeViz);
					sidePane.revalidate();
				}
				
			} catch (Exception e) {
				ErrorWindow.showErrorWindow(e);
				e.printStackTrace();
			} 
			
		}

		
		
		//Sneak in a new listener that will store data that we can quickly write to displays
		chain.addListener(memLogger);
		
		//XYSeries dlSeries = memLogger.getSeries( memLogger.getSeriesNames().get(0) );
		seriesPanel.initializeLogger(memLogger);
		seriesPanel.addDefaultSeriesPanel();
		
		//The job is actually run by submitting it to a global "JobQueue" that manages all running jobs
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
		
		seriesPanel = new MultiSeriesPanel();
		sidePane.addTab("Parameters & Likelihoods", icon, seriesPanel);
		
		
		ImageIcon icon2 = UIConstants.getIcon("gui/icons/scaledBlueArrow.png");
		
		BPDensityViz bpDensityViz = new BPDensityViz();
		
		//sidePane.addTab("Breakpoint Density", icon2, bpDensityViz);
		
		sidePane.selectTab(0);
		
		add(sidePane, BorderLayout.CENTER);
	}
	
	MultiSeriesPanel seriesPanel;
	MemoryStateLogger memLogger; //Listens to chains and logs parameter values / likelihoods
	List<PropertyLogger> propLoggers = new ArrayList<PropertyLogger>();
	SideTabPane sidePane;
	JobView jobView;
}


