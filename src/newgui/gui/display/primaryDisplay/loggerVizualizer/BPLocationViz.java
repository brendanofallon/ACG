package newgui.gui.display.primaryDisplay.loggerVizualizer;

import gui.figure.TextElement;
import gui.figure.heatMapFigure.HeatMapElement;
import gui.figure.series.XYSeriesFigure;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
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
			heatMapEl.setHeatMax(0.1);

			fig.repaint();
		}
	}
	
	@Override
	public void initialize() {
		this.bpLogger = (BreakpointLocation)logger;

		heatMapEl = new HeatMapElement(fig);
		heatMapEl.setBounds(0.1, 0.05, 0.85, 0.9);
		fig.addElement(heatMapEl);
		
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
