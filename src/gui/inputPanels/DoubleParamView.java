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
				//comp.setFont(new Font("Sans", Font.PLAIN, 12));
			}
		});
		
		this.model = model;
		setPreferredSize(new Dimension(350, 38));
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		stylist.applyStyle(this);
		
		
		String infoText = model.getLabel() + " : "  + model.getValue() + " [" + model.getLowerBound() + " - " + model.getUpperBound() + "]";
		if (model.getFrequency()==0 || model.getModType() == null) {
			infoText = model.getLabel() + " : "  + model.getValue() + " [ constant ]";
		}
		infoLabel = new JLabel(infoText);
		stylist.applyStyle(infoLabel);
		add(Box.createHorizontalGlue());
		add(infoLabel);
		
		ImageIcon configIcon = ACGFrame.getIcon("icons/settings3.png");
		BorderlessButton configButton;
		if (configIcon != null) {
			configButton = new BorderlessButton(configIcon);
			configButton.setPreferredSize(new Dimension(34, 32));
			configButton.setMaximumSize(new Dimension(34, 32));
			//configButton.setYDif(-3);
			configButton.setXDif(2);
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
		configFrame.updateView();
		configFrame.setVisible(true);
	}

	
	private void redrawLabel() {
		String infoText = model.getLabel() + " : "  + model.getValue() + " [" + model.getLowerBound() + " - " + model.getUpperBound() + "]";
		if (model.getFrequency()==0 || model.getModType() == null) {
			infoText = model.getLabel() + " : "  + model.getValue() + " [ constant ]";
		}
		infoLabel.setText(infoText);
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
