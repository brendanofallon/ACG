package newgui.gui.display.primaryDisplay;

import gui.ErrorWindow;
import gui.document.ACGDocument;
import gui.inputPanels.AnalysisModel;
import gui.inputPanels.Configurator.InputConfigException;
import gui.inputPanels.MCMCModelView;
import gui.inputPanels.loggerConfigs.LoggersPanel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import jobqueue.ExecutingChain;
import jobqueue.JobQueue;
import jobqueue.QueueManager;
import jobqueue.JobQueue.Mode;
import logging.MemoryStateLogger;

import net.miginfocom.swing.MigLayout;
import newgui.UIConstants;
import newgui.datafile.AnalysisDataFile;
import newgui.datafile.XMLConversionError;
import newgui.gui.ViewerWindow;
import newgui.gui.modelViews.CoalModelView;
import newgui.gui.modelViews.LoggersView;
import newgui.gui.modelViews.MCModelView;
import newgui.gui.modelViews.SiteModelView;
import newgui.gui.widgets.BorderlessButton;
import newgui.gui.widgets.HighlightButton;
import newgui.gui.widgets.ImageButton;
import newgui.gui.widgets.ToolbarPanel;

/**
 * This panel appears when the user has selected an alignment and analysis type from the AnalysisPrepPanel
 * It gives users a chance to alter fine-grained details of the analysis (priors, operator types, etc)
 * before launching the analysis 
 * @author brendan
 *
 */
public class AnalysisDetailsPanel extends JPanel {

	private JPanel detailsPanel;
	private PrimaryDisplay displayParent;
	private AnalysisModel analysis = null; //Will be set after call to initialize(..)
	private AnalysisDataFile sourceFile = null; //Data file from which current model was read. May be null. 
	
	public AnalysisDetailsPanel(PrimaryDisplay displayParent) {
		this.displayParent = displayParent;
		initComponents();
	}

	/**
	 * Populate various widgets and settings in this panel with the options in the given model
	 * @param analysis
	 */
	public void initialize(AnalysisModel analysis, AnalysisDataFile sourceFile) {
		this.analysis = analysis;
		this.sourceFile = sourceFile;
		siteModelView = new SiteModelView( analysis.getSiteModel() );
		coalView = new CoalModelView( analysis.getCoalescentModel() );
		loggersView.setLoggerModels(analysis.getLoggerModels());
		mcView = new MCModelView( analysis.getMCModelElement() );
		substModelButton.fireActionEvent(null); //Cause subst model to be shown at first
		repaint();
	}
	
	private void initComponents() {
		setOpaque(false);
		this.setLayout(new BorderLayout());
		JPanel mainPanel = new JPanel();
		this.add(mainPanel, BorderLayout.CENTER);
		
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.X_AXIS));
		
		int buttonPanelWidth = 200;
		
		JPanel buttonsPanel = new JPanel();
		buttonsPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 10));
		buttonsPanel.setMinimumSize(new Dimension(buttonPanelWidth, 400));
		buttonsPanel.setPreferredSize(new Dimension(buttonPanelWidth, 400));
		buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.Y_AXIS));
		
		buttonsPanel.add(Box.createVerticalStrut(25));
		buttonsPanel.setAlignmentY(Component.TOP_ALIGNMENT);
		
		detailsPanel = new JPanel();
		detailsPanel.setAlignmentY(Component.TOP_ALIGNMENT);
		detailsPanel.setLayout(new BorderLayout());
		detailsPanel.setBorder(BorderFactory.createEmptyBorder(20,10,4,10));
		detailsPanel.setMinimumSize(new Dimension(400, 400));
		detailsPanel.setPreferredSize(new Dimension(500, 400));
		mainPanel.add(buttonsPanel);
		mainPanel.add(detailsPanel);
		
		substModelButton = new BorderlessButton("Nucleotide model");
		substModelButton.setToolTipText("Change options affecting the model of nucleotide evolution");
		substModelButton.setAlignmentX(Component.RIGHT_ALIGNMENT);
		substModelButton.setHorizontalTextAlignment(Component.RIGHT_ALIGNMENT);
		substModelButton.setMinimumSize(new Dimension(buttonPanelWidth, 40));
		substModelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				detailsPanel.removeAll();
				detailsPanel.add(siteModelView, BorderLayout.CENTER);
				setButtonHighlight(substModelButton);
				revalidate();
				repaint();
			}
		});
		buttonsPanel.add(substModelButton);

//		demoButton = new BorderlessButton("Demographic model");
//		demoButton.setToolTipText("Options affecting the model of population size change");
//		demoButton.setAlignmentX(Component.RIGHT_ALIGNMENT);
//		demoButton.setHorizontalTextAlignment(Component.RIGHT_ALIGNMENT);
//		demoButton.setMinimumSize(new Dimension(buttonPanelWidth, 40));
//		demoButton.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent e) {
//				detailsPanel.removeAll();
//				setButtonHighlight(demoButton);
//				revalidate();
//				repaint();
//			}
//		});
//		buttonsPanel.add(demoButton);
		
		coalescentButton = new BorderlessButton("Coalescent model");
		coalescentButton.setToolTipText("Options affecting popultion size and recombination");
		coalescentButton.setAlignmentX(Component.RIGHT_ALIGNMENT);
		coalescentButton.setHorizontalTextAlignment(Component.RIGHT_ALIGNMENT);
		coalescentButton.setMinimumSize(new Dimension(buttonPanelWidth, 40));
		coalescentButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				detailsPanel.removeAll();
				detailsPanel.add(coalView, BorderLayout.CENTER);
				setButtonHighlight(coalescentButton);
				revalidate();
				repaint();
			}
		});
		buttonsPanel.add(coalescentButton);
		
		loggingButton = new BorderlessButton("Logging options");
		loggingButton.setToolTipText("Options regarding the type of data collected");
		loggingButton.setAlignmentX(Component.RIGHT_ALIGNMENT);
		loggingButton.setHorizontalTextAlignment(Component.RIGHT_ALIGNMENT);
		loggingButton.setMinimumSize(new Dimension(buttonPanelWidth, 40));
		loggingButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				detailsPanel.removeAll();
				detailsPanel.add(loggersView, BorderLayout.CENTER);
				setButtonHighlight(loggingButton);
				revalidate();
				repaint();
			}
		});
		buttonsPanel.add(loggingButton);

		mcButton = new BorderlessButton("Markov chain options");
		mcButton.setToolTipText("Options regarding the length and number of chains run");
		mcButton.setAlignmentX(Component.RIGHT_ALIGNMENT);
		mcButton.setHorizontalTextAlignment(Component.RIGHT_ALIGNMENT);
		mcButton.setMinimumSize(new Dimension(buttonPanelWidth, 40));
		mcButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				detailsPanel.removeAll();
				detailsPanel.add(mcView, BorderLayout.CENTER);
				setButtonHighlight(mcButton);
				revalidate();
				repaint();
			}
		});
		buttonsPanel.add(mcButton);
		buttonsPanel.add(Box.createVerticalGlue());
		
		JPanel bottomPanel = new ToolbarPanel();
		bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.X_AXIS));
		bottomPanel.setPreferredSize(new Dimension(buttonPanelWidth, 50));
		bottomPanel.setMaximumSize(new Dimension(buttonPanelWidth, 50));
		BorderlessButton backButton = new BorderlessButton(UIConstants.blueLeftArrow);
		backButton.setToolTipText("Previous page");
		backButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				displayParent.showAlignmentPrepPanel();
			}			
		});
		
		
		BorderlessButton saveButton = new BorderlessButton(UIConstants.saveGrayButton);
		saveButton.setToolTipText("Save these settings");
		saveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveAnalysisFile();
			}
		});
		
		
		ImageButton runButton = new ImageButton(UIConstants.getIcon("gui/icons/beginRunButton.png"), UIConstants.getIcon("gui/icons/beginRunButton_pressed.png"));
		runButton.setToolTipText("Begin the analysis using the current settings");
		runButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				beginNewRun();
			}			
		});
		bottomPanel.add(backButton);
		bottomPanel.add(Box.createHorizontalGlue());
		bottomPanel.add(saveButton);
		bottomPanel.add(runButton);
		bottomPanel.add(Box.createHorizontalStrut(25));
		this.add(bottomPanel, BorderLayout.NORTH);
		
		loggersView = new LoggersView();
	}
	
	
	protected void saveAnalysisFile() {
		updateAllModels();
		try {
			
			ACGDocument acgDocument = analysis.getACGDocument();
			
			String suggestedName = displayParent.getTitle();
			if (sourceFile != null) {
				suggestedName = sourceFile.getSourceFile().getName().replace(".xml", "");
			}
			
			String analysisName = (String)JOptionPane.showInputDialog(ViewerWindow.getViewer(), 
													"Choose a name for these settings:",
													"Save analysis",
													JOptionPane.PLAIN_MESSAGE,
													null, 
													null,
													suggestedName);
			if (analysisName == null) {
				return;
			}
			
			if (! analysisName.endsWith(".xml")) {
				analysisName = analysisName + ".xml";
			}
			
			sourceFile = new AnalysisDataFile();
			sourceFile.setACGDocument(acgDocument);
			
			ViewerWindow.getViewer().getFileManager().showSaveDialog(sourceFile, analysisName.replace(".xml", ""));
			
		} catch (InputConfigException e) {
			ErrorWindow.showErrorWindow(e);
		} catch (XMLConversionError e) {
			e.printStackTrace();
			ErrorWindow.showErrorWindow(e);
		}
	}

	/**
	 * Start a new run of the analysis described in this pane. This involves  creating a new ACGDocument
	 * based on the current settings, and then creating a new ExecutingChain object which can actually
	 * run the analysis. We then submit the ExecutingChain to the default job queue, and tell the 
	 * ViewerWindow to open the JobQueueDisplay so
	 * we can watch it run. 
	 */
	protected void beginNewRun() {
		ACGDocument acgDocument;
		try {
			updateAllModels();
		
			acgDocument = analysis.getACGDocument();
			
			displayParent.showJobPanel(acgDocument);
			
		} catch (InputConfigException e) {
			System.out.println("Input config exception, could not create ACG document: " + e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
			System.out.println("Input config exception, could not create ACG document: " + e.getMessage());
			e.printStackTrace();
		}
		
	}

	private void updateAllModels() {
		try {
			siteModelView.updateModel();
			coalView.updateModel();
			loggersView.updateModels();
			mcView.updateModel();
	
			analysis.setLoggerModels(loggersView.getLoggerModels());		
		} catch (InputConfigException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	protected void setButtonHighlight(BorderlessButton button) {
		if (prevButton != null)
			prevButton.setFont(BorderlessButton.getDefaultFont());
		button.setFont(UIConstants.sansFontBold.deriveFont(15f));
		prevButton = button;
	}

	
	
	/**
	 * Open a file browser that allows the user to select a file to import
	 * @return
	 */
	private File browseForFile() {
		File selectedFile = null;
        
		//If we're on a mac then a FileDialog looks better and supports a few mac-specific options 
		if (UIConstants.isMac()) {
			FileDialog fileDialog = new FileDialog(ViewerWindow.getViewer(), "Choose a file");
			fileDialog.setMode(FileDialog.LOAD);
			String userDir = System.getProperty("user.dir");
			if (userDir != null)
				fileDialog.setDirectory(userDir);
			
			fileDialog.setVisible(true);
			
			String filename = fileDialog.getFile();
			String path = fileDialog.getDirectory();
			selectedFile = new File(path + filename);
		}
		else {
			//Not on a mac, use a JFileChooser instead of a FileDialog
			
			//Construct a new file choose whose default path is the path to this executable, which 
			//is returned by System.getProperty("user.dir")
			JFileChooser fileChooser = new JFileChooser( System.getProperty("user.dir"));
			int option = fileChooser.showOpenDialog(ViewerWindow.getViewer());
			if (option == JFileChooser.APPROVE_OPTION) {
				selectedFile = fileChooser.getSelectedFile();
			}
		}
		
		return selectedFile;
	}
	
	
	private BorderlessButton demoButton;
	private BorderlessButton coalescentButton;
	private BorderlessButton loggingButton;
	private BorderlessButton substModelButton;
	private BorderlessButton mcButton;
	private BorderlessButton prevButton = null;
	private SiteModelView siteModelView;
	private CoalModelView coalView;
	private LoggersView loggersView;
	private MCModelView mcView;
	
}
