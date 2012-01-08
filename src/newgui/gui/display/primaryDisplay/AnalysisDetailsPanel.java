package newgui.gui.display.primaryDisplay;

import gui.inputPanels.AnalysisModel;
import gui.inputPanels.MCMCModelView;
import gui.inputPanels.loggerConfigs.LoggersPanel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;
import newgui.gui.modelViews.CoalModelView;
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
		setLayout(new FlowLayout(FlowLayout.LEFT));
		
		JPanel buttonsPanel = new JPanel();
		buttonsPanel.setBorder(BorderFactory.createLineBorder(Color.GREEN));
		buttonsPanel.setMinimumSize(new Dimension(200, 400));
		buttonsPanel.setPreferredSize(new Dimension(200, 400));
		buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.Y_AXIS));
		buttonsPanel.add(Box.createVerticalStrut(25));
		buttonsPanel.setAlignmentY(Component.TOP_ALIGNMENT);
		
		detailsPanel = new JPanel();
		detailsPanel.setAlignmentY(Component.TOP_ALIGNMENT);
		detailsPanel.setLayout(new BorderLayout());
		detailsPanel.setBorder(BorderFactory.createEmptyBorder(4,10,4,10));
		detailsPanel.setBorder(BorderFactory.createLineBorder(Color.GREEN));
		detailsPanel.setMinimumSize(new Dimension(200, 400));
		detailsPanel.setPreferredSize(new Dimension(400, 400));
		add(buttonsPanel);
		add(detailsPanel);
		
		BorderlessButton substModelButton = new BorderlessButton("Nucleotide model");
		substModelButton.setToolTipText("Change options affecting the model of nucleotide evolution");
		substModelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				detailsPanel.removeAll();
				detailsPanel.add(siteModelView, BorderLayout.CENTER);
				revalidate();
				repaint();
			}
		});
		buttonsPanel.add(substModelButton);

		BorderlessButton demoButton = new BorderlessButton("Demographic model");
		demoButton.setToolTipText("Options affecting the model of population size change");
		demoButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
//				detailsPanel.removeAll();
//				detailsPanel.add(siteModelView, BorderLayout.CENTER);				
			}
		});
		buttonsPanel.add(demoButton);
		
		BorderlessButton coalescentButton = new BorderlessButton("Recombination model");
		coalescentButton.setToolTipText("Change options affecting recombination model");
		coalescentButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				detailsPanel.removeAll();
				detailsPanel.add(coalView, BorderLayout.CENTER);
				revalidate();
				repaint();
			}
		});
		buttonsPanel.add(coalescentButton);
		
		BorderlessButton loggingButton = new BorderlessButton("Logging options");
		loggingButton.setToolTipText("Change options regarding the type of data collected");
		buttonsPanel.add(loggingButton);

		BorderlessButton mcButton = new BorderlessButton("Markov chain options");
		mcButton.setToolTipText("Change options regarding the length and number of chains run");
		buttonsPanel.add(mcButton);
		buttonsPanel.add(Box.createVerticalGlue());
		
		siteModelView = new SiteModelView();
		coalView = new CoalModelView();
		loggersView = new LoggersPanel();
		//mcView = new MCMCModelView();
	}
	
	
	private SiteModelView siteModelView;
	private CoalModelView coalView;
	private LoggersPanel loggersView;
	private MCMCModelView mcView;
	
}
