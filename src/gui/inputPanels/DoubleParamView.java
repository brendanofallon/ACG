package gui.inputPanels;

import gui.ErrorWindow;
import gui.inputPanels.DoubleModifierElement.ModType;
import gui.widgets.Style;
import gui.widgets.Stylist;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * A generic view / controller component for DoubleParameters
 * @author brendano
 *
 */
public class DoubleParamView extends JPanel {

	private DoubleParamElement model;
	
	
	public DoubleParamView(String name, final DoubleParamElement model) {
		
		stylist.addStyle(new Style() {
			public void apply(JComponent comp) {
				comp.setOpaque(false);
				comp.setAlignmentX(Component.LEFT_ALIGNMENT);
				comp.setFont(new Font("Sans", Font.PLAIN, 12));
			}
		});
		
		this.model = model;
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.setPreferredSize(new Dimension(250, 155));
		this.setMaximumSize(new Dimension(2500, 220));
		stylist.applyStyle(this);
		
		
		add(stylist.applyStyle(new JLabel(name)));
		initValueField = new JTextField("" + model.getValue() );
		addComps("Init value : ", initValueField);
		lowerBoundField = new JTextField("" + model.getLowerBound() );
		addComps("Lower bound:", lowerBoundField);
		upperBoundField = new JTextField("" + model.getUpperBound() );
		addComps("Upper bound:", upperBoundField);
		
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
		addComps("Modifier type: ", modifierBox);
		add(Box.createVerticalGlue());
		add(Box.createGlue());
		updateView();
	}
	
	private void addComps(String label, JComponent comp) {
		JPanel panel = new JPanel();
		stylist.applyStyle(panel);
		panel.setLayout(new FlowLayout(FlowLayout.LEFT));
		panel.add(stylist.applyStyle(new JLabel(label)));
		panel.add(stylist.applyStyle(comp));
		this.add(panel);
	}
	
	private void addComps(String label, final JTextField field) {
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
		this.add(panel);
	}
	
	protected void updateFieldInfo(JTextField field) {
		Double value = null;
		try {
			value = Double.parseDouble( field.getText() );
		}
		catch (NumberFormatException nfe) {
			Exception ex = new Exception("Please enter a number.");
			ErrorWindow.showErrorWindow(ex);
		}
		
		if (field == initValueField) {
			model.setValue( value );
		}
		if (field == lowerBoundField)
			model.setLowerBound(value);
		if (field == upperBoundField)
			model.setUpperBound(value);
	}

	public void updateView() {
		ModType modType = model.getModType();
		if (modType == null)
			modifierBox.setSelectedIndex(0);
		if (modType == ModType.Simple)
			modifierBox.setSelectedIndex(1);
		if (modType == ModType.Scale)
			modifierBox.setSelectedIndex(2);
		
		initValueField.setText("" + model.getValue() );
		lowerBoundField.setText("" + model.getLowerBound());
		upperBoundField.setText("" + model.getUpperBound());
		repaint();
	}
	
	public DoubleParamElement getModel() {
		return model;
	}


	private Stylist stylist = new Stylist();
	private JTextField initValueField;
	private JTextField lowerBoundField;
	private JTextField upperBoundField;
	private String[] modTypes = new String[]{"None", "Simple", "Scale"};
	private JComboBox modifierBox;
}
