package plugins.treePlugin.treeFigure;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import errorHandling.ErrorWindow;

public class ScaleAxisConfigFrame extends JFrame {

	
	
	protected JPanel mainPanel;
	protected 	JSpinner tickSpinner;
	protected JButton doneButton;
	protected JButton cancelButton;
	protected JCheckBox gridLinesBox;
	protected ScaleAxisElement parent;
	protected JCheckBox reverseDirectionBox;
	
	protected JCheckBox useTickDistance;
	protected JTextField tickDistanceField;
	
	public ScaleAxisConfigFrame(ScaleAxisElement parent) {
		super("Configure Scale Axis");
		this.parent = parent;
		mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));
		mainPanel.setBorder(BorderFactory.createEmptyBorder(8, 6, 4, 4));
		
		gridLinesBox = new JCheckBox("Draw grid lines");
		gridLinesBox.setSelected(false);
		gridLinesBox.setAlignmentX(Component.LEFT_ALIGNMENT);
		mainPanel.add(gridLinesBox);
		
		reverseDirectionBox = new JCheckBox("Reverse direction");
		reverseDirectionBox.setAlignmentX(LEFT_ALIGNMENT);
		mainPanel.add(reverseDirectionBox);
		
		JPanel panel1 = new JPanel();
		
		panel1.setLayout(new FlowLayout(FlowLayout.LEFT));
		panel1.setAlignmentX(Component.LEFT_ALIGNMENT);
		panel1.add(new JLabel("Number of ticks: "));
		
		SpinnerModel model =  new SpinnerNumberModel(7, //initial value
	                               0, //min
	                               100, //max
	                               1);                //step

		tickSpinner = new JSpinner();
		tickSpinner.setModel(model);
		panel1.add(tickSpinner);
		mainPanel.add(panel1);
		
		
		useTickDistance = new JCheckBox("Use custom distance:");
		useTickDistance.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				tickDistanceBoxUpdated();
			}
		});
		mainPanel.add(useTickDistance);
		useTickDistance.setSelected(false);
		useTickDistance.repaint();
		
		JPanel dPanel = new JPanel();
		dPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		
		
		tickDistanceField = new JTextField("0");
		tickDistanceField.setPreferredSize(new Dimension(100, 30));
		tickDistanceField.setEnabled(false);
		dPanel.add(new JLabel("Distance:"));
		dPanel.add(tickDistanceField);
		dPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		mainPanel.add(dPanel);
		
		
		JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new BorderLayout());
		bottomPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		doneButton = new JButton("Accept");
		doneButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                done();
            }
        });
		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancel();
            }
        });
		bottomPanel.add(cancelButton, BorderLayout.WEST);
		bottomPanel.add(doneButton, BorderLayout.EAST);
		
		mainPanel.add(bottomPanel);
		add(mainPanel);
		pack();
		setLocationRelativeTo(null);
		this.getRootPane().setDefaultButton(doneButton);
		setVisible(false);
	}
	
	protected void tickDistanceBoxUpdated() {
		if (useTickDistance.isSelected()) {
			tickDistanceField.setEnabled(true);
			tickDistanceField.setEditable(true);
		}
		else {
			tickDistanceField.setEnabled(false);
			tickDistanceField.setEditable(false);			
		}
	}

	protected void cancel() {
		setVisible(false);
	}

	public void display(ScaleAxisOptions ops) {
		tickSpinner.setValue(ops.numberOfTicks);
		reverseDirectionBox.setSelected(ops.reverse);
		setVisible(true);
	}
	
	public void done() {
		ScaleAxisOptions ops = new ScaleAxisOptions();
		ops.numberOfTicks = (Integer)tickSpinner.getValue();
		ops.drawGridLines = gridLinesBox.isSelected();
		ops.reverse = reverseDirectionBox.isSelected();
		ops.useTickDistance = useTickDistance.isSelected();
		
		try {
			ops.tickDistance = Double.parseDouble( tickDistanceField.getText());
			if (ops.tickDistance <=0 ) 
				throw new NumberFormatException();
			
		}
		catch (NumberFormatException nfe) {
			if (useTickDistance.isSelected()) {
				ErrorWindow.showErrorWindow(new Exception("Please enter a positive number for the distance between ticks"));
			}
		}
		
		parent.setOptions(ops);
		setVisible(false);
	}
	
	public ScaleAxisOptions getOptions() {
		return new ScaleAxisOptions();
	}
	
	class ScaleAxisOptions {
		
		int numberOfTicks;
		double tickDistance;
		boolean drawGridLines;
		boolean reverse;
		boolean useTickDistance;
		
		
	}
	
}
