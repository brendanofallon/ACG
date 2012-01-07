package newgui.gui.display.primaryDisplay;

import gui.inputPanels.AnalysisModel;
import gui.inputPanels.CoalescentView;
import gui.inputPanels.MCMCModelView;
import gui.inputPanels.SiteModelView;
import gui.inputPanels.loggerConfigs.LoggersPanel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;
import newgui.analysisTemplate.BasicAnalysis;
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
	
	public AnalysisDetailsPanel() {
		initComponents();
	}

	/**
	 * Populate various widgets and settings in this panel with the options in the given model
	 * @param analysis
	 */
	public void initialize(AnalysisModel analysis) {
		//FIX ME!
		siteModelView.setSiteModel(analysis.getSiteModel());
		coalView.setCoalModel(analysis.getCoalescentModel());
		repaint();
	}
	
	private void initComponents() {
		setOpaque(false);
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		
		JPanel buttonsPanel = new JPanel();
		buttonsPanel.setPreferredSize(new Dimension(150, 400));
		buttonsPanel.setLayout(new MigLayout());
		
		detailsPanel = new JPanel();
		detailsPanel.setLayout(new BorderLayout());
		detailsPanel.setBorder(BorderFactory.createEmptyBorder(4,10,4,10));
		
		add(buttonsPanel);
		add(detailsPanel);
		
		BorderlessButton substModelButton = new BorderlessButton("Nucleotide substitution model");
		substModelButton.setToolTipText("Change options affecting the model of nucleotide evolution");
		substModelButton.setPreferredSize(new Dimension(150, 50));
		substModelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				detailsPanel.removeAll();
				detailsPanel.add(siteModelView, BorderLayout.CENTER);
				revalidate();
				repaint();
			}
		});
		buttonsPanel.add(substModelButton, "wrap");

		BorderlessButton demoButton = new BorderlessButton("Demographic model");
		demoButton.setToolTipText("Options affecting the model of population size change");
		demoButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
//				detailsPanel.removeAll();
//				detailsPanel.add(siteModelView, BorderLayout.CENTER);				
			}
		});
		demoButton.setPreferredSize(new Dimension(150, 50));
		buttonsPanel.add(demoButton, "wrap");
		
		BorderlessButton coalescentButton = new BorderlessButton("Recombination model");
		coalescentButton.setToolTipText("Change options affecting recombination model");
		coalescentButton.setPreferredSize(new Dimension(150, 50));
		coalescentButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				detailsPanel.removeAll();
				detailsPanel.add(coalView, BorderLayout.CENTER);
				revalidate();
				repaint();
			}
		});
		buttonsPanel.add(coalescentButton, "wrap");
		
		BorderlessButton loggingButton = new BorderlessButton("Logging options");
		loggingButton.setToolTipText("Change options regarding the type of data collected");
		loggingButton.setPreferredSize(new Dimension(150, 50));
		buttonsPanel.add(loggingButton, "wrap");

		BorderlessButton mcButton = new BorderlessButton("Markov chain options");
		mcButton.setToolTipText("Change options regarding the length and number of chains run");
		mcButton.setPreferredSize(new Dimension(150, 50));
		buttonsPanel.add(mcButton, "wrap");

		
		siteModelView = new SiteModelView();
		coalView = new CoalescentView();
		loggersView = new LoggersPanel();
		//mcView = new MCMCModelView();
	}
	
	
	private SiteModelView siteModelView;
	private CoalescentView coalView;
	private LoggersPanel loggersView;
	private MCMCModelView mcView;
	
}
