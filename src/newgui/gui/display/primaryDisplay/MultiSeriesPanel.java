package newgui.gui.display.primaryDisplay;

import gui.figure.series.XYSeries;
import gui.figure.series.XYSeriesElement;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import logging.MemoryStateLogger;

import newgui.gui.widgets.BorderlessButton;
import newgui.gui.widgets.MultiFigurePanel;

/**
 * Wraps a MultiFigurePanel and adds some convenience methods for adding / removing series figures
 * @author brendano
 *
 */
public class MultiSeriesPanel extends JPanel {
	
	private MemoryStateLogger memLogger; //Stores all series data as XYSeries
	
	public MultiSeriesPanel() {
		initComponents();
	}
	
	public void initializeLogger(MemoryStateLogger logger) {
		if (memLogger != null) {
			throw new IllegalArgumentException("Memory logger already set for this component");
		}
		this.memLogger = logger;
	}
	
	/**
	 * Add a new panel that displays the given series to this multi-panel container
	 * @param series
	 */
//	public void addSeriesPanel(XYSeries series) {
//		SeriesFigurePanel fig = new SeriesFigurePanel(this);
//		fig.setMemoryLogger(memLogger);
//		multiPanel.addComponent(fig);
//		fig.addSeries(series);
//	}
	
	/**
	 * Add a new panel displaying the default basic series (current the "data likelihood series")
	 */
	public void addDefaultSeriesPanel() {
		SeriesFigurePanel fig = new SeriesFigurePanel(this);
		fig.setMemoryLogger(memLogger);
		multiPanel.addComponent(fig);
		fig.addSelectedSeries();
	}
	
	/**
	 * Remove the given figure panel from this component
	 * @param seriesFigurePanel
	 */
	public void removeFigure(SeriesFigurePanel seriesFigurePanel) {
		multiPanel.removeComponent(seriesFigurePanel);
	}
	
	private void  initComponents() {
		this.setOpaque(false);
		this.setLayout(new BorderLayout());
		this.setBackground(Color.white);
		
		multiPanel = new MultiFigurePanel();
		multiPanel.setBackground(Color.white);
		add(multiPanel, BorderLayout.CENTER);
		
		JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.X_AXIS));
		bottomPanel.setOpaque(false);
		
//		Object[] rowNums = new Object[20];
//		for(int i=0; i<rowNums.length; i++) {
//			rowNums[i] = new Integer(i);
//		}
//		JComboBox rowsBox = new JComboBox(rowNums);
//		
//		bottomPanel.add(Box.createHorizontalStrut(30));
//		bottomPanel.add(rowsBox);
//		
//		
//		Object[] colNums = new Object[20];
//		for(int i=0; i<colNums.length; i++) {
//			colNums[i] = new Integer(i);
//		}
//		JComboBox colBox = new JComboBox(colNums);
//		bottomPanel.add(colBox);
		
		bottomPanel.add(Box.createHorizontalGlue());
		BorderlessButton addSeriesButton = new BorderlessButton("Add new panel");
		addSeriesButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				addDefaultSeriesPanel();
			}
		});
		bottomPanel.add(addSeriesButton);
		bottomPanel.add(Box.createHorizontalStrut(25));
		
		
		this.add(bottomPanel, BorderLayout.SOUTH);
	}
	
	private MultiFigurePanel multiPanel;

	
}
