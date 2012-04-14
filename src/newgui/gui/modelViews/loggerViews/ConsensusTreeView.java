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


package newgui.gui.modelViews.loggerViews;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;
import javax.swing.JTextField;


import gui.inputPanels.Configurator.InputConfigException;
import gui.inputPanels.loggerConfigs.ConsensusTreeModel;

public class ConsensusTreeView extends DefaultLoggerView {

	JTextField siteField;
	ConsensusTreeModel treeModel;
	
	public ConsensusTreeView() {
		this( new ConsensusTreeModel() );
		loggerLabelField.setText("Consensus Tree");
	}
	
	public ConsensusTreeView(final ConsensusTreeModel model) {
		super(model);
		this.treeModel = model;
		siteField = new JTextField("Enter site");
		siteField.setMinimumSize(new Dimension(60, 10));
		siteField.setMaximumSize(new Dimension(60, 1000));
		siteField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					updateSiteField();
				} catch (InputConfigException e) {
					
				}
			}
		});
		centerPanel.add(new JLabel("Site :"));
		centerPanel.add(siteField);
		updateView();
	}
	
	
	public Dimension getPreferredDimensions() {
		return new Dimension(400, 180);
	}
	
	protected void updateSiteField() throws InputConfigException {
		try {
			Integer site = Integer.parseInt(siteField.getText());
			treeModel.setSite(site);
			if (treeModel.getUseDefaultFilename()) {
				loggerLabelField.setText("Consensus tree " + site);
			}
		}
		catch (NumberFormatException nfe) {
			throw new InputConfigException("Please enter an integer for the site at which to build the consensus tree");
		}
	}

	@Override
	public String getName() {
		return "Consensus site tree";
	}

	@Override
	public String getDescription() {
		return "Consensus of trees at single site";
	}

	@Override
	protected void updateModelFromView() throws InputConfigException {
		updateSiteField();
	}

	/**
	 * Updates widgets with info from model
	 */
	public void updateView() {
		loggerLabelField.setText( model.getModelLabel() );
		loggerLabelField.repaint();
		burninSpinner.setValue( model.getBurnin() );
		burninSpinner.repaint();
		freqSpinner.setValue( model.getLogFrequency() );
		siteField.setText( treeModel.getSite() + "");
		revalidate();
		repaint();
	}
}
