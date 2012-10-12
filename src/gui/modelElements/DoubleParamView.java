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

import gui.widgets.Style;
import gui.widgets.Stylist;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import newgui.UIConstants;
import newgui.gui.widgets.BorderlessButton;

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
				comp.setFont(UIConstants.sansFont.deriveFont(14f));
			}
		});
		
		this.model = model;
		setPreferredSize(new Dimension(350, 38));
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		stylist.applyStyle(this);
		
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));
		String infoText = model.getLabel() + " : "  + model.getValue() + " [" + model.getLowerBound() + " - " + model.getUpperBound() + "]";
		if (model.getFrequency()==0 || model.getModType() == null) {
			infoText = model.getLabel() + " : "  + model.getValue() + " [ constant ]";
		}
		infoLabel = new JLabel(infoText);
		stylist.applyStyle(infoLabel);
		topPanel.add(infoLabel);
		
		ImageIcon configIcon = UIConstants.settings;
		BorderlessButton configButton;
		if (configIcon != null) {
			configButton = new BorderlessButton(configIcon);
			configButton.setPreferredSize(new Dimension(34, 32));
			configButton.setMaximumSize(new Dimension(34, 32));
			configButton.setXDif(-1);
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

		topPanel.add(Box.createHorizontalGlue());
		topPanel.add(configButton);
		topPanel.add(Box.createHorizontalGlue());
		topPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		add(topPanel);
		
		Font subFont = UIConstants.sansFont.deriveFont(12f);
		initValLabel = new JLabel("Initial value : ");
		add( makeLabelPanel(initValLabel, subFont));
		
		rangeLabel = new JLabel("Range : ");
		add( makeLabelPanel(rangeLabel, subFont));
		
		priorLabel = new JLabel("Prior : ");
		add( makeLabelPanel(priorLabel, subFont));
		
		updateView();
	}
	
	private static JPanel makeLabelPanel(JLabel label, Font font) {
		JPanel panel = new JPanel();
		panel.setOpaque(false);
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		
		panel.add(Box.createHorizontalStrut(25));
		label.setFont(font);
		panel.add(label);
		panel.add(Box.createHorizontalGlue());
		panel.setAlignmentX(Component.LEFT_ALIGNMENT);
		return panel;
	}
	
	protected void showConfigFrame() {
		if (configFrame == null)
			configFrame = new ParamConfigFrame(this);
		configFrame.updateView();
		configFrame.setVisible(true);
	}

	
	private void redrawLabels() {
		String infoText = model.getLabel(); // + " : "  + model.getValue() + " [" + model.getLowerBound() + " - " + model.getUpperBound() + "]";
		if (model.getFrequency()==0 || model.getModType() == null) {
			infoText = model.getLabel() + " : "  + model.getValue() + " [ constant ]";
		}
		infoLabel.setText(infoText);
		
		initValLabel.setText("Initial value : " + model.getValue());
		rangeLabel.setText("Range : " + model.getLowerBound() + " - " + model.getUpperBound());
		priorLabel.setText("Prior : " + model.getPriorModel().getType());
		revalidate();
	}


	public void updateView() {
		redrawLabels();
		repaint();
	}
	
	public DoubleParamElement getModel() {
		return model;
	}
	
	private JLabel initValLabel = null;
	private JLabel rangeLabel = null;
	private JLabel priorLabel = null;
	private JLabel modLabel = null;
	private Stylist stylist = new Stylist();
	private JLabel infoLabel;
	
}
