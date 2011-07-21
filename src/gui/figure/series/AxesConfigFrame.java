 package figure.series;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;

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

import figure.Figure;

public class AxesConfigFrame extends JFrame {

	public static final String X_AXIS = "x axis";
	public static final String Y_AXIS = "y axis";
	
	JPanel mainPanel;
	JPanel panel1;
	JPanel panel2;
	JPanel panel25;
	JPanel panel3;
	JPanel panel4;
	
	
	JButton cancelButton;
	JButton doneButton;
	
	JCheckBox gridLinesBox;
	
	JTextField tickXField;
	JTextField maxXField;
	JTextField minXField;
	
	Figure parentFig;
	
	AxesElement currentAxes = null;
	
	boolean changingXAxis = true;
	
	public AxesConfigFrame(Figure parentFig, String title) {
		super(title);
		this.parentFig = parentFig;
		mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));
		this.add(mainPanel);
		mainPanel.setPreferredSize(new Dimension(200, 205));
		setPreferredSize(new Dimension(200, 205) );
		
		panel1 = new JPanel();
		panel1.setLayout(new FlowLayout(FlowLayout.LEFT));
		maxXField = new JTextField();
		panel1.add(maxXField);
		panel1.add(new JLabel("Maximum value"));
		mainPanel.add(panel1);
		
		panel2 = new JPanel();
		panel2.setLayout(new FlowLayout(FlowLayout.LEFT));
		minXField = new JTextField();
		panel2.add(minXField);
		panel2.add(new JLabel("Minimum value"));
		mainPanel.add(panel2);
		
		panel25 = new JPanel();
		panel25.setLayout(new FlowLayout(FlowLayout.LEFT));
		gridLinesBox = new JCheckBox();
		panel25.add(gridLinesBox);
		panel25.add(new JLabel("Show grid"));
		mainPanel.add(panel25);
		
		panel3 = new JPanel();
		//SpinnerModel model = new SpinnerNumberModel(5, 0, 50, 1 );
		tickXField = new JTextField();
		
		tickXField.setMinimumSize(new Dimension(65, 1));
		tickXField.setPreferredSize(new Dimension(65, 24));
		panel3.setLayout(new FlowLayout(FlowLayout.LEFT));
		panel3.add(tickXField);
		panel3.add(new JLabel("X tick spacing"));
		mainPanel.add(panel3);
		
		panel4 = new JPanel();
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
		mainPanel.add(panel4);
	
		this.getRootPane().setDefaultButton(doneButton);
		
		//It's just a lot easier to have both the x and y axis options 
		//both be set here... so rejigger the whole gui to have field for both... 
		pack();
		setVisible(false);
	}

	protected void done() {
		double max = Double.NaN;
		try {
			max = Double.parseDouble( maxXField.getText());
		}
		catch (NumberFormatException nfe) {	
		}
		
		double min = Double.NaN;
		try {
			min = Double.parseDouble( minXField.getText());
		}
		catch (NumberFormatException nfe) {	
		}
		
		
		
		double tickSpacing = 0;
		try {
			tickSpacing = Double.parseDouble(tickXField.getText());
		}
		catch (NumberFormatException nfe) {
			
		}
		
		AxesOptions ops = new AxesOptions(min, max, tickSpacing, gridLinesBox.isSelected() );
		
		if (changingXAxis)
			currentAxes.setXAxisOptions(ops);
		else
			currentAxes.setYAxisOptions(ops);
		
		setVisible(false);
		currentAxes = null;
	}

	protected void cancel() {
		setVisible(false);
		currentAxes = null;
	}
	
	public void display(AxesElement axes, double min, double max, double num, java.awt.Point pos, String axis) {
		setLocationRelativeTo(parentFig);
		if (axis.equals(X_AXIS)) {
			this.setTitle("Configure X Axis");
			changingXAxis = true;
		}
		else {
			changingXAxis = false;
			this.setTitle("Configure Y Axis");
		}
		currentAxes = axes;
		pos.x += parentFig.getBounds().x;
		pos.y += parentFig.getBounds().y;

		setLocation(pos);
		minXField.setText(Double.toString(min));
		maxXField.setText(Double.toString(max));
		if (changingXAxis)
			gridLinesBox.setSelected( axes.showXGrid() );
		else {
			gridLinesBox.setSelected( axes.showYGrid() );
		}
		tickXField.setText(String.valueOf(num));
		setVisible(true);
	}
	
	class AxesOptions {
		
		public double max;
		public double min;
		public double tickSpacing;
		public boolean drawAxis;
		
		public AxesOptions(double min, 
							double max, 
							double tickSpacing,
							boolean axis) 
		{
			this.max = max;
			this.min = min;
			this.tickSpacing = tickSpacing;
			this.drawAxis = axis;
		}
		
	}
}
