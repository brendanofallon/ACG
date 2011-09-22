package gui.inputPanels;

import gui.ErrorWindow;
import gui.inputPanels.DoubleModifierElement.ModType;
import gui.inputPanels.DoublePriorModel.PriorType;
import gui.widgets.Style;
import gui.widgets.Stylist;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;

public class ParamConfigFrame extends JFrame {

	private DoubleParamElement model;
	private DoubleParamView view;
	
	private JPanel valuesPanel;
	private JPanel modifierPanel;
	private JPanel priorPanel;
	
	public ParamConfigFrame(final DoubleParamView paramView) {
		super("Configure " + paramView.getModel().getElementName());
		this.view = paramView;
		this.model = paramView.getModel();
		stylist.addStyle(new Style() {
			public void apply(JComponent comp) {
				comp.setOpaque(false);
				comp.setAlignmentX(Component.RIGHT_ALIGNMENT);
				comp.setFont(new Font("Sans", Font.PLAIN, 12));
			}
		});
		
		String name = model.getElementName();
		mainPanel = new JPanel();
		stylist.applyStyle(mainPanel);
		
		this.getContentPane().setLayout(new BorderLayout());
		this.getContentPane().add(mainPanel, BorderLayout.CENTER);
		mainPanel.setBorder(BorderFactory.createEmptyBorder(6,6,6,6));
		
		
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.X_AXIS));
		this.setPreferredSize(new Dimension(650, 280));
		this.setMaximumSize(new Dimension(650, 320));
		
		createValuesPanel();
		mainPanel.add(valuesPanel);
		
		createModifiersPanel();
		JSeparator sep0 = new JSeparator(JSeparator.VERTICAL);
		mainPanel.add(sep0);
		mainPanel.add(modifierPanel);
		
		createPriorsPanel();
		JSeparator sep = new JSeparator(JSeparator.VERTICAL);
		mainPanel.add(sep);
		mainPanel.add(priorPanel);
		
		JButton doneButton = new JButton("Done");
		doneButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				done();
			}
		});
		doneButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		this.getContentPane().add(doneButton, BorderLayout.SOUTH);
		
		pack();
		setVisible(false);
		this.getRootPane().setDefaultButton(doneButton);
		setLocationRelativeTo(getParent());
		updateView();
	}
	
	protected void done() {
		updateFieldInfo(paramNameField);
		updateFieldInfo(initValueField);
		updateFieldInfo(lowerBoundField);
		updateFieldInfo(upperBoundField);
		setVisible(false);
		view.updateView();
	}

	public void updateView() {
		ModType modType = model.getModType();
		if (modType == null)
			modifierBox.setSelectedIndex(0);
		if (modType == ModType.Simple)
			modifierBox.setSelectedIndex(1);
		if (modType == ModType.Scale)
			modifierBox.setSelectedIndex(2);
		
		paramNameField.setText(model.getElementName());
		initValueField.setText("" + model.getValue() );
		lowerBoundField.setText("" + model.getLowerBound());
		upperBoundField.setText("" + model.getUpperBound());
		view.updateView();
		repaint();
	}
	
	
	private void createValuesPanel() {
		valuesPanel = new JPanel();
		valuesPanel.setLayout(new BoxLayout(valuesPanel, BoxLayout.Y_AXIS));
		
		//mainPanel.add(stylist.applyStyle(new JLabel("Enter settings below:")));
		paramNameField = new JTextField(model.getElementName());
		addComps(valuesPanel, "Param. label : ", paramNameField);
		
		initValueField = new JTextField("" + model.getValue() );
		addComps(valuesPanel,"Initial value : ", initValueField);
		lowerBoundField = new JTextField("" + model.getLowerBound() );
		addComps(valuesPanel,"Lower bound:", lowerBoundField);
		upperBoundField = new JTextField("" + model.getUpperBound() );
		addComps(valuesPanel,"Upper bound:", upperBoundField);
		
		valuesPanel.add(Box.createVerticalGlue());
	}
	
	private void createModifiersPanel() {
		modifierPanel = new JPanel();
		modifierPanel.setLayout(new BorderLayout());
		
		modifierBox = new JComboBox(modTypes);
		modifierBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (modifierBox.getSelectedIndex()==0)
					model.setModifierType(null);
				if (modifierBox.getSelectedIndex()==1)
					model.setModifierType(ModType.Simple);
				if (modifierBox.getSelectedIndex()==2)
					model.setModifierType(ModType.Scale);
			}
		});
		addComps(modifierPanel, "Modifier type: ", modifierBox);
	}
	
	private void createPriorsPanel() {
		priorPanel = new JPanel();
		priorPanel.setLayout(new BoxLayout(priorPanel, BoxLayout.Y_AXIS));
		priorBox = new JComboBox(priorTypes);
		priorBox.setSelectedIndex(0);
		priorBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				updatePriorBox();
			}
		});
		

		addComps(priorPanel, "Prior type:", priorBox);
		
		priorMeanField = new JTextField("Enter mean");
		addComps(priorPanel, "Prior mean:", priorMeanField);
		priorMeanField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				updatePriorMeanField();
			}
		});
		
		priorStdevField = new JTextField("Enter st. dev.");
		addComps(priorPanel, "Prior stdev:", priorStdevField);
		priorStdevField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				updatePriorStdevField();
			}
		});
		priorPanel.add(Box.createVerticalGlue());
		priorMeanField.setEnabled(false);
		priorStdevField.setEnabled(false);
	}
	
	protected void updatePriorStdevField() {
		String text = priorMeanField.getText();
		try {
			Double val = Double.parseDouble(text);
			if (priorBox.getSelectedIndex()==2 && val <= 0) {
				JOptionPane.showMessageDialog(this, "Mean must be strictly positive");
				return;
			}
			model.getPriorModel().setMean(val);
		}
		catch(NumberFormatException nfe) {
			JOptionPane.showMessageDialog(this, "Please enter a valid number");
		}
	}

	protected void updatePriorMeanField() {
		String text = priorStdevField.getText();
		try {
			Double val = Double.parseDouble(text);
			if (val <= 0) {
				JOptionPane.showMessageDialog(this, "Standard deviation must be a strictly positive number");
				return;
			}
			model.getPriorModel().setStdev(val);
		}
		catch(NumberFormatException nfe) {
			JOptionPane.showMessageDialog(this, "Please enter a valid number");
		}
	}

	protected void updatePriorBox() {
		if (priorBox.getSelectedIndex()==0) {
			priorMeanField.setEnabled(false);
			priorStdevField.setEnabled(false);
			model.getPriorModel().setType(PriorType.Uniform);
		}
		if (priorBox.getSelectedIndex()==1) {
			priorMeanField.setEnabled(true);
			priorStdevField.setEnabled(true);			
			model.getPriorModel().setType(PriorType.Gaussian);
		}
		if (priorBox.getSelectedIndex()==2) {
			priorMeanField.setEnabled(true);
			priorStdevField.setEnabled(false);			
			model.getPriorModel().setType(PriorType.Exponential);
		}
		if (priorBox.getSelectedIndex()==3) {
			priorMeanField.setEnabled(true);
			priorStdevField.setEnabled(true);			
			model.getPriorModel().setType(PriorType.Gamma);
		}
	}

	private void addComps(JComponent parent, String label, JComponent comp) {
		JPanel panel = new JPanel();
		stylist.applyStyle(panel);
		panel.setLayout(new FlowLayout(FlowLayout.LEFT));
		panel.add(stylist.applyStyle(new JLabel(label)));
		panel.add(stylist.applyStyle(comp));
		parent.add(panel);
	}
	
	private void addComps(JComponent parent, String label, final JTextField field) {
		field.setPreferredSize(new Dimension(100, 28));
		field.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				updateFieldInfo(field);
			}
		});
		stylist.applyStyle(field);
		JPanel panel = new JPanel();
		stylist.applyStyle(panel);
		panel.setOpaque(false);
		panel.setLayout(new FlowLayout(FlowLayout.LEFT));
		panel.add(stylist.applyStyle(new JLabel(label)));
		panel.add(field);
		parent.add(panel);
	}
	
	protected void updateFieldInfo(JTextField field) {
		if (field.getText().trim().length() == 0) {
			JOptionPane.showMessageDialog(this, "Field cannot be empty");
			return;
		}
		
		if (field == paramNameField) {
			//Make sure there are no spaces in the name
			String text = field.getText();
			text = text.trim();
			String[] toks = text.split("[\\s\\t]+");
			if (toks.length>1) {
				JOptionPane.showMessageDialog(this, "Warning: white space in label will be removed.");
				text = text.replaceAll(" ", ".");
			}
			model.setLabel(text);
			view.updateView();
			return;
		}
		
		Double value = null;
		try {
			value = Double.parseDouble( field.getText() );
		}
		catch (NumberFormatException nfe) {
			Exception ex = new Exception("Please enter a number.");
			ErrorWindow.showErrorWindow(ex);
			return;
		}
		
		if (field == initValueField) {
			model.setValue( value );
		}
		if (field == lowerBoundField)
			model.setLowerBound(value);
		if (field == upperBoundField)
			model.setUpperBound(value);

		view.updateView();
	}
	
	private JPanel mainPanel;
	private Stylist stylist = new Stylist();
	private JTextField paramNameField; 
	private JTextField initValueField;
	private JTextField lowerBoundField;
	private JTextField upperBoundField;
	private String[] modTypes = new String[]{"None", "Simple", "Scale"};
	private JComboBox modifierBox;
	private String modLabel;
	private JTextField modFreqField;
	

	private String[] priorTypes = new String[]{"Uniform", "Gaussian", "Exponential", "Gamma"};
	private JComboBox priorBox;
	private JTextField priorMeanField;
	private JTextField priorStdevField;
}
