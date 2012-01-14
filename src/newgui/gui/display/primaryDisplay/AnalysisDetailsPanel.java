package newgui.gui.display.primaryDisplay;

import gui.inputPanels.AnalysisModel;
import gui.inputPanels.MCMCModelView;
import gui.inputPanels.loggerConfigs.LoggersPanel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;
import newgui.gui.ViewerWindow;
import newgui.gui.modelViews.CoalModelView;
import newgui.gui.modelViews.LoggersView;
import newgui.gui.modelViews.MCModelView;
import newgui.gui.modelViews.SiteModelView;
import newgui.gui.widgets.BorderlessButton;

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
	
	public AnalysisDetailsPanel(PrimaryDisplay displayParent) {
		this.displayParent = displayParent;
		initComponents();
	}

	/**
	 * Populate various widgets and settings in this panel with the options in the given model
	 * @param analysis
	 */
	public void initialize(AnalysisModel analysis) {
		//FIX ME!
		this.analysis = analysis;
		siteModelView.setSiteModel(analysis.getSiteModel());
		coalView.setCoalModel(analysis.getCoalescentModel());
		loggersView.setLoggerModels(analysis.getLoggerModels());
		mcView.setModel( analysis.getMCModelElement() );
		repaint();
	}
	
	private void initComponents() {
		setOpaque(false);
		this.setLayout(new BorderLayout());
		JPanel mainPanel = new JPanel();
		this.add(mainPanel, BorderLayout.CENTER);
		
		mainPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		
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
		//detailsPanel.setBorder(BorderFactory.createLineBorder(Color.GREEN));
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

		demoButton = new BorderlessButton("Demographic model");
		demoButton.setToolTipText("Options affecting the model of population size change");
		demoButton.setAlignmentX(Component.RIGHT_ALIGNMENT);
		demoButton.setHorizontalTextAlignment(Component.RIGHT_ALIGNMENT);
		demoButton.setMinimumSize(new Dimension(buttonPanelWidth, 40));
		demoButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				detailsPanel.removeAll();
//				detailsPanel.add(siteModelView, BorderLayout.CENTER);
				setButtonHighlight(demoButton);
				revalidate();
				repaint();
			}
		});
		buttonsPanel.add(demoButton);
		
		coalescentButton = new BorderlessButton("Recombination model");
		coalescentButton.setToolTipText("Change options affecting recombination model");
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
		loggingButton.setToolTipText("Change options regarding the type of data collected");
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
		mcButton.setToolTipText("Change options regarding the length and number of chains run");
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
		
		JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.X_AXIS));
		bottomPanel.setPreferredSize(new Dimension(buttonPanelWidth, 50));
		bottomPanel.setMaximumSize(new Dimension(buttonPanelWidth, 50));
		BorderlessButton backButton = new BorderlessButton("Back");
		backButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				displayParent.showAlignmentPrepPanel();
			}			
		});
		bottomPanel.add(backButton);
		
		BorderlessButton runButton = new BorderlessButton("Begin run");
		backButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// displayParent.();
			}			
		});
		bottomPanel.add(Box.createHorizontalGlue());
		bottomPanel.add(runButton);
		this.add(bottomPanel, BorderLayout.SOUTH);
		
		siteModelView = new SiteModelView();
		coalView = new CoalModelView();
		loggersView = new LoggersView();
		mcView = new MCModelView();
	}
	
	protected void setButtonHighlight(BorderlessButton button) {
		if (prevButton != null)
			prevButton.setFont(BorderlessButton.getDefaultFont());
		button.setFont(ViewerWindow.getFont("fonts/ClienB.ttf").deriveFont(15f));
		prevButton = button;
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
