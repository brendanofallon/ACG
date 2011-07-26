package gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;

import gui.figure.series.HistogramSeries;
import gui.figure.series.XYSeriesElement;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

/**
 * A small frame to configrue some options for the histogram series
 * Right now all the user can do is set the number of bins
 * @author brendan
 *
 */
public class HistoOptionsFrame extends JFrame {
	
	HistogramSeries series;
	XYSeriesElement element;
	StatusFigure figure;
	JSpinner binsSpinner;
	
	public HistoOptionsFrame(StatusFigure figure, HistogramSeries series) {
		super("Configure histogram");
		this.series = series;
		this.figure = figure;
		
		Container contentPane = this.getContentPane();
		contentPane.setLayout(new BorderLayout());
		
		JPanel binsPanel = new JPanel();
		binsPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		JLabel binsLabel = new JLabel("Number of bins:");
		binsPanel.add(binsLabel);
		binsSpinner = new JSpinner(new SpinnerNumberModel(series.getBinCount(), 1, 10000, 1));
		binsPanel.add(binsSpinner);
		contentPane.add(binsPanel, BorderLayout.CENTER);
		
		JPanel panel4 = new JPanel();
		panel4.setLayout(new BorderLayout());
		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
            	cancel();
            }
        });
		doneButton = new JButton("Done");
		doneButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
            	done();
            }
        } );
		panel4.add(cancelButton, BorderLayout.WEST);
		panel4.add(doneButton, BorderLayout.EAST);
		contentPane.add(panel4, BorderLayout.SOUTH);
	
		this.getRootPane().setDefaultButton(doneButton);
		this.setLocationRelativeTo(figure);
		pack();
	}

	protected void cancel() {
		this.setVisible(false);
		this.dispose();
	}
	
	protected void done() {
		//Easy way out...for now, will have to be refactored if we want more complicated settings
		figure.updateHistogram(series, (Integer)binsSpinner.getValue());
		cancel(); //dispose of frame
	}
	
	
	
	JButton cancelButton;
	JButton doneButton;
}
