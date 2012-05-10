package newgui.gui.display.primaryDisplay.loggerVizualizer;

import gui.figure.ColorSwatchButton;
import gui.figure.TextElement;
import gui.figure.VerticalTextElement;
import gui.figure.heatMapFigure.HeatMapElement;
import gui.figure.series.XYSeriesFigure;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;

import newgui.UIConstants;
import newgui.gui.widgets.BorderlessButton;

import logging.BreakpointLocation;

public class BPLocationViz extends AbstractLoggerViz {

	@Override
	public void update() {		
		fig.repaint();
		if (burninMessage != null && logger.getBurninExceeded()) {
			fig.removeElement(burninMessage);
			burninMessage = null;
			
		}
		

		if (logger.getBurninExceeded()) {
			densities = bpLogger.getDensities(densities);
			if (densities == null)
				System.out.println("Densities is null after returning from call");
			heatMapEl.setData(densities);
			heatMapEl.setHeatMax(bpLogger.getApproxMaxDensity()*0.5);
			heatMapEl.setYMax(bpLogger.getTreeHeight());
			heatMapEl.setXMax(bpLogger.getARGSites());
			fig.repaint();
		}
	}
	
	@Override
	public void initialize() {
		this.bpLogger = (BreakpointLocation)logger;

		heatMapEl = new HeatMapElement(fig);
		heatMapEl.setBounds(0.1, 0.05, 0.85, 0.85);
		fig.addElement(heatMapEl);
		TextElement xAxisLabel = new TextElement("Site", fig);
		xAxisLabel.setPosition(0.45, 0.95);
		fig.addElement(xAxisLabel);
		VerticalTextElement yAxisLabel = new VerticalTextElement("Time in past (subs./ site)", fig);
		yAxisLabel.setPosition(0.02, 0.4);
		yAxisLabel.setFont(UIConstants.sansFont.deriveFont(14f));
		xAxisLabel.setFont(UIConstants.sansFont.deriveFont(14f));
		fig.addElement(yAxisLabel);
		
		burninMessage = new TextElement("Burnin period (" + logger.getBurnin() + ") not exceeded", fig);
		burninMessage.setPosition(0.45, 0.5);
		fig.addElement(burninMessage);
	}


	@Override
	public String getDataString() {
		String data = logger.getSummaryString();
		return data;
	}

	
	
	protected void initComponents() {
		this.setLayout(new BorderLayout());
		fig = new XYSeriesFigure();
		fig.removeAllSeries();
		fig.removeAllElements();
		add(fig, BorderLayout.CENTER);
		
		optionsPanel = new JPanel();
		optionsPanel.setBackground(fig.getBackground());
		optionsPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		optionsPanel.add(Box.createHorizontalStrut(25));
		this.add(optionsPanel, BorderLayout.SOUTH);
		

		final ColorSwatchButton coldColorButton = new ColorSwatchButton(Color.BLUE);
		coldColorButton.setPreferredSize(new Dimension(28, 28));
		JLabel coldColorLabel = new JLabel("Cold color:");
		coldColorButton.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent pc) {
				heatMapEl.setColdColor(coldColorButton.getColor());
			}
		});
		coldColorLabel.setFont(UIConstants.sansFont);
		optionsPanel.add(coldColorLabel);
		optionsPanel.add(coldColorButton);
		
		final ColorSwatchButton hotColorButton = new ColorSwatchButton(Color.RED);
		hotColorButton.setPreferredSize(new Dimension(28, 28));
		JLabel hotColorLabel = new JLabel("Hot color:");
		hotColorButton.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent pc) {
				heatMapEl.setHotColor(hotColorButton.getColor());
				//System.out.println("Setting hot color to: " + hotColorButton.getColor());
			}
		});
		hotColorLabel.setFont(UIConstants.sansFont);
		optionsPanel.add(hotColorLabel);
		optionsPanel.add(hotColorButton);
		
		
		BorderlessButton showConfigButton = new BorderlessButton(UIConstants.settings);
		showConfigButton.setToolTipText("Select figure options");
		showConfigButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				showConfigFrame();
			}
		});
		optionsPanel.add(showConfigButton);
		
		
		BorderlessButton exportDataButton = new BorderlessButton(UIConstants.writeData);
		exportDataButton.setToolTipText("Export raw data");
		exportDataButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				exportData();
			}
		});
		optionsPanel.add(exportDataButton);
		
		BorderlessButton saveButton = new BorderlessButton(UIConstants.saveGrayButton);
		saveButton.setToolTipText("Save figure image");
		saveButton.setToolTipText("Save image");
		saveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveImage();
			}
		});
		optionsPanel.add(saveButton);
		
	}

	private double[][] densities = null; //Used to store densities so we're not always reallocating
	private HeatMapElement heatMapEl = null;
	private TextElement burninMessage;
	private BreakpointLocation bpLogger;

	
}
