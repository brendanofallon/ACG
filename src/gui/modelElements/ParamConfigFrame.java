/********************************************************************
*
* 	Copyright 2011 Brendan O'Fallon
*
*   Licensed under the Apache License, Version 2.0 (the "License");
*   you may not use this file except in compliance with the License.
*   You may obtain a copy of the License at
*
*       http://www.apache.org/licenses/LICENSE-2.0
*
*   Unless required by applicable law or agreed to in writing, software
*   distributed under the License is distributed on an "AS IS" BASIS,
*   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*   See the License for the specific language governing permissions and
*   limitations under the License.
*
***********************************************************************/


package gui.modelElements;

import gui.ErrorWindow;
import gui.modelElements.DoubleModifierElement.ModType;
import gui.modelElements.DoublePriorModel.PriorType;
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
	private JPanel priorPanel;
	
	public ParamConfigFrame(final DoubleParamView paramView) {
		super("Configure parameter " + paramView.getModel().getLabel());
		this.view = paramView;
		this.model = paramView.getModel();
		stylist.addStyle(new Style() {
			public void apply(JComponent comp) {
				comp.setOpaque(false);
				comp.setAlignmentX(Component.RIGHT_ALIGNMENT);
				comp.setFont(new Font("Sans", Font.PLAIN, 12));
			}
		});
		
		String name = model.getLabel();
		mainPanel = new JPanel();
		stylist.applyStyle(mainPanel);
		
		this.getContentPane().setLayout(new BorderLayout());
		this.getContentPane().add(mainPanel, BorderLayout.CENTER);
		mainPanel.setBorder(BorderFactory.createEmptyBorder(6,6,6,6));
		
		
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.X_AXIS));
		this.setPreferredSize(new Dimension(500, 300));
		this.setMaximumSize(new Dimension(500, 320));
		
		createValuesPanel();
		mainPanel.add(valuesPanel);
		
		JSeparator sep0 = new JSeparator(JSeparator.VERTICAL);
		mainPanel.add(sep0);
		
		createPriorsPanel();
		mainPanel.add(priorPanel);
		
		JPanel bottomPanel = new JPanel();
		bottomPanel.setOpaque(false);
		bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.X_AXIS));
		
		JButton doneButton = new JButton("Done");
		doneButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				done();
			}
		});
		doneButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		
		
		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				cancel();
			}
		});
		
		
		bottomPanel.add(Box.createHorizontalGlue());
		bottomPanel.add(cancelButton);
		bottomPanel.add(Box.createHorizontalGlue());
		bottomPanel.add(doneButton);
		bottomPanel.add(Box.createHorizontalGlue());
		this.getContentPane().add(bottomPanel, BorderLayout.SOUTH);
		
		pack();
		setVisible(false);
		this.getRootPane().setDefaultButton(doneButton);
		setLocationRelativeTo(getParent());
		updateView();
	}
	
	/**
	 * Make the config frame not visible, but dont write anything to the model
	 */
	protected void cancel() {
		setVisible(false);
	}

	/**
	 * Write the values of all components to the model and make the config frame not visible
	 */
	protected void done() {
		//ActionPerformed is only called if the user hits the return button whilst the cursor is in
		//one of the fields, which may never happen. When the done button is clicked we therefore
		//have to make sure that all of the info is updated in the models
		updateFieldInfo(paramNameField);
		updateFieldInfo(initValueField);
		updateFieldInfo(paramFreqField);
		updateFieldInfo(lowerBoundField);
		updateFieldInfo(upperBoundField);
		if (priorBox.getSelectedIndex() != 0)
			updatePriorMeanField();
		
		if (priorBox.getSelectedIndex() != 0 && priorBox.getSelectedIndex() != 2)
			updatePriorStdevField();
		updatePriorBox();
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
		
		paramNameField.setText(model.getLabel());
		initValueField.setText("" + model.getValue() );
		paramFreqField.setText("" + model.getFrequency());
		lowerBoundField.setText("" + model.getLowerBound());
		upperBoundField.setText("" + model.getUpperBound());
		
		PriorType priorType = model.getPriorModel().getType();
		if (priorType == PriorType.Uniform) {
			priorBox.setSelectedIndex(0);
		}
		if (priorType == PriorType.Gaussian) {
			priorBox.setSelectedIndex(1);
			priorMeanField.setText( model.getPriorModel().getMean() + "");
			priorStdevField.setText( model.getPriorModel().getStdev() + "");
			priorStdevField.setEnabled(true);
			priorMeanField.setEnabled(true);
		}
		if (priorType == PriorType.Exponential) {
			priorBox.setSelectedIndex(2);
			priorMeanField.setText( model.getPriorModel().getMean() + "");
			priorMeanField.setEnabled(true);
		}
		if (priorType == PriorType.Gamma) {
			priorBox.setSelectedIndex(3);
			priorMeanField.setText( model.getPriorModel().getMean() + "");
			priorStdevField.setText( model.getPriorModel().getStdev() + "");
			priorStdevField.setEnabled(true);
			priorMeanField.setEnabled(true);
		}
		
		if (priorType == PriorType.Uniform) {
			priorStdevField.setEnabled(false);
			priorMeanField.setEnabled(false);
		}
		
		view.updateView();
		repaint();
	}
	
	
	private void createValuesPanel() {
		valuesPanel = new JPanel();
		valuesPanel.setLayout(new BoxLayout(valuesPanel, BoxLayout.Y_AXIS));
		
		//mainPanel.add(stylist.applyStyle(new JLabel("Enter settings below:")));
		paramNameField = new JTextField(model.getLabel());
		addComps(valuesPanel, "Param. label : ", paramNameField);
		
		paramFreqField = new JTextField("" + model.getFrequency() );
		paramFreqField.setToolTipText("Relative frequency of new proposals for this parameter");
		addComps(valuesPanel,"Sample rate : ", paramFreqField, 10);
		
		initValueField = new JTextField("" + model.getValue() );
		addComps(valuesPanel,"Initial value : ", initValueField, 10);
		lowerBoundField = new JTextField("" + model.getLowerBound() );
		addComps(valuesPanel,"Lower bound:", lowerBoundField, 10);
		upperBoundField = new JTextField("" + model.getUpperBound() );
		addComps(valuesPanel,"Upper bound:", upperBoundField, 10);
		
		valuesPanel.add(Box.createVerticalGlue());
	}

	
	private void createPriorsPanel() {
		priorPanel = new JPanel();
		priorPanel.setLayout(new BoxLayout(priorPanel, BoxLayout.Y_AXIS));
		
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
		addComps(priorPanel, "Modifier type: ", modifierBox);
		

		priorBox = new JComboBox(priorTypes);
		priorBox.setSelectedIndex(0);
		priorBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				updatePriorBox();
			}
		});

		addComps(priorPanel, "Prior type:", priorBox);
		
		priorMeanField = new JTextField("Enter mean");
		addComps(priorPanel, "Prior mean:", priorMeanField, 10);
		
		priorStdevField = new JTextField("Enter st. dev.");
		addComps(priorPanel, "Prior stdev:", priorStdevField, 10);
		priorPanel.add(Box.createVerticalGlue());
		priorMeanField.setEnabled(false);
		priorStdevField.setEnabled(false);
	}
	
	protected void updatePriorStdevField() {
		String text = priorStdevField.getText();
		try {
			Double val = Double.parseDouble(text);
			if (priorBox.getSelectedIndex()==2 && val <= 0) {
				JOptionPane.showMessageDialog(this, "Mean must be strictly positive");
				return;
			}
			model.getPriorModel().setStdev(val);
		}
		catch(NumberFormatException nfe) {
			JOptionPane.showMessageDialog(this, "Please enter a valid number for the std. dev.");
		}
	}

	protected void updatePriorMeanField() {
		String text = priorMeanField.getText();
		try {
			Double val = Double.parseDouble(text);
			if (val <= 0) {
				JOptionPane.showMessageDialog(this, "Standard deviation must be a strictly positive number");
				return;
			}
			model.getPriorModel().setMean(val);
		}
		catch(NumberFormatException nfe) {
			JOptionPane.showMessageDialog(this, "Please enter a valid number for the mean");
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
		addComps(parent, label, field, 0);
	}
	
	private void addComps(JComponent parent, String label, final JTextField field, int indent) {
		field.setPreferredSize(new Dimension(100, 28));

		stylist.applyStyle(field);
		JPanel panel = new JPanel();
		stylist.applyStyle(panel);
		panel.setOpaque(false);
		panel.setLayout(new FlowLayout(FlowLayout.LEFT));
		if (indent > 0) {
			panel.add(Box.createHorizontalStrut(indent));
		}
		
		panel.add(stylist.applyStyle(new JLabel(label)));
		panel.add(field);
		parent.add(panel);
	}
	
	/**
	 * Parse value from given field and update the appropriate model value
	 * @param field
	 */
	protected void updateFieldInfo(JTextField field) {
		if (field.getText().trim().length() == 0) {
			JOptionPane.showMessageDialog(this, "Please enter a numerical value in the text box");
			return;
		}
		
		if (field == paramNameField) {
			//Make sure there are no spaces in the name
			String text = field.getText();
			text = text.trim();
			String[] toks = text.split("[\\s\\t]+");
			if (toks.length>1) {
				JOptionPane.showMessageDialog(this, "Warning: white space in label will be removed.");
				text = text.replaceAll(" ", "_");
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
		if (field == paramFreqField) {
			if (value < 0) {
				JOptionPane.showMessageDialog(this, "Sample rate must be a positive number");
				return;
			}
			model.setFrequency( value );
		}
		if (field == lowerBoundField)
			model.setLowerBound(value);
		if (field == upperBoundField)
			model.setUpperBound(value);

		view.updateView();
	}
	
	private JPanel mainPanel;
	private Stylist stylist = new Stylist();
	private JTextField paramFreqField; 
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
