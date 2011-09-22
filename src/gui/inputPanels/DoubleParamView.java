package gui.inputPanels;

import gui.ACGFrame;
import gui.ErrorWindow;
import gui.inputPanels.DoubleModifierElement.ModType;
import gui.widgets.BorderlessButton;
import gui.widgets.RoundedPanel;
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
import javax.swing.ImageIcon;
import javax.swing.JButton;
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
	private ParamConfigFrame configFrame = null; //Instantiated only when we need it
	
	public DoubleParamView(String name, final DoubleParamElement model) {
		
		stylist.addStyle(new Style() {
			public void apply(JComponent comp) {
				comp.setOpaque(false);
				comp.setAlignmentX(Component.LEFT_ALIGNMENT);
				comp.setFont(new Font("Sans", Font.PLAIN, 12));
			}
		});
		
		this.model = model;
		setPreferredSize(new Dimension(350, 38));
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		stylist.applyStyle(this);
		
		infoLabel = new JLabel(model.getElementName() + " : "  + model.getValue() + " [" + model.getLowerBound() + " - " + model.getUpperBound() + "]");
		stylist.applyStyle(infoLabel);
		add(Box.createHorizontalGlue());
		add(infoLabel);
		
		ImageIcon configIcon = ACGFrame.getIcon("icons/settings2.png");
		BorderlessButton configButton;
		if (configIcon != null) {
			configButton = new BorderlessButton(configIcon);
			configButton.setPreferredSize(new Dimension(34, 20));
			configButton.setMaximumSize(new Dimension(34, 32));
		}
		else {
			configButton = new BorderlessButton("Configure", null);
		}
		configButton.setToolTipText("Configure properties for this parameter");
		configButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showConfigFrame();
			}
		});

		add(Box.createHorizontalGlue());
		add(Box.createHorizontalGlue());
		add(configButton);
		updateView();
	}
	
	protected void showConfigFrame() {
		if (configFrame == null)
			configFrame = new ParamConfigFrame(this);
		configFrame.setVisible(true);
	}

	
	private void redrawLabel() {
		infoLabel.setText(model.getElementName() + " : "  + model.getValue() + " [" + model.getLowerBound() + " - " + model.getUpperBound() + "]");
		revalidate();
	}


	public void updateView() {
		redrawLabel();
		repaint();
	}
	
	public DoubleParamElement getModel() {
		return model;
	}
	
	private Stylist stylist = new Stylist();
	private JLabel infoLabel;
	
}
