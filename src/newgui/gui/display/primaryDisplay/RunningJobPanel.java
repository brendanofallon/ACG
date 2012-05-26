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
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import mcmc.MCMC;
import mcmc.MCMCListener;
import newgui.UIConstants;
import newgui.datafile.XMLConversionError;
import newgui.datafile.resultsfile.ResultsFile;
import newgui.gui.ViewerWindow;
import newgui.gui.display.jobDisplay.JobView;
import newgui.gui.display.primaryDisplay.loggerVizualizer.BPDensityViz;
import newgui.gui.display.primaryDisplay.loggerVizualizer.BPLocationViz;
import newgui.gui.display.primaryDisplay.loggerVizualizer.ConsensusTreeViz;
import newgui.gui.display.primaryDisplay.loggerVizualizer.PopSizeViz;
import newgui.gui.display.primaryDisplay.loggerVizualizer.TMRCAViz;
import newgui.gui.widgets.sideTabPane.SideTabPane;

import jobqueue.ExecutingChain;
import jobqueue.JobQueue;
import jobqueue.QueueManager;
import logging.BreakpointDensity;
import logging.ConsensusTreeLogger;
import logging.MemoryStateLogger;
import logging.PopSizeLogger;
import logging.PropertyLogger;
import logging.RootHeightDensity;
import logging.BreakpointLocation;

/**
 * A panel that shows various characteristics of an executing run. 
 * @author brendano
 *
 */
public class RunningJobPanel extends JPanel implements MCMCListener {

	private PrimaryDisplay displayParent;
	private ACGDocument acgDoc = null;
	private ExecutingChain chain = null;
	
	public RunningJobPanel(PrimaryDisplay parentDisplay) {
		this.displayParent = parentDisplay;
		memLogger = new MemoryStateLogger();
		initComponents();
	}
	
	/**
	 * Obtain the ExecutingChain that encapsulates the running MCMC chain
	 * @return
	 */
	public ExecutingChain getChain() {
		return chain;
	}
	
	/**
	 * Obtain the ACGDocument that was used to create the chain
	 * @return
	 */
	public ACGDocument getACGDocument() {
		return acgDoc;
	}
	
	public void runJob(ACGDocument doc) {
		if (chain != null)
			throw new IllegalStateException("Only one chain per job panel");

		this.acgDoc = doc;
		
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
				
				if (logger instanceof BreakpointDensity) {
					BPDensityViz bpDensityViz = new BPDensityViz();
					bpDensityViz.initialize(logger);
					ImageIcon icon2 = UIConstants.getIcon("gui/icons/bpDensityIcon.png");
					sidePane.addTab(logger.getName(), icon2, bpDensityViz);
					sidePane.revalidate();
				}
				
				if (logger instanceof BreakpointLocation) {
					BPLocationViz bpLocationViz = new BPLocationViz();
					bpLocationViz.initialize(logger);
					ImageIcon icon2 = UIConstants.getIcon("gui/icons/bpLocationIcon.png");
					sidePane.addTab(logger.getName(), icon2, bpLocationViz);
					sidePane.revalidate();
				}
				
				if (logger instanceof RootHeightDensity) {
					TMRCAViz tmrcaViz = new TMRCAViz();
					tmrcaViz.initialize(logger);
					ImageIcon icon2 = UIConstants.getIcon("gui/icons/rootHeightIcon.png");
					sidePane.addTab(logger.getName(), icon2, tmrcaViz);
					sidePane.revalidate();
				}
				
				
				if (logger instanceof ConsensusTreeLogger) {
					ConsensusTreeViz treeViz = new ConsensusTreeViz();
					treeViz.initialize(logger);
					ImageIcon icon2 = UIConstants.getIcon("gui/icons/treeIcon.png");
					sidePane.addTab(logger.getName(), icon2, treeViz);
					sidePane.revalidate();
				}
				
				if (logger instanceof PopSizeLogger) {
					PopSizeViz popSizeViz = new PopSizeViz();
					popSizeViz.initialize(logger);
					ImageIcon icon2 = UIConstants.getIcon("gui/icons/popSizeIcon.png");
					sidePane.addTab(logger.getName(), icon2, popSizeViz);
					sidePane.revalidate();
				}
				
			} catch (Exception e) {
				ErrorWindow.showErrorWindow(e);
				e.printStackTrace();
			} 
			
		}
		
		//Sneak in a new listener that will store data that we can quickly write to displays
		chain.addListener(memLogger);
		memLogger.setBurnin( chain.getTotalRunLength()/10 ); //Someday we'll probably want to be more flexible about this
		
		seriesPanel.setMemoryLogger(memLogger);
		
		//The job is actually run by submitting it to a global "JobQueue" that manages all running jobs
		chain.setJobTitle( jobTitle + "-analysis" );
		JobQueue currentQueue = QueueManager.getCurrentQueue();
		currentQueue.addJob(chain);
		
		jobView = new JobView(this, chain);
		add(jobView, BorderLayout.NORTH);
		
		chain.addListener(this);
		
		revalidate();
		repaint();
	}
	
	public void saveResults() {
		ResultsFile file = new ResultsFile();
		try {
			file.addAllResults(chain, getACGDocument(), memLogger);			
			ViewerWindow.getViewer().getFileManager().showSaveDialog(file, displayParent.getTitle());

		} catch (XMLConversionError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void initComponents() {
		setLayout(new BorderLayout());
		
		sidePane = new SideTabPane();
		ImageIcon icon = UIConstants.getIcon("gui/icons/stateIcon.png");
		
		seriesPanel = new SeriesFigurePanel();
		sidePane.addTab("Model state", icon, seriesPanel);
		

		sidePane.selectTab(0);
		
		add(sidePane, BorderLayout.CENTER);
	}

	
	private void promptToSaveResults() {
		Object[] options = {"Don't save",
				"Save results"};
		int n = JOptionPane.showOptionDialog(this.getRootPane(),
				"Save these results?",
						"Run '" + displayParent.getTitle() + "' has completed",
						JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE,
						null,
						options,
						options[1]);
		if (n==1) {
			saveResults();
		}
	}

	///////////// MCMCListener implementation /////////////////////
	
	@Override
	public void setMCMC(MCMC chain) {
		this.mcmc = chain;
	}

	@Override
	public void newState(int stateNumber) {
		//Dont do anything
	}

	@Override
	public void chainIsFinished() {
		promptToSaveResults();
	}


	SeriesFigurePanel seriesPanel;
	MemoryStateLogger memLogger; //Listens to chains and logs parameter values / likelihoods
	List<PropertyLogger> propLoggers = new ArrayList<PropertyLogger>();
	SideTabPane sidePane;
	JobView jobView;
	MCMC mcmc = null;


}


